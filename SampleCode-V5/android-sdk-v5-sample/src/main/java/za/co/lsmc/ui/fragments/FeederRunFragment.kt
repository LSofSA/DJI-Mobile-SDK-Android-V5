package za.co.lsmc.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import dji.sampleV5.aircraft.R

class FeederRunFragment : Fragment() {

    private lateinit var lssaFeederCableButtonBack : Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.lssa_fragment_feeder_run_capture, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

        lssaFeederCableButtonBack = view.findViewById(R.id.lssaFeederCableButtonBack)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        lssaFeederCableButtonBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }
}