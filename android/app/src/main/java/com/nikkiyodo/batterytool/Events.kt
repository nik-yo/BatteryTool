package com.nikkiyodo.batterytool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.room.Room
import com.nikkiyodo.batterytool.data.BatteryEvent
import com.nikkiyodo.batterytool.ui.theme.BatteryToolTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime

@Composable
fun Events(db: AppDatabase) {
    val batteryEventList = remember { mutableStateListOf<BatteryEvent>() } //*savedBatteryEvent) }
    val context = LocalContext.current
    var isFirstReceive = true

    LaunchedEffect(context) {
        val batteryEvents = db.batteryEventDao().getAllEvents()
        if (batteryEvents.isNotEmpty()) {
            batteryEventList.addAll(batteryEvents)
        }
    }

    DisposableEffect(context) {
        val batteryChangedFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        val batteryChangedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                if (isFirstReceive) {
                    isFirstReceive = false
                    return
                }

                val batteryEvent = Util.getBatteryEventFrom(intent)
                batteryEventList.add(batteryEvent)

                CoroutineScope(Dispatchers.IO).launch {
                    db.batteryEventDao().insertEvent(batteryEvent)
                    val firstBatteryEvent = batteryEventList[0]
                    if (batteryEventList.count() > BuildConfig.MAX_EVENTS.toInt()) {
                        db.batteryEventDao().deleteEvents(firstBatteryEvent)
                        withContext(Dispatchers.Main) {
                            batteryEventList.removeAt(0)
                        }
                    }
                }
            }
        }

        context.registerReceiver(batteryChangedReceiver, batteryChangedFilter)

        onDispose {
            context.unregisterReceiver(batteryChangedReceiver)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        if (batteryEventList.isEmpty()) {
            Row (
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(64.dp)
            ) {
                Text(text = "Events will be automatically displayed when received",
                    textAlign = TextAlign.Center)
            }
        } else {
            Column {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(items = batteryEventList,
                          key = { it.timestamp }) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box {
                                Text(text = Instant.fromEpochMilliseconds(it.timestamp).toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
                                    date(
                                        LocalDate.Format {
                                            monthNumber()
                                            char('/')
                                            dayOfMonth()
                                            char('/')
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
                            }
                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (it.batteryIconResId > 0) {
                                    context.getDrawable(it.batteryIconResId)?.toBitmap()?.let {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "Icon",
                                                fontWeight = FontWeight.Bold
                                            )
                                            Image(
                                                bitmap = it.asImageBitmap(),
                                                contentDescription = stringResource(id = R.string.battery_icon)
                                            )
                                        }
                                    }
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Low",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = it.batteryLow.toString())
                                    }
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Health",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = it.batteryHealth)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Level",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = it.level.toString())
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Scale",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = it.scale.toString())
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Percent",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = "${"%.2f".format(it.level * 100 / it.scale.toFloat())}%")
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Source",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = it.plugged)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Tech",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = it.batteryTech)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Temperature",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = "${"%.1f".format(it.temperature.toFloat() / 10)} \u2103")
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Status",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = it.status)
                                }
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Voltage",
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(text = "${it.voltage} mV")
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Charging",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = it.chargingStatus)
                                    }
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Cycle",
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(text = it.cycleCount.toString())
                                    }
                                }
                            }
                        }
                    }
                }
                Row (
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    OutlinedButton(
                        onClick = {
                            CoroutineScope(Dispatchers.IO).launch {
                                db.batteryEventDao().deleteEvents(*batteryEventList.toTypedArray())
                                withContext(Dispatchers.Main) {
                                    batteryEventList.clear()
                                }
                            }
                        }
                    ) {
                        Text(text = "Clear")
                    }
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun EventsPreview() {
    val context = LocalContext.current
    BatteryToolTheme {
        val db = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "battery-tool-preview"
        ).build()

//        LaunchedEffect(context) {
//            val events = db.batteryEventDao().getAllEvents()
//            if (events.isEmpty()) {
//
//            }
//        }

        Events(db)
    }
}