package za.co.lsmc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class AccessRoute : Fragment() {

    private lateinit var tvLiveInfo: TextView
    private lateinit var tvLiveError: TextView
    private lateinit var svCameraStream: SurfaceView

    companion object {
        fun newInstance() = AccessRoute()
    }

    private lateinit var lssaAccessRouteButtonBack: Button

    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private fun setupClickListeners() {
        lssaAccessRouteButtonBack.setOnClickListener {
            findNavController().popBackStack()
            //requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.lssa_fragment_access_route, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvLiveInfo = view.findViewById(R.id.tv_live_info)
        tvLiveError = view.findViewById(R.id.tv_live_error)
        svCameraStream = view.findViewById(R.id.sv_camera_stream)
        lssaAccessRouteButtonBack = view.findViewById(R.id.lssaAccessRouteButtonBack)

        setupClickListeners()
    }
}