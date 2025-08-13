package za.co.lsmc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import dji.sampleV5.aircraft.R
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class AccessRoute : Fragment() {

    companion object {
        fun newInstance() = AccessRoute()
    }

    private val viewModel: LSSASiteSurveyViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.lssa_fragment_access_route, container, false)
    }
}