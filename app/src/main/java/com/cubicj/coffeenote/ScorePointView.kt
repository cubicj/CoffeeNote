package com.cubicj.coffeenote

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.min

class ScorePointView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    deStyleAttr: Int = 0
) : View(context, attrs, deStyleAttr) {

    init {
        updatePointPosition() // 생성자에서 함수 호출
    }
    // 점을 그릴 Paint 객체
    private val pointPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }


    var scoreRelativeX: Float? = null
        set(value) {
            field = value
            updatePointPosition()
        }
    var scoreRelativeY: Float? = null
        set(value) {
            field = value
            updatePointPosition()
        }
    private var pointX = 0f
    private var pointY = 0f

    private lateinit var backgroundBitmap: Bitmap

    init {
        // 배경 이미지 로드 (파일명 확인 필요)
        val options = BitmapFactory.Options().apply {
            inScaled = false // 원본 크기로 로드
        }
        backgroundBitmap = BitmapFactory.decodeResource(resources, R.drawable.scoreboard, options)
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("ScorePointView", "onSizeChanged() called - width: $w, height: $h")
        updatePointPosition()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        Log.d("ScorePointView", "onDraw() called")
        val imageSize = min(width, height)

        val left = (width - imageSize) / 2f
        val top = (height - imageSize) / 2f

        canvas.drawBitmap(backgroundBitmap, null, android.graphics.Rect(left.toInt(), top.toInt(), (left + imageSize).toInt(), (top + imageSize).toInt()), null)

        // 점 그리기
        if (scoreRelativeX != null && scoreRelativeY != null) {
            Log.d("ScorePointView", "Drawing circle at pointX: $pointX, pointY: $pointY")
            canvas.drawCircle(pointX, pointY, 20f, pointPaint) // pointPaint 사용
            Log.d("ScorePointView", "Circle drawn")
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        Log.d("ScorePointView", "onLayout() called - left: $left, top: $top, right: $right, bottom: $bottom, width: ${right - left}, height: ${bottom - top}")
    }

    private fun updatePointPosition() {
        // scoreRelativeX와 scoreRelativeY가 null이 아닌 경우에만 계산
        if (scoreRelativeX != null && scoreRelativeY != null) {
            val x = scoreRelativeX!!
            val y = scoreRelativeY!!
            val centerX = width / 2f
            val centerY = height / 2f

            // 뷰의 크기가 0보다 큰 경우에만 계산
            if (width > 0 && height > 0) {
                pointX = centerX + (x - 0.5f) * width
                pointY = centerY + (y - 0.5f) * height
            } else {
                // 뷰의 크기가 0인 경우 기본 위치 설정
                pointX = centerX
                pointY = centerY
            }

            Log.d("ScorePointView", "pointX: $pointX, pointY: $pointY, scoreRelativeX: $x, scoreRelativeY: $y, centerX: $centerX, centerY: $centerY")
        } else {
            // null일 경우 기본 위치 설정 (예: 중앙)
            pointX = width / 2f
            pointY = height / 2f
        }
    }
}
