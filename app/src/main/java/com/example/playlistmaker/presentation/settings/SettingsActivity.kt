package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.util.applyEdgeToEdge
import com.example.playlistmaker.domain.interactor.ThemeInteractor
import com.example.playlistmaker.util.Creator
import com.google.android.material.switchmaterial.SwitchMaterial
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeInteractor: ThemeInteractor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        themeInteractor = Creator.provideThemeInteractor(this)

        val root = findViewById<View>(R.id.settingsRoot)
        val toolbar = findViewById<View>(R.id.settingsToolbar)
        applyEdgeToEdge(rootView = root, topView = toolbar)
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, insets ->
            val statusBarInsets = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            toolbar.updatePadding(top = statusBarInsets.top)
            insets
        }

        val btnBack: Button = findViewById(R.id.btnBack)
        val shareAppLayout: LinearLayout = findViewById(R.id.shareApp)
        val supportLayout: LinearLayout = findViewById(R.id.contactSupport)
        val userAgreementLayout: LinearLayout = findViewById(R.id.userAgreement)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        btnBack.setOnClickListener { finish() }
        shareAppLayout.setOnClickListener { shareApp() }
        supportLayout.setOnClickListener { writeToSupport() }
        userAgreementLayout.setOnClickListener { openUserAgreement() }

        // Читаем текущее состояние через интерактор
        themeSwitcher.isChecked = themeInteractor.isDarkTheme()

        themeSwitcher.setOnCheckedChangeListener { _, checked ->
            themeInteractor.setDarkTheme(checked)
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_message))
        }
        startActivity(Intent.createChooser(intent, null))
    }

    private fun writeToSupport() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support_email)))
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.support_subject))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.support_body))
        }
        startActivity(intent)
    }

    private fun openUserAgreement() {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.user_agreement_url))))
    }
}
