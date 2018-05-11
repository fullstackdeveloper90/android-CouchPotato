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
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.utils.addFragmentOnBoarding
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ForgotPasswordView : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    @Email
    private lateinit var inputEmail: EditText

    private lateinit var btnSend: Button
    private lateinit var stateProgress: ProgressBar

    private val validator = Validator(this)
    private lateinit var email: String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_forgot_password, container, false)
        rootView.apply {
            inputEmail = findViewById(R.id.input_forgotpw_email)
            btnSend = findViewById(R.id.btn_forgotpw_send)
            stateProgress = findViewById(R.id.progress_forgotpw)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnSend.setOnClickListener { performClick() }

        validator.setValidationListener(this)
    }

    private fun performClick() {
        inputEmail.setBackgroundResource(R.drawable.bg_edittext)

        hideKeyboardIfVisible(activity!!)
        validator.validate()
    }

    override fun onValidationSucceeded() {
        if (isInternetConnected(context!!)) {
            stateNotClickable()
            email = inputEmail.text.trim().toString()
            try {
                RestClient.instance().userDataApiService.forgotPasswordRequest(email)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .onErrorReturn { onError(); it.toString() }
                        .subscribe { onSuccess(it) }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show()
                onError()
            }
        } else {
            Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
        }
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        inputEmail.setBackgroundResource(R.drawable.bg_error)
    }

    private fun onSuccess(response: String) {
        stateClickable()

        when (response.toLowerCase()) {
            "success" -> {
                Toast.makeText(context, "Please check your email.", Toast.LENGTH_LONG).show()
                val bundle = Bundle()
                bundle.putString("email", email)
                val fragment = KeyVerifyView()
                fragment.arguments = bundle
                fragmentManager?.addFragmentOnBoarding(fragment)
            }
            else -> {
                onError()
                Toast.makeText(context, response, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onError() {
        stateClickable()
        inputEmail.setBackgroundResource(R.drawable.bg_error)
    }

    private fun stateClickable() {
        stateProgress.visibility = View.INVISIBLE
        btnSend.text = getString(R.string.send)
        btnSend.isClickable = true
    }

    private fun stateNotClickable() {
        stateProgress.visibility = View.VISIBLE
        btnSend.text = ""
        btnSend.isClickable = false
    }
}