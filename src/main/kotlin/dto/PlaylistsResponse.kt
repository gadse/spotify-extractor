package dto

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistsResponse (
    val href: String,
    val limit: Integer,
    val offset: Integer,
    val next: String,
    val total: Integer,
    val items: Array<SimplifiedPlaylist>
)