package com.thorangs.couchpotato.ui.dashboard

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.*
import android.widget.*
import com.anjlab.android.iab.v3.BillingProcessor
import com.anjlab.android.iab.v3.TransactionDetails
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.backend.syncing.Step
import com.thorangs.couchpotato.libs.VerticalViewPager
import com.thorangs.couchpotato.services.StepCounterService
import com.thorangs.couchpotato.ui.leaderboard.PotatoBoardView
import com.thorangs.couchpotato.ui.onboarding.OnBoardingActivity
import com.thorangs.couchpotato.ui.refer.ReferView
import com.thorangs.couchpotato.ui.setting.ChangePasswordView
import com.thorangs.couchpotato.ui.setting.SettingsView
import com.thorangs.couchpotato.utils.*
import io.reactivex.Observer
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*


class DashBoardActivity : AppCompatActivity(), DrawerLayout.DrawerListener, BillingProcessor.IBillingHandler {

    private lateinit var viewPager: VerticalViewPager
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var menu: Menu
    private lateinit var bp: BillingProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)

        startService(Intent(this, StepCounterService::class.java))

        //bp = BillingProcessor(this,null , this)
        bp = BillingProcessor(this, "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAi0hJbnTJQI13nD4F3O3b+guWq2FxIO2AclT4sZr9j1aVYIxoanbaxY0K9S/tKWTYFz0ttvR4rnwia10NuYMYrZAUKSwNLrtE7+6NLzwLZjf7BEY9bd2SFkhJ/8iasLY/qdw3mE2I4XP9ylBQ8blDyZMPS6oikVTzvUkPsDLS36f1vUn6HZt+OvV+lCmabDXitz1WD1R54QvLa8j07kb/TY1c8UUtDHjyAlpQgDWG7t61XTcFNvFQfkZndH9rR/Pc68EEj90HRmJMk6sMj5vxQigbC+3GIHxzi14cnxve4ezRX8f9Ck3dreltUMNawO56b5mR0T4KgLGp72dhZDdFKwIDAQAB", this)

        drawerLayout = findViewById(R.id.drawer_dashboard)
        drawerLayout.addDrawerListener(this)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.findViewById<ImageView>(R.id.toggleIcon).setOnClickListener { drawerLayout.openDrawer(GravityCompat.START) }
        viewPager = findViewById(R.id.dashboard_contents)
        viewPager.adapter = VerticalViewPager.VerticalPagerAdapter(supportFragmentManager)
        setUpNavigationDrawer()
        supportFragmentManager.addOnBackStackChangedListener { onBackStackChanged() }
    }

    private var statePotatoFragmentShowing = false

    override fun onBackPressed() {
        if (statePotatoFragmentShowing
                && (supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount - 1).name == PotatoBoardView::class.java.simpleName)) {
            removePotatoBoard(menu.findItem(R.id.potato_switcher))
            return
        }
        super.onBackPressed()
    }

    private fun setUpNavigationDrawer() {
        val v: View = findViewById(R.id.navigation_drawer)

        v.findViewById<TextView>(R.id.navigation_settings).setOnClickListener { launchSettingsPage() }
        v.findViewById<TextView>(R.id.navigation_subscribe).setOnClickListener { handleSubscribe() }
        v.findViewById<TextView>(R.id.navigation_help).setOnClickListener { onHelp() }
        v.findViewById<TextView>(R.id.navigation_logout).setOnClickListener { onLogout() }
    }

    private fun onBackStackChanged() {
        val count = supportFragmentManager.backStackEntryCount

        if (count > 0) {
            val entry = supportFragmentManager.getBackStackEntryAt(count - 1)

            if (arrayOf(SettingsView::class.java.simpleName, ReferView::class.java.simpleName, ChangePasswordView::class.java.simpleName).contains(entry.name)) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

            } else {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
        } else {
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.switcher_potatoboard, menu)
        this.menu = menu!!
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {

            android.R.id.home -> {
                drawerLayout.openDrawer(GravityCompat.START)
            }
            R.id.potato_switcher -> {
                if (statePotatoFragmentShowing) {
                    removePotatoBoard(item)
                } else {
                    if (subscriptionHasNotExpired()) {
                        addPotatoBoard(item)
                    } else {
                        subscriptionPopUp(getString(R.string.subscription_expired_title))
                    }
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (!bp.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private val isActiveObserver = object : DisposableObserver<String>() {
        override fun onComplete() {
        }

        override fun onNext(t: String) {
            if (t == "success") {
                Logger.log("success updating payment history ")
            } else {
                Logger.log("payment history was not updated")
            }
        }

        override fun onError(e: Throwable) {
            Logger.log("error loading payment history")
        }


    }

    private fun subscriptionPopUp(titleText: String) {
        val dialog = Dialog(this)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (!subscriptionHasNotExpired()) {
            //dialog.setCancelable(false)
            dialog.setOnCancelListener { this@DashBoardActivity.finish() }
        }
        dialog.setContentView(R.layout.dialog_subscribe)
        val subscribe = dialog.findViewById<Button>(R.id.subscribe_button)
        val title = dialog.findViewById<TextView>(R.id.subscribe_title)
        title.text = titleText
        subscribe.setOnClickListener {
            openPlaystoreForInapp()
        }
        dialog.show()
    }

    private fun openPlaystoreForInapp() {
        bp.purchase(this, "yearly_subscription")
    }

    private fun addPotatoBoard(item: MenuItem) {
        statePotatoFragmentShowing = true
        item.setIcon(R.drawable.potato_rank_dark)

        val fragment = PotatoBoardView()
        supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.open_potato_fragment, R.anim.close_potato_fragment, R.anim.open_potato_fragment, R.anim.close_potato_fragment)
                .add(R.id.bg_viewpager, fragment)
                .addToBackStack(fragment.javaClass.simpleName)
                .commit()
    }

    private fun removePotatoBoard(item: MenuItem) {
        statePotatoFragmentShowing = false
        item.setIcon(R.drawable.potato_light)
        supportFragmentManager.popBackStackImmediate()
    }

    private fun launchSettingsPage() {
        drawerLayout.closeDrawer(Gravity.START)
        val page = SettingsView()
        supportFragmentManager.addFragmentFromNavigation(page)
    }

    private fun handleSubscribe() {
        drawerLayout.closeDrawer(Gravity.START)
        subscriptionPopUp(getString(R.string.subscribe_title))

    }

    private fun onHelp() {
        drawerLayout.closeDrawer(Gravity.START)

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("support@getcouchpotatoapp.com"))
        intent.putExtra(Intent.EXTRA_SUBJECT, "HELP")
        try {
            startActivity(intent)
        } catch (ex: android.content.ActivityNotFoundException) {
            Toast.makeText(this, "There are no email applications installed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onLogout() {
        drawerLayout.closeDrawer(Gravity.START)
        val dialog = Dialog(this)
        dialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_logout)
        dialog.setCancelable(false)
        val title = dialog.findViewById<TextView>(R.id.logout_title)
        val body = dialog.findViewById<TextView>(R.id.logout_body)
        val progressBar = dialog.findViewById<ProgressBar>(R.id.logout_progress)
        progressBar.indeterminateDrawable.setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.MULTIPLY)
        dialog.show()

        stopService(Intent(this, OnBoardingActivity::class.java))

        val stepLogFactory = PedometerApp.stepLogFactory()!!
        if (stepLogFactory.getUserId() > 0) {
            Single.fromCallable {
                val step = stepLogFactory.getNonReactiveSteps(today)
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = step.date
                val dateForService = DateUtils.getEnglishDateString(calendar)
                val stepToSend = Step(stepLogFactory.getUserId(), dateForService, step.steps, step.targetSteps)
                val stepsSyncService = RestClient.instance().stepSyncService
                if (isInternetConnected(this@DashBoardActivity)) {
                    stepsSyncService.saveSteps(stepToSend).enqueue(object : Callback<String?> {
                        override fun onResponse(call: Call<String?>?, response: Response<String?>?) {
                            stepLogFactory.clearAllPreferences()

                            Single.fromCallable {
                                PedometerApp.stepLogFactory()?.deleteSteps()
                            }.subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe()
                            dialog.dismiss()
                            startActivity(Intent(this@DashBoardActivity, OnBoardingActivity::class.java))
                            this@DashBoardActivity.finish()
                        }

                        override fun onFailure(call: Call<String?>?, t: Throwable?) {
                            Logger.log("retrofit success " + t?.message)
                            progressBar.visibility = View.INVISIBLE
                            dialog.setCancelable(true)
                            title.text = "Logout Failed"
                            body.text = "Failed to sync your data to server. Please try again with internet connection."
                        }
                    })
                } else {
                    dialog.setCancelable(true)
                    progressBar.visibility = View.INVISIBLE
                    title.text = "Logout Failed"
                    body.text = "Failed to sync your data to server. Please try again with internet connection."
                }
            }.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe()
        }

    }

    override fun onDrawerOpened(drawerView: View) {
        drawerView.bringToFront()
        drawerView.requestLayout()
    }

    override fun onResume() {
        super.onResume()
        PedometerApp.stepLogFactory()?.syncLocalAndCloud()
        if (!subscriptionHasNotExpired()) {
            subscriptionPopUp(getString(R.string.subscribe_title))
            RestClient.instance().userDataApiService.setActive(false, PedometerApp.stepLogFactory()?.getUserEmail()!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isActiveObserver)

        }else{
            RestClient.instance().userDataApiService.setActive(true, PedometerApp.stepLogFactory()?.getUserEmail()!!)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isActiveObserver)
        }
    }

    override fun onDrawerStateChanged(newState: Int) {
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
    }

    override fun onDrawerClosed(drawerView: View) {
    }

    override fun onBillingInitialized() {
        Logger.log("on billing initialized")

    }

    override fun onPurchaseHistoryRestored() {
        Logger.log("on purchase history restored")
    }

    override fun onProductPurchased(productId: String, details: TransactionDetails?) {
        val purchasedDate = details?.purchaseInfo?.purchaseData?.purchaseTime
        val cal = Calendar.getInstance()
        cal.timeInMillis = purchasedDate?.time!!
        val signUpDateString = DateUtils.getEnglishDateString(cal)
        PedometerApp.stepLogFactory()?.savePurchasedDate(signUpDateString)
        Logger.log("on product purchased")
    }

    override fun onBillingError(errorCode: Int, error: Throwable?) {
        Logger.log("on Billing error")
        val factory = PedometerApp.stepLogFactory()!!
        val signupDateString = factory.getCreatedDate()
        val signedUpDate = DateUtils.getCalendarFromString(signupDateString)
        signedUpDate.add(Calendar.MONTH, 1)
        if (signedUpDate.timeInMillis < System.currentTimeMillis()) {
            subscriptionPopUp("Subscription Expired")
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (bp != null) bp.release()
    }

    companion object {
        fun subscriptionHasNotExpired(): Boolean {
            //take the purchase date
            val purchaseDateString = PedometerApp.stepLogFactory()?.getPurchasedDate()!!
            val purchaseDate = DateUtils.getCalendarFromString(purchaseDateString)
            //take the signed up date
            val signUpDateString = PedometerApp.stepLogFactory()?.getCreatedDate()!!
            val signedUpDate = DateUtils.getCalendarFromString(signUpDateString)
            // x = add 1 year in purchase date(coz the subscription will expire in one year)
            purchaseDate.add(Calendar.YEAR, 1)
            // y = add 14 days to  sign up date coz user can use app for 14 days for free
            signedUpDate.add(Calendar.DAY_OF_MONTH, 14)
            //take greater between x and y
            val actualExpiryDate = if (purchaseDate.timeInMillis > signedUpDate.timeInMillis) purchaseDate else signedUpDate
            val today = Calendar.getInstance()
            // if x or y 's MS value is smaller than today MS value return true if not return false
            return actualExpiryDate.timeInMillis > today.timeInMillis
        }
    }
}