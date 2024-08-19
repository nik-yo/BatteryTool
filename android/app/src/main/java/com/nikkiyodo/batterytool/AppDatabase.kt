package com.nikkiyodo.batterytool

import androidx.room.Database
import androidx.room.RoomDatabase
import com.nikkiyodo.batterytool.data.BatteryEvent
import com.nikkiyodo.batterytool.data.BatteryEventDao
import com.nikkiyodo.batterytool.data.BatteryMetric
import com.nikkiyodo.batterytool.data.BatteryMetricDao

@Database(entities = [BatteryEvent::class,BatteryMetric::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun batteryEventDao(): BatteryEventDao
    abstract fun batteryMetricDao(): BatteryMetricDao
}