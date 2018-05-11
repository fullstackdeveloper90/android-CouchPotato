package com.thorangs.couchpotato.ui.onboarding

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.BuddyEmailSignInUser
import com.thorangs.couchpotato.utils.addFragmentOnBoarding
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SetBuddyFragment : Fragment(), Validator.ValidationListener {

    @Email
    @NotEmpty(trim = true)
    private lateinit var buddyEmail: EditText
    private lateinit var buddySubmit: Button
    private lateinit var stateProgress: ProgressBar

    private val validator = Validator(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_set_buddy_email, container, false)
        v?.apply {
            buddyEmail = findViewById(R.id.input_buddy_email)
            buddySubmit = findViewById(R.id.btn_confirm_buddy)
            stateProgress = findViewById(R.id.buddy_progressbar)
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        buddySubmit.setOnClickListener { submitBuddy(it) }

        validator.setValidationListener(this)
    }

    private fun submitBuddy(v: View) {
        buddyEmail.setBackgroundResource(R.drawable.bg_edittext)

        hideKeyboardIfVisible(activity!!)
        validator.validate(true)
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        if (errors != null) {
            for (error in errors) {
                error.view.setBackgroundResource(R.drawable.bg_error)
            }
        }
    }

    override fun onValidationSucceeded() {
        if (isInternetConnected(context!!)) {
            stateNotClickable()

            val email = arguments?.getString("email")!!
            try{
                RestClient.instance().userDataApiService.updateBuddyEmail(BuddyEmailSignInUser(email,buddyEmail.text.toString()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn { proceed("Failure"); null }
                        .subscribe { proceed(it) }
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show()
                proceed("failure")
            }
        } else {
            Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
        }
    }

    private fun proceed(message: String) {
        stateClickable()

        when (message.toLowerCase()) {

            "success" -> {
                val fragment = OnboardingFinalFragment()
                fragment.arguments = arguments
                fragmentManager?.addFragmentOnBoarding(fragment)
            }

            else -> {
                buddySubmit.isClickable = true
                buddySubmit.text = getString(R.string.next)
            }
        }
    }

    private fun stateClickable(){
        buddySubmit.isClickable = true
        buddySubmit.text = getString(R.string.next)
        stateProgress.visibility= View.INVISIBLE
    }

    private fun stateNotClickable(){
        buddySubmit.isClickable = false
        buddySubmit.text = ""
        stateProgress.visibility = VISIBLE
    }
}