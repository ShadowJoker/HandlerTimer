package com.sfexpress.tcic.android.wiget

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.os.SystemClock

/**
 * 计时器
 */
open class CountTimer(private var mMillisInterval: Long) {

    private var mMillisStart: Long = -1
    private var mMillisPause: Long = 0
    private var mMillisLastTickStart: Long = 0
    private var mTotalPausedFly: Long = 0
    private var state = State.TIMER_NOT_START

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {
            synchronized(this@CountTimer) {
                if (state !== State.TIMER_RUNNING) {
                    return
                }

                mMillisLastTickStart = SystemClock.elapsedRealtime()
                onTimeTick(mMillisLastTickStart - mMillisStart - mTotalPausedFly)
                if (state !== State.TIMER_RUNNING) {
                    return
                }

                var delay = mMillisLastTickStart + mMillisInterval - SystemClock.elapsedRealtime()

                while (delay < 0) {
                    delay += mMillisInterval
                }

                sendMessageDelayed(obtainMessage(MSG), delay)
            }
        }
    }

    @Synchronized
    protected fun setInterval(interval: Long) {
        mMillisInterval = interval
    }

    @Synchronized
    fun start() {
        if (state === State.TIMER_RUNNING) {
            return
        }
        mTotalPausedFly = 0
        mMillisStart = SystemClock.elapsedRealtime()
        state = State.TIMER_RUNNING
        onStart(0)
        mHandler.sendEmptyMessageDelayed(MSG, mMillisInterval)
    }

    @Synchronized
    fun pause() {
        if (state !== State.TIMER_RUNNING) {
            return
        }
        mHandler.removeMessages(MSG)
        state = State.TIMER_PAUSED

        mMillisPause = SystemClock.elapsedRealtime()
        onPause(mMillisPause - mMillisStart - mTotalPausedFly)
    }

    @Synchronized
    fun resume() {
        if (state !== State.TIMER_PAUSED) {
            return
        }
        state = State.TIMER_RUNNING

        onResume(mMillisPause - mMillisStart - mTotalPausedFly)

        val delay = mMillisInterval - (mMillisPause - mMillisLastTickStart)
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause
        mHandler.sendEmptyMessageDelayed(MSG, delay)
    }

    @Synchronized
    fun cancel() {
        if (state === State.TIMER_NOT_START) {
            return
        }
        val preState = state
        mHandler.removeMessages(MSG)
        state = State.TIMER_NOT_START

        if (preState === State.TIMER_RUNNING) { //running -> cancel
            onCancel(SystemClock.elapsedRealtime() - mMillisStart - mTotalPausedFly)
        } else if (preState === State.TIMER_PAUSED) { //pause -> cancel
            onCancel(mMillisPause - mMillisStart - mTotalPausedFly)
        }
    }

    protected open fun onStart(millisFly: Long) {}
    protected open fun onCancel(millisFly: Long) {}
    protected open fun onPause(millisFly: Long) {}
    protected open fun onResume(millisFly: Long) {}
    protected open fun onTimeTick(millisFly: Long) {}

    companion object {
        private const val MSG = 1
    }
}
