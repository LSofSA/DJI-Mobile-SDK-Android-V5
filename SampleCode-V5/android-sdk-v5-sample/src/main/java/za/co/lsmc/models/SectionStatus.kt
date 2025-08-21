package za.co.lsmc.models

import za.co.lsmc.models.enums.Category

// might just want to keep this in the  related adapter class?
data class SectionStatus(
    val name: String,
    val category: Category? = null,
    val isCompleted: Boolean = false,
    val isParent: Boolean = false,
    val indentLevel: Int = 0
)