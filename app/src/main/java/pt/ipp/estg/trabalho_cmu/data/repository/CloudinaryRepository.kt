package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.providers.CloudinaryModule
import java.util.UUID

object CloudinaryRepository {

    suspend fun uploadImageToFirebase(context: Context, imageUri: Uri): String? {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(imageUri)
                    ?: return@withContext null

                val bytes = inputStream.readBytes()

                val result = CloudinaryModule.cloudinary.uploader().upload(
                    bytes,
                    ObjectUtils.asMap(
                        "public_id", "animals/${UUID.randomUUID()}",
                        "folder", "animals"
                    )
                )

                result["secure_url"] as? String
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}
