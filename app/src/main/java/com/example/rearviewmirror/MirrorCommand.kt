package com.example.rearviewmirror

import android.util.Log
import android.util.Size

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

object MirrorCommand {
    var mirror: RearMirror = RearMirror()
    var updated: Boolean = false
    fun ifUpdated():Boolean{
        return updated
    }
    fun setMirrorSwitch(rearSwitch: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_SWITCH
        cmd[1] = rearSwitch
        cmd[2] = 0
        CommandHandler.sendCommand(cmd)
    }

    fun setLightVolume(lightVolume: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_LIGHT_VOLUME
        cmd[1] = lightVolume
        cmd[2] = 0
        CommandHandler.sendCommand(cmd)
    }

    fun setHeightVolume(heightVolume: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_HEIGHT_VOLUME
        cmd[1] = heightVolume
        cmd[2] = 0
        CommandHandler.sendCommand(cmd)
    }

    fun setViewZoom(viewZoom: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_VIEW_ZOOM
        cmd[1] = viewZoom
        cmd[2] = 0
        CommandHandler.sendCommand(cmd)
    }

    fun setViewMode(viewMode: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_VIEW_MODE
        cmd[1] = viewMode
        cmd[2] = 0
        CommandHandler.sendCommand(cmd)
    }

    fun getRearviewStatus() {
        updated = false
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_STATUS
        CommandHandler.sendCommand(cmd)
    }

    fun updateRearViewStatus(cmd: IntArray): RearMirror? {
        if (cmd.size >= 6) {
            //val mirror = RearMirror()
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
            updated = true
            return mirror

        } else {
            Log.d(
                "UART",
                "updateRearViewStatus 命令长度不足: $cmd.size, 命令数据: ${cmd.contentToString()}"
            )
            return null
        }
    }

    fun getRearviewSoftwareVersion() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_VERSION
        CommandHandler.sendCommand(cmd)
    }

    fun decodeRearviewSoftwareVersion() {
        //TODO
    }

    fun getRearviewDeviceIndentity() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_IDENTITY
        CommandHandler.sendCommand(cmd)
    }

    fun decodeRearviewDeviceIdentity() {
        //TODO
    }

    fun ackStatusUpdate() {
        val cmd = IntArray(1)
        cmd[0] = Command.SLAVE_UPDATE_STATUS
        CommandHandler.sendCommand(cmd)
    }
    fun ackHeartBeat() {
        val cmd = IntArray(1)
        cmd[0] = Command.SLAVE_HEART_BEAT
        CommandHandler.sendCommand(cmd)
    }

    fun receiveCommand(cmd: IntArray, cmdSize: Int) {
        if (cmd.size > 0) {
            when (cmd[0]) {
                Command.HOST_GET_STATUS -> {
                    updateRearViewStatus(cmd)
                }

                Command.HOST_GET_VERSION -> {}
                Command.HOST_GET_IDENTITY -> {}
                Command.SLAVE_UPDATE_STATUS -> {
                    ackStatusUpdate()
                    updateRearViewStatus(cmd)
                }

                Command.SLAVE_HEART_BEAT -> {ackHeartBeat()}
                Command.HOST_SET_SWITCH -> {}
                Command.HOST_SET_LIGHT_VOLUME -> {}
                Command.HOST_SET_HEIGHT_VOLUME -> {}
                Command.HOST_SET_VIEW_ZOOM -> {}
                Command.HOST_SET_VIEW_MODE -> {}
                else -> {}
            }
        }
        else {
        }
    }
}