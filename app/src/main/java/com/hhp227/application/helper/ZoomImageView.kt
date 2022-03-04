package com.hhp227.application.helper

import android.content.Context
import kotlin.jvm.JvmOverloads
import androidx.appcompat.widget.AppCompatImageView
import com.hhp227.application.helper.ZoomImageView
import android.graphics.PointF
import android.graphics.Bitmap
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.ImageView.ScaleType
import kotlin.math.sqrt

class ZoomImageView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    AppCompatImageView(context, attrs, defStyleAttr) {
    private val matrix = Matrix()
    private val savedMatrix = Matrix()
    private val savedMatrix2 = Matrix()
    private var mode = NONE
    private val start = PointF()
    private val mid = PointF()
    private var oldDist = 1f
    private var isInit = false

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (!isInit) {
            init()
            isInit = true
        }
    }

    override fun setImageBitmap(bm: Bitmap) {
        super.setImageBitmap(bm)
        isInit = false
        init()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        isInit = false
        init()
    }

    override fun setImageResource(resId: Int) {
        super.setImageResource(resId)
        isInit = false
        init()
    }

    protected fun init() {
        matrixTurning(matrix, this)
        imageMatrix = matrix
        setImagePit()
    }

    private fun setImagePit() {
        // matrix value
        val value = FloatArray(9)
        matrix.getValues(value)

        // view volume
        val width = this.width
        val height = this.height

        // image volume
        val d = this.drawable ?: return
        val imageWidth = d.intrinsicWidth
        val imageHeight = d.intrinsicHeight
        var scaleWidth: Int
        var scaleHeight: Int

        // image should not outside
        value[2] = 0F
        value[5] = 0F
        if (imageWidth > width || imageHeight > height) {
            var target = WIDTH
            if (imageWidth < imageHeight) target = HEIGHT
            if (target == WIDTH) {
                value[4] = width.toFloat() / imageWidth
                value[0] = value[4]
            }
            if (target == HEIGHT) {
                value[4] = height.toFloat() / imageHeight
                value[0] = value[4]
            }
            scaleWidth = (imageWidth * value[0]).toInt()
            scaleHeight = (imageHeight * value[4]).toInt()
            if (scaleWidth > width) {
                value[4] = width.toFloat() / imageWidth
                value[0] = value[4]
            }
            if (scaleHeight > height) {
                value[4] = height.toFloat() / imageHeight
                value[0] = value[4]
            }
        }

        // center
        scaleWidth = (imageWidth * value[0]).toInt()
        scaleHeight = (imageHeight * value[4]).toInt()
        if (scaleWidth < width) value[2] = width.toFloat() / 2 - scaleWidth.toFloat() / 2
        if (scaleHeight < height) value[5] = height.toFloat() / 2 - scaleHeight.toFloat() / 2
        matrix.setValues(value)
        imageMatrix = matrix
    }

    private fun onTouch(v: View, event: MotionEvent): Boolean {
        val view = v as ImageView
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                start[event.x] = event.y
                mode = DRAG
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    savedMatrix.set(matrix)
                    midPoint(mid, event)
                    mode = ZOOM
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> mode = NONE
            MotionEvent.ACTION_MOVE -> if (mode == DRAG) {
                matrix.set(savedMatrix)
                matrix.postTranslate(event.x - start.x, event.y - start.y)
            } else if (mode == ZOOM) {
                val newDist = spacing(event)
                if (newDist > 10f) {
                    matrix.set(savedMatrix)
                    val scale = newDist / oldDist
                    matrix.postScale(scale, scale, mid.x, mid.y)
                }
            }
        }
        matrixTurning(matrix, view)
        view.imageMatrix = matrix
        return true
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point[x / 2] = y / 2
    }

    private fun matrixTurning(matrix: Matrix, view: ImageView) {
        // matrix value
        val value = FloatArray(9)
        matrix.getValues(value)
        val savedValue = FloatArray(9)
        savedMatrix2.getValues(savedValue)

        // view volume
        val width = view.width
        val height = view.height

        // image volume
        val d = view.drawable ?: return
        val imageWidth = d.intrinsicWidth
        val imageHeight = d.intrinsicHeight
        var scaleWidth = (imageWidth * value[0]).toInt()
        var scaleHeight = (imageHeight * value[4]).toInt()

        // image should not move outside
        if (value[2] < width - scaleWidth) value[2] = (width - scaleWidth).toFloat()
        if (value[5] < height - scaleHeight) value[5] = (height - scaleHeight).toFloat()
        if (value[2] > 0) value[2] = 0F
        if (value[5] > 0) value[5] = 0F

        // image should not increase than 10 times
        if (value[0] > 10 || value[4] > 10) {
            value[0] = savedValue[0]
            value[4] = savedValue[4]
            value[2] = savedValue[2]
            value[5] = savedValue[5]
        }

        // image should not decrease than original screen
        if (imageWidth > width || imageHeight > height) {
            if (scaleWidth < width && scaleHeight < height) {
                var target = WIDTH
                if (imageWidth < imageHeight) target = HEIGHT
                if (target == WIDTH) {
                    value[4] = width.toFloat() / imageWidth
                    value[0] = value[4]
                }
                if (target == HEIGHT) {
                    value[4] = height.toFloat() / imageHeight
                    value[0] = value[4]
                }
                scaleWidth = (imageWidth * value[0]).toInt()
                scaleHeight = (imageHeight * value[4]).toInt()
                if (scaleWidth > width) {
                    value[4] = width.toFloat() / imageWidth
                    value[0] = value[4]
                }
                if (scaleHeight > height) {
                    value[4] = height.toFloat() / imageHeight
                    value[0] = value[4]
                }
            }
        } else {
            if (value[0] < 1) value[0] = 1F
            if (value[4] < 1) value[4] = 1F
        }

        // image should order center
        scaleWidth = (imageWidth * value[0]).toInt()
        scaleHeight = (imageHeight * value[4]).toInt()
        if (scaleWidth < width) value[2] = width.toFloat() / 2 - scaleWidth.toFloat() / 2
        if (scaleHeight < height) value[5] = height.toFloat() / 2 - scaleHeight.toFloat() / 2
        matrix.setValues(value)
        savedMatrix2.set(matrix)
    }

    companion object {
        private const val NONE = 0
        private const val DRAG = 1
        private const val ZOOM = 2
        private const val WIDTH = 0
        private const val HEIGHT = 1
    }

    init {
        setOnTouchListener { v: View, event: MotionEvent -> onTouch(v, event) }
        scaleType = ScaleType.MATRIX
    }
}