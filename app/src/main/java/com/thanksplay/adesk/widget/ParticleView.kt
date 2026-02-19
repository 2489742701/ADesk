package com.thanksplay.adesk.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import java.util.Random

class ParticleView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    
    private val particles = mutableListOf<Particle>()
    private val random = Random()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val handler = Handler(Looper.getMainLooper())
    
    private var lastX = 0f
    private var lastY = 0f
    private var velocityX = 0f
    private var velocityY = 0f
    
    private val colors = intArrayOf(
        0xFFFFFFFF.toInt(),
        0xFF4FC3F7.toInt(),
        0xFF81D4FA.toInt(),
        0xFFB3E5FC.toInt(),
        0xFFE1F5FE.toInt(),
        0xFFFFF59D.toInt(),
        0xFFFFF176.toInt()
    )
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            updateParticles()
            invalidate()
            handler.postDelayed(this, 16)
        }
    }
    
    fun onScroll(dx: Float, dy: Float, x: Float, y: Float) {
        velocityX = x - lastX
        velocityY = y - lastY
        lastX = x
        lastY = y
        
        val distance = kotlin.math.sqrt(velocityX * velocityX + velocityY * velocityY)
        val count = (distance / 5).toInt().coerceIn(1, 8)
        
        for (i in 0 until count) {
            createParticle(x, y, velocityX, velocityY)
        }
    }
    
    fun onTouchEnd() {
        velocityX = 0f
        velocityY = 0f
    }
    
    private fun createParticle(x: Float, y: Float, vx: Float, vy: Float) {
        val angle = random.nextFloat() * Math.PI * 2
        val speed = random.nextFloat() * 2 + 0.5f
        val size = random.nextFloat() * 8 + 2
        
        val particle = Particle(
            x = x + random.nextFloat() * 30 - 15,
            y = y + random.nextFloat() * 30 - 15,
            vx = (Math.cos(angle) * speed).toFloat() - vx * 0.05f,
            vy = (Math.sin(angle) * speed).toFloat() - vy * 0.05f,
            size = size,
            alpha = 255,
            color = colors[random.nextInt(colors.size)]
        )
        
        particles.add(particle)
        
        if (particles.size > 300) {
            particles.removeAt(0)
        }
    }
    
    private fun updateParticles() {
        val iterator = particles.iterator()
        while (iterator.hasNext()) {
            val p = iterator.next()
            p.x += p.vx
            p.y += p.vy
            p.vy += 0.15f
            p.vx *= 0.98f
            p.alpha -= 4
            
            if (p.alpha <= 0) {
                iterator.remove()
            }
        }
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        for (p in particles) {
            paint.color = p.color
            paint.alpha = p.alpha
            paint.setShadowLayer(p.size, 0f, 0f, p.color)
            canvas.drawCircle(p.x, p.y, p.size, paint)
        }
    }
    
    fun start() {
        handler.post(updateRunnable)
    }
    
    fun stop() {
        handler.removeCallbacks(updateRunnable)
    }
    
    private data class Particle(
        var x: Float,
        var y: Float,
        var vx: Float,
        var vy: Float,
        var size: Float,
        var alpha: Int,
        var color: Int
    )
}
