package com.sfexpress.tcic.android.wiget

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.os.SystemClock

/**
 * 倒计时器
 */
open class CountDownTimer(private val mMillisInFuture: Long, private val mInterval: Long) {

    private var mStopTimeInFuture: Long = 0

    private var mMillisStart = NOT_START.toLong()
    private var mMillisPause: Long = 0
    private var mMillisLastTickStart: Long = 0

    private var mTotalPausedFly: Long = 0
    private var state = State.TIMER_NOT_START

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            synchronized(this@CountDownTimer) {
                if (state != State.TIMER_RUNNING) {
                    return
                }
                val millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime()
                if (millisLeft <= 0) {
                    onTimeTick(0)
                    state = State.TIMER_NOT_START
                    onFinish()
                } else if (millisLeft < mInterval) {
                    // no tick, just delay until done
                    this.sendEmptyMessageDelayed(MSG, millisLeft)
                } else {
                    mMillisLastTickStart = SystemClock.elapsedRealtime()
                    onTimeTick(millisLeft)
                    if (state != State.TIMER_RUNNING) {
                        return
                    }
                    // take into account user's onTimeTick taking time to execute
                    var delay = mMillisLastTickStart + mInterval - SystemClock.elapsedRealtime()

                    // special case: user's onTimeTick took more than interval to
                    // complete, skip to next interval
                    while (delay < 0) {
                        delay += mInterval
                    }
                    this.sendEmptyMessageDelayed(MSG, delay)
                }
            }
        }
    }

    fun start() {
        synchronized(this) {
            if (state == State.TIMER_RUNNING) {
                return
            }
            if (mMillisInFuture <= 0) {
                onFinish()
                return
            }
            mTotalPausedFly = 0
            mMillisStart = SystemClock.elapsedRealtime()
            state = State.TIMER_RUNNING
            mStopTimeInFuture = mMillisStart + mMillisInFuture

            onStart(mMillisInFuture)
            mHandler.sendEmptyMessage(MSG)
        }
    }

    @Synchronized
    fun pause() {
        if (state != State.TIMER_RUNNING) {
            return
        }
        mHandler.removeMessages(MSG)
        state = State.TIMER_PAUSED

        mMillisPause = SystemClock.elapsedRealtime()
        onPause(mStopTimeInFuture - mMillisPause)
    }

    @Synchronized
    fun resume() {
        if (state != State.TIMER_PAUSED) {
            return
        }
        state = State.TIMER_RUNNING
        onResume(mStopTimeInFuture - mMillisPause)

        val delay = mInterval - (mMillisPause - mMillisLastTickStart)
        mTotalPausedFly += SystemClock.elapsedRealtime() - mMillisPause
        mStopTimeInFuture = mMillisStart + mMillisInFuture + mTotalPausedFly
        mHandler.sendEmptyMessageDelayed(MSG, delay)
    }

    @Synchronized
    fun cancel() {
        if (state == State.TIMER_NOT_START) {
            return
        }
        val preState = state
        mHandler.removeMessages(MSG)
        state = State.TIMER_NOT_START

        if (preState == State.TIMER_RUNNING) { //running -> cancel
            onCancel(mStopTimeInFuture - SystemClock.elapsedRealtime())
        } else if (preState == State.TIMER_PAUSED) { //pause -> cancel
            onCancel(mStopTimeInFuture - mMillisPause)
        }
    }

    protected open fun onStart(millisUntilFinished: Long) {}
    protected open fun onPause(millisUntilFinished: Long) {}
    protected open fun onResume(millisUntilFinished: Long) {}
    protected open fun onCancel(millisUntilFinished: Long) {}
    protected open fun onTimeTick(millisUntilFinished: Long) {}
    open fun onFinish() {}

    companion object {
        private const val NOT_START = -1
        private const val MSG = 1
    }
}
