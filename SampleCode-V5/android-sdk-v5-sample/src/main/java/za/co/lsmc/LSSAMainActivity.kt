package za.co.lsmc

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import dji.sampleV5.aircraft.R

class LSSAMainActivity : AppCompatActivity() {

    private val viewModel: LSSASiteSurveyViewModel by viewModels()

    // methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.lssa_main_activity)
    }
}