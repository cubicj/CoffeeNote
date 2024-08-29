package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot
import kotlin.math.min

class TouchPointView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private var touchX = -1f
    private var touchY = -1f
    private var relativeTouchX = 0f // 상대적인 X 좌표 저장
    private var relativeTouchY = 0f // 상대적인 Y 좌표 저장

    private lateinit var backgroundBitmap: Bitmap

    init {
        // 배경 이미지 로드
        val options = BitmapFactory.Options().apply {
            inScaled = false // 원본 크기로 로드
        }
        backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.scoreboard, options)
    }

    fun setTouchPoint(x: Float, y: Float) {
        touchX = x * width
        touchY = y * height
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val imageSize = min(width, height)

        val left = (width - imageSize) / 2f
        val top = (height - imageSize) / 2f

        canvas.drawBitmap(backgroundBitmap, null, android.graphics.Rect(left.toInt(), top.toInt(), (left + imageSize).toInt(), (top + imageSize).toInt()), null)

        // 터치 지점에 점 그리기
        if (touchX != -1f && touchY != -1f) {
            canvas.drawCircle(touchX, touchY, 12f, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchX = event.x
                touchY = event.y
                relativeTouchX = if (width > 0) touchX / width else 0f
                relativeTouchY = if (height > 0) touchY / height else 0f
                invalidate() // 뷰 다시 그리기
                Log.d("TouchPointView", "onTouchEvent: touchX=$touchX, touchY=$touchY, relativeTouchX=$relativeTouchX, relativeTouchY=$relativeTouchY") // 로그 추가
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun getXDistanceFromOrigin(): Float {
        return Math.abs(touchX - (width / 2f))
    }

    fun getYDistanceFromOrigin(): Float {
        return Math.abs(touchY - (height / 2f))
    }

    fun getRelativeTouchX(): Float {
        return if (width > 0) touchX / width else 0f
    }

    fun getRelativeTouchY(): Float {
        return if (height > 0) touchY / height else 0f
    }
}
