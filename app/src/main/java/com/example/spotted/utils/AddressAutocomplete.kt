import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

@Serializable
data class OSMPlace(
    @SerialName("place_id") val placeId: Long,
    @SerialName("display_name") val displayName: String,
    val lat: String,
    val lon: String
)

suspend fun searchAddressOSM(query: String): List<OSMPlace> {
    if (query.isBlank() || query.length < 3) return emptyList()

    return withContext(Dispatchers.IO) {
        try {
            // Codifica la stringa di ricerca per l'URL (es: spazi in %20)
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=5&addressdetails=1"

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection

            // FONDAMENTALE per le policy di OpenStreetMap: identifica la tua app
            connection.setRequestProperty("User-Agent", "SpottedApp/1.0 (unibo.studenti.app)")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = Json { ignoreUnknownKeys = true }
                json.decodeFromString<List<OSMPlace>>(responseText)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}