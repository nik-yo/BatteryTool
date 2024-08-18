package com.nikkiyodo.batterytool

import android.content.Intent
import android.os.BatteryManager
import android.os.Build
import kotlinx.datetime.Clock

class Util {
    companion object {
        fun getBatteryEventFrom (intent: Intent): BatteryEvent {
            val batteryIconResId = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, -1)

            var batteryLow = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                batteryLow =
                    intent.getBooleanExtra(BatteryManager.EXTRA_BATTERY_LOW, false)
            }

            var chargingStatus = ""
            var cycleCount = 0
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val chargingStatusInt = intent.getIntExtra(BatteryManager.EXTRA_CHARGING_STATUS, -1)
                chargingStatus = when (chargingStatusInt) {
                    0 -> "Not charging"
                    1 -> "Charging"
                    else -> "Unknown"
                }

                cycleCount = intent.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)
            }

            val batteryHealthInt = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
            val batteryHealth = when (batteryHealthInt) {
                BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
                BatteryManager.BATTERY_HEALTH_UNKNOWN -> "Unknown"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
                else -> "Invalid health"
            }

            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)

            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            val pluggedInt = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            val plugged = when (pluggedInt) {
                BatteryManager.BATTERY_PLUGGED_AC -> "AC charger"
                BatteryManager.BATTERY_PLUGGED_DOCK -> "Dock"
                BatteryManager.BATTERY_PLUGGED_USB -> "USB port"
                BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                else -> "On battery"
            }

            val batteryTech = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

            val temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1)

            val statusInt = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val status = when (statusInt) {
                BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                BatteryManager.BATTERY_STATUS_FULL -> "Full"
                BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
                BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
                else -> "Invalid status"
            }

            val voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)

            return BatteryEvent(
                Clock.System.now(),
                batteryIconResId,
                batteryLow,
                chargingStatus,
                cycleCount,
                batteryHealth,
                level,
                scale,
                plugged,
                batteryTech,
                temperature,
                status,
                voltage
            )
        }
    }
}