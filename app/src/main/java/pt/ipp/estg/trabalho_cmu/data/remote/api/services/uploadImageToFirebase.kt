package pt.ipp.estg.trabalho_cmu.data.remote.api.services

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

fun uploadImageToFirebase(
    uri: Uri,
    onStart: () -> Unit = {},
    onSuccess: (String) -> Unit,
    onError: (Exception) -> Unit = {},
    onComplete: () -> Unit = {}
) {
    val storage = FirebaseStorage.getInstance().reference
    val fileRef = storage.child("animals/${UUID.randomUUID()}.jpg")

    onStart()

    fileRef.putFile(uri)
        .continueWithTask { fileRef.downloadUrl }
        .addOnSuccessListener { downloadUrl ->
            onSuccess(downloadUrl.toString())
            onComplete()
        }
        .addOnFailureListener { e ->
            onError(e)
            onComplete()
        }
}
