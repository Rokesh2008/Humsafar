package com.humsafar.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import java.net.NetworkInterface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun EventMode(vm: HumsafarViewModel) {
    val address = remember { localAddress() }
    val url = "http://$address:8080"
    val qr = remember(url) { runCatching { MultiFormatWriter().encode(url, BarcodeFormat.QR_CODE, 520, 520) }.getOrNull() }
    Column(Modifier.padding(20.dp)) {
        Text("EVENT MODE", fontSize = 10.sp, color = Color(0xFF6F8584))
        Text("Open the crowd channel.", fontSize = 28.sp, color = Color(0xFF09292B))
        Spacer(Modifier.height(18.dp))
        Text("Guests on the same hotspot can submit reports from any browser — no app or internet required.", fontSize = 14.sp, color = Color(0xFF6F8584))
        Spacer(Modifier.height(18.dp))
        Button(onClick = vm::toggleServer, modifier = Modifier.fillMaxWidth()) {
            Text(if (vm.serverRunning.value) "STOP LOCAL REPORT PAGE" else "START LOCAL REPORT PAGE")
        }
        Spacer(Modifier.height(12.dp))
        Text(if (vm.serverRunning.value) "Serving on port 8080. Open http://<phone-ip>:8080 from the same hotspot." else "Server is stopped.", fontSize = 12.sp, color = Color(0xFF6F8584))
        if (qr != null) {
            Spacer(Modifier.height(16.dp))
            Image(qrBitmap(qr).asImageBitmap(), "Report page QR code", Modifier.size(180.dp))
            Text(url, fontSize = 12.sp, color = Color(0xFF6F8584))
        }
        Text(vm.notice.value, fontSize = 12.sp, color = Color(0xFFED755F), modifier = Modifier.padding(top = 14.dp))
    }
}

private fun localAddress(): String = runCatching {
    NetworkInterface.getNetworkInterfaces().toList().asSequence().flatMap { it.inetAddresses.toList().asSequence() }
        .first { !it.isLoopbackAddress && it.hostAddress?.contains(':') == false }.hostAddress
}.getOrDefault("192.168.43.1")

private fun qrBitmap(matrix: com.google.zxing.common.BitMatrix): Bitmap {
    val bitmap = Bitmap.createBitmap(matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
    for (x in 0 until matrix.width) for (y in 0 until matrix.height) bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
    return bitmap
}
