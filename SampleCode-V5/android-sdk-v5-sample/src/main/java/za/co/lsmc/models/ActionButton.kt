package za.co.lsmc.models

// might just want to keep this in the adapter class?
data class ActionButton(
    val title: String,
    val description: String,
    val isEnabled: Boolean = true,
    val action: () -> Unit
)