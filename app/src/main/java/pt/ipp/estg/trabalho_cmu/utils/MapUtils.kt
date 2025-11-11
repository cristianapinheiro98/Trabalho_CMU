package pt.ipp.estg.trabalho_cmu.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

fun openGoogleMaps(context: Context, address: String) {
    val encodedAddress = Uri.encode(address)
    val uri = Uri.parse("geo:0,0?q=$encodedAddress")
    val intent = Intent(Intent.ACTION_VIEW, uri)
    intent.setPackage("com.google.android.apps.maps")

    // if the user doesn't have the google maps installed
    if (intent.resolveActivity(context.packageManager) != null) {
        context.startActivity(intent)
    } else {
        Toast.makeText(context, "Google Maps não está instalado.", Toast.LENGTH_SHORT).show()
    }
}
