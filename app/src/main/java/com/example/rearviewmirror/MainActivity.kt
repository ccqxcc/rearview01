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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MyViewModel : ViewModel() {
// 定义一个公开的MutableLiveData，用于保存一个字符串
    val myLiveData = MutableLiveData<String>()
    val rearSwitchData = MutableLiveData<Boolean>()
    val lightData = MutableLiveData<Int>()
    val heightData = MutableLiveData<Int>()
    val zoomData = MutableLiveData<Int>()
    val modeData = MutableLiveData<Int>()
}

class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        registerObserver()

        val cmmdHandler = CommandHandler(viewModel)
        val mirrorCmmd = MirrorCommand(cmmdHandler)
        mirrorCmmd.getRearviewStatus()
        // 查询后视镜状态
        val mirrorStatus = findViewById<Button>(R.id.getMirrorStatus)
        mirrorStatus.setOnClickListener {
            //发命令到后视镜并待返回
            mirrorCmmd.getRearviewStatus()

            //按后视镜返回的信息更新车机界面状态
//          updateActivityView(mirror)

        }

        val rearSwitch = findViewById<Switch>(R.id.rearSwitch)
        // 监听 Switch 状态变化
        rearSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            var rearSwitch: Int = 0
            if (isChecked) {
                // Switch 被打开（ON）
                Toast.makeText(getApplicationContext(), "switch on", Toast.LENGTH_LONG).show()
                Log.d("Switch", "开关已打开")
                rearSwitch = 1
            } else {
                // Switch 被关闭（OFF）
                Toast.makeText(getApplicationContext(), "switch off", Toast.LENGTH_LONG).show()
                Log.d("Switch", "开关已关闭")
                rearSwitch = 0
            }
            mirrorCmmd.setMirrorSwitch(rearSwitch)
        }

        //监听亮度调节滑动事件
        val lightVolume = findViewById<SeekBar>(R.id.lightVolume)
        lightVolume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    val lightVolume = seekBar.progress
                    val tvProgress = findViewById<TextView>(R.id.lightVolumeText)
                    tvProgress.text = (lightVolume+1).toString()
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
                    val heightVolume = seekBar.progress
                    val tvProgress = findViewById<TextView>(R.id.heightVolumeText)
                    tvProgress.text = (heightVolume+1).toString()
                    mirrorCmmd.setHeightVolume(heightVolume)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // 可在此处保存最终进度或执行耗时操作
            }
        })

        //缩放
        val zoomGroup = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        zoomGroup.setOnCheckedChangeListener { group, checkedId ->
            var zoomX:Int = -1
            when (checkedId) {
                R.id.zoomX1 -> { /* 处理选项1 */
                    zoomX = 0
                    Toast.makeText(getApplicationContext(), "zoomX1", Toast.LENGTH_LONG).show()
                }
                R.id.zoomX12 -> { /* 处理选项2 */
                    zoomX = 1
                    Toast.makeText(getApplicationContext(), "zoomX1.2", Toast.LENGTH_LONG).show()
                }
                R.id.zoomX14 -> {/* 处理选项3 */
                    zoomX = 2
                    Toast.makeText(getApplicationContext(), "zoomX1.4", Toast.LENGTH_LONG).show()
                }
                else -> {
                    /* 处理异常 */
                    zoomX = 0
                    Toast.makeText(getApplicationContext(), "非法值", Toast.LENGTH_LONG).show()
                }
            }
            mirrorCmmd.setViewZoom(zoomX)
        }

        val viewMode = findViewById<RadioGroup>(R.id.viewMode)
        viewMode.setOnCheckedChangeListener { group, checkedId ->
            var modeX: Int = -1
            when (checkedId) {
                R.id.viewStandard -> { /* 处理选项1 */
                    modeX = 0
                    Toast.makeText(getApplicationContext(), "viewStandard", Toast.LENGTH_LONG)
                        .show()
                }

                R.id.viewEnhanced -> {/* 处理选项2 */
                    modeX = 1

                    Toast.makeText(getApplicationContext(), "viewEnhanced", Toast.LENGTH_LONG)
                        .show()
                    Log.d("zoomRadioGroup", "viewEnhanced")
                }

                else -> {/* 处理异常 */
                    modeX = 0
                    Toast.makeText(getApplicationContext(), "非法值", Toast.LENGTH_LONG).show()
                }
            }
            mirrorCmmd.setViewMode(modeX)
        }

    }

    fun registerObserver()
    {
        // 初始化ViewModel
        viewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        // 观察LiveData，当数据变化时更新TextView
        val rearSwitchText = findViewById<TextView>(R.id.rearSwitchText)
        viewModel.myLiveData.observe(this, Observer { newValue ->
            // 更新UI，例如设置TextView的文本
            rearSwitchText.text = newValue
        })
        //TODO BUG: 此处因更新了switch控件，会触发不必要switch事件从而发不必要的设置命令到后视镜
        val rearSwitchStatus = findViewById<Switch>(R.id.rearSwitch)
        viewModel.rearSwitchData.observe(this, Observer { newValue ->
            rearSwitchStatus.setChecked(newValue)
        })
        val lightVolume = findViewById<SeekBar>(R.id.lightVolume)
        val lightProgress = findViewById<TextView>(R.id.lightVolumeText)
        viewModel.lightData.observe(this, Observer { newValue ->
            if (newValue <= 14) {
                lightVolume.setProgress(newValue + 1)
                lightProgress.text = (newValue + 1).toString()
            }
        })
        val heightVolume = findViewById<SeekBar>(R.id.heightVolume)
        val heightProgress = findViewById<TextView>(R.id.heightVolumeText)
        viewModel.lightData.observe(this, Observer { newValue ->
            if (newValue <= 6) {
                heightVolume.setProgress(newValue + 1)
                heightProgress.text = (newValue + 1).toString()
            }
        })
        val zoomGroup = findViewById<RadioGroup>(R.id.zoomRadioGroup)
        val viewMode = findViewById<RadioGroup>(R.id.viewMode)
        viewModel.zoomData.observe(this, Observer { newValue ->
            if (newValue == 0) {
                zoomGroup.check(R.id.zoomX1)
            }
            if (newValue == 1) {
                zoomGroup.check(R.id.zoomX12)
            }
            if (newValue == 2) {
                zoomGroup.check(R.id.zoomX14)
            }
            else{}
        })
        viewModel.modeData.observe(this, Observer { newValue ->
            if (newValue == 0) {
                viewMode.check(R.id.viewStandard)
            }
            if (newValue == 1) {
                viewMode.check(R.id.viewEnhanced)
            }
            else{
            }
        })
    }

}
