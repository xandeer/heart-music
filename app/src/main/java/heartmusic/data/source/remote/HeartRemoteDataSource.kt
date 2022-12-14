package heartmusic.data.source.remote

import heartmusic.data.PlaylistSongsResponse
import heartmusic.data.PlaylistsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface HeartRemoteDataSource {
  @GET("top/playlist/highquality")
  suspend fun getTopPlaylists(
    @Query("limit") size: Int = 1,
    @Query("before") anchor: Long = 0
  ): PlaylistsResponse

  @GET("playlist/track/all")
  suspend fun getSongsOfPlaylist(
    @Query("id") id: Long,
    @Query("limit") size: Int = 1,
    @Query("offset") offset: Int = 0
  ): PlaylistSongsResponse
}
