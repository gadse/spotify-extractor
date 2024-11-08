package dto

import kotlinx.serialization.Serializable

@Serializable
data class SimplifiedPlaylist(
    var collaborative: Boolean,
    var description: String,
    // external_urls
    var href: String,
    var id: String,
    // images
    // owner
    var public: Boolean,
    var snapshot_id: String,
    var tracks: TrackDetails,
    var type: String,
    var uri: String
)