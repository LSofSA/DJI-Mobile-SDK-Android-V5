package za.co.lsmc

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R

class LSSAWelcomeFragment : Fragment() {

    private lateinit var statusText: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_lssa_welcome, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        statusText = view.findViewById(R.id.tvSDKStatus)
        initializeSDK()
    }

    private fun initializeSDK() {
        // Update status text
        statusText.text = "SDK Status: Initializing DJI SDK..."

        // Simulate SDK initialization (replace with actual DJI SDK init)
        Handler(Looper.getMainLooper()).postDelayed({
            statusText.text = "SDK Status: SDK Initialization Complete"

            // Navigate to main screen after another second
            Handler(Looper.getMainLooper()).postDelayed({
                findNavController().navigate(R.id.action_welcome_to_main)
            }, 1000)
        }, 4000) // 4 seconds for initialization
    }
}