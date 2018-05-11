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
import com.mobsandgeeks.saripaar.annotation.NotEmpty
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.utils.addFragmentOnBoarding
import com.thorangs.couchpotato.utils.hideKeyboardIfVisible
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 ** Created by nbnsresta on 11/22/2017.
 **/
class KeyVerifyView : Fragment(), Validator.ValidationListener {

    @NotEmpty(trim = true)
    private lateinit var inputKey: EditText

    private lateinit var btnSend: Button
    private lateinit var stateProgress: ProgressBar

    private val validator = Validator(this)

    private lateinit var key: String
    private lateinit var email: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        email = arguments?.getString("email")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_key_verify, container, false)
        rootView.apply {
            inputKey = findViewById(R.id.input_verification_key)
            btnSend = findViewById(R.id.btn_verification_key)
            stateProgress = findViewById(R.id.progress_key_verify)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnSend.setOnClickListener { buttonPressed() }

        validator.setValidationListener(this)
    }

    private fun buttonPressed() {
        inputKey.setBackgroundResource(R.drawable.bg_edittext)

        hideKeyboardIfVisible(activity!!)
        validator.validate()
    }

    override fun onValidationFailed(errors: MutableList<ValidationError>?) {
        inputKey.setBackgroundResource(R.drawable.bg_error)
    }

    override fun onValidationSucceeded() {
        if (isInternetConnected(context!!)) {
            stateNotClickable()

            key = inputKey.text.toString()
            try {
                RestClient.instance().userDataApiService.verifyKey(key, email)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribeOn(Schedulers.io())
                        .doOnError { onError(); null }
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

    private fun onSuccess(response: String) {
        stateClickable()

        when (response.toLowerCase()) {
            "success" -> {
                val fragment = SetPasswordView()
                arguments?.putString("key", key)
                fragment.arguments = arguments
                fragmentManager?.addFragmentOnBoarding(fragment)
            }
            else -> onError()
        }
    }

    private fun onError() {
        stateClickable()
        inputKey.setBackgroundResource(R.drawable.bg_error)
        Toast.makeText(context, "Error", Toast.LENGTH_LONG).show()

    }

    private fun stateClickable() {
        stateProgress.visibility = View.INVISIBLE
        btnSend.text = getString(R.string.send)
        btnSend.isClickable = true
    }
    private fun stateNotClickable(){
        stateProgress.visibility = View.VISIBLE
        btnSend.text = ""
        btnSend.isClickable = false
    }
}