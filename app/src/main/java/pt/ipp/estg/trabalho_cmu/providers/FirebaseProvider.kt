package pt.ipp.estg.trabalho_cmu.providers

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Provides singleton instances of Firebase Authentication and Firestore.
 *
 * These instances are lazily initialized and reused across the entire app.
 *
 * This provider does not display UI errors nor handle exceptions directly.
 */
object FirebaseProvider {
    val auth: FirebaseAuth by lazy<FirebaseAuth> { FirebaseAuth.getInstance() }
    val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
}