package za.co.lsmc.data.entities

import za.co.lsmc.data.Category

data class Photo(
    var id: Long,
    var siteId: Long,
    var filename: String,
    var category: Category,
    var number: Double
)
