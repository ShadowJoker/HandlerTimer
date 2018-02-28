package com.sfexpress.tcic.android.wiget

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Message
import android.util.SparseArray

/**
 * 监控多个任务的定时器
 */
class MultiCountTimer(private var mDefaultInterval: Long) {

    private val mTicks = SparseArray<CountTimerTask>()

    @SuppressLint("HandlerLeak")
    private val mHandler = object : Handler() {
        override fun handleMessage(msg: Message) {

            val id = msg.what

            synchronized(this@MultiCountTimer) {

                val task = mTicks.get(id)
                if (task == null || task.state !== State.TIMER_RUNNING) {
                    return
                }

                task.tickAndNext()
            }
        }
    }

    @Synchronized
    fun add(task: CountTimerTask): MultiCountTimer {
        task.attachHandler(mHandler)
        if (task.mCountInterval == CountTimerTask.INVALID_INTERVAL) {
            task.mCountInterval = mDefaultInterval
        }
        mTicks.append(task.mId, task)
        return this
    }

    @Synchronized
    fun startAll() {
        var i = 0
        val size = mTicks.size()
        while (i < size) {
            val key = mTicks.keyAt(i)
            mTicks.get(key).start()
            i++
        }
    }

    @Synchronized
    fun cancelAll() {
        var i = 0
        val size = mTicks.size()
        while (i < size) {
            val key = mTicks.keyAt(i)
            mTicks.get(key).cancel()
            i++
        }
        mTicks.clear()
    }

    @Synchronized
    fun start(id: Int) {
        val task = mTicks.get(id) ?: return
        task.start()
    }

    @Synchronized
    fun pause(id: Int) {
        val task = mTicks.get(id) ?: return
        task.pause()
    }

    @Synchronized
    fun resume(id: Int) {
        val task = mTicks.get(id) ?: return
        task.resume()
    }

    @Synchronized
    fun cancel(id: Int) {
        val task = mTicks.get(id) ?: return
        task.cancel()
        mTicks.remove(id)
    }

    companion object {
        private const val DEFAULT_INTERVAL: Long = 1000
    }

}
