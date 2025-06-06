package com.example.rearviewmirror

import android.util.Log
import com.vi.vioserial.NormalSerial
import java.util.concurrent.Executors


class Protocal {
    companion object {
        const val START_BYTE = 0xA5
        const val END_BYTE = 0x5A
    }
}

class CommandHandler {
    var uartOpenStatus: Int = -4
    // 处理数据的函数
    fun sendCommand(cmd: IntArray) {
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
        Log.d("UART", "发送数据: ${uartData.contentToString()}")
        writeUart(uartData)
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

        Log.i("UART", "成功解析命令: 长度=$cmdSize, 数据=${cmd.contentToString()}")
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
        return joinToString("") {
            "${it.toString(16).uppercase().padStart(2, '0')}"
        }
    }

    fun writeUart(uartData: IntArray) {
//        if (uartOpenStatus != 0) {
//            uartOpenStatus = NormalSerial.instance().open("/dev/ttyS1", 9600)
//        }
//        if (uartOpenStatus == 0) {
//            Log.i("UART", "open success")
//            NormalSerial.instance().sendHex(uartData.toHexString())
//        }
    }
    fun monitorUart() {
        if (uartOpenStatus == 0) {
            // 使用专用线程处理数据
            val dataProcessor = Executors.newSingleThreadExecutor()
            NormalSerial.instance().addDataListener { data ->
                dataProcessor.submit {
                    // 耗时解析操作,注意，默认接收的类型为hex
                    //val result = parseComplexData(data)
                    //runOnUiThread { updateUI(result) }
                }
            }
        }
    }

    fun openUart()
    {
        val serial = NormalSerial.instance().apply { open ("/dev/ttyS1", 9600)}
    }
}