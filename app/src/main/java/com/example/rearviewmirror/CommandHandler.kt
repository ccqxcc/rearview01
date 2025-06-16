package com.example.rearviewmirror

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Date


class Protocal {
    companion object {
        const val START_BYTE = 0xA5
        const val END_BYTE = 0x5A
    }
}

class CommandHandler(private val _viewModel: MyViewModel,val _filePath:String) {
    private lateinit var serialHandler: SerialHandler
    private lateinit var viewModel: MyViewModel
    private lateinit var logFile: File
    private var isInSendGap: Boolean = false
    private var cmdNumber: Int = 0
    init {
        // 初始化串口处理类
        serialHandler = SerialHandler(this,
            devicePath = "/dev/ttyS1",
            baudRate = 9600
        )
        viewModel = _viewModel

        logFile = File(_filePath, "Mirror.log")
        if (!logFile.exists()) {
            logFile.createNewFile()
        }
        Log.i("UART", "logfileName:$logFile")
    }

    fun close() {
        // 清理资源
        serialHandler.close()
    }

    fun delayUsingCoroutines(delayInMillis: Long) {
        CoroutineScope(Dispatchers.Main).launch {
            isInSendGap = true
            delay(delayInMillis)
            isInSendGap = false
        }
    }

    // 处理数据的函数
    fun sendCommand(cmd: IntArray) {
        if (isInSendGap) return
        val cmdSize = cmd.size
        // 创建足够长的 uartData 数组（至少 cmdSize + 4）
        val uartData = IntArray(cmdSize + 5)
        uartData[0] = Protocal.START_BYTE // 0xA5
        // 设置第2/3个元素为 cmdSize（索引为1,2, 大端模式）
        uartData[1] = cmdSize / 256
        uartData[2] = cmdSize % 256
        // 将cmd数组数据写入uartData的第3个元素开始的位置
        System.arraycopy(cmd, 0, uartData, 3, cmdSize)
        var checksum: Int = 0
        for (index in 0..(2 + cmdSize)) {
            checksum = checksum xor uartData[index]
        }
        uartData[3 + cmdSize] = checksum; //异或操作
        uartData[4 + cmdSize] = Protocal.END_BYTE
        Log.i("UART", "发送数据: ${uartData.toHexString()}")
        logFile.appendText("${Date()}:UART发送数据: ${uartData.toHexString()}\n")
        val byteData = intArrayToByteArray(uartData)
        serialHandler.sendData(byteData)

        val delayGap: Long = (if (cmdSize < 100) (100 - cmdSize.toLong()) else 100)
        delayUsingCoroutines(delayGap)

    }

    // 解析UART数据帧的函数
    fun parseUartData(uartData: IntArray): Pair<IntArray, Int>? {
        val dataSize = uartData.size

        // 1. 检查最小长度要求
        if (dataSize < 5) {
            Log.e("UART", "数据帧太短，至少需要5字节")
            return null
        }

        // 2. 检查起始字节和结束字节
        if (uartData[0] != Protocal.START_BYTE) {
            Log.e("UART", "无效的起始字节: ${uartData[0].toHex()} (应为0xA5)")
            return null
        }

        // 3. 解析命令长度 (高字节 << 8 + 低字节)
        val cmdSize = (uartData[1] shl 8) or uartData[2]

        // 4. 检查数据帧长度是否足够
        val expectedSize = 5 + cmdSize
        if (dataSize < expectedSize) {
            Log.e("UART", "数据帧长度不足: 需要$expectedSize 字节, 实际$dataSize 字节")
            return null
        }

        // 5. 检查结束字节
        val endIndex = 4 + cmdSize
        if (uartData[endIndex] != Protocal.END_BYTE) {
            Log.e("UART", "无效的结束字节: ${uartData[endIndex].toHex()} (应为0x5A)")
            return null
        }

        // 6. 计算校验值 (从索引0到2+cmdSize)
        val checkIndex = 3 + cmdSize
        val calculatedChecksum = calculateChecksum(uartData, 0, 3 + cmdSize)

        // 7. 验证校验值
        if (uartData[checkIndex] != calculatedChecksum) {
            Log.e(
                "UART",
                "校验失败: 计算值=${calculatedChecksum.toHex()}, 接收值=${uartData[checkIndex].toHex()}"
            )
            return null
        }

        // 8. 提取命令数据 (从索引3到2+cmdSize)
        val cmd = IntArray(cmdSize)
        for (i in 0 until cmdSize) {
            cmd[i] = uartData[3 + i]
        }

        Log.i("UART", "接收命令: 长度=$cmdSize,数据=${cmd.toHexString()}")
        logFile.appendText("${Date()}:UART接收命令: ${cmd.toHexString()}\n")
        return Pair(cmd, cmdSize)
    }

    // 计算异或校验和的辅助函数
    private fun calculateChecksum(data: IntArray, start: Int, end: Int): Int {
        var checksum = 0
        for (i in start until end) {
            checksum = checksum xor data[i]
        }
        return checksum
    }

    // 将整数转换为十六进制字符串的扩展函数
    private fun Int.toHex() = "0x${this.toString(16).padStart(2, '0').uppercase()}"

    // 将整数数组转为十六进制大写字符串数组
    fun IntArray.toHexString(): String {
        return joinToString(" ") {
            "${it.toString(16).uppercase().padStart(2, '0')}"
        }
    }

    fun intArrayToByteArray(intArray: IntArray): ByteArray {
        return ByteArray(intArray.size) { index ->
            intArray[index].toByte() // 直接转换，但注意符号问题
        }
    }
    fun receiveCommand(cmd: IntArray, cmdSize: Int) {
        if (cmd.size > 0) {
            showDebugText(cmd, cmdSize)

            when (cmd[0]) {
                Command.HOST_GET_STATUS -> {
                    updateRearViewStatus(cmd, cmdSize)
                }

                Command.HOST_GET_VERSION -> {}
                Command.HOST_GET_IDENTITY -> {}
                Command.SLAVE_UPDATE_STATUS -> {
                    ackStatusUpdate()
                    updateRearViewStatus(cmd, cmdSize)
                }

                Command.SLAVE_HEART_BEAT -> {
                    // ackHeartBeat()
                }

                Command.HOST_SET_SWITCH -> {}
                Command.HOST_SET_LIGHT_VOLUME -> {}
                Command.HOST_SET_HEIGHT_VOLUME -> {}
                Command.HOST_SET_VIEW_ZOOM -> {}
                Command.HOST_SET_VIEW_MODE -> {}
                else -> {}
            }
        } else {
        }
    }
    fun ackStatusUpdate() {
        val cmd = IntArray(1)
        cmd[0] = Command.SLAVE_UPDATE_STATUS
        sendCommand(cmd)
    }
    fun ackHeartBeat() {
        val cmd = IntArray(1)
        cmd[0] = Command.SLAVE_HEART_BEAT
        sendCommand(cmd)
    }
    fun updateRearViewStatus(cmd: IntArray, cmdSize: Int){
        if (cmd.size >= 6) {
            viewModel.rearSwitchData.postValue(cmd[1] == 1)
            viewModel.lightData.postValue(cmd[2])
            viewModel.heightData.postValue(cmd[3])
            viewModel.zoomData.postValue(cmd[4])
            viewModel.modeData.postValue(cmd[5])
            Log.d(
                "UART",
                "命令长度: $cmd.size, 命令数据: ${cmd.toHexString()}"
            )
        } else {
            Log.d(
                "UART",
                "命令长度不足: $cmd.size, 命令数据: ${cmd.toHexString()}"
            )

        }
    }
    fun showDebugText(cmd: IntArray, cmdSize: Int) {
        if (cmd.size > 0) {
            if (cmd[0] != Command.SLAVE_HEART_BEAT) {
                cmdNumber++
                viewModel.myLiveData.postValue("$cmdNumber $cmdSize ${cmd.toHexString()}")
            }
        }
    }
}