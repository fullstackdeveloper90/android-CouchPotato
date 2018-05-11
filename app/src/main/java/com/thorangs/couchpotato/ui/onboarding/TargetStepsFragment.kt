package com.thorangs.couchpotato.ui.onboarding

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Min
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.TargetStepsSignInUser
import com.thorangs.couchpotato.utils.addFragmentOnBoarding
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class TargetStepsFragment : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    private lateinit var targetStepsInput: EditText

    private lateinit var stateProgress: ProgressBar
    private lateinit var btnConfirm: Button

    private val validator = Validator(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_target_steps, container, false)
        v.apply {
            targetStepsInput = findViewById(R.id.input_target_steps)
            btnConfirm = findViewById(R.id.btn_confirm_target_steps)
            stateProgress = findViewById(R.id.target_progressbar)
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnConfirm.setOnClickListener { v -> submit(v) }
        validator.setValidationListener(this)
    }

    private fun submit(v: View) {
        targetStepsInput.setBackgroundResource(R.drawable.bg_edittext)
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

            val count = Integer.parseInt(targetStepsInput.text.toString())
            val email = arguments?.getString("email")!!

            try {
                RestClient.instance().userDataApiService.updateStepTarget(TargetStepsSignInUser(email, count))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn { proceed("Failure"); null }
                        .subscribe { proceed(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show()
                proceed("failure")
            }
        } else {
            Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
        }
    }

    private fun proceed(message: String?) {
        stateClickable()

        when (message) {

            "success" -> {
                val fragment = SetBuddyFragment()
                fragment.arguments = arguments
                fragmentManager?.addFragmentOnBoarding(fragment)
            }

            else -> {
                btnConfirm.isClickable = true
                btnConfirm.text = getString(R.string.next)
            }
        }
    }

    private fun stateClickable() {
        btnConfirm.isClickable = true
        btnConfirm.text = getString(R.string.next)
        stateProgress.visibility = View.INVISIBLE
    }

    private fun stateNotClickable() {
        btnConfirm.isClickable = false
        btnConfirm.text = ""
        stateProgress.visibility = View.VISIBLE
    }

}