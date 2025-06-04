package com.example.rearviewmirror

class RearMirror {
    // 成员变量
    var rearSwitch: Int = 0 //[0,1]
    var lightVolume: Int = 0 //[0,15]
    var heightVolume: Int = 0 //[0,5]
    var viewZoom: ViewZoom = ViewZoom.ZOOM_10
    var viewMode: ViewMode = ViewMode.MODE_STANDARD

    // 视图缩放枚举, 对应1.0，1.2，1.4
    enum class ViewZoom(val value: Int) {
        ZOOM_10(0),
        ZOOM_12(1),
        ZOOM_14(2)
    }

    // 视图模式枚举, 对应标准，增强
    enum class ViewMode(val value: Int) {
        MODE_STANDARD(0),
        MODE_ENHANCED(1)
    }

}
