package za.co.lsmc

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import dji.sampleV5.aircraft.R
import dji.sampleV5.aircraft.databinding.LssaMainActivityBinding
import io.reactivex.rxjava3.disposables.CompositeDisposable

class LSSAMainActivity : AppCompatActivity() {

    private lateinit var binding: LssaMainActivityBinding
    private val handler: Handler = Handler(Looper.getMainLooper())
    private val disposable = CompositeDisposable()

    // methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LssaMainActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.btnNavLssaDashBoard) as NavHostFragment
        val navController = navHostFragment.navController

        if (!isTaskRoot && intent.hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN == intent.action) {
            finish()
            return
        }

        binding.btnNavLssaDashBoard.setOnClickListener {
            Intent(this, LSSASurveyDashBoardActivity::class.java).also {

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