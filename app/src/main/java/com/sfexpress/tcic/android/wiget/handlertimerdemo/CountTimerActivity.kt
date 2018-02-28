package com.sfexpress.tcic.android.wiget.handlertimerdemo

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.animation.AnimationUtils
import com.sfexpress.tcic.android.wiget.CountDownTimer
import com.sfexpress.tcic.android.wiget.CountTimer
import com.sfexpress.tcic.android.wiget.CountTimerTask
import com.sfexpress.tcic.android.wiget.MultiCountTimer
import kotlinx.android.synthetic.main.activity_main.*

@SuppressLint("SetTextI18n")
class CountTimerActivity : AppCompatActivity() {

    private val countTimer = object : CountTimer(500) {
        public override fun onStart(millisFly: Long) {
            count_timer_status.text = millisFly.toString() + ":开始"
        }

        public override fun onCancel(millisFly: Long) {
            count_timer_status.text = millisFly.toString() + ":清零"
            count_timer_ts.setText("")
        }

        public override fun onPause(millisFly: Long) {
            count_timer_status.text = millisFly.toString() + ":暂停"
        }

        public override fun onResume(millisFly: Long) {
            count_timer_status.text = millisFly.toString() + ":继续"
        }

        public override fun onTimeTick(millisFly: Long) {
            count_timer_ts.setText(millisFly.toString())
        }
    }

    private val countDownTimer = object : CountDownTimer(10000, 100) {
        public override fun onStart(millisUntilFinished: Long) {
            countdown_timer_status.text = millisUntilFinished.toString() + ":开始"
        }

        public override fun onCancel(millisUntilFinished: Long) {
            countdown_timer_status.text = millisUntilFinished.toString() + ":清零"
            countdown_timer_ts.setText("")
        }

        public override fun onPause(millisUntilFinished: Long) {
            countdown_timer_status.text = millisUntilFinished.toString() + ":暂停"
        }

        public override fun onResume(millisUntilFinished: Long) {
            countdown_timer_status.text = "$millisUntilFinished:继续"
        }

        public override fun onTimeTick(millisUntilFinished: Long) {
            countdown_timer_ts.setText(millisUntilFinished.toString())
        }

        override fun onFinish() {
            countdown_timer_status.text = "计时结束"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val inAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        val outAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        count_timer_ts.inAnimation = inAnim
        count_timer_ts.outAnimation = outAnim

        val multiCountTimer = MultiCountTimer(10)
        multiCountTimer.add(object : CountTimerTask(1) {
            public override fun onTick(millisFly: Long) {
                multi_1.setText("计时器1:$millisFly")
            }
        }).add(object : CountTimerTask(2, 100) {
            public override fun onTick(millisFly: Long) {
                multi_2.setText("计时器2:$millisFly")
            }
        }).add(object : CountTimerTask(3, 1000) {
            public override fun onTick(millisFly: Long) {
                multi_3.setText("计时器3:$millisFly")
            }
        })
        multiCountTimer.startAll()

        initAction()
    }

    private fun initAction() {
        count_timer_start.setOnClickListener { countTimer.start() }
        count_timer_pause.setOnClickListener { countTimer.pause() }
        count_timer_resume.setOnClickListener { countTimer.resume() }
        count_timer_cancel.setOnClickListener { countTimer.cancel() }

        countdown_timer_start.setOnClickListener { countDownTimer.start() }
        countdown_timer_pause.setOnClickListener { countDownTimer.pause() }
        countdown_timer_resume.setOnClickListener { countDownTimer.resume() }
        countdown_timer_cancel.setOnClickListener { countDownTimer.cancel() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        countTimer.cancel()
        countDownTimer.cancel()
    }
}
