package pt.ipp.estg.trabalho_cmu.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import pt.ipp.estg.trabalho_cmu.MainActivity

/**
 * BroadcastReceiver to handle stop walk action from notification.
 *
 * When the user taps the "Stop Walk" button in the ongoing walk notification,
 * this receiver is triggered. It closes the notification panel and opens
 * the MainActivity with flags to navigate to the walk screen and show
 * the stop confirmation dialog.
 */
class StopWalkReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STOP_WALK = "pt.ipp.estg.trabalho_cmu.STOP_WALK"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_STOP_WALK) {

            // Open MainActivity with flags to navigate to WalkScreen and show stop dialog
            val openIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP or
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
                putExtra("navigate_to_walk", true)
                putExtra("stop_walk_requested", true)
            }
            context.startActivity(openIntent)
        }
    }
}