package com.thorangs.couchpotato.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.util.Log
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
import com.thorangs.couchpotato.database.StepsLog
import com.thorangs.couchpotato.database.StepsLogDTO
import com.thorangs.couchpotato.models.user.PasswordSetSignInUser
import com.thorangs.couchpotato.models.user.UserDTO
import com.thorangs.couchpotato.ui.dashboard.DashBoardActivity
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.observers.DisposableObserver
import io.reactivex.schedulers.Schedulers
import java.util.*

/**
 ** Created by nbnsresta on 11/22/2017.
 **/
class SetPasswordView : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    private lateinit var inputNewPassword: EditText

    @NotEmpty(trim = true)
    private lateinit var inputConfirmNewPassword: EditText

    private lateinit var btnSend: Button
    private lateinit var stateProgress: ProgressBar

    private val validator = Validator(this)

    private lateinit var email: String
    private lateinit var key: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")!!
        key = arguments?.getString("key")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_set_password, container, false)
        rootView.apply {
            inputNewPassword = findViewById(R.id.input_set_new_password)
            inputConfirmNewPassword = findViewById(R.id.input_set_confirm_new_password)
            btnSend = findViewById(R.id.btn_setpw_done)
            stateProgress = findViewById(R.id.progress_setpw_done)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnSend.setOnClickListener { buttonClicked() }
        validator.setValidationListener(this)
    }

    private fun buttonClicked() {
        inputNewPassword.setBackgroundResource(R.drawable.bg_edittext)
        inputConfirmNewPassword.setBackgroundResource(R.drawable.bg_edittext)

        hideKeyboardIfVisible(activity!!)
        validator.validate()
    }

    override fun onValidationSucceeded() {
        if (inputNewPassword.text.toString() == inputConfirmNewPassword.text.toString()) {

            if (isInternetConnected(context!!)) {
                stateNotClickable()
                val password = inputNewPassword.text.toString()

                try {
                    RestClient.instance().userDataApiService.setPasswordWithToken(PasswordSetSignInUser(key, email, password))
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeOn(Schedulers.io())
                            .onErrorReturn { onError("Error Verifying"); null }
                            .subscribe {
                                saveUser(it)
                                syncStepsFromServer(it)

                            }

                } catch (e: Exception) {
                    e.printStackTrace()
                    onError("Server Error")
                }
            } else {
                Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
            }
        } else {
            inputNewPassword.setBackgroundResource(R.drawable.bg_error)
            inputConfirmNewPassword.setBackgroundResource(R.drawable.bg_error)
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

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        if (errors != null) {
            for (error in errors) {
                error.view.setBackgroundResource(R.drawable.bg_error)
            }
        }
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

    private fun save(userDto: UserDTO, logs: List<StepsLogDTO>) {
        val stepsLogFactory = PedometerApp.stepLogFactory()!!
        stepsLogFactory.apply {
            saveUserId(userDto.id)
            saveUserName(userDto.name)
            saveUserEmail(userDto.email)
            updateTargetSteps(userDto.targetSteps)
            saveBuddyEmail(userDto.buddyEmail)
            setNotifyDaily(userDto.notifyFrequency == "DAILY")
            saveCreatedDate(userDto.createdDate)
        }

        Single.fromCallable {
            stepsLogFactory.saveAllSteps(logs.map { it -> it.getStepLog() })
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { t ->
                    Logger.log("came here solta")
                    activity?.startActivity(Intent(activity, DashBoardActivity::class.java))
                    activity?.finish()
                }
        stateClickable()
    }

    private fun onError(s :String) {
        stateClickable()
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }

    private fun stateClickable() {
        btnSend.isClickable = true
        btnSend.text = getString(R.string.done)
        stateProgress.visibility = View.INVISIBLE
    }

    private fun stateNotClickable() {
        btnSend.isClickable = false
        btnSend.text = ""
        stateProgress.visibility = View.VISIBLE
    }
}