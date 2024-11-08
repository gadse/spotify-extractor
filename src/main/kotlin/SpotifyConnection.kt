import dto.AuthResponse
import dto.PlaylistsResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import model.Playlist
import java.io.Closeable
import java.security.MessageDigest
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi


class SpotifyConnection (
    val properties: Properties
) : Closeable {

    private val CLIENT_ID = properties.get("CLIENT_ID")
    private val CLIENT_SECRET = properties.get("CLIENT_SECRET")
    private val AUTH_URL = properties.get("auth.url")

    private val MY_PLAYLISTS_URL = "https://api.spotify.com/v1/me/playlists"

    private var AUTH_TOKEN: String = ""

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
     * Calls the appropriate endpoint on the spotify API to obtain a bearer token of limited lifetime.
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
            "client_secret" to CLIENT_SECRET
        )
        val body = data.entries
            .map { entry -> entry.key + "=" + entry.value }
            .joinToString("&")

        val response = do_post_pall(AUTH_URL, headers, body)

        if (response.status.value in 200..299) {
            AUTH_TOKEN = response.body<AuthResponse>().access_token
            return AUTH_TOKEN
        } else {
            throw IOException(
                "Error during authorization!\n"
                + response.status.toString()
            )
        }
    }

//    suspend fun obtain_authorization_code(): String {
//
//    }

    suspend fun obtain_playlists(): String {
        val auth_headers = mapOf(
            HttpHeaders.Authorization to "Bearer $AUTH_TOKEN"
        )

        val data = mapOf(
            "grant_type" to "client_credentials",
            "client_id" to CLIENT_ID,
            "client_secret" to CLIENT_SECRET,
        )
        val body = data.entries
            .map { entry -> entry.key + "=" + entry.value }
            .joinToString("&")

        val playlistResponse = do_get_call("$MY_PLAYLISTS_URL?limit=50", auth_headers)

        var playlistInfo: PlaylistsResponse
        if (playlistResponse.status.value in 200 .. 299) {
            playlistInfo = playlistResponse.body<PlaylistsResponse>()
        } else {
            throw IOException(
                "Error during playlist retrieval!\n"
                        + playlistResponse.status.toString()
            )
        }

        var all_playlists = emptyList<Playlist>()
        for (playlist in playlistInfo.items) {
            var trackDetailsResponse = do_get_call(playlist.tracks.href, auth_headers)

        }
        return "foo"
    }

    private suspend fun do_get_call(
        url: String,
        headers: Map<String, String>
    ): HttpResponse {
        val response: HttpResponse = client.get(url) {
            headers {
                for (entry in headers) {
                    append(entry.key, entry.value)
                }
            }
        }
        return response
    }

    private suspend fun do_post_pall(
        url: String,
        headers: Map<String, String>,
        body: String
    ): HttpResponse {
        val response: HttpResponse = client.post(url) {
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

    fun generate_code_challenge() : String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9') + '_' + '-' + '~' + '.'
        return (1..128)
            .map { allowedChars.random() }
            .joinToString("")
    }

    /**
     * Taken from https://gist.github.com/lovubuntu/164b6b9021f5ba54cefc67f60f7a1a25
     */
    fun hash(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun encode_base64(data: String) : String {
        return Base64.encode(data.toByteArray())
    }

}



