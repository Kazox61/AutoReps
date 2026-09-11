package com.kazox.autoreps

import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import org.koin.mp.KoinPlatformTools
import kotlin.random.Random
import kotlin.time.Clock

/**
 * True on the kind of device store captures run on: the iOS simulator or the Android emulator.
 * Gate for [seedDemoDataForStoreCaptures] — hardware devices must never seed.
 */
expect fun isStoreCaptureDevice(): Boolean

/**
 * Seeds demo workouts into an empty database, but only on a store-capture device.
 *
 * Store screenshots and preview videos are captured on a fresh install (goldie reinstalls the
 * app with cleared data before every flow), and an empty AutoReps shows zeros and empty states
 * that do not sell the app. Only the camera creates real reps, so there is no UI path to a
 * populated home screen on a simulator.
 */
fun seedDemoDataForStoreCaptures() {
    if (!isStoreCaptureDevice()) return

    val repository = KoinPlatformTools.defaultContext().get().get<WorkoutRepository>()

    runBlocking {
        if (repository.getWorkouts().first().isNotEmpty()) return@runBlocking

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault())
        plannedWorkouts(today).forEach { plan ->
            val reps = demoReps(plan)
            val workout = Workout(
                name = plan.name,
                reps = reps.size,
                startedAt = startedAt(plan),
                duration = (reps.maxOfOrNull { it.timestamp } ?: 0) + 2,
            )
            val id = repository.insertWorkout(workout)
            repository.insertReps(reps.map { it.copy(workoutId = id) })
        }
    }
}

// ─── Plan ─────────────────────────────────────────────────────

private data class PlannedWorkout(
    val name: String?,
    val date: LocalDate,
    val hour: Int,
    val minute: Int,
    val sets: List<Int>,
    /** EMOM-style rounds rest a fixed minute; free training rests 25–55 s. */
    val emom: Boolean = false,
)

/**
 * Five weeks of history ending today: a nine-day streak, a skip-day pattern that leaves the
 * heatmap looking lived-in, and one standout EMOM session for the "Bestwert" tile.
 *
 * Today leads with a partial goal (68 of 100) — a full ring reads as "done for the day", a
 * partial one as "mid-training", which is the moment the app is worth photographing.
 */
private fun plannedWorkouts(today: LocalDate): List<PlannedWorkout> {
    val random = Random(DEMO_SEED)
    val names = listOf("Morgen-Session", "Abendrunde", "Kraftsession", null, null, "kurz & knackig", null)
    val plans = mutableListOf<PlannedWorkout>()

    // The EMOM benchmark session: eight rounds of twenty, five weeks back.
    plans += PlannedWorkout(
        name = "Runde nach Runde",
        date = today.minus(DatePeriod(days = 33)),
        hour = 18,
        minute = 0,
        sets = List(8) { 20 },
        emom = true,
    )

    for (back in 34 downTo 1) {
        val day = today.minus(DatePeriod(days = back))
        val inStreak = back <= 8
        val weekday = day.dayOfWeek.isoDayNumber
        val resting = !inStreak && (random.nextDouble() < 0.22 || (weekday >= 6 && random.nextDouble() < 0.35))
        if (resting) continue

        val morning = random.nextBoolean()
        plans += PlannedWorkout(
            name = names[random.nextInt(names.size)],
            date = day,
            hour = if (morning) 7 else 18,
            minute = random.nextInt(0, 59),
            sets = decliningSets(random),
        )
    }

    plans += PlannedWorkout(
        name = "Morgen-Session",
        date = today,
        hour = 7,
        minute = 30,
        sets = listOf(25, 22, 21),
    )
    return plans
}

/** Three to five sets, each a bit shorter than the one before — how push-up sessions actually go. */
private fun decliningSets(random: Random): List<Int> {
    val sets = mutableListOf<Int>()
    var size = random.nextInt(18, 27)
    repeat(random.nextInt(3, 6)) {
        sets += size
        size = (size * 100 / random.nextInt(112, 132)).coerceAtLeast(5)
    }
    return sets
}

/** One [Rep] every 2.2–3.4 s, a rest gap between sets (a fixed minute for EMOM). */
private fun demoReps(plan: PlannedWorkout): List<Rep> {
    val random = Random(DEMO_SEED + plan.date.dayOfYear)
    val reps = mutableListOf<Rep>()
    var second = 2
    plan.sets.forEachIndexed { index, count ->
        repeat(count) {
            reps += Rep(workoutId = 0, timestamp = second, setId = index)
            second += 2 + random.nextInt(0, 13) / 10
        }
        if (index < plan.sets.lastIndex) {
            second += if (plan.emom) 60 else random.nextInt(25, 55)
        }
    }
    return reps
}

/** `yyyy-MM-ddTHH:mm:ss`, the format the record flow writes. */
private fun startedAt(plan: PlannedWorkout): String =
    "${plan.date}T${plan.hour.toString().padStart(2, '0')}:${plan.minute.toString().padStart(2, '0')}:00"

private const val DEMO_SEED = 42
