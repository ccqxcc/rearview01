package com.example.rearviewmirror

import android.os.Bundle
import android.util.Log
import android.widget.Button
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

        val mirror = RearMirror()
        val mirrorCommand = MirrorCommand()
        // 查询后视镜状态
        val mirrorStatus = findViewById<Button>(R.id.getMirrorStatus)
        mirrorStatus.setOnClickListener {
            //发命令到后视镜并待返回
            mirrorCommand.GetRearviewStatus()

            // 模拟后视镜返回信息，读取并解码
            mirrorCommand.decodeRearviewStatus(mirror)
            Log.d("Button", mirror.lightVolume.toString())
            //按后视镜返回的信息更新车机界面状态
            updateActivityView(mirror)
        }

        val rearSwitch = findViewById<Switch>(R.id.rearSwitch)
        // 监听 Switch 状态变化
        rearSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // Switch 被打开（ON）
                Toast.makeText(getApplicationContext(), "switch on", Toast.LENGTH_LONG).show()
                Log.d("Switch", "开关已打开")
                mirror.rearSwitch = 1
            } else {
                // Switch 被关闭（OFF）
                Toast.makeText(getApplicationContext(), "switch off", Toast.LENGTH_LONG).show()
                Log.d("Switch", "开关已关闭")
                mirror.rearSwitch = 0
            }
            mirrorCommand.setMirrorSwitch(mirror.rearSwitch)
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
                mirror.lightVolume = finalProgress
                mirrorCommand.setLightVolume(mirror.lightVolume)
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
                mirror.heightVolume = finalProgress
                mirrorCommand.setHeightVolume(mirror.heightVolume)
            }
        })

        //缩放
        val zoomGroup = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        zoomGroup.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.zoomX1 -> { /* 处理选项1 */
                    mirror.viewZoom = RearMirror.ViewZoom.ZOOM_10
                    mirrorCommand.setViewZoom(0)
                    Toast.makeText(getApplicationContext(), "zoomX1", Toast.LENGTH_LONG).show()
                }

                R.id.zoomX12 -> { /* 处理选项2 */
                    mirror.viewZoom = RearMirror.ViewZoom.ZOOM_12
                    mirrorCommand.setViewZoom(1)
                    Toast.makeText(getApplicationContext(), "zoomX1.2", Toast.LENGTH_LONG).show()
                }

                R.id.zoomX14 -> {/* 处理选项3 */
                    mirror.viewZoom = RearMirror.ViewZoom.ZOOM_14
                    mirrorCommand.setViewZoom(2)
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
                    mirror.viewMode = RearMirror.ViewMode.MODE_STANDARD
                    mirrorCommand.setViewMode(0)
                    Toast.makeText(getApplicationContext(), "viewStandard", Toast.LENGTH_LONG)
                        .show()
                }

                R.id.viewEnhanced -> {/* 处理选项2 */
                    mirror.viewMode = RearMirror.ViewMode.MODE_ENHANCED
                    mirrorCommand.setViewMode(1)
                    Toast.makeText(getApplicationContext(), "viewEnhanced", Toast.LENGTH_LONG)
                        .show()
                    Log.d("zoomRadioGroup", "viewEnhanced")
                }

                else -> {/* 处理异常 */
                }
            }
        }

    }

    fun updateActivityView(mirror: RearMirror) {
        val rearSwitch = findViewById<Switch>(R.id.rearSwitch)
        //val listener = rearSwitch.setOnCheckedChangeListener(null) // 临时移除监听器
        if (mirror.rearSwitch == 0) rearSwitch.setChecked(false)
        else if (mirror.rearSwitch == 1) rearSwitch.setChecked(true)
        else {
        }
        //rearSwitch.setOnCheckedChangeListener(listener) // 恢复监听器
        //TODO BUG: 此处因更新了switch控件，会触发不必要switch事件从而发不必要的设置命令到后视镜

        if (mirror.lightVolume <= 15) {
            val lightVolume = findViewById<SeekBar>(R.id.lightVolume)
            lightVolume.setProgress(mirror.lightVolume)
            val tvProgress = findViewById<TextView>(R.id.lightVolumeText)
            tvProgress.text = mirror.lightVolume.toString()
        } else {
        }

        if (mirror.heightVolume <= 5) {
            val heightVolume = findViewById<SeekBar>(R.id.heightVolume)
            heightVolume.setProgress(mirror.heightVolume)
            val tvProgress = findViewById<TextView>(R.id.heightVolumeText)
            tvProgress.text = mirror.heightVolume.toString()
        } else {
        }

        val zoomGroup = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        if (mirror.viewZoom == RearMirror.ViewZoom.ZOOM_10) zoomGroup.check(R.id.zoomX1)
        else if (mirror.viewZoom == RearMirror.ViewZoom.ZOOM_12) zoomGroup.check(R.id.zoomX12)
        else if (mirror.viewZoom == RearMirror.ViewZoom.ZOOM_14) zoomGroup.check(R.id.zoomX14)
        else {
        }

        val viewMode = findViewById<RadioGroup>(R.id.viewMode)
        if (mirror.viewMode == RearMirror.ViewMode.MODE_STANDARD) viewMode.check(R.id.viewStandard)
        else if (mirror.viewMode == RearMirror.ViewMode.MODE_ENHANCED) viewMode.check(R.id.viewEnhanced)
        else {
        }
    }
}