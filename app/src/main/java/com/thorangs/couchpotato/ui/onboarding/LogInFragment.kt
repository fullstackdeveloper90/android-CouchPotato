package com.thorangs.couchpotato.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.database.StepsLog
import com.thorangs.couchpotato.models.user.SignInUser
import com.thorangs.couchpotato.models.user.UserDTO
import com.thorangs.couchpotato.ui.dashboard.DashBoardActivity
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

class LogInFragment : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    @Email
    private lateinit var inputEmail: EditText

    @NotEmpty(trim = true)
    private lateinit var inputPassword: EditText

    private lateinit var btnLogIn: Button
    private lateinit var forgotPassword: TextView
    private lateinit var signUp: TextView
    private lateinit var errorMessage: TextView
    private lateinit var stateProgress: ProgressBar

    private val validator = Validator(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.layout_login, container, false)

        return rootView.apply {
            inputEmail = findViewById(R.id.input_login_email)
            inputPassword = findViewById(R.id.input_login_password)
            btnLogIn = findViewById(R.id.btn_login)
            forgotPassword = findViewById(R.id.text_forgot_password)
            signUp = findViewById(R.id.txt_signup)
            errorMessage = findViewById(R.id.login_txt_error_message)
            stateProgress = findViewById(R.id.login_progressbar)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        btnLogIn.setOnClickListener { loginButtonPressed() }
        forgotPassword.setOnClickListener { forgotPasswordPressed() }
        signUp.setOnClickListener { signUpPressed() }

        validator.setValidationListener(this)
    }

    private fun loginButtonPressed() {
        inputEmail.setBackgroundResource(R.drawable.bg_edittext)
        inputPassword.setBackgroundResource(R.drawable.bg_edittext)

        hideKeyboardIfVisible(activity!!)
        validator.validate(true)
    }

    private fun forgotPasswordPressed() {
        hideKeyboardIfVisible(activity!!)
        val fragment = ForgotPasswordView()
        fragmentManager?.beginTransaction()
                ?.add(R.id.onboarding_base, fragment, fragment.javaClass.simpleName)
                ?.addToBackStack(fragment.javaClass.simpleName)
                ?.commit()
    }

    private fun signUpPressed() {
        hideKeyboardIfVisible(activity!!)

        val signUpFragment = SignUpFragment()
        fragmentManager?.beginTransaction()
                ?.replace(R.id.onboarding_base, signUpFragment, signUpFragment.javaClass.simpleName)
                ?.commit()
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
            errorMessage.visibility = View.INVISIBLE

            val email = inputEmail.text.trim().toString()
            val password = inputPassword.text.toString()

            try {
                RestClient.instance().userDataApiService.signIn(SignInUser(email, password))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn { onError("* " + it.message); UserDTO(0, "", "", 0, "", "", "") }
                        .subscribe {
                            saveUser(it)
                            syncStepsFromServer(it)
                        }

            } catch (e: Exception) {
                Toast.makeText(context, "Server Error", Toast.LENGTH_LONG).show()
                onError("* " + e.message)
            }
        } else {
            Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
        }
    }

    private fun saveUser(userDto: UserDTO) {
        val stepsLogFactory = PedometerApp.stepLogFactory()!!
        stepsLogFactory.apply {
            saveUserId(userDto.id)
            saveUserName(userDto.name)
            saveUserEmail(userDto.email)
            updateTargetSteps(userDto.targetSteps)
            saveBuddyEmail(userDto.buddyEmail)
            setNotifyDaily(userDto.notifyFrequency.toUpperCase() == "DAILY")
            saveCreatedDate(userDto.createdDate)
        }
    }

    private fun syncStepsFromServer(userDto: UserDTO?) {

        if (userDto != null && userDto.email.isNotEmpty()) {

            try {
                RestClient.instance().stepSyncService.getAllSteps(userDto.id)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn {
                            onError("* " + it.message)
                            Collections.emptyList()
                        }
                        .subscribe { t ->
                            Log.i("datatest", t.toString())
                            saveStepsToDevice(t.map { it -> it.getStepLog() })
                        }

            } catch (e: Exception) {
                Toast.makeText(context, "Error retrieving database", Toast.LENGTH_SHORT).show()
            }
        } else {
            onError("user & password did not match")
        }
    }

    private fun onError(s: String) {
        stateClickable()
        errorMessage.text = String.format("* %s", s)
        errorMessage.visibility = View.VISIBLE

        inputEmail.setBackgroundResource(R.drawable.bg_error)
        inputPassword.setBackgroundResource(R.drawable.bg_error)
    }

    private fun saveStepsToDevice(logs: List<StepsLog>) {
        val stepsLogFactory = PedometerApp.stepLogFactory()!!
        val observable = Observable.just(1)
                .map({
                    stepsLogFactory.saveAllSteps(logs)
                    ""
                })

        val observer = object : DisposableObserver<String>() {
            override fun onError(e: Throwable) {
            }

            override fun onNext(t: String) {

            }

            override fun onComplete() {
                val intent = Intent(activity, DashBoardActivity::class.java)
                intent.putExtra("refresh_activity", true)
                activity?.startActivity(intent)
                activity?.finish()
            }

        }
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)

    }

    private fun stateClickable() {
        btnLogIn.text = getString(R.string.login)
        btnLogIn.isClickable = true
        signUp.isClickable = true
        stateProgress.visibility = View.INVISIBLE
    }

    private fun stateNotClickable() {
        btnLogIn.text = ""
        btnLogIn.isClickable = false
        signUp.isClickable = false
        stateProgress.visibility = View.VISIBLE
    }
}