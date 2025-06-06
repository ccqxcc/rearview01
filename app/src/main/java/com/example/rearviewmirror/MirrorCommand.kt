package com.example.rearviewmirror

import android.util.Log

//设车机为HOST, 后视镜为SLAVE
class Command {
    companion object {
        // 车机发送命令列表
        const val HOST_SET_SWITCH = 0x84
        const val HOST_SET_LIGHT_VOLUME = 0x85
        const val HOST_SET_HEIGHT_VOLUME = 0x86
        const val HOST_SET_VIEW_ZOOM = 0x87
        const val HOST_SET_VIEW_MODE = 0x88
        const val HOST_GET_STATUS = 0x8A
        const val HOST_GET_VERSION = 0x81
        const val HOST_GET_IDENTITY = 0x82

        // 后视镜发送命令列表
        const val SLAVE_UPDATE_STATUS = 0x8B
        const val SLAVE_HEART_BEAT = 0x8C
    }
}

class MirrorCommand {
    fun setMirrorSwitch(rearSwitch: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_SWITCH
        cmd[1] = rearSwitch
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun setLightVolume(lightVolume: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_LIGHT_VOLUME
        cmd[1] = lightVolume
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun setHeightVolume(heightVolume: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_HEIGHT_VOLUME
        cmd[1] = heightVolume
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun setViewZoom(viewZoom: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_VIEW_ZOOM
        cmd[1] = viewZoom
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun setViewMode(viewMode: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_VIEW_MODE
        cmd[1] = viewMode
        cmd[2] = 0
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun getRearviewStatus() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_STATUS
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun updateRearViewStatus(cmd: IntArray): RearMirror? {
        if (cmd.size >= 6) {
            val mirror = RearMirror()
            mirror.rearSwitch = cmd[1]
            mirror.lightVolume = cmd[2]
            mirror.heightVolume = cmd[3]
            mirror.viewZoom = if (cmd[4] == 0) RearMirror.ViewZoom.ZOOM_10
            else if (cmd[4] == 1) RearMirror.ViewZoom.ZOOM_12
            else if (cmd[4] == 2) RearMirror.ViewZoom.ZOOM_14
            else mirror.viewZoom
            mirror.viewMode = if (cmd[5] == 0) RearMirror.ViewMode.MODE_STANDARD
            else if (cmd[5] == 1) RearMirror.ViewMode.MODE_ENHANCED
            else mirror.viewMode
            Log.d(
                "UART",
                "updateRearViewStatus 命令长度: $cmd.size, 命令数据: ${cmd.contentToString()}"
            )
            return mirror

        } else {
            Log.d(
                "UART",
                "updateRearViewStatus 命令长度不足: $cmd.size, 命令数据: ${cmd.contentToString()}"
            )
            return null
        }
    }

    fun decodeRearviewStatus(): RearMirror? {
        val parser = CommandHandler()

        // 测试用UART数据帧
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
            return updateRearViewStatus(cmd)
        } else return null
    }

    fun getRearviewSoftwareVersion() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_VERSION
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun decodeRearviewSoftwareVersion() {
        //TODO
    }

    fun getRearviewDeviceIndentity() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_IDENTITY
        val cmdHandler = CommandHandler()
        cmdHandler.sendCommand(cmd)
    }

    fun decodeRearviewDeviceIdentity() {
        //TODO
    }

    fun receiveCommand(uartData: IntArray, len: Int) {
        val parser = CommandHandler()
        // 解析数据
        val result = parser.parseUartData(uartData)
        if (result != null) {
            val (cmd, cmdSize) = result
            when (cmd[0]) {
                Command.HOST_GET_STATUS -> {
                    updateRearViewStatus(cmd)
                }

                Command.HOST_GET_VERSION -> {}
                Command.HOST_GET_IDENTITY -> {}
                Command.SLAVE_UPDATE_STATUS -> {
                    updateRearViewStatus(cmd)
                }

                Command.SLAVE_HEART_BEAT -> {}
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
}