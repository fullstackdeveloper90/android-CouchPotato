package com.thorangs.couchpotato.utils

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.view.inputmethod.InputMethodManager
import com.thorangs.couchpotato.R
import java.util.*

fun flooredCalendar(ms: Long): Calendar {
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = ms
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar
}

val today: Long
    get() {
        return flooredCalendar(System.currentTimeMillis()).timeInMillis
    }

val yesterday: Long
    get() {
        val c = flooredCalendar(System.currentTimeMillis())
        c.add(Calendar.DATE, -1)
        return c.timeInMillis
    }

val tomorrow: Long
    get() {
        val c = flooredCalendar(System.currentTimeMillis())
        c.add(Calendar.DATE, 1)
        return c.timeInMillis
    }

val twoDaysAgo: Long
    get() {
        val c = flooredCalendar(System.currentTimeMillis())
        c.add(Calendar.DATE, -2)
        return c.timeInMillis
    }

fun FragmentManager.addFragmentOnBoarding(fragment: Fragment) {
    beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out)
            .add(R.id.onboarding_base, fragment, fragment.javaClass.simpleName)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
}

fun FragmentManager.addFragmentFromNavigation(fragment: Fragment) {
    beginTransaction()
            .setCustomAnimations(R.anim.slide_in, R.anim.slide_out, R.anim.slide_in, R.anim.slide_out)
            .add(R.id.drawer_dashboard, fragment, fragment.javaClass.simpleName)
            .addToBackStack(fragment.javaClass.simpleName)
            .commit()
}

fun isInternetConnected(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    return activeNetworkInfo != null && activeNetworkInfo.isConnected
}

fun showKeyboard(context: Context) {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun hideKeyboardIfVisible(activity: Activity) {
    val currentFocus = activity.currentFocus
    if (currentFocus != null) {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus.windowToken, 0)
    }
}
