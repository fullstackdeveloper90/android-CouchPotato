package com.thorangs.couchpotato.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.backend.RestClient
import com.thorangs.couchpotato.models.user.SignInUser
import com.thorangs.couchpotato.models.user.UserDTO
import com.thorangs.couchpotato.ui.dashboard.DashBoardActivity
import com.thorangs.couchpotato.utils.isInternetConnected
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class OnboardingFinalFragment : Fragment() {

    private lateinit var btnFinalize: Button
    private lateinit var stateProgress: ProgressBar

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.layout_all_set, container, false)
        v?.apply {
            btnFinalize = findViewById(R.id.btn_confirm_all_set)
            stateProgress = findViewById(R.id.all_set_progressbar)
        }
        return v
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnFinalize.setOnClickListener { launchActivity() }
    }

    private fun launchActivity() {
        if (isInternetConnected(context!!)) {
            stateNotClickable()

            val email = arguments?.getString("email")!!
            val password = arguments?.getString("password")!!

            try {
                RestClient.instance().userDataApiService.signIn(SignInUser(email, password))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .onErrorReturn { onError(); UserDTO(0, "", "", 0, "", "dialy", "") }
                        .subscribe { proceed(it) }

            } catch (e: Exception) {
                Toast.makeText(context, "Network or server error", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Please connect to Internet first", Toast.LENGTH_LONG).show()
        }
    }

    private fun proceed(userDto: UserDTO?) {
        if (userDto != null && userDto.email.isNotEmpty()) {
            val stepLogFactory = PedometerApp.stepLogFactory()!!
            stepLogFactory.apply {
                saveUserId(userDto.id)
                saveUserName(userDto.name)
                saveUserEmail(userDto.email)
                updateTargetSteps(userDto.targetSteps)
                saveBuddyEmail(userDto.buddyEmail)
                setNotifyDaily(true)
                saveCreatedDate(userDto.createdDate)
                val intent = Intent(context, DashBoardActivity::class.java)
                activity?.startActivity(intent)
                activity?.finish()
            }

        } else {
            onError()
        }
    }

    private fun onError() {
        stateClickable()
    }

    private fun stateClickable() {
        btnFinalize.isClickable = true
        btnFinalize.text = getString(R.string.next)
        stateProgress.visibility = INVISIBLE
    }

    private fun stateNotClickable() {
        btnFinalize.isClickable = false
        btnFinalize.text = ""
        stateProgress.visibility = VISIBLE
    }
}