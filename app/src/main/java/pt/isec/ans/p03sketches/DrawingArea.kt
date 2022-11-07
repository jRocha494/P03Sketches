package pt.isec.ans.p03sketches

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.OnGestureListener
import android.view.MotionEvent
import android.view.View

class DrawingArea @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : View(context, attrs, defStyleAttr,defStyleRes), OnGestureListener{

    private var color : Int = Color.WHITE

    val paint = Paint(Paint.DITHER_FLAG).also {
        it.color = Color.BLACK
        it.strokeWidth = 4.0f
        it.style = Paint.Style.FILL_AND_STROKE
    }

    private val gestureDetector = GestureDetector(context, this)

    var lineColor : Int = Color.BLACK
    set(value) {
        field = value
        drawing.add(Line(Path(), value))
    }

    private val drawing = arrayListOf(Line(Path(), lineColor))

    private val path get() = drawing.last().path

    constructor(context: Context, color: Int) : this(context){
        this.color = color
        setBackgroundColor(color)
    }

    private var imageFile : String? = null

    constructor(context: Context, imageFile: String) : this(context) {
        this.imageFile = imageFile
        setPic(this, imageFile)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for(Line in drawing){
            canvas.drawPath(path, paint.apply { color = Line.color })
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if(gestureDetector.onTouchEvent(event)){
            return true
        }
        Log.d(TAG, "DrawingArea")
        return super.onTouchEvent(event)
    }

    override fun onDown(e: MotionEvent): Boolean {
        Log.d(TAG, "onDown")
        path.moveTo(e.x,e.y)
        invalidate()
        return true
    }

    override fun onShowPress(e: MotionEvent) {
        Log.d(TAG, "onShowPress")
    }

    override fun onSingleTapUp(e: MotionEvent): Boolean {
        Log.d(TAG, "onSingleTapUp")
        return false
    }

    override fun onScroll(
        e1: MotionEvent,
        e2: MotionEvent,
        distanceX: Float,
        distanceY: Float
    ): Boolean {
        Log.d(TAG, "onScroll")
        path.lineTo(e2.x,e2.y)
        path.moveTo(e2.x,e2.y)
        invalidate()
        return true
    }

    override fun onLongPress(e: MotionEvent) {
        Log.d(TAG, "onLongPress")
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        Log.d(TAG, "onLongPress")
        return false
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        imageFile?.let { setPic(this, it) }
    }

    data class Line(val path: Path, val color: Int)

    companion object{
        const val TAG = "DrawingArea"
    }
}