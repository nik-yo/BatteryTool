package com.nikkiyodo.batterytool.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BatteryEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(batteryEvent: BatteryEvent)

    @Query("SELECT * FROM battery_events")
    suspend fun getAllEvents(): Array<BatteryEvent>

    @Delete
    suspend fun deleteEvents(vararg batteryEvent: BatteryEvent)
}