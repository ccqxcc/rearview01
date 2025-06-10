package com.example.rearviewmirror

import android.content.Context
import android.util.Log
import com.vi.vioserial.NormalSerial

class SerialHandler(cmmdHandler: CommandHandler, devicePath: String, baudRate: Int) {
    // 成员变量：串口实例
    private val serial: NormalSerial

    // 1. 声明成员变量保存监听器引用
    private lateinit var dataListener: (String) -> Unit

    // 关闭串口方法
    fun close() {
        serial.removeDataListener(dataListener)
        serial.close()
        Log.i("SerialHandler", "串口已关闭")
    }

    // 初始化块（在构造函数执行时调用）
    init {
        // 1. 获取 NormalSerial 单例
        serial = NormalSerial.instance()

        // 2. 配置并打开串口
        serial.open(devicePath, baudRate) // "/dev/ttyS1", 9600

        // 3. 创建监听器并保存引用
        dataListener = { uartData ->
            handleReceivedData(cmmdHandler, uartData)
        }

        // 4 注册数据监听器
        serial.addDataListener(dataListener)
    }

    // 数据处理函数
    /*private*/ fun handleReceivedData(cmmdHandler: CommandHandler, data: String) {
        if (data.isNotEmpty()) {
            // sendData(data.toByteArray())// 示例：简单回显
            // 转换成IntArray
            val uartData = hexStringToIntArray(data)

            // 解析命令, 与receiveCommand重复
            val cmdResult = cmmdHandler.parseUartData(uartData)
            if (cmdResult != null) {
                val (cmd, cmdSize) = cmdResult
                cmmdHandler.receiveCommand(cmd, cmdSize)
            }
            else{
            }
        }
    }

    fun hexStringToIntArray(hexString: String): IntArray {
        // 1. 移除字符串中的空格和非十六进制字符
        val cleanHex = hexString.replace("\\s".toRegex(), "")

        // 2. 检查长度是否为偶数（每两个字符表示一个字节）
        require(cleanHex.length % 2 == 0) { "十六进制字符串长度必须为偶数" }

        // 3. 每两个字符一组转换为整数
        return IntArray(cleanHex.length / 2) { index ->
            val start = index * 2
            val end = start + 2
            val byteStr = cleanHex.substring(start, end)

            // 4. 将十六进制字符串转换为整数
            byteStr.toInt(16)
        }
    }

    // 发送数据方法
    fun sendData(uartData: ByteArray) {
        serial.sendHex(uartData.toHexString())
    }

    // ByteArray转十六进制字符串的扩展函数
    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }

}