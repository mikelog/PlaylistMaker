package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.playlistmaker.R

class SearchActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private var searchQuery: String = ""

    companion object {
        private const val SEARCH_QUERY_KEY = "SEARCH_QUERY_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        val btnBack: Button = findViewById(R.id.btnBack)
        searchEditText = findViewById(R.id.searchEditText)

        btnBack.setOnClickListener {
            finish()
        }

        initSearch()
    }
    private fun initSearch() {
        updateClearIcon(null)

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(
                s: CharSequence?,
                start: Int,
                count: Int,
                after: Int
            ) = Unit

            override fun onTextChanged(
                s: CharSequence?,
                start: Int,
                before: Int,
                count: Int
            ) {
                searchQuery = s?.toString().orEmpty()
                updateClearIcon(s)
            }

            override fun afterTextChanged(s: Editable?) {
                // TODO: логика поиска будет добавлена позже
            }
        })

        searchEditText.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawableEnd = searchEditText.compoundDrawables[2]
                if (drawableEnd != null) {
                    val clearIconStart =
                        searchEditText.width -
                                searchEditText.paddingEnd -
                                drawableEnd.intrinsicWidth

                    if (event.x >= clearIconStart) {
                        searchEditText.text.clear()
                        hideKeyboard()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_QUERY_KEY, searchQuery)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredQuery = savedInstanceState.getString(SEARCH_QUERY_KEY).orEmpty()
        searchEditText.setText(restoredQuery)
        searchEditText.setSelection(restoredQuery.length)
    }


    private fun updateClearIcon(text: CharSequence?) {
        val searchIcon = getDrawable(R.drawable.ic_search)
        val clearIcon =
            if (text.isNullOrEmpty()) null else getDrawable(R.drawable.ic_search_clear)

        searchEditText.setCompoundDrawablesWithIntrinsicBounds(
            searchIcon,
            null,
            clearIcon,
            null
        )
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(searchEditText.windowToken, 0)
        searchEditText.clearFocus()
    }
}
