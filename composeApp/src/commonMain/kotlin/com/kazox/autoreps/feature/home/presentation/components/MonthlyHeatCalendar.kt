package com.kazox.autoreps.feature.home.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import autoreps.composeapp.generated.resources.Res
import autoreps.composeapp.generated.resources.days_short
import autoreps.composeapp.generated.resources.months
import com.kazox.autoreps.core.presentation.theme.onSuccess
import com.kazox.autoreps.feature.workout.domain.model.Workout
import com.kazox.autoreps.core.presentation.theme.success
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kizitonwose.calendar.core.now
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.YearMonth
import org.jetbrains.compose.resources.stringArrayResource
import kotlin.time.ExperimentalTime

private enum class RepsLevel {
    None,
    Low,
    Goal
}

@OptIn(ExperimentalTime::class)
@Composable
fun MonthlyHeatmapCalendar(
    dailyGoal: Int,
    workouts: List<Workout>,
) {
    val currentMonth = remember { YearMonth.now() }
    var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

    val dailyData = remember(workouts, dailyGoal) {
        processWorkoutData(workouts, dailyGoal)
    }

    Column {
        Text(
            text = "${stringArrayResource(Res.array.months)[currentMonth.month.ordinal]} ${currentMonth.year}",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val daysOfWeek = daysOfWeek()
            daysOfWeek.forEach { dayOfWeek ->
                Text(
                    text = stringArrayResource(Res.array.days_short)[dayOfWeek.ordinal],
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        val state = rememberCalendarState(
            startMonth = currentMonth,
            endMonth = currentMonth,
            firstVisibleMonth = currentMonth,
            firstDayOfWeek = firstDayOfWeekFromLocale()
        )

        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                DayCell(
                    day = day,
                    level = dailyData[day.date] ?: RepsLevel.None,
                    isSelected = selectedDate == day.date,
                    onClick = { selectedDate = if (selectedDate == it) null else it }
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        selectedDate?.let { date ->
            val dayWorkouts = workouts.filter { workout ->
                workout.startedAt.startsWith(date.toString())
            }
            if (dayWorkouts.isNotEmpty() || selectedDate == LocalDate.now()) {
//                SelectedDayDetails(
//                    date = date,
//                    workouts = dayWorkouts,
//                    dailyGoal = dailyGoal
//                )
            }
        }
    }
}

private val daySize = 40.dp

@OptIn(ExperimentalTime::class)
@Composable
private fun DayCell(
    day: CalendarDay,
    level: RepsLevel,
    isSelected: Boolean,
    onClick: (LocalDate) -> Unit,
) {
    val isToday = day.date == LocalDate.now()
    val currentMonth = remember { YearMonth.now() }
    val isCurrentMonth = day.date.month == currentMonth.month && day.date.year == currentMonth.year
    val isAfterToday = day.date > LocalDate.now()

    Box(
        modifier = Modifier
            .size(daySize)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                color = if (isCurrentMonth) {
                    when (level) {
                        RepsLevel.Goal -> MaterialTheme.colorScheme.success
                        RepsLevel.Low -> if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        RepsLevel.None -> if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
                    }
                } else Color.Transparent
            )
            .clickable(enabled = isToday || (isCurrentMonth && !isAfterToday && level != RepsLevel.None)) {
                if (isCurrentMonth) onClick(day.date)
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.date.day.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = if (isCurrentMonth) {
                when (level) {
                    RepsLevel.Goal -> MaterialTheme.colorScheme.onSuccess
                    RepsLevel.Low -> if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onError
                    RepsLevel.None -> if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                }
            } else MaterialTheme.colorScheme.onBackground
        )
    }
}

private fun daysOfWeek(firstDayOfWeek: DayOfWeek = firstDayOfWeekFromLocale()): List<DayOfWeek> {
    val daysOfWeek = DayOfWeek.entries
    val firstDayIndex = daysOfWeek.indexOf(firstDayOfWeek)
    return daysOfWeek.drop(firstDayIndex) + daysOfWeek.take(firstDayIndex)
}

private fun processWorkoutData(
    workouts: List<Workout>,
    dailyGoal: Int
): Map<LocalDate, RepsLevel> {
    return workouts
        .groupBy { LocalDate.parse(it.startedAt.split("T")[0]) }
        .mapValues { (_, dayWorkouts) ->
            val totalReps = dayWorkouts.sumOf { it.reps }
            val percentage = (totalReps.toFloat() / dailyGoal) * 100

            when {
                percentage >= 100f -> RepsLevel.Goal
                totalReps > 0 -> RepsLevel.Low
                else -> RepsLevel.None
            }
        }
}