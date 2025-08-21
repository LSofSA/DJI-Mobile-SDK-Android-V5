package za.co.lsmc.models

import za.co.lsmc.data.entities.HeadFrame

sealed class TowerScanStepItem {
    data class HeadFrameItem(val headFrame: HeadFrame, val number: Int) : TowerScanStepItem()
    data class InfoItem(val text: String) : TowerScanStepItem()
}