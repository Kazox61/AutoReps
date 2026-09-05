package com.kazox.autoreps.app.di

import com.kazox.autoreps.core.domain.model.Rep
import com.kazox.autoreps.core.domain.model.Workout
import com.kazox.autoreps.core.domain.repository.WorkoutRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.mp.KoinPlatform
import kotlin.random.Random

/**
 * Desktop-only seed so the workout screens have something to show (only compiled for the JVM
 * target).
 *
 * Recording now exists, but not here: pose detection is mobile-only, so on desktop this is still
 * the only way to get workouts on screen. It stays until the desktop app stops being the place
 * these screens are reviewed.
 *
 * Runs when the database is empty, or whenever `-Dautoreps.reseed=true` is set — that wipes
 * every workout first, so the demo set can be regenerated without hunting for the db file.
 */
fun seedDemoDataIfEmpty() {
    // getKoin() throws when Koin never started; main.kt always runs initKoin() first.
    val koin = KoinPlatform.getKoin()
    val reseed = System.getProperty("autoreps.reseed") == "true"

    runBlocking {
        val repository = koin.get<WorkoutRepository>()
        val existing = repository.getWorkouts().first()
        when {
            existing.isEmpty() -> Unit
            reseed -> existing.forEach { repository.deleteWorkout(it) }
            else -> return@runBlocking
        }

        // Fixed seed: the charts look the same on every run, so a visual change is always a
        // code change and never the dice.
        val random = Random(seed = 42)

        // Oldest first, so each session's "previous" is the one before it. Every session is
        // push-ups — the app supports nothing else — so the names here are just labels, and one
        // is deliberately blank to exercise the unnamed-workout fallback.
        val sessions =
            listOf(
                Session("Morgens", "2026-08-11T07:20:00", topSet = 18, sets = 9),
                Session(null, "2026-08-13T18:30:00", topSet = 19, sets = 9),
                Session("Nach der Arbeit", "2026-08-15T18:30:00", topSet = 20, sets = 10),
                Session("Morgens", "2026-08-18T07:15:00", topSet = 21, sets = 10),
                Session("Kurze Einheit", "2026-08-19T21:05:00", topSet = 22, sets = 11),
                Session("Bestleistung", "2026-08-22T18:30:00", topSet = 24, sets = 12),
            )

        sessions.forEach { session -> insertSession(repository, session, random) }
    }
}

private data class Session(
    /** Optional session label — not an exercise name; everything is push-ups. */
    val name: String?,
    val startedAt: String,
    /** Reps in the opening set; later sets decay from here. */
    val topSet: Int,
    val sets: Int,
)

private suspend fun insertSession(
    repository: WorkoutRepository,
    session: Session,
    random: Random,
) {
    var timestamp = 0
    var trailingRest = 0

    val reps =
        buildList {
            repeat(session.sets) { setIndex ->
                // Reps decay toward roughly a quarter of the top set, with a little noise so
                // the trend line is not a ruler.
                val decay = 1.0 - 0.72 * (setIndex.toDouble() / (session.sets - 1))
                val count = (session.topSet * decay).toInt().coerceAtLeast(4) + random.nextInt(-1, 2)

                repeat(count.coerceAtLeast(3)) { repIndex ->
                    add(Rep(timestamp = timestamp, setId = setIndex + 1))
                    // Cadence drags as the set runs on and as the session wears on. Whole
                    // seconds, because that is the column's resolution — the Tempo chart will
                    // stay blocky until Rep.timestamp becomes milliseconds.
                    val fatigue = repIndex / 6 + setIndex / 4
                    timestamp += 2 + fatigue + random.nextInt(0, 2)
                }

                // Rest grows across the session and varies by ±10s, so the Pausen chart has a
                // readable upward trend rather than a flat row of equal bars.
                trailingRest = 50 + setIndex * 8 + random.nextInt(-10, 11)
                timestamp += trailingRest
            }
        }

    val workoutId =
        repository.insertWorkout(
            Workout(
                name = session.name,
                reps = reps.size,
                startedAt = session.startedAt,
                // The final rest happened after the last rep, so it is not part of the workout.
                duration = timestamp - trailingRest,
            ),
        )
    repository.insertReps(reps.map { it.copy(workoutId = workoutId) })
}
