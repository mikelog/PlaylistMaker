package com.example.playlistmaker.ui.audioplayer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.playlistmaker.R

class PlaybackButtonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var isPlaying = false

    private val playBitmap: Bitmap
    private val pauseBitmap: Bitmap

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val imageRect = RectF()

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.PlaybackButtonView)
        playBitmap = typedArray.getDrawable(R.styleable.PlaybackButtonView_playImage)
            ?.toBitmap()
            ?: throw IllegalArgumentException("app:playImage attribute is required")
        pauseBitmap = typedArray.getDrawable(R.styleable.PlaybackButtonView_pauseImage)
            ?.toBitmap()
            ?: throw IllegalArgumentException("app:pauseImage attribute is required")
        typedArray.recycle()

        isClickable = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageRect.set(
            paddingLeft.toFloat(),
            paddingTop.toFloat(),
            (w - paddingRight).toFloat(),
            (h - paddingBottom).toFloat()
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val bitmap = if (isPlaying) pauseBitmap else playBitmap
        canvas.drawBitmap(bitmap, null, imageRect, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isEnabled) return false
        when (event.action) {
            MotionEvent.ACTION_DOWN -> return true
            MotionEvent.ACTION_UP -> {
                switchState()
                performClick()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }

    fun switchState() {
        isPlaying = !isPlaying
        invalidate()
    }

    fun setPlayingState(isPlaying: Boolean) {
        if (this.isPlaying != isPlaying) {
            switchState()
        }
    }

    private fun Drawable.toBitmap(): Bitmap {
        if (this is BitmapDrawable) return bitmap
        val bitmap = Bitmap.createBitmap(
            intrinsicWidth.coerceAtLeast(1),
            intrinsicHeight.coerceAtLeast(1),
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        setBounds(0, 0, canvas.width, canvas.height)
        draw(canvas)
        return bitmap
    }
}
