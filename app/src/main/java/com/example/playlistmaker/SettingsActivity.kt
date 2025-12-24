package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.widget.LinearLayout

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val btnBack: Button = findViewById(R.id.btnBack)
        val shareAppLayout: LinearLayout = findViewById(R.id.shareApp)
        val supportLayout: LinearLayout = findViewById(R.id.contactSupport)
        val userAgreementLayout: LinearLayout = findViewById(R.id.userAgreement)

        btnBack.setOnClickListener {
            finish()
        }

        shareAppLayout.setOnClickListener {
            shareApp()
        }

        supportLayout.setOnClickListener {
            writeToSupport()
        }

        userAgreementLayout.setOnClickListener {
            openUserAgreement()
        }
    }


    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "text/plain"
        intent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.share_message)
        )
        startActivity(Intent.createChooser(intent, null))
    }

    private fun writeToSupport() {
        val intent = Intent(Intent.ACTION_SENDTO)
        intent.data = Uri.parse("mailto:")

        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(getString(R.string.support_email))
        )
        intent.putExtra(
            Intent.EXTRA_SUBJECT,
            getString(R.string.support_subject)
        )
        intent.putExtra(
            Intent.EXTRA_TEXT,
            getString(R.string.support_body)
        )

        startActivity(intent)
    }

    private fun openUserAgreement() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(getString(R.string.user_agreement_url))
        )
        startActivity(intent)
    }
}