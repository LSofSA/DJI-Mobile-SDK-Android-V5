package za.co.lsmc.data

object SiteSurveyDbContact {

    object SiteTable {
        const val TABLE_NAME = "Sites"
        const val COLUMN_NAME_ID = "ID"
        const val COLUMN_NAME_NAME = "Name"
        const val COLUMN_NAME_COMPLETED = "Completed"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (\n" +
                "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT,'\n" +
                "$COLUMN_NAME_NAME TEXT NOT NULL,\n" +
                "$COLUMN_NAME_COMPLETED BIGINT)"
    }

    object HeadFrameTable {
        const val TABLE_NAME = "HeadFrames"
        const val COLUMN_NAME_ID = "ID"
        const val COLUMN_NAME_SITE_ID = "Site_ID"
        const val COLUMN_NAME_ALTITUDE = "Altitude"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (\n" +
                "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT,'\n" +
                "$COLUMN_NAME_SITE_ID INTEGER REFERENCES ${SiteTable.TABLE_NAME} ON DELETE CASCADE,'\n" +
                "$COLUMN_NAME_ALTITUDE REAL NOT NULL)"
    }

    object PhotoTable {
        const val TABLE_NAME = "Photos"
        const val COLUMN_NAME_ID = "ID"
        const val COLUMN_NAME_SITE_ID = "Site_ID"
        const val COLUMN_NAME_FILENAME = "Filename"
        const val COLUMN_NAME_CATEGORY = "Category"
        const val COLUMN_NAME_NUMBER = "Number"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (\n" +
                "$COLUMN_NAME_ID INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "$COLUMN_NAME_SITE_ID INTEGER REFERENCES ${SiteTable.TABLE_NAME} ON DELETE CASCADE,\n" +
                "$COLUMN_NAME_FILENAME TEXT NOT NULL,\n" +
                "$COLUMN_NAME_CATEGORY INTEGER NOT NULL,\n" +
                "$COLUMN_NAME_NUMBER REAL)"
    }

    object TowerScanTable {
        const val TABLE_NAME = "TowerScans"
        const val COLUMN_NAME_SITE_ID = "Site_ID"
        const val COLUMN_NAME_POI_LATITUDE = "POI_Latitude"
        const val COLUMN_NAME_POI_LONGITUDE = "POI_Longitude"
        const val COLUMN_NAME_POI_ALTITUDE = "POI_Altitude"
        const val COLUMN_NAME_RADIUS = "Radius"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (\n" +
                "$COLUMN_NAME_SITE_ID INTEGER REFERENCES ${SiteTable.TABLE_NAME} ON DELETE CASCADE,\n" +
                "$COLUMN_NAME_POI_LATITUDE REAL,\n" +
                "$COLUMN_NAME_POI_LONGITUDE REAL,\n" +
                "$COLUMN_NAME_POI_ALTITUDE REAL,\n" +
                "$COLUMN_NAME_RADIUS REAL,\n" +
                "PRIMARY KEY ($COLUMN_NAME_SITE_ID))"
    }

    object SaveTable {
        const val TABLE_NAME = "Saves"
        const val COLUMN_NAME_SITE_ID = "Site_ID"
        const val COLUMN_NAME_START_LATITUDE = "Start_Latitude"
        const val COLUMN_NAME_START_LONGITUDE = "Start_Longitude"
        const val COLUMN_NAME_STOP_LATITUDE = "Stop_Latitude"
        const val COLUMN_NAME_STOP_LONGITUDE = "Stop_Longitude"
        const val COLUMN_NAME_CATEGORY = "Category"
        const val COLUMN_NAME_NUMBER = "Number"

        const val CREATE_TABLE = "CREATE TABLE $TABLE_NAME (\n" +
                "$COLUMN_NAME_SITE_ID INTEGER REFERENCES ${SiteTable.TABLE_NAME} ON DELETE CASCADE,\n" +
                "$COLUMN_NAME_START_LATITUDE REAL NOT NULL,\n" +
                "$COLUMN_NAME_START_LONGITUDE REAL NOT NULL,\n" +
                "$COLUMN_NAME_STOP_LATITUDE REAL NOT NULL,\n" +
                "$COLUMN_NAME_STOP_LONGITUDE REAL NOT NULL,\n" +
                "$COLUMN_NAME_CATEGORY INTEGER NOT NULL,\n" +
                "$COLUMN_NAME_NUMBER INTEGER NOT NULL,\n" +
                "PRIMARY KEY ($COLUMN_NAME_SITE_ID))"
    }
}