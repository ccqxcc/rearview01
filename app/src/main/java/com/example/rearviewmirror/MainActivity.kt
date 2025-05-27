package com.example.rearviewmirror

import android.os.Bundle
import android.util.Log
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val mySwitch = findViewById<Switch>(R.id.rearSwitch)
        // 监听 Switch 状态变化
        mySwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Switch 被打开（ON）
                Toast.makeText(getApplicationContext(), "switch on", Toast.LENGTH_LONG).show()
                Log.d("Switch", "开关已打开")
            } else {
                // Switch 被关闭（OFF）
                Toast.makeText(getApplicationContext(), "switch off", Toast.LENGTH_LONG).show()
                Log.d("Switch", "开关已关闭")
            }
        }

        //监听亮度调节滑动事件
        val lightVolume = findViewById<SeekBar>(R.id.lightVolume)
        lightVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val message = "当前亮度：$progress"
                Log.d("SeekBar", message)
                if (fromUser) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 可在此处保存最终进度或执行耗时操作
                val finalProgress = seekBar.progress
                val tvProgress = findViewById<TextView>(R.id.lightVolumeText)
                tvProgress.text = finalProgress.toString()
                Log.d("SeekBar", "最终亮度：$finalProgress")
            }
        })

        //监听高度调节滑动事件
        val heightVolume = findViewById<SeekBar>(R.id.heightVolume)
        heightVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val message = "当前高度：$progress"
                Log.d("SeekBar", message)
                if (fromUser) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 可在此处保存最终进度或执行耗时操作
                val finalProgress = seekBar.progress
                val tvProgress = findViewById<TextView>(R.id.heightVolumeText)
                tvProgress.text = finalProgress.toString()
                Log.d("SeekBar", "最终高度：$finalProgress")
            }
        })

        //缩放
        val zoomGroup = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        zoomGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.zoomX1 -> { /* 处理选项1 */
                    Toast.makeText(getApplicationContext(), "zoomX1", Toast.LENGTH_LONG).show()
                }

                R.id.zoomX12 -> { /* 处理选项2 */
                    Toast.makeText(getApplicationContext(), "zoomX1.2", Toast.LENGTH_LONG).show()
                }

                R.id.zoomX14 -> {
                    /* 处理选项3 */
                    Toast.makeText(getApplicationContext(), "zoomX1.4", Toast.LENGTH_LONG).show()
                }

                else -> {
                    /* 处理异常 */
                    Log.d("zoomRadioGroup", "zoomGroup Exception")
                }
            }
        }

        val viewMode = findViewById<RadioGroup>(R.id.viewMode)
        viewMode.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.viewStandard -> { /* 处理选项1 */
                    Toast.makeText(getApplicationContext(), "viewStandard", Toast.LENGTH_LONG)
                        .show()
                }

                R.id.viewEnhanced -> {
                    /* 处理选项3 */
                    Toast.makeText(getApplicationContext(), "viewEnhanced", Toast.LENGTH_LONG)
                        .show()
                    Log.d("zoomRadioGroup", "viewEnhanced")
                }

                else -> {/* 处理异常 */
                }
            }
        }

    }
}