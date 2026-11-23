package com.example.aura.engine

import com.example.aura.data.model.UserProfile
import java.util.Calendar
import java.util.Locale

data class WorkoutTimeSuggestion(
    val suggestedMinutes: Int,
    val reason: String,
    val freeSlotsMinutes: List<Int>
)

object WorkoutScheduleEngine {

    val WEEKDAYS: List<Pair<Int, String>> = listOf(
        1 to "Mon",
        2 to "Tue",
        3 to "Wed",
        4 to "Thu",
        5 to "Fri",
        6 to "Sat",
        7 to "Sun"
    )

    fun formatTimeMinutes(totalMinutes: Int): String {
        val clamped = ((totalMinutes % 1440) + 1440) % 1440
        val hour24 = clamped / 60
        val min = clamped % 60
        val amPm = if (hour24 < 12) "AM" else "PM"
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, min, amPm)
    }

    fun defaultWorkoutDays(daysPerWeek: Int): List<Int> {
        return when (daysPerWeek.coerceIn(1, 7)) {
            1 -> listOf(1)
            2 -> listOf(1, 4) // Mon, Thu
            3 -> listOf(1, 3, 5) // Mon, Wed, Fri
            4 -> listOf(1, 2, 4, 5) // Mon, Tue, Thu, Fri
            5 -> listOf(1, 2, 3, 4, 5) // Mon..Fri
            6 -> listOf(1, 2, 3, 4, 5, 6) // Mon..Sat
            else -> listOf(1, 2, 3, 4, 5, 6, 7)
        }
    }

    fun resolveWorkoutDays(profile: UserProfile): List<Int> {
        val targetCount = profile.daysPerWeek.coerceIn(1, 7)
        val cleaned = profile.preferredWorkoutDays
            .filter { it in 1..7 }
            .distinct()
            .sorted()
        if (cleaned.size == targetCount) return cleaned
        val merged = (cleaned + defaultWorkoutDays(targetCount) + (1..7)).distinct()
        return merged.take(targetCount).sorted()
    }

    fun adjustDaysForCount(currentDays: List<Int>, targetCount: Int): List<Int> {
        val count = targetCount.coerceIn(1, 7)
        val cleaned = currentDays.filter { it in 1..7 }.distinct()
        return when {
            cleaned.size == count -> cleaned.sorted()
            cleaned.size > count -> cleaned.take(count).sorted()
            else -> (cleaned + defaultWorkoutDays(count) + (1..7)).distinct().take(count).sorted()
        }
    }

    /**
     * Converts Calendar.DAY_OF_WEEK into Monday=1 .. Sunday=7.
     */
    fun calendarToWeekdayIndex(cal: Calendar = Calendar.getInstance()): Int {
        return when (cal.get(Calendar.DAY_OF_WEEK)) {
            Calendar.MONDAY -> 1
            Calendar.TUESDAY -> 2
            Calendar.WEDNESDAY -> 3
            Calendar.THURSDAY -> 4
            Calendar.FRIDAY -> 5
            Calendar.SATURDAY -> 6
            else -> 7
        }
    }

    fun isWorkoutDayForCalendar(profile: UserProfile, cal: Calendar = Calendar.getInstance()): Boolean {
        val weekday = calendarToWeekdayIndex(cal)
        val activeDays = resolveWorkoutDays(profile)
        return weekday in activeDays
    }

    /**
     * Finds candidate workout start times (30-min increments) outside work/college hours,
     * leaving >= 30 min after waking and >= 45 min before sleep.
     */
    fun computeFreeSlotCandidates(
        wakeTimeMinutes: Int,
        hasWorkOrSchoolHours: Boolean,
        workStartMinutes: Int,
        workEndMinutes: Int,
        sleepTimeMinutes: Int,
        sessionDurationMinutes: Int
    ): List<Int> {
        val wake = wakeTimeMinutes.coerceIn(4 * 60, 14 * 60)
        val sleep = if (sleepTimeMinutes <= wake) sleepTimeMinutes + 1440 else sleepTimeMinutes
        val duration = sessionDurationMinutes.coerceIn(20, 120)

        val earliestStart = wake + 30
        val latestStart = sleep - 45 - duration

        if (latestStart < earliestStart) {
            return listOf((wake + 60) % 1440)
        }

        val candidates = mutableListOf<Int>()
        var slot = ((earliestStart + 29) / 30) * 30 // snap up to 30m
        while (slot <= latestStart) {
            val slotEnd = slot + duration
            val overlapsWork = if (hasWorkOrSchoolHours && workEndMinutes > workStartMinutes) {
                // Leave a 15-min transition buffer around work/college
                slot < workEndMinutes + 15 && slotEnd > workStartMinutes - 15
            } else {
                false
            }
            if (!overlapsWork) {
                candidates.add(slot % 1440)
            }
            slot += 30
        }

        if (candidates.isEmpty()) {
            val fallback = (if (hasWorkOrSchoolHours) workEndMinutes + 30 else wake + 90) % 1440
            candidates.add(fallback)
        }
        return candidates
    }

    /**
     * Suggests the best free slot for training based on wake time, work/college hours, and sleep time.
     * Prefers an early evening / post-work slot (~17:00-19:30), otherwise morning pre-work, otherwise first open window.
     */
    fun suggestBestWorkoutTime(
        wakeTimeMinutes: Int,
        hasWorkOrSchoolHours: Boolean,
        workStartMinutes: Int,
        workEndMinutes: Int,
        sleepTimeMinutes: Int,
        sessionDurationMinutes: Int
    ): WorkoutTimeSuggestion {
        val slots = computeFreeSlotCandidates(
            wakeTimeMinutes = wakeTimeMinutes,
            hasWorkOrSchoolHours = hasWorkOrSchoolHours,
            workStartMinutes = workStartMinutes,
            workEndMinutes = workEndMinutes,
            sleepTimeMinutes = sleepTimeMinutes,
            sessionDurationMinutes = sessionDurationMinutes
        )

        val preferredEvening = slots.firstOrNull { it in (16 * 60 + 30)..(19 * 60 + 30) }
        val postWorkSlot = if (hasWorkOrSchoolHours) {
            slots.firstOrNull { it >= (workEndMinutes + 15) && it <= (21 * 60) }
        } else null
        val preWorkMorning = if (hasWorkOrSchoolHours) {
            slots.firstOrNull { it >= (wakeTimeMinutes + 30) && it + sessionDurationMinutes <= workStartMinutes - 15 }
        } else null
        val midMorning = slots.firstOrNull { it in (8 * 60)..(11 * 60) }

        val best = postWorkSlot ?: preferredEvening ?: preWorkMorning ?: midMorning ?: slots.first()

        val reason = when {
            postWorkSlot != null && best == postWorkSlot ->
                "Open right after your work/classes (${formatTimeMinutes(workEndMinutes)}), with time to unwind before sleep."
            preWorkMorning != null && best == preWorkMorning ->
                "Best open window in the morning before you start at ${formatTimeMinutes(workStartMinutes)}."
            preferredEvening != null && best == preferredEvening ->
                "Comfortable late-afternoon energy window well before bedtime."
            else ->
                "Fits cleanly between waking at ${formatTimeMinutes(wakeTimeMinutes)} and sleep at ${formatTimeMinutes(sleepTimeMinutes)}."
        }

        return WorkoutTimeSuggestion(
            suggestedMinutes = best,
            reason = reason,
            freeSlotsMinutes = slots
        )
    }

    fun computeInitialDelayMillis(
        preferredWorkoutMinutes: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Long {
        val clamped = ((preferredWorkoutMinutes % 1440) + 1440) % 1440
        val hour = clamped / 60
        val minute = clamped % 60

        val nowCal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val nextCal = (nowCal.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (nextCal.timeInMillis <= nowMillis + 60_000L) {
            nextCal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return (nextCal.timeInMillis - nowMillis).coerceAtLeast(60_000L)
    }
}
