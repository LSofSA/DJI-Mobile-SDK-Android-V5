package za.co.lsmc.data

data class TowerScan(var siteId: Long, var poiLatitude: Double? = null, var poiLongitude: Double? = null, var poiAltitude: Double? = null, var radius: Double? = null) {
}
