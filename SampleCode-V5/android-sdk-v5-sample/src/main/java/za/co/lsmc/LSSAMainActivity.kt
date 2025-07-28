package za.co.lsmc

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity

class LSSAMainActivity : AppCompatActivity() {

    private val viewModel: LSSASiteSurveyViewModel by viewModels()

    // methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}