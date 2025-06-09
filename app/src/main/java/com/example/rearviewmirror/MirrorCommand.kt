package com.example.rearviewmirror

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

class MirrorCommand(_cmmdHandler: CommandHandler)  {
    private lateinit var cmmdHandler: CommandHandler
    // 初始化块（在构造函数执行时调用）
    init {
        cmmdHandler = _cmmdHandler
    }
    fun setMirrorSwitch(rearSwitch: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_SWITCH
        cmd[1] = rearSwitch
        cmd[2] = 0
        cmmdHandler.sendCommand(cmd)
    }

    fun setLightVolume(lightVolume: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_LIGHT_VOLUME
        cmd[1] = lightVolume
        cmd[2] = 0
        cmmdHandler.sendCommand(cmd)
    }

    fun setHeightVolume(heightVolume: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_HEIGHT_VOLUME
        cmd[1] = heightVolume
        cmd[2] = 0
        cmmdHandler.sendCommand(cmd)
    }

    fun setViewZoom(viewZoom: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_VIEW_ZOOM
        cmd[1] = viewZoom
        cmd[2] = 0
        cmmdHandler.sendCommand(cmd)
    }

    fun setViewMode(viewMode: Int) {
        val cmd = IntArray(3)
        cmd[0] = Command.HOST_SET_VIEW_MODE
        cmd[1] = viewMode
        cmd[2] = 0
        cmmdHandler.sendCommand(cmd)
    }

    fun getRearviewStatus() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_STATUS
        cmmdHandler.sendCommand(cmd)
    }

    fun getRearviewSoftwareVersion() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_VERSION
        cmmdHandler.sendCommand(cmd)
    }

    fun decodeRearviewSoftwareVersion() {
        //TODO
    }

    fun getRearviewDeviceIndentity() {
        val cmd = IntArray(1)
        cmd[0] = Command.HOST_GET_IDENTITY
        cmmdHandler.sendCommand(cmd)
    }

    fun decodeRearviewDeviceIdentity() {
        //TODO
    }

}