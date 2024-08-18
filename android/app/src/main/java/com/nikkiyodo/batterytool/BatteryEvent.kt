package com.nikkiyodo.batterytool

import kotlinx.datetime.Instant

data class BatteryEvent(
    val timestamp: Instant,
    val batteryIconResId: Int,
    val batteryLow: Boolean,
    val chargingStatus: String,
    val cycleCount: Int,
    val batteryHealth: String,
    val level: Int,
    val scale: Int,
    val plugged: String,
    val batteryTech: String,
    val temperature: Int,
    val status: String,
    val voltage: Int
)
