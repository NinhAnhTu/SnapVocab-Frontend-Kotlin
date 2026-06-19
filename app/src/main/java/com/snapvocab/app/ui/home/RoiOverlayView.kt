package com.snapvocab.app.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class RoiOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint().apply {
        color = Color.parseColor("#80FFEB3B") // Màu vàng trong suốt
        style = Paint.Style.FILL
    }
    private val borderPaint = Paint().apply {
        color = Color.parseColor("#FFEB3B")
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    private var startX = 0f
    private var startY = 0f
    private var endX = 0f
    private var endY = 0f
    private var drawing = false
    private var roiRect: RectF? = null

    var onRoiChanged: ((RectF?) -> Unit)? = null

    fun clearRoi() {
        roiRect = null
        drawing = false
        invalidate()
        onRoiChanged?.invoke(null)
    }

    fun getRoiRect(): RectF? = roiRect

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
                drawing = true
                roiRect = null
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (drawing) {
                    endX = event.x
                    endY = event.y
                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP -> {
                if (drawing) {
                    endX = event.x
                    endY = event.y
                    val left = minOf(startX, endX)
                    val top = minOf(startY, endY)
                    val right = maxOf(startX, endX)
                    val bottom = maxOf(startY, endY)
                    if (right - left > 20 && bottom - top > 20) {
                        roiRect = RectF(left, top, right, bottom)
                    } else {
                        roiRect = null
                    }
                    drawing = false
                    invalidate()
                    onRoiChanged?.invoke(roiRect)
                }
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        roiRect?.let {
            canvas.drawRect(it, paint)
            canvas.drawRect(it, borderPaint)
        }
        if (drawing) {
            val left = minOf(startX, endX)
            val top = minOf(startY, endY)
            val right = maxOf(startX, endX)
            val bottom = maxOf(startY, endY)
            val rect = RectF(left, top, right, bottom)
            canvas.drawRect(rect, paint)
            canvas.drawRect(rect, borderPaint)
        }
    }
}