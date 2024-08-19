package com.nikkiyodo.batterytool

import android.content.Context
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.nikkiyodo.batterytool.data.BatteryMetric
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@Composable
fun Properties(db: AppDatabase) {
    val metricList = remember { mutableStateListOf<BatteryMetric>() }
//    var metricList by remember { mutableStateOf(listOf<Metric>())}
    var isCapturing by remember { mutableStateOf(false) }
    var openMetricDialog by remember { mutableStateOf<BatteryMetric?>(null) }

    var handlerThread by remember { mutableStateOf<HandlerThread?>(null) }
    var handler by remember { mutableStateOf<Handler?>(null) }

    val context = LocalContext.current

    val manager = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager

    val batteryStatsPermission = context.checkSelfPermission(android.Manifest.permission.BATTERY_STATS)

    LaunchedEffect(context) {
        val batteryMetrics = db.batteryMetricDao().getAllMetrics()
        if (batteryMetrics.isNotEmpty()) {
            metricList.addAll(batteryMetrics)
        }
    }

    fun getMetric(): BatteryMetric? {
        manager?.let {
            val lastRefreshed = Clock.System.now().toEpochMilliseconds()
            val capacity = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val chargeCounter = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
            val currentAverage = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
            val currentNow = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            val energyCounter = manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_ENERGY_COUNTER)
            var status = "Unknown"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                status = getStatus(manager.getIntProperty(BatteryManager.BATTERY_PROPERTY_STATUS))
            }
            var chargeRemaining = 0L
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                chargeRemaining = manager.computeChargeTimeRemaining()
            }
            val isCharging = manager.isCharging

            return BatteryMetric(
                            timestamp = lastRefreshed,
                            capacity = capacity,
                            chargeCounter = chargeCounter,
                            currentAverage = currentAverage,
                            currentNow = currentNow,
                            energyCounter = energyCounter,
                            status = status,
                            chargeRemaining = chargeRemaining,
                            isCharging = isCharging)
        }
        return null
    }

    fun stop() {
        handlerThread?.let {
            if (it.isAlive) {
                it.quitSafely()
            }
            handlerThread = null
            handler = null
        }
    }

    val runnable = object : Runnable {
        override fun run() {
            val batteryMetric = getMetric()
            if (batteryMetric != null) {
                metricList.add(batteryMetric)
                CoroutineScope(Dispatchers.IO).launch {
                    db.batteryMetricDao().insertMetric(batteryMetric)
                    val firstBatteryMetric = metricList[0]
                    if (metricList.count() > BuildConfig.MAX_METRICS.toInt()) {
                        db.batteryMetricDao().deleteMetrics(firstBatteryMetric)
                        withContext(Dispatchers.Main) {
                            metricList.removeAt(0)
                        }
                    }
                }
            }
            handler?.postDelayed(this, 1000)
        }
    }

    DisposableEffect(context) {

        onDispose {
            stop()
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (metricList.isEmpty()) {
                Row (
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                ) {
                    Text(text = "Press start to capture metrics every 1 second",
                        textAlign = TextAlign.Center)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = metricList,
                          key = { it.timestamp }) {
                        Card(
                            onClick = {
                                openMetricDialog = it
                            }
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(text = Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
                                    date(
                                        LocalDate.Format {
                                            monthNumber(); char('/')
                                            dayOfMonth(); char('/')
                                            year()
                                        }
                                    )
                                    chars(" ")
                                    time(
                                        LocalTime.Format {
                                            amPmHour(); char(':')
                                            minute(); char(':')
                                            second(); char(' ')
                                            amPmMarker("AM", "PM")
                                        }
                                    )}))
                                Row(
                                    horizontalArrangement = Arrangement.SpaceAround,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "${it.capacity}%", fontSize = 20.sp)
                                        Text(text = "capacity", fontWeight = FontWeight.Bold)
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(text = "${it.currentNow.toFloat() / 1000} mA", fontSize = 20.sp)
                                        Text(text = "current", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
                openMetricDialog?.let {
                    MetricDialog(
                        onDismissRequest = { openMetricDialog = null },
                        metric = it,
                        batteryStatsPermission = batteryStatsPermission
                    )
                }
            }
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                OutlinedButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            db.batteryMetricDao().deleteMetrics(*metricList.toTypedArray())
                            withContext(Dispatchers.Main) {
                                metricList.clear()
                            }
                        }
                    }
                ) {
                    Text(text = "Clear")
                }
                Button(
                    onClick = {
                        if (isCapturing) {
                            stop()
                        } else {
                            handlerThread = HandlerThread("metricCollectorThread").apply {
                                start()
                                handler = Handler(looper).apply {
                                    post(runnable)
                                }
                            }
                        }
                        isCapturing = handlerThread?.isAlive ?: false
                    }
                ) {
                    Text(text = if (isCapturing) { "Stop" } else { "Start" })
                }
            }
        }
    }
}

fun getStatus(statusInt: Int) : String {
    return when (statusInt) {
        BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
        BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
        BatteryManager.BATTERY_STATUS_FULL -> "Full"
        BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
        BatteryManager.BATTERY_STATUS_UNKNOWN -> "Unknown"
        else -> "Invalid status"
    }
}

const val secondInMs = 1000
const val minuteInMs = secondInMs * 60
const val hourInMs = minuteInMs * 60
const val dayInMs = hourInMs * 24

fun getChargeRemaining(chargeRemainingMs: Long): String {
    var days = 0
    var hours = 0
    var minutes = 0
    val seconds: Float

    var chargeRemaining = chargeRemainingMs

    if (chargeRemaining > dayInMs) {
        days = (chargeRemaining.toFloat() / dayInMs).toInt()
        chargeRemaining %= dayInMs
    }

    if (chargeRemaining > hourInMs) {
        hours = (chargeRemaining.toFloat() / hourInMs).toInt()
        chargeRemaining %= hourInMs
    }

    if (chargeRemaining > minuteInMs) {
        minutes = (chargeRemaining.toFloat() / minuteInMs).toInt()
        chargeRemaining %= minuteInMs
    }

    seconds = chargeRemaining.toFloat() / secondInMs

    val secondsUnit = "${"%.3f".format(seconds)}s"
    val minutesUnit = "${minutes}m $secondsUnit"
    val hoursUnit = "${hours}h $minutesUnit"
    val daysUnit = "${days}d $hoursUnit"

    return if (days > 0) {
        daysUnit
    } else if (hours > 0) {
        hoursUnit
    } else if (minutes > 0) {
        minutesUnit
    } else {
        secondsUnit
    }
}

@Composable
fun MetricDialog(
    onDismissRequest: () -> Unit,
//    onConfirmation: () -> Unit,
    metric: BatteryMetric,
    batteryStatsPermission: Int
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(text = Instant.fromEpochMilliseconds(metric.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).format(
                    LocalDateTime.Format {
                        monthNumber()
                        char('/')
                        dayOfMonth()
                        char('/')
                        year()
                        char(' ')
                        amPmHour()
                        char(':')
                        minute()
                        char(':')
                        second()
                        char(' ')
                        amPmMarker("AM","PM")
                    }))
                LazyVerticalGrid (
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Column {
                            Text(
                                text = "Capacity",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "${metric.capacity}%")
                        }
                    }
                    item {
                        Column {
                            Text(
                                text = "Max capacity",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "${metric.chargeCounter.toFloat() / 1000} mAh")
                        }
                    }
                    item {
                        Column {
                            Text(
                                text = "Current",
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${metric.currentNow.toFloat() / 1000} mA"
                            )
                        }
                    }
                    item {
                        Column {
                            Text(
                                text = "Is charging",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = metric.isCharging.toString())
                        }
                    }
                    if (batteryStatsPermission == PackageManager.PERMISSION_GRANTED) {
                        item {
                            Column {
                                Text(
                                    text = "Avg current",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${metric.currentAverage.toFloat() / 1000} mA (${
                                        if (metric.currentAverage > 0) {
                                            "Charging"
                                        } else {
                                            "Discharging"
                                        }
                                    })"
                                )
                            }
                        }
                    }
                    if (batteryStatsPermission == PackageManager.PERMISSION_GRANTED) {
                        item {
                            Column {
                                Text(
                                    text = "Remaining energy",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = "${"%.2f".format(metric.energyCounter.toFloat() / 1000000000)} Watt-hours")
                            }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        item {
                            Column {
                                Text(
                                    text = "Status",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = metric.status)
                            }
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // && chargeRemaining > 0) {
                        item {
                            Column {
                                Text(
                                    text = "Charge left",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(text = getChargeRemaining(3 * 24 * 3600 * 1000))
                            }
                        }
                    }
                }
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                }
            }
        }
    }
}