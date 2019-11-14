package com.anwesh.uiprojects.bouncyarclineview

/**
 * Created by anweshmishra on 14/11/19.
 */

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Canvas
import android.graphics.Color
import android.app.Activity
import android.content.Context

val nodes : Int = 5
val parts : Int = 2
val scGap : Float = 0.02f
val strokeFactor : Int = 90
val sizeFactor : Float = 2.9f
val foreColor : Int = Color.parseColor("#1A237E")
val backColor : Int = Color.parseColor("#BDBDBD")
val delay : Long = 20

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(Math.PI * this).toFloat()
fun Float.cosify() : Float = 1f - Math.sin(Math.PI / 2 + (Math.PI / 2) * this).toFloat()

fun Canvas.drawBouncyArcLine(i : Int, scale : Float, size : Float, paint : Paint) {
    val sf : Float = scale.divideScale(i, parts).sinify()
    val sc : Float = scale.divideScale(i, parts).divideScale(1, 2).cosify()
    save()
    scale(1f - 2 * i, 1f)
    drawArc(RectF(-size, -size, size, size), 90f, 90f * sf, false, paint)
    drawLine(-size, 0f, -size + size * sc, 0f, paint)
    restore()
}

fun Canvas.drawBouncyArcLines(scale : Float, size : Float, paint : Paint) {
    for (j in 0..(parts - 1)) {
        drawBouncyArcLine(j, scale, size, paint)
    }
}

fun Canvas.drawBALNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    val gap : Float = w / (nodes + 1)
    val size : Float = gap / sizeFactor
    paint.color = foreColor
    paint.strokeCap = Paint.Cap.ROUND
    paint.strokeWidth = Math.min(w, h) / strokeFactor
    save()
    translate(gap * (i + 1), h / 2)
    drawBouncyArcLines(scale, size, paint)
    restore()
}

class BouncyArcLineView(ctx : Context) : View(ctx) {

    private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun onDraw(canvas : Canvas) {

    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {

            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BALNode(var i : Int, val state : State = State()) {

        private var next : BALNode? = null
        private var prev : BALNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < nodes - 1) {
                next = BALNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawBALNode(i, state.scale, paint)
            next?.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUpdating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BALNode {
            var curr : BALNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BouncyArcLine(var i : Int) {

        private val root : BALNode = BALNode(0)
        private var curr : BALNode = root
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            root.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUpdating(cb)
        }
    }

    data class Renderer(var view : BouncyArcLineView) {

        private val animator : Animator = Animator(view)
        private val bal : BouncyArcLine = BouncyArcLine(0)

        fun render(canvas : Canvas, paint : Paint) {
            canvas.drawColor(backColor)
            bal.draw(canvas, paint)
            animator.animate {
                bal.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bal.startUpdating {
                animator.start()
            }
        }
    }
}