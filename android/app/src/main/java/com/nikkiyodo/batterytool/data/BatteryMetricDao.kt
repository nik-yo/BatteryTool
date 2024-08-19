package com.nikkiyodo.batterytool.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BatteryMetricDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMetric(batteryMetric: BatteryMetric)

    @Query("SELECT * FROM battery_metrics")
    suspend fun getAllMetrics(): Array<BatteryMetric>

    @Delete
    suspend fun deleteMetrics(vararg batteryEvent: BatteryMetric)
}