package za.co.lsmc.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import dji.sampleV5.aircraft.R
import za.co.lsmc.models.ActionButton

class ActionButtonAdapter(
    private val context: Context,
    private val actions: List<ActionButton>,
    private val onActionClick: (ActionButton) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = actions.size

    override fun getItem(position: Int): ActionButton = actions[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.lssa_item_action_button, parent, false)

        val action = actions[position]
        val titleView = view.findViewById<TextView>(R.id.lssaTextViewActionTitle)
        val descriptionView = view.findViewById<TextView>(R.id.lssaTextViewActionDescription)

        titleView.text = action.title
        descriptionView.text = action.description

        view.alpha = if (action.isEnabled) 1.0f else 0.5f
        view.isClickable = action.isEnabled
        view.isFocusable = action.isEnabled

        if (action.isEnabled) {
            view.setOnClickListener {
                onActionClick(action)
            }
        } else {
            view.setOnClickListener(null)
        }

        return view
    }
}