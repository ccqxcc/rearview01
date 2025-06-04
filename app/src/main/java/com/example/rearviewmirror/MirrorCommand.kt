package com.example.rearviewmirror

import android.util.Log

class MirrorCommand{
    fun setMirrorSwitch(rearSwitch: Int)
    {
        val cmd = IntArray(3)
        cmd[0] = 0x84
        cmd[1] = rearSwitch
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun setLightVolume(lightVolume: Int)
    {
        val cmd = IntArray(3)
        cmd[0] = 0x85
        cmd[1] = lightVolume
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun setHeightVolume(heightVolume: Int)
    {
        val cmd = IntArray(3)
        cmd[0] = 0x86
        cmd[1] = heightVolume
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun setViewZoom(viewZoom: Int)
    {
        val cmd = IntArray(3)
        cmd[0] = 0x87
        cmd[1] = viewZoom
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun setViewMode(viewMode: Int)
    {
        val cmd = IntArray(3)
        cmd[0] = 0x88
        cmd[1] = viewMode
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun GetRearviewStatus()
    {
        val cmd = IntArray(1)
        cmd[0] = 0x8A
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun decodeRearviewStatus(mirror: RearMirror)
    {
        val parser = CommandHandler()

        // 示例UART数据帧 (符合协议规范)
        val validUartData = intArrayOf(
            0xA5,                   // 起始字节
            0x00, 0x07,             // 命令长度高字节和低字节 (长度=7)
            0x8A, 0x01, 0x0F, 0x05, 0x02, 0x01, 0X00,      // 命令数据
            0xA5 xor 0x00 xor 0x07 xor 0x8A xor 0x01 xor 0x0F xor 0x05 xor 0x02 xor 0x01 xor 0x00, // 校验字节
            0x5A                    // 结束字节
        )

        // 解析数据
        val result = parser.parseUartData(validUartData)

        if (result != null) {
            val (cmd, cmdSize) = result
            mirror.rearSwitch = cmd[1]
            mirror.lightVolume = cmd[2]
            mirror.heightVolume = cmd[3]
            mirror.viewZoom = if (cmd[4] == 0) RearMirror.ViewZoom.ZOOM_10
                else if(cmd[4] == 1) RearMirror.ViewZoom.ZOOM_12
                else if(cmd[4] == 2) RearMirror.ViewZoom.ZOOM_14
                else mirror.viewZoom
            mirror.viewMode = if (cmd[5] == 0) RearMirror.ViewMode.MODE_STANDARD
                else if (cmd[5] == 1) RearMirror.ViewMode.MODE_ENHANCED
                else mirror.viewMode
            //println("解析成功! 命令长度: $cmdSize, 命令数据: ${cmd.contentToString()}")
            //Log.d("UART", "发送数据: ${data.contentToString()}")
            Log.d("UART","解析成功! 命令长度: $cmdSize, 命令数据: ${cmd.contentToString()}")
        } else {
            Log.d("UART","解析失败")
        }
    }
    fun GetRearviewSoftwareVersion()
    {
        val cmd = IntArray(1)
        cmd[0] = 0x81
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun decodeRearviewSoftwareVersion()
    {
        //TODO
    }
    fun GetRearviewDeviceIndentity()
    {
        val cmd = IntArray(1)
        cmd[0] = 0x82
        val cmdHandler = CommandHandler()
        val uartData = cmdHandler.sendCommand(cmd)
    }
    fun decodeRearviewDeviceIndentity()
    {
        //TODO
    }
}