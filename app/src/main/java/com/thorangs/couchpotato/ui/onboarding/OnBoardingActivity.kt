package com.thorangs.couchpotato.ui.onboarding

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.thorangs.couchpotato.PedometerApp
import com.thorangs.couchpotato.R
import com.thorangs.couchpotato.ui.dashboard.DashBoardActivity

class OnBoardingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val factory = PedometerApp.stepLogFactory()!!
        if(factory.getUserEmail().isNotEmpty() && (factory.getUserId() != 0L)){
            val intent = Intent(this, DashBoardActivity::class.java)
            startActivity(intent)
            finish()
        }

        setContentView(R.layout.activity_on_boarding)
        val fragment = SignUpFragment()
        supportFragmentManager
                .beginTransaction()
                .add(R.id.onboarding_base, fragment, fragment.javaClass.simpleName)
                .commit()
    }
}
