package za.co.lsmc.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R
import za.co.lsmc.viewmodels.LSSASiteSurveyViewModel

class AccessRoute : Fragment() {

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

    /**
     * Called immediately after [.onCreateView]
     * has returned, but before any saved state has been restored in to the view.
     * This gives subclasses a chance to initialize themselves once
     * they know their view hierarchy has been completely created.  The fragment's
     * view hierarchy is not however attached to its parent at this point.
     * @param view The View returned by [.onCreateView].
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lssaAccessRouteButtonBack = view.findViewById(R.id.lssaAccessRouteButtonBack)

        setupClickListeners()
    }
}