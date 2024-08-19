package com.nikkiyodo.batterytool

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import com.nikkiyodo.batterytool.data.BatteryEvent
import com.nikkiyodo.batterytool.ui.theme.BatteryToolTheme

@Composable
fun Status() {
    var batteryEvent by remember { mutableStateOf<BatteryEvent?>(null) }

    val context = LocalContext.current

    DisposableEffect(context) {
        val batteryChangedFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)

        val batteryChangedReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                batteryEvent = Util.getBatteryEventFrom(intent)
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
        LazyVerticalGrid (
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            batteryEvent?.let {
                if (it.batteryIconResId > 0) {
                    context.getDrawable(it.batteryIconResId)?.toBitmap()?.let {
                        item {
                            Column {
                                Text(
                                    text = "Battery icon",
                                    fontWeight = FontWeight.Bold
                                )
                                Image(
                                    bitmap = it.asImageBitmap(),
                                    contentDescription = stringResource(id = R.string.battery_icon)
                                )
                            }
                        }
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    item {
                        Column {
                            Text(
                                text = "Battery low",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = it.batteryLow.toString())
                        }
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Battery health",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.batteryHealth)
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Level",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.level.toString())
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Scale",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.scale.toString())
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Percent",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "${"%.2f".format(it.level * 100 / it.scale.toFloat())}%")
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Power source",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.plugged)
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Battery tech",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.batteryTech)
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Temperature",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "${"%.1f".format(it.temperature.toFloat() / 10)} \u2103")
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Status",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = it.status)
                    }
                }
                item {
                    Column {
                        Text(
                            text = "Voltage",
                            fontWeight = FontWeight.Bold
                        )
                        Text(text = "${it.voltage} mV")
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    item {
                        Column {
                            Text(
                                text = "Charging status",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = it.chargingStatus)
                        }
                    }
                    item {
                        Column {
                            Text(
                                text = "Cycle count",
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = it.cycleCount.toString())
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusPreview() {
    BatteryToolTheme {
        Status()
    }
}