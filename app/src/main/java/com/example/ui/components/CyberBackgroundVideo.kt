package com.example.ui.components

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.ui.theme.BloodRedBackground
import com.example.ui.theme.CyberDarkBg

private const val TAG = "CyberBackgroundVideo"

@OptIn(UnstableApi::class)
@Composable
fun CyberBackgroundVideo(
  videoUrls: List<String>,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var hasCodecError by remember { mutableStateOf(false) }

  // Gracefully initialize ExoPlayer with fallback decoder capabilities and error listener
  val exoPlayer = remember {
    try {
      val renderersFactory = DefaultRenderersFactory(context.applicationContext)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        .setEnableDecoderFallback(true)

      ExoPlayer.Builder(context.applicationContext, renderersFactory).build().apply {
        repeatMode = Player.REPEAT_MODE_ALL
        volume = 0f // muted background video
        playWhenReady = true

        addListener(object : Player.Listener {
          override fun onPlayerError(error: PlaybackException) {
            Log.w(TAG, "ExoPlayer playback error: ${error.errorCodeName} (${error.errorCode}): ${error.message}")
            if (error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED ||
              error.errorCode == PlaybackException.ERROR_CODE_DECODING_FAILED ||
              error.errorCode == PlaybackException.ERROR_CODE_DECODER_QUERY_FAILED ||
              error.errorCodeName.contains("DECODER", ignoreCase = true)
            ) {
              Log.e(TAG, "MediaCodec resource release/error detected. Falling back cleanly.")
              hasCodecError = true
            }
          }
        })
      }
    } catch (e: Throwable) {
      Log.e(TAG, "Failed to instantiate ExoPlayer: ${e.message}", e)
      hasCodecError = true
      null
    }
  }

  // Manage activity lifecycle to pause/resume codec resources and avoid resource starvation
  DisposableEffect(lifecycleOwner, exoPlayer) {
    val observer = LifecycleEventObserver { _, event ->
      exoPlayer?.let { player ->
        try {
          when (event) {
            Lifecycle.Event.ON_RESUME -> {
              if (!hasCodecError) {
                player.playWhenReady = true
              }
            }
            Lifecycle.Event.ON_PAUSE -> {
              player.playWhenReady = false
            }
            Lifecycle.Event.ON_STOP -> {
              player.pause()
            }
            else -> {}
          }
        } catch (e: Exception) {
          Log.w(TAG, "Lifecycle state change notice: ${e.message}")
        }
      }
    }

    lifecycleOwner.lifecycle.addObserver(observer)

    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      try {
        exoPlayer?.stop()
        exoPlayer?.release()
      } catch (e: Exception) {
        Log.w(TAG, "ExoPlayer release notice: ${e.message}")
      }
    }
  }

  // Queue videos safely
  LaunchedEffect(videoUrls, exoPlayer, hasCodecError) {
    if (exoPlayer != null && videoUrls.isNotEmpty() && !hasCodecError) {
      try {
        exoPlayer.clearMediaItems()
        videoUrls.forEach { url ->
          exoPlayer.addMediaItem(MediaItem.fromUri(url))
        }
        exoPlayer.prepare()
        exoPlayer.play()
      } catch (e: Throwable) {
        Log.e(TAG, "ExoPlayer media queue exception: ${e.message}", e)
        hasCodecError = true
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    // Underlying high-aesthetic fallback dark cyber gradient
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors = listOf(
              CyberDarkBg,
              BloodRedBackground,
              CyberDarkBg
            )
          )
        )
    )

    // AndroidView hosting ExoPlayer PlayerView
    if (exoPlayer != null && !hasCodecError) {
      AndroidView(
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            layoutParams = FrameLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.MATCH_PARENT
            )
          }
        },
        modifier = Modifier.fillMaxSize()
      )
    }

    // Ultra-clean transparent vignette: transparent enough so video background is clearly visible behind glassy cards
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(
          Brush.radialGradient(
            colors = listOf(
              Color(0x10000000), // Very light central transparent tint
              Color(0x40080204), // Subtle edge darkening
              Color(0x75050102)  // Soft dark outer boundary
            )
          )
        )
    )

    // Blood-red & Neon-green animated drip overlay
    BloodDripOverlay(modifier = Modifier.fillMaxSize())
  }
}
