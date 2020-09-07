package com.idodoron.kidsdrawingapp

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.roundToInt

class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs){
    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null
    var mBrushSize: Float = 0f
        private set
    private var canvas: Canvas? = null
    private val mPaths = ArrayList<CustomPath>()
    private val actionsStack = Stack<CustomPath>()
    val maxBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40.5f, resources.displayMetrics)
    val minBrushSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, resources.displayMetrics)
    private val defaultBrushSizePercentage = 10

    var color = Color.BLUE
        set(value) {
            field = value
            mDrawPaint!!.color = value
        }

    init{
        setUpDrawing()
    }

    private fun setUpDrawing(){
        mDrawPaint = Paint()
        mDrawPath = CustomPath(color, mBrushSize)
        mDrawPaint!!.color = color
        mDrawPaint!!.style = Paint.Style.STROKE
        mDrawPaint!!.strokeJoin = Paint.Join.ROUND
        mDrawPaint!!.strokeCap = Paint.Cap.ROUND
        mCanvasPaint = Paint(Paint.DITHER_FLAG)
        setSizeForBrush(defaultBrushSizePercentage)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(mCanvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)


        /**
         * TODO: improve runtime complexity by avoiding iterating over the entire
         *  history of actions every time a new action is made. We could append
         *  the last action the the previous state of the canvas, so we get the
         *  job done n times faster.
         *  This inefficient approach may be still useful, though, for supporting
         *  'undo' and 'redo' buttons.
         */
        for (path in mPaths){
            mDrawPaint!!.strokeWidth = path.brushThickness
            mDrawPaint!!.color = path.color
            canvas.drawPath(path, mDrawPaint!!)
        }

        if(!mDrawPath!!.isEmpty) {
            mDrawPaint!!.strokeWidth = mDrawPath!!.brushThickness
            mDrawPaint!!.color = mDrawPath!!.color
            canvas.drawPath(mDrawPath!!, mDrawPaint!!)
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val touchX = event?.x
        val touchY = event?.y

        when(event?.action){
            // What should happen at the moment a user touches the canvas
            MotionEvent.ACTION_DOWN -> {
                mDrawPath!!.color = color
                mDrawPath!!.brushThickness = mBrushSize

                mDrawPath!!.reset()
                mDrawPath!!.moveTo(touchX!!, touchY!!)
                actionsStack.removeAll(actionsStack)
            }

            // What should happen after a user has moved her/his finger over the canvas
            MotionEvent.ACTION_MOVE -> {
                mDrawPath!!.lineTo(touchX!!, touchY!!)
            }

            // What should happen the moment a user releases her/his finger
            MotionEvent.ACTION_UP -> {
                mPaths.add(mDrawPath!!)
                mDrawPath = CustomPath(color, mBrushSize)
            }

            else -> return false
        }

        // Invalidate the whole view.
        invalidate()

        return true
    }

    fun setSizeForBrush(percentage: Int){
        mBrushSize = (percentage / 100f) * (maxBrushSize - minBrushSize)
        mDrawPaint!!.strokeWidth = mBrushSize
    }

    fun undoLastChange(){
        if(mPaths.isNotEmpty()) {
            val lastChange: CustomPath = mPaths.removeAt(mPaths.size-1)
            actionsStack.push(lastChange)
            invalidate()
        }
    }

    fun redoLastChange(){
        if(!actionsStack.isEmpty()){
            val lastChange: CustomPath = actionsStack.pop()
            mPaths.add(lastChange)
            invalidate()
        }
    }

    internal inner class CustomPath(var color: Int, var brushThickness: Float): Path()

    private inner class ExecuteASyncTask()
}