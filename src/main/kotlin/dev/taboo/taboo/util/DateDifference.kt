package dev.taboo.taboo.util

object DateDifference {

    @JvmOverloads
    fun timeSince(diff: Long, useSeconds: Boolean = true): String {
        var difference = diff
        val secondsInMillis: Long = 1000
        val minutesInMillis = secondsInMillis * 60
        val hoursInMillis = minutesInMillis * 60
        val daysInMillis = hoursInMillis * 24
        val yearsInMillis = daysInMillis * 365
        val elapsedYears = difference / yearsInMillis
        difference %= yearsInMillis
        val elapsedDays = difference / daysInMillis
        difference %= daysInMillis
        val elapsedHours = difference / hoursInMillis
        difference %= hoursInMillis
        val elapsedMinutes = difference / minutesInMillis
        difference %= minutesInMillis
        val elapsedSeconds = difference / secondsInMillis
        val output = ArrayList<String>()
        if (elapsedYears > 0) output.add(elapsedYears.toString() + " year" + if (elapsedYears > 1) "s" else "")
        if (elapsedDays > 0) output.add(elapsedDays.toString() + " day" + if (elapsedDays > 1) "s" else "")
        if (elapsedHours > 0) output.add(elapsedHours.toString() + " hour" + if (elapsedHours > 1) "s" else "")
        if (elapsedMinutes > 0) output.add(elapsedMinutes.toString() + " minute" + if (elapsedMinutes > 1) "s" else "")
        if (elapsedSeconds > 0 && useSeconds) output.add(elapsedSeconds.toString() + " second" + if (elapsedSeconds > 1) "s" else "")
        var response = StringBuilder()
        for (module in output) {
            if (response.toString() == "") {
                response = StringBuilder(module)
            } else if (module == output[output.size - 1]) {
                response.append(", and ").append(module)
            } else {
                response.append(", ").append(module)
            }
        }
        return response.toString()
    }

}