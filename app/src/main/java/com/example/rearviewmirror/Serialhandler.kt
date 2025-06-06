package com.example.rearviewmirror

import android.content.Context
import android.util.Log
import com.vi.vioserial.NormalSerial

class SerialHandler(devicePath: String, baudRate: Int) {
    // 成员变量：串口实例
    private val serial: NormalSerial

    // 1. 声明成员变量保存监听器引用
    private lateinit var dataListener: (String) -> Unit

    // 初始化块（在构造函数执行时调用）
    init {
        // 1. 获取 NormalSerial 单例
        serial = NormalSerial.instance()

        // 2. 配置并打开串口
        serial.open(devicePath, baudRate) // "/dev/ttyS1", 9600

        // 3. 创建监听器并保存引用
        dataListener = { uartData ->
            handleReceivedData(uartData)
        }

        // 4 注册数据监听器
        serial.addDataListener(dataListener)
        //{ uartData ->
        //    handleReceivedData(uartData.toByteArray())
        //}
    }

    // 数据处理函数
    private fun handleReceivedData(data: String) {
        if (data.isNotEmpty()) {
            // sendData(data.toByteArray())// 示例：简单回显
            // 转换成IntArray

            val result = StringToIntArray(data)
            val (uartData, uartDataSize) = result
/*
            // 解析命令, 与receiveCommand重复
            val parser = CommandHandler()
            val cmdResult = parser.parseUartData(uartData)
            if (cmdResult != null) {
                val (cmd, cmdSize) = cmdResult
//
//              val mirrorCommand = MirrorCommand()
//              mirrorCommand.receiveCommand(cmd,cmdSize)
                //return updateRearViewStatus(cmd)
            }
*/
        }
    }

    fun StringToIntArray(data:String):Pair<IntArray, Int>
    {
        val dataUart = IntArray(128)
        var dataIndex = 0
        for(i in data.indices) {
            dataUart[dataIndex] = data[i].code
            dataIndex ++
        }
        return Pair(dataUart, dataIndex)
    }
    // 发送数据方法
    fun sendData(uartData: ByteArray) {
        serial.sendHex(uartData.toHexString())
    }

    // 关闭串口方法
    fun close() {
        serial.removeDataListener(dataListener)
        serial.close()
        Log.i("SerialHandler", "串口已关闭")
    }

    // ByteArray转十六进制字符串的扩展函数
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

}