import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import java.io.Closeable

class SpotifyConnection (
    val properties: Properties
) : Closeable {

    private val CLIENT_ID = properties.get("CLIENT_ID")
    private val CLIENT_SECRET = properties.get("CLIENT_SECRET")
    private val AUTH_URL = properties.get("auth.url")

    private val client: HttpClient
        get() {
            val client = HttpClient(CIO) {
                install(ContentNegotiation) {
                    json() // Example: Register JSON content transformation
                    // Add more transformations as needed for other content types
                }
            }
            return client
        }

    /**
     * Calls the apropriate endpoint on the spotify API to obtain a bearer token of limited lifetime.
     * This token can then be used to call the rest of the API.
     *
     * Examples:
     *      curl -X POST "https://accounts.spotify.com/api/token" \
     *          -H "Content-Type: application/x-www-form-urlencoded" \
     *          -d "grant_type=client_credentials&client_id=your-client-id&client_secret=your-client-secret"
     *
     *      curl "https://api.spotify.com/v1/artists/4Z8W4fKeB5YxbusRsdQVPb" \
     *          -H "Authorization: Bearer  BQDBKJ5eo5jxbtpWjVOj7ryS84khybFpP_lTqzV7uV-T_m0cTfwvdn5BnBSKPxKgEb11"
     */
    suspend fun obtain_token(): String {
        val headers = mapOf(
            HttpHeaders.ContentType to "application/x-www-form-urlencoded"
        )

        val data = mapOf(
            "grant_type" to "client_credentials",
            "client_id" to CLIENT_ID,
            "client_secret" to CLIENT_SECRET,
        )
        val body = data.entries
            .map { entry -> entry.key + "=" + entry.value }
            .joinToString("&")

        val response: HttpResponse = doPostCall(headers, body)

        if (response.status.value in 200..299) {
            return response.body<AuthResponse>().access_token
        } else {
            throw IOException(
                "Error during authorization!\n"
                + response.status.toString()
            )
        }
    }

    private suspend fun doPostCall(
        headers: Map<String, String>,
        body: String
    ): HttpResponse {
        val response: HttpResponse = client.post(AUTH_URL) {
            headers {
                for (entry in headers) {
                    append(entry.key, entry.value)
                }
            }
            setBody(body)
        }
        return response
    }

    override fun close() {
        println("Connection closed")
        client.close()
    }

}



