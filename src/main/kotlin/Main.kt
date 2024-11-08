suspend fun main(args: Array<String>) {
    // Try adding program arguments via Run/Debug configuration.
    // Learn more about running applications: https://www.jetbrains.com/help/idea/running-applications.html.
    println("Program arguments: ${args.joinToString()}")

    SpotifyConnection(Properties()).use {
        connection ->
            println(connection.obtain_token())
            println(connection.obtain_playlists())

    }
    //val favourites = spotify.fetchFavourites()
    //val playlists = spotify.fetchPlaylist()

    //println("Favourites: ${favourites}")
    //println("model.Playlist: ${playlists}")
}