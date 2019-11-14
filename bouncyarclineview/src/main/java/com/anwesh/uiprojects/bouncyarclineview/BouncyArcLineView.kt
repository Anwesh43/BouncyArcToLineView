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
