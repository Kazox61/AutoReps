package com.kazox.autoreps.feature.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.core.domain.repository.SettingsRepository
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

class HomeViewModel(
    private val workoutRepository: WorkoutRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {
    private val _state = MutableStateFlow(HomeState())
    val state = _state.asStateFlow()

    init {
        observeWorkouts()
        observeDailyGoal()
    }

    fun onAction(action: HomeAction) {
        when (action) {
            is HomeAction.SelectDay -> Unit // The heatmap has no detail view yet.
        }
    }

    /**
     * Everything on the screen comes from one query.
     *
     * Deliberately derived here rather than through the DAO's aggregate queries: the streak in
     * particular needs the "or yesterday" rule below, which SQL's version does not have, and
     * splitting the numbers across several flows would let them disagree mid-emission.
     */
    private fun observeWorkouts() {
        viewModelScope.launch {
            workoutRepository.getWorkouts().collect { workouts ->
                val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                val repsByDate = repsByDate(workouts)

                _state.update {
                    it.copy(
                        today = today,
                        todayReps = repsByDate[today] ?: 0,
                        streak = streakEndingNear(repsByDate, today),
                        weekTotal = lastSevenDays(today).sumOf { day -> repsByDate[day] ?: 0 },
                        bestDay = repsByDate.values.maxOrNull() ?: 0,
                        lifetimeReps = workouts.sumOf { workout -> workout.reps },
                        repsByDate = repsByDate,
                        recentWorkouts = workouts.take(RECENT_COUNT),
                        isLoading = false,
                    )
                }
            }
        }
    }

    /**
     * The goal is its own subscription rather than a `combine` with the workouts.
     *
     * Changing it in Settings has to move the ring on this screen immediately, and combining
     * would make that wait for the next workout emission — which, on a day with no training, is
     * never.
     */
    private fun observeDailyGoal() {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _state.update { it.copy(dailyGoal = settings.dailyGoal) }
            }
        }
    }

    private companion object {
        const val RECENT_COUNT = 3
    }
}

/** Reps summed per calendar day. Unparseable timestamps are skipped rather than crashing. */
internal fun repsByDate(workouts: List<Workout>): Map<LocalDate, Int> =
    workouts
        .mapNotNull { workout ->
            val date = runCatching { LocalDate.parse(workout.startedAt.substringBefore('T')) }.getOrNull()
            date?.let { it to workout.reps }
        }.groupBy({ it.first }, { it.second })
        .mapValues { (_, reps) -> reps.sum() }

/** The seven days ending on [today], oldest first. */
internal fun lastSevenDays(today: LocalDate): List<LocalDate> =
    (6 downTo 0).map { back -> today.minus(DatePeriod(days = back)) }

/**
 * Consecutive days with at least one rep, counting back from today — or from yesterday when
 * today is still empty.
 *
 * The "or yesterday" part matters: a streak should not read as broken at 8am simply because the
 * day's push-ups have not happened yet. A plain SQL COUNT of consecutive days returns 0 in that
 * case, which would show a hard-won streak collapsing every midnight — which is why this is
 * derived here rather than in the DAO.
 */
internal fun streakEndingNear(
    repsByDate: Map<LocalDate, Int>,
    today: LocalDate,
): Int {
    val yesterday = today.minus(DatePeriod(days = 1))
    var cursor =
        when {
            (repsByDate[today] ?: 0) > 0 -> today
            (repsByDate[yesterday] ?: 0) > 0 -> yesterday
            else -> return 0
        }
    var count = 0
    while ((repsByDate[cursor] ?: 0) > 0) {
        count++
        cursor = cursor.minus(DatePeriod(days = 1))
    }
    return count
}
