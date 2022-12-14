package heartmusic.data.source

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import heartmusic.data.PlaylistQuerySong
import heartmusic.data.PlaylistSong
import heartmusic.data.source.db.HeartPlaylistDb
import heartmusic.data.source.remote.HeartRemoteDataSource
import timber.log.Timber
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PlaylistSongsRemoteMediator(
  private val query: Long,
  private val db: HeartPlaylistDb,
  private val remote: HeartRemoteDataSource,
) : RemoteMediator<Int, PlaylistQuerySong>() {
  private val songsDao = db.playlistSongs()
  private val cacheTimeDao = db.cacheTime()

  init {
    Timber.tag("PlaylistSongsMediator")
  }

  override suspend fun initialize(): InitializeAction {
    val cacheTimeout = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS)
    val lastUpdated = db.withTransaction {
      cacheTimeDao.lastPlaylistSongsUpdateTime(query)
    }
    return if (System.currentTimeMillis() - lastUpdated > cacheTimeout) {
      InitializeAction.LAUNCH_INITIAL_REFRESH
    } else {
      Timber.i("Skip initial refresh.")
      InitializeAction.SKIP_INITIAL_REFRESH
    }
  }

  override suspend fun load(
    loadType: LoadType,
    state: PagingState<Int, PlaylistQuerySong>
  ): MediatorResult {
    return try {
      val offset = when (loadType) {
        LoadType.REFRESH -> 0
        LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
        LoadType.APPEND -> {
          songsDao.getNextOffset(query)
            ?: return MediatorResult.Success(endOfPaginationReached = true)
        }
      }
      val songs = remote.getSongsOfPlaylist(
        id = query,
        size = when (loadType) {
          LoadType.REFRESH -> state.config.initialLoadSize
          else -> state.config.pageSize
        },
        offset = offset
      ).songs

      Timber.d("type: $loadType, size: ${songs.size}, offset: $offset")

      db.withTransaction {
        if (loadType == LoadType.REFRESH) {
          songsDao.deleteByPlaylistId(query)
          cacheTimeDao.updatePlaylistSongsUpdateTime(query)
        }

        songsDao.insertSongs(songs)
        songs.mapIndexed { i, it ->
          PlaylistSong(
            playlistId = query,
            songId = it.id,
            offset = offset + i
          )
        }.also { playlistSongs ->
          songsDao.insertPlaylistSongs(playlistSongs)
        }
      }
      MediatorResult.Success(endOfPaginationReached = songs.isEmpty())
    } catch (e: Exception) {
      Timber.e(e, "load failed")
      MediatorResult.Error(e)
    }
  }
}
