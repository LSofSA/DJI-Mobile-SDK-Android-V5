package za.co.lsmc.data.entities

import za.co.lsmc.models.enums.Category

data class Save(
    var siteId: Long,
    var startLatitude: Double,
    var startLongitude: Double,
    var category: Category,
    var number: Double,
    var stopLatitude: Double,
    var stopLongitude: Double
)
