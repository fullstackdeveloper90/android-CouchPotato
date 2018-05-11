package com.thorangs.couchpotato.ui.setting

import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.mobsandgeeks.saripaar.ValidationError
import com.mobsandgeeks.saripaar.Validator
import com.mobsandgeeks.saripaar.annotation.Email
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.UserDTO
import com.thorangs.couchpotato.utils.Logger
import com.thorangs.couchpotato.utils.addFragmentFromNavigation
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class SettingsView : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    private lateinit var inputName: EditText

    private lateinit var textEmail: TextView

    @NotEmpty(trim = true)
    @Email
    private lateinit var inputBuddyEmail: EditText

    @NotEmpty(trim = true)
    private lateinit var inputNumberOfSteps: EditText

    private lateinit var checkNotifyDaily: CheckBox
    private var notifyDaily = true

    private lateinit var textChangePassword: TextView

    private lateinit var btnBack: TextView
    private lateinit var btnSave: TextView

    private val validator = Validator(this)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_settings, container, false)
        rootView?.apply {

            inputName = findViewById(R.id.input_settings_name)
            textEmail = findViewById(R.id.text_settings_email)
            textChangePassword = findViewById(R.id.txt_settings_change_password)
            inputBuddyEmail = findViewById(R.id.input_settings_buddy_email)
            inputNumberOfSteps = findViewById(R.id.input_settings_daily_goal)

            checkNotifyDaily = findViewById(R.id.check_settings_notify_daily)

            btnBack = findViewById(R.id.text_settings_back)
            btnSave = findViewById(R.id.text_settings_save)

        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val factory = PedometerApp.stepLogFactory()!!
        inputName.setText(factory.getUserName())
        textEmail.text = factory.getUserEmail()
        inputBuddyEmail.setText(factory.getBuddyEmail())
        inputNumberOfSteps.setText(factory.getCurrentTarget().toString())

        checkNotifyDaily.isChecked = factory.getNotifyDaily()
        //checkNotifyDaily.setOnCheckedChangeListener { }

        textChangePassword.setOnClickListener { handlePasswordChange() }
        btnBack.setOnClickListener { goBack() }
        btnSave.setOnClickListener { saveButtonPressed() }

        validator.setValidationListener(this)
    }

    private fun handlePasswordChange() {
        hideKeyboardIfVisible(activity!!)
        val fragment = ChangePasswordView()
        fragmentManager?.addFragmentFromNavigation(fragment)
    }

    private fun goBack() {
        hideKeyboardIfVisible(activity!!)
        fragmentManager?.popBackStack()
    }

    private fun saveButtonPressed() {
        hideKeyboardIfVisible(activity!!)

        inputName.setBackgroundResource(R.drawable.bg_edittext)
        inputBuddyEmail.setBackgroundResource(R.drawable.bg_edittext)
        inputNumberOfSteps.setBackgroundResource(R.drawable.bg_edittext)
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

        if (isInternetConnected(activity!!)) {
            val userId = PedometerApp.stepLogFactory()!!.getUserId()
            val name = inputName.text.trim().toString()
            val email = PedometerApp.stepLogFactory()!!.getUserEmail()
            val buddyEmail = inputBuddyEmail.text.trim().toString()
            val targetSteps = Integer.parseInt(inputNumberOfSteps.text.toString())
            val dateCreated = PedometerApp.stepLogFactory()!!.getCreatedDate()

            try {
                RestClient.instance().userDataApiService.updateUser(UserDTO(userId, name, email, targetSteps, buddyEmail, if (checkNotifyDaily.isChecked) "DAILY" else "NONE", dateCreated))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn { t: Throwable -> Logger.log("error occurred " + t.message); t.message }
                        .subscribe {
                            if (it.toLowerCase() == "success") {

                                val factory = PedometerApp.stepLogFactory()!!
                                factory.apply {
                                    saveUserName(name)
                                    updateTargetSteps(targetSteps)
                                    saveUserEmail(email)
                                    saveBuddyEmail(buddyEmail)
                                    setNotifyDaily(notifyDaily)
                                }
                                btnSave.setTextColor(Color.WHITE)
                                Toast.makeText(activity, "Data updated", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(activity, "Data not updated " + it, Toast.LENGTH_SHORT).show()
                            }
                        }
            } catch (e: Exception) {
                Toast.makeText(activity, "Server Error", Toast.LENGTH_SHORT).show()
                btnSave.setTextColor(Color.RED)
            }
        } else {
            Toast.makeText(activity, "Internet not connected.", Toast.LENGTH_LONG).show()
        }
    }
}