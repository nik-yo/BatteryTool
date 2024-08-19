package com.nikkiyodo.batterytool.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "battery_metrics")
data class BatteryMetric(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo val timestamp: Long,
    @ColumnInfo val capacity: Int,
    @ColumnInfo(name = "charge_counter") val chargeCounter: Int,
    @ColumnInfo(name = "current_average") val currentAverage: Int,
    @ColumnInfo(name = "current_now") val currentNow: Int,
    @ColumnInfo(name = "energy_counter") val energyCounter: Int,
    @ColumnInfo val status: String,
    @ColumnInfo(name = "charge_remain") val chargeRemaining: Long,
    @ColumnInfo(name = "is_charging") val isCharging: Boolean
)