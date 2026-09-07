package com.kazox.autoreps.core.domain.model

/**
 * Every preference the app has, as one value.
 *
 * A single object rather than a setting-per-flow: the settings screen reads all of them at once,
 * and a partial update that emitted mid-write would show the screen a state the user never chose.
 */
data class AppSettings(
    // ─── Ziel ───
    val dailyGoal: Int = 100,
    // ─── Training ───
    /** Seconds of rest that end a set, when not running an EMOM. */
    val restSeconds: Int = 10,
    // ─── EMOM ───
    val emomEnabled: Boolean = false,
    /** Length of one round. 60 is the "on the minute" default the name comes from. */
    val emomIntervalSeconds: Int = 60,
    /** Seconds of warning tones before a round starts. 0 disables them. */
    val emomWarningSeconds: Int = 3,
    // ─── Ton ───
    val soundPerRep: Boolean = true,
    // ─── Darstellung ───
    val theme: ThemeChoice = ThemeChoice.System,
)

/**
 * Which palette to render in.
 *
 * [System] is the default rather than [Light]: following the device is what a user who never
 * opens Settings expects, and it is the only choice that changes with the time of day.
 */
enum class ThemeChoice {
    Light,
    Dark,
    System,
}
