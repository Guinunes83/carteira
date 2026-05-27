package com.example.util

import java.util.Calendar

object DateUtils {

    fun getStartOfCycleMs(timestamp: Long = System.currentTimeMillis(), startDay: Int = 1, endDay: Int = 31): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (startDay > endDay) {
            if (currentDay < startDay) {
                calendar.add(Calendar.MONTH, -1)
            }
        }
        
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        calendar.set(Calendar.DAY_OF_MONTH, startDay.coerceAtMost(maxDays))
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    fun getEndOfCycleMs(timestamp: Long = System.currentTimeMillis(), startDay: Int = 1, endDay: Int = 31): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        if (startDay > endDay) {
            if (currentDay >= startDay) {
                calendar.add(Calendar.MONTH, 1)
            }
        }
        
        val maxDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        var adjustedEndDay = endDay
        if (startDay <= endDay && endDay >= 30) {
             adjustedEndDay = maxDays
        }
        
        calendar.set(Calendar.DAY_OF_MONTH, adjustedEndDay.coerceAtMost(maxDays))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun getStartOfDayMs(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfDayMs(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun getDaysRemainingInCycle(timestamp: Long = System.currentTimeMillis(), startDay: Int = 1, endDay: Int = 31): Int {
        val endMs = getEndOfCycleMs(timestamp, startDay, endDay)
        val diff = endMs - timestamp
        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
        return days.coerceAtLeast(0) + 1
    }

    fun utcMidnightToLocal(utcMs: Long): Long {
        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMs }
        val localCal = java.util.Calendar.getInstance().apply {
            set(utcCal.get(java.util.Calendar.YEAR), utcCal.get(java.util.Calendar.MONTH), utcCal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return localCal.timeInMillis
    }

    fun localToUtcMidnight(localMs: Long): Long {
        val localCal = java.util.Calendar.getInstance().apply { timeInMillis = localMs }
        val utcCal = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC")).apply {
            set(localCal.get(java.util.Calendar.YEAR), localCal.get(java.util.Calendar.MONTH), localCal.get(java.util.Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        return utcCal.timeInMillis
    }
}
