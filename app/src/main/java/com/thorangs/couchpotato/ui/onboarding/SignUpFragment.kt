package com.thorangs.couchpotato.ui.onboarding

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.SignUpUser
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.addFragmentOnBoarding
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*

class SignUpFragment : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    private lateinit var inputName: EditText

    @NotEmpty(trim = true)
    @Email
    private lateinit var inputEmail: EditText

    @NotEmpty(trim = true)
    private lateinit var inputPassword: EditText

    @NotEmpty(trim = true)
    private lateinit var inputConfirmPassword: EditText

    private lateinit var errorMessage: TextView
    private lateinit var stateProgress: ProgressBar

    private lateinit var btnSignUp: Button
    private lateinit var login: TextView

    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private var gmtTimezoneOffset: Int = Calendar.getInstance().timeZone.rawOffset

    private val validator = Validator(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.layout_signup, container, false)
        rootView?.apply {
            inputName = findViewById(R.id.signup_input_name)
            inputEmail = findViewById(R.id.signup_input_email)
            inputPassword = findViewById(R.id.signup_input_password)
            inputConfirmPassword = findViewById(R.id.signup_input_confirm_password)
            errorMessage = findViewById(R.id.signup_txt_error_message)
            stateProgress = findViewById(R.id.signup_progressbar)
            btnSignUp = findViewById(R.id.btn_signup)
            login = findViewById(R.id.txt_login)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnSignUp.setOnClickListener { signUpButtonPressed() }
        login.setOnClickListener { logInPressed() }
        validator.setValidationListener(this)
    }

    private fun signUpButtonPressed() {
        inputName.setBackgroundResource(R.drawable.bg_edittext)
        inputEmail.setBackgroundResource(R.drawable.bg_edittext)
        inputPassword.setBackgroundResource(R.drawable.bg_edittext)
        inputConfirmPassword.setBackgroundResource(R.drawable.bg_edittext)

        hideKeyboardIfVisible(activity!!)
        validator.validate(true)
    }

    private fun logInPressed() {
        hideKeyboardIfVisible(activity!!)
        val logInFragment = LogInFragment()
        fragmentManager?.beginTransaction()
                ?.replace(R.id.onboarding_base, logInFragment, logInFragment.javaClass.simpleName)
                ?.commit()
    }

    override fun onValidationSucceeded() {
        if (inputPassword.text.toString() == inputConfirmPassword.text.toString()) {
            if (isInternetConnected(context!!)) {
                stateNotClickable()
                errorMessage.visibility = INVISIBLE

                name = inputName.text.toString()
                email = inputEmail.text.toString()
                password = inputPassword.text.toString()

                try {
                    RestClient.instance().userDataApiService.signUp(SignUpUser(name, email, password,gmtTimezoneOffset))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .onErrorReturn { Logger.log(it.message!!);it.message }
                            .subscribe { proceed(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show()
                    proceed("failure")
                }
            } else {
                Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
            }
        } else {
            inputPassword.setBackgroundResource(R.drawable.bg_error)
            inputConfirmPassword.setBackgroundResource(R.drawable.bg_error)
        }
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        if (errors != null) {
            for (error in errors) {
                error.view.setBackgroundResource(R.drawable.bg_error)
            }
        }
    }

    private fun proceed(response: String) {
        stateClickable()

        when (response.toLowerCase()) {

            "success" -> {

                val fragment = TargetStepsFragment()
                val bundle = Bundle()
                bundle.putString("email", email)
                bundle.putString("password", password)
                fragment.arguments = bundle
                fragmentManager?.addFragmentOnBoarding(fragment)
            }

            else -> {
                errorMessage.visibility = View.VISIBLE
                errorMessage.text = response
            }
        }
    }

    private fun stateClickable() {
        btnSignUp.isClickable = true
        login.isClickable = true
        btnSignUp.text = getString(R.string.signup)
        stateProgress.visibility = INVISIBLE
    }

    private fun stateNotClickable() {
        btnSignUp.isClickable = false
        login.isClickable = false
        btnSignUp.text = ""
        stateProgress.visibility = VISIBLE
    }
}