package danmusic.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.items
import coil.compose.AsyncImage
import danmusic.app.data.Playlist
import danmusic.app.ui.theme.DanmusicTheme
import danmusic.app.viewmodel.DanViewModel
import org.koin.androidx.compose.getViewModel

@Composable
internal fun PlaylistsScreen() {
  val vm: DanViewModel = getViewModel()
  val playlists = vm.playlists.collectAsLazyPagingItems()
  Box(modifier = Modifier.fillMaxSize()) {
    Playlists(playlists = playlists)
    if (playlists.loadState.refresh is LoadState.Loading) {
      InitLoadingProgress()
    }
    if (playlists.loadState.refresh is LoadState.Error) {
      RetryButton(Modifier.align(Alignment.Center)) {
        playlists.retry()
      }
    }
  }
}

@Composable
private fun RetryButton(
  modifier: Modifier = Modifier,
  onClick: () -> Unit,
) {
  Button(
    onClick = onClick,
    modifier = modifier
  ) {
    Text(
      text = "Loading failed, tap to retry",
    )
  }
}

@Preview
@Composable
private fun PreviewRetryButton() {
  DanmusicTheme {
    RetryButton(onClick = {})
  }
}

@Composable
private fun InitLoadingProgress() {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color.White),
    contentAlignment = Alignment.Center,
  ) {
    CircularProgressIndicator()
  }
}

@Composable
private fun Playlists(playlists: LazyPagingItems<Playlist>) {
  LazyColumn(
    verticalArrangement = Arrangement.spacedBy(8.dp),
    contentPadding = PaddingValues(16.dp, 8.dp)
  ) {
    items(playlists) {
      it?.let { playlist ->
        PlayListItem(playlist = playlist)
      }
    }
  }
}

@Composable
private fun PlayListItem(playlist: Playlist) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    AsyncImage(
      modifier = Modifier
        .size(120.dp)
        .clip(MaterialTheme.shapes.medium)
        .background(Color.LightGray),
      model = playlist.coverImgUrl, contentDescription = null
    )

    Column {
      Text(
        text = playlist.name,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = playlist.description,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
      )
    }
  }
}

@Preview
@Composable
private fun PreviewPlaylistItem() {
  DanmusicTheme {
    val playlist = Playlist(
      id = 1,
      name = "Playlist 1",
      description = "Description 1",
      coverImgUrl = "https://picsum.photos/200/300",
      updateTime = 0
    )
    PlayListItem(playlist)
  }
}