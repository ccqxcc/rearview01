package com.example.rearviewmirror

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.animation.OvershootInterpolator
import android.widget.Button
import android.widget.CompoundButton
import android.widget.RadioGroup
import android.widget.SeekBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyViewModel : ViewModel() {
    // 定义一个公开的MutableLiveData，用于保存一个字符串
    val myLiveData = MutableLiveData<String>()
    val debugTextData = MutableLiveData<String>()
    val rearSwitchData = MutableLiveData<Boolean>()
    val lightData = MutableLiveData<Int>()
    val heightData = MutableLiveData<Int>()
    val zoomData = MutableLiveData<Int>()
    val modeData = MutableLiveData<Int>()

    override fun onCleared() {
        // ViewModel 销毁时清理
    }
}

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MyViewModel

    // 声明监听器为成员变量
    private lateinit var switchListener: CompoundButton.OnCheckedChangeListener
    private lateinit var lightVolumeListener: SeekBar.OnSeekBarChangeListener
    private lateinit var heightVolumeListener: SeekBar.OnSeekBarChangeListener
    private lateinit var viewZoomListener: RadioGroup.OnCheckedChangeListener
    private lateinit var viewModeListener: RadioGroup.OnCheckedChangeListener
    private lateinit var cmmdHandler: CommandHandler
    private lateinit var mirrorCmmd: MirrorCommand

    override fun onStop() {
        super.onStop()
        onDestroy()
    }

    override fun onDestroy() {
        mirrorCmmd.close()
        cmmdHandler.close()  // 手动关闭资源
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val exitButton = findViewById<Button>(R.id.exit_button)
        exitButton.setOnClickListener {
            // 1. 添加按钮动画（可选）
            it.animate().rotationBy(405f).setDuration(300).start()
            // 2. 延迟执行退出（让动画完成）
            it.postDelayed({
                // 3. 安全结束当前 Activity
                if (!isFinishing) {
                    finish()
                }
            }, 300) // 匹配动画时长
        }

        registerObserver()

        cmmdHandler = CommandHandler(viewModel, getLogFilePath())
        mirrorCmmd = MirrorCommand(cmmdHandler)

        // 初始化监听器
        switchListener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
            mirrorCmmd.setMirrorSwitch(if (isChecked) 1 else 0)
        }
        val rearSwitch = findViewById<Switch>(R.id.rearSwitch)
        rearSwitch.setOnCheckedChangeListener(switchListener)

        //监听亮度调节滑动事件
        val lightVolume = findViewById<SeekBar>(R.id.lightVolume)
        lightVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val lightVolume = seekBar.progress /2
                    val tvProgress = findViewById<TextView>(R.id.lightVolumeText)
                    tvProgress.text = (lightVolume + 1).toString()
                    Log.d("Main", "lightVolume=$lightVolume")
                    mirrorCmmd.setLightVolume(lightVolume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 可在此处保存最终进度或执行耗时操作
            }
        })

        //监听高度调节滑动事件
        val heightVolume = findViewById<SeekBar>(R.id.heightVolume)
        heightVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val heightVolume = seekBar.progress /4
                    val tvProgress = findViewById<TextView>(R.id.heightVolumeText)
                    tvProgress.text = (heightVolume + 1).toString()
                    Log.d("Main", "heightVolume=$heightVolume")
                    mirrorCmmd.setHeightVolume(heightVolume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 可在此处保存最终进度或执行耗时操作
            }
        })

        //缩放, 初始化监听器
        viewZoomListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
            var zoomX: Int = -1
            when (checkedId) {
                R.id.zoomX1 -> zoomX = 0
                R.id.zoomX12 -> zoomX = 1
                R.id.zoomX14 -> zoomX = 2
                else -> {} //处理异常
            }
            Log.d("Main", "viewZoom=$zoomX")
            if (zoomX != -1) mirrorCmmd.setViewZoom(zoomX)
        }
        val zoomStatus = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        zoomStatus.setOnCheckedChangeListener(viewZoomListener)

        //模式, 初始化监听器
        viewModeListener = RadioGroup.OnCheckedChangeListener { group, checkedId ->
            var modeX: Int = -1
            when (checkedId) {
                R.id.viewStandard -> modeX = 0
                R.id.viewEnhanced -> modeX = 1
                else -> {} //处理异常
            }
            Log.d("Main", "viewMode=$modeX")
            if (modeX != -1) mirrorCmmd.setViewMode(modeX)
        }
        val modeStatus = findViewById<RadioGroup>(R.id.viewMode)
        modeStatus.setOnCheckedChangeListener(viewModeListener)
        mirrorCmmd.getRearviewStatus() // 查询后视镜状态
    }

    fun registerObserver() {
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
//        // 观察LiveData，当数据变化时更新TextView
//        val debugLog = findViewById<TextView>(R.id.viewDebugLog)
//        viewModel.myLiveData.observe(this, Observer { newValue ->
//            debugLog.text = newValue
//        })
//        val debugText = findViewById<TextView>(R.id.viewDebugText)
//        viewModel.debugTextData.observe(this, Observer { newValue ->
//            debugText.text = newValue
//        })

        viewModel.rearSwitchData.observe(this, Observer { newValue ->
            updateSwitchWithoutTriggeringListener(newValue)
        })

        val lightVolume = findViewById<SeekBar>(R.id.lightVolume)
        val lightProgress = findViewById<TextView>(R.id.lightVolumeText)
        viewModel.lightData.observe(this, Observer { newValue ->
            if (newValue <= 14) {
                lightVolume.setProgress(newValue*2 + 1)
                lightProgress.text = (newValue + 1).toString()
            }
        })

        val heightVolume = findViewById<SeekBar>(R.id.heightVolume)
        val heightProgress = findViewById<TextView>(R.id.heightVolumeText)
        viewModel.heightData.observe(this, Observer { newValue ->
            if (newValue <= 6) {
                heightVolume.setProgress(newValue*4 + 1)
                heightProgress.text = (newValue + 1).toString()
            }
        })

        viewModel.zoomData.observe(this, Observer { newValue ->
            updateViewZoomWithoutTriggeringListener(newValue)
        })
        viewModel.modeData.observe(this, Observer { newValue ->
            updateViewModeWithoutTriggeringListener(newValue)
        })
    }

    private fun updateSwitchWithoutTriggeringListener(newState: Boolean) {
        val switchStatus = findViewById<Switch>(R.id.rearSwitch)
        // 1. 移除监听器
        switchStatus.setOnCheckedChangeListener(null)
        // 2. 更新状态
        switchStatus.isChecked = newState
        // 3. 恢复监听器
        switchStatus.setOnCheckedChangeListener(switchListener)
    }

    private fun updateViewZoomWithoutTriggeringListener(newValue: Int) {
        val zoomStatus = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        // 1. 移除监听器
        zoomStatus.setOnCheckedChangeListener(null)
        // 2. 更新状态
        Log.d("Main", "updateViewZoom=$newValue")
        // Toast.makeText(getApplicationContext(), "updateViewZoom=$newValue", Toast.LENGTH_LONG).show()
        when (newValue) {
            0 -> zoomStatus.check(R.id.zoomX1)
            1 -> zoomStatus.check(R.id.zoomX12)
            2 -> zoomStatus.check(R.id.zoomX14)
            else -> {}
        }
        // 3. 恢复监听器
        zoomStatus.setOnCheckedChangeListener(viewZoomListener)
    }

    private fun updateViewModeWithoutTriggeringListener(newValue: Int) {
        val modeStatus = findViewById<RadioGroup>(R.id.viewMode)
        // 1. 移除监听器
        modeStatus.setOnCheckedChangeListener(null)
        // 2. 更新状态
        Log.d("Main", "updateViewZoom=$newValue")
        when (newValue) {
            0 -> modeStatus.check(R.id.viewStandard)
            1 -> modeStatus.check(R.id.viewEnhanced)
            else -> {}
        }
        // 3. 恢复监听器
        modeStatus.setOnCheckedChangeListener(viewModeListener)
    }

    fun getLogFilePath(): String {
//        var textString:String=""
        val files =
            ContextCompat.getExternalFilesDirs(getApplicationContext(), Environment.MEDIA_MOUNTED)
        for (file in files) {
//            Log.e("Main","file_dir:$file.absolutePath")
//            textString += ":"+ file.absolutePath
            return file.absolutePath
        }
//        val textForPrint = findViewById<TextView>(R.id.rearSwitchText)
//        textForPrint.text = textString
        return ""
    }
}
