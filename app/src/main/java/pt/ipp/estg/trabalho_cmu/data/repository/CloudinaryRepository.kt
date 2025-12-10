package pt.ipp.estg.trabalho_cmu.data.repository

import android.content.Context
import android.net.Uri
import com.cloudinary.utils.ObjectUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pt.ipp.estg.trabalho_cmu.providers.CloudinaryModule
import java.util.UUID

/**
 * Repository responsible for uploading images to Cloudinary.
 *
 * This object provides a single suspend function that:
 *  - Reads an image from a given URI
 *  - Uploads its byte content to Cloudinary
 *  - Returns the secure (HTTPS) URL of the uploaded file
 *
 * The upload runs on a background thread via Dispatchers.IO.
 * In case of failure, the function prints the stack trace and returns null.
 *
 * Notes:
 * - Images are stored under the "animals" folder on Cloudinary
 * - A random UUID is generated for each uploaded image
 */
object CloudinaryRepository {

    /**
     * Uploads an image file referenced by a URI to Cloudinary.
     *
     * @param context Android context used to access the content resolver.
     * @param imageUri The URI of the image to upload.
     *
     * @return The secure URL of the uploaded image, or null if:
     *   - the file could not be opened,
     *   - the upload failed,
     *   - an exception occurred.
     */
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
