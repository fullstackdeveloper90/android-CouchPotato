package com.thorangs.couchpotato.ui.refer

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.thorangs.couchpotato.R
import io.reactivex.disposables.CompositeDisposable

class ReferView : Fragment() {

    private lateinit var btnShare: Button
    private lateinit var btnEmail: Button

    private val disposable = CompositeDisposable()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_refer, container, false)
        rootView?.apply {
            btnShare = findViewById(R.id.btn_refer_share)
            btnEmail = findViewById(R.id.btn_refer_email)
        }
        return rootView
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btnShare.setOnClickListener { shareApplication() }
        btnEmail.setOnClickListener { sendReferEmail() }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun shareApplication() {
        val facebookPackageName = "com.facebook.katana"

        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "text/plain"
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.refer_share_fb))

        val installed = isApplicationInstalled(facebookPackageName)

        if (installed) {
            intent.`package` = facebookPackageName
            startActivity(intent)
        } else {
            Toast.makeText(context,"Facebook application not found.",Toast.LENGTH_SHORT).show()
            val dialog = Dialog(context)
            dialog.setContentView(R.layout.dialog_share_error)
            //       startActivity(Intent.createChooser(intent, "Share on"))
        }
    }

    private fun sendReferEmail() {

    }

    private fun isApplicationInstalled(packageName: String): Boolean {
        val pm = context?.packageManager
        try {
            pm?.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            return true
        } catch (e: PackageManager.NameNotFoundException) {
        }

        return false
    }
}