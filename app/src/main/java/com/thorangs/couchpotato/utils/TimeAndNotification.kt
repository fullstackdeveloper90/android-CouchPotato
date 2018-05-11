package com.thorangs.couchpotato.utils

import java.util.*


/**
 * Created by Laxman Bhattarai on 11/30/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
fun calculateSecondsDifferenceBetweenClientMidNightAndServerNow(clientTZOffset: Int): Int {
    return clientTZOffset - getMachineTimeZoneOffset()
}





fun shouldFireEmailBuddy(clientTZOffset: Int): Boolean {
    val c = Calendar.getInstance()
    val tzDiff = clientTZOffset - c.timeZone.rawOffset
    val now = c.timeInMillis
    c.set(Calendar.HOUR_OF_DAY, 0)
    c.set(Calendar.MINUTE, 0)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    val secondsPassed = (now - c.timeInMillis )/ 1000
    return (tzDiff + secondsPassed > 86400)
}















fun getMachineTimeZoneOffset(): Int {
    val calendar = Calendar.getInstance()
    return calendar.timeZone.rawOffset
}

val checagoTimeZoneOffset = -21600000
val malasiyaTZOffset = 28800000
val nepalTZOffset = 20700000