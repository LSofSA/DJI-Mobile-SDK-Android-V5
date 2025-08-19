package za.co.lsmc.data.entities

import java.util.Date

data class Site(
    var id: Long,
    var name: String,
    var completed: Date?
)