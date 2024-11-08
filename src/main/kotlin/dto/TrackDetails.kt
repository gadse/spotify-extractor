package dto

import kotlinx.serialization.Serializable

@Serializable
data class TrackDetails(
    var href: String,
    var total: Integer
)