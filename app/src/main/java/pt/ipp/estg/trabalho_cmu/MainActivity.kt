package pt.ipp.estg.trabalho_cmu

import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
//import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.tasks.await
import pt.ipp.estg.trabalho_cmu.preferences.LanguagePreferences
import pt.ipp.estg.trabalho_cmu.utils.LocaleHelper

//@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        val lang = LanguagePreferences.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val windowSize = calculateWindowSizeClass(this)

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PetAdoptionApp(
                        windowSize = windowSize.widthSizeClass
                    )
                }
            }
        }
    }
}