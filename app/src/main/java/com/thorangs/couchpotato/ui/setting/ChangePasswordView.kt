package com.thorangs.couchpotato.ui.setting

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
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.PasswordChangeUser
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class ChangePasswordView : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    private lateinit var inputCurrentPassword: EditText

    @NotEmpty(trim = true)
    private lateinit var inputNewPassword: EditText

    @NotEmpty(trim = true)
    private lateinit var inputConfirmNewPassword: EditText

    private lateinit var btnSubmit: Button
    private lateinit var stateProgress: ProgressBar
    private lateinit var newPassword: String

    private val validator = Validator(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_change_password, container, false)
        rootView.apply {
            inputCurrentPassword = findViewById(R.id.input_current_password)
            inputNewPassword = findViewById(R.id.input_new_password)
            inputConfirmNewPassword = findViewById(R.id.input_confirm_new_password)
            btnSubmit = findViewById(R.id.btn_change_password)
            stateProgress = findViewById(R.id.progress_change_password)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnSubmit.setOnClickListener { submitPressed() }
        validator.setValidationListener(this)
    }

    private fun submitPressed() {
        inputCurrentPassword.setBackgroundResource(R.drawable.bg_edittext)
        inputNewPassword.setBackgroundResource(R.drawable.bg_edittext)
        inputConfirmNewPassword.setBackgroundResource(R.drawable.bg_edittext)

        newPassword = inputNewPassword.text.toString()
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
        if (inputNewPassword.text.toString() == inputConfirmNewPassword.text.toString()) {
            val factory = PedometerApp.stepLogFactory()!!
            val userEmail = factory.getUserEmail()
            val currentPassword = inputCurrentPassword.text.toString()
            val newPassword = inputNewPassword.text.toString()
            if (isInternetConnected(context!!)) {
                stateNotClickable()

                try {
                    RestClient.instance().userDataApiService.changePassword(PasswordChangeUser(userEmail, currentPassword, newPassword))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .onErrorReturn { onErrorResponse(); it.message }
                            .subscribe { onSuccessResponse(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show()
                    onErrorResponse()
                }
            }
        } else {   //New passwords do not match
            inputNewPassword.setBackgroundResource(R.drawable.bg_error)
            inputConfirmNewPassword.setBackgroundResource(R.drawable.bg_error)
            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onErrorResponse() {
        inputCurrentPassword.setBackgroundResource(R.drawable.bg_error)
        stateClickable()
        Toast.makeText(context, "Error Occurred", Toast.LENGTH_LONG).show()
    }

    private fun onSuccessResponse(response: String) {
        stateClickable()
        when (response) {
            "success" -> {
                Toast.makeText(context, "Password changed", Toast.LENGTH_LONG).show()
                fragmentManager?.popBackStack()
            }
            else -> {
                inputCurrentPassword.setBackgroundResource(R.drawable.bg_error)
                Toast.makeText(activity, "Incorrect Password", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun stateClickable() {
        stateProgress.visibility = View.INVISIBLE
        btnSubmit.text = getString(R.string.done)
        btnSubmit.isClickable = true
    }

    private fun stateNotClickable() {
        stateProgress.visibility = View.VISIBLE
        btnSubmit.text = ""
        btnSubmit.isClickable = false
    }
}