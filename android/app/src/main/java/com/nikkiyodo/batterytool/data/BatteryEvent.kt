package com.nikkiyodo.batterytool.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_events")
data class BatteryEvent(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val timestamp: Long,
    @ColumnInfo(name = "icon_id")  val batteryIconResId: Int,
    @ColumnInfo(name = "battery_low") val batteryLow: Boolean,
    @ColumnInfo(name = "charging_status") val chargingStatus: String,
    @ColumnInfo(name = "cycle_count") val cycleCount: Int,
    @ColumnInfo(name = "battery_health") val batteryHealth: String,
    @ColumnInfo val level: Int,
    @ColumnInfo val scale: Int,
    @ColumnInfo val plugged: String,
    @ColumnInfo(name = "battery_tech") val batteryTech: String,
    @ColumnInfo val temperature: Int,
    @ColumnInfo val status: String,
    @ColumnInfo val voltage: Int
)
