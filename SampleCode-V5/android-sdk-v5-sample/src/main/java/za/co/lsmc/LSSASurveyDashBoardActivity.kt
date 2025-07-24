package za.co.lsmc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import dji.sampleV5.aircraft.databinding.LssaSurveyDashboardActivityBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

class LSSASurveyDashBoardActivity : AppCompatActivity() {

    private lateinit var binding: LssaSurveyDashboardActivityBinding
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val disposable = CompositeDisposable()

    // methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LssaSurveyDashboardActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
            finish()
            return
        }

        binding.btnNavLssaGoBackMain.setOnClickListener {
            Intent(this, LSSAMainActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        disposable.dispose()
    }
}