package pt.ipp.estg.trabalho_cmu.providers

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils

/**
 * Provides a configured Cloudinary instance for image uploads.
 *
 * The module initializes the Cloudinary client using the project's
 * cloud name, API key, and API secret.
 *
 * This object acts as a singleton provider and does not produce
 * UI-visible messages or require any error handling at this level.
 */

object CloudinaryModule {

    private const val CLOUD_NAME = "dykreewmy"
    private const val API_KEY = "153925541834969"
    private const val API_SECRET = "cTi0sHhqjbYp4dqI9weiWg_ZL6c"

    val cloudinary = Cloudinary(
        ObjectUtils.asMap(
            "cloud_name", CLOUD_NAME,
            "api_key", API_KEY,
            "api_secret", API_SECRET
        )
    )
}
