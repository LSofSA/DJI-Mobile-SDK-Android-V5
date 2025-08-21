package za.co.lsmc.models

import android.view.View
import za.co.lsmc.ui.fragments.TowerScanFragment

interface TowerScanStep {
    fun getActionButtonText(): String
    fun onActionButtonClick()
    fun onNextButtonClick(): Boolean // returns true if can proceed to next step
    fun getLayoutId(): Int
    fun setupView(view: View, fragment: TowerScanFragment)
    fun onResume() {}
    fun onPause() {}
}