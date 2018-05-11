package com.thorangs.couchpotato.utils

import org.junit.Assert
import org.junit.Test
import java.util.*

/**
 * Created by Laxman Bhattarai on 11/30/17.
 * erluxman@gmail.com
 * https://github.com/erluxman
 * https://twitter.com/erluxman
 */
class TimeAndNotificationKtTest {
    @Test
    fun `is the difference zero`() {
        Assert.assertEquals(0, calculateSecondsDifferenceBetweenClientMidNightAndServerNow(4))
    }

    @Test
    fun `is positive tz offset`() {
        val hour = `get hour of client`(20700000)
        Assert.assertTrue(2 == hour)
    }

    @Test
    fun `is negative tz offset`() {
        val hour = `get hour of client`(-20700000)
        Assert.assertFalse(-2 == hour)
    }

    fun `get hour of client`(clientTZOffset: Int): Int {
        val c = Calendar.getInstance()
        val clinetOffset = 20700000
        val serverOffset = c.timeZone.rawOffset
        val tzDiff = clientTZOffset - c.timeZone.rawOffset
        val tempSeconds = tzDiff / 1000
        println("Hour = " + tempSeconds / 3600 + " Minute : " + (tempSeconds % 3600) / 60)
        println(c.get(Calendar.MONTH).toString()+" / "+ c.get(Calendar.DAY_OF_MONTH).toString()+"/"+c.get(Calendar.HOUR_OF_DAY).toString() + "/" + c.get(Calendar.MINUTE).toString() + "/" + c.get(Calendar.SECOND).toString())
        c.add(Calendar.MILLISECOND, tzDiff)
        println(c.get(Calendar.MONTH).toString()+" / "+ c.get(Calendar.DAY_OF_MONTH).toString()+"/"+c.get(Calendar.HOUR_OF_DAY).toString() + "/" + c.get(Calendar.MINUTE).toString() + "/" + c.get(Calendar.SECOND).toString())
        return c.get(Calendar.HOUR_OF_DAY)
    }
}