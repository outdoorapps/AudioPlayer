package com.common.bting.audioplayer

import android.graphics.Color
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.support.design.widget.CoordinatorLayout
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.*
import java.io.IOException


/**
 * Created by bting on 1/30/17.
 *
 * Usage:
 * - Use only one AudioPlayer object per fragment (Re-initialize it will cause interfaces to
 *   overlap)
 *
 * - Call the setAudioUri(Uri) method whenever an audio file needs to be play/needs to be
 *   changed, the player interface will appear
 *
 * - Closing the player with the close button or back button will release the resources and hide the
 *   player but you can do it manually by calling release()
 *
 * @param view     The view to find a parent from (similar to that of a Snackbar),
 *                 the player interface will attach to the bottom of the parent
 *
 */
class AudioPlayer(view: View) {

    private val context = view.context
    private val progressBar: ProgressBar
    private val parentViewGroup: ViewGroup
    private val audioPlayerLayout: AudioPlayerLayout

    private var audioMediaPlayer: AudioMediaPlayer? = null
    private val mediaController: MediaController

    private val MEDIA_CONTROLLER_BACKGROUND = 0xAA000000

    init {
        parentViewGroup = findSuitableParent(view) ?: throw IllegalArgumentException("AudioPlayer must have non-null parent")

        val inflater = LayoutInflater.from(parentViewGroup.context)
        audioPlayerLayout = inflater.inflate(R.layout.audio_player, parentViewGroup, false) as AudioPlayerLayout
        audioPlayerLayout.setBackgroundColor(MEDIA_CONTROLLER_BACKGROUND.toInt())

        audioPlayerLayout.audioBarFrameListener = object : AudioPlayerLayout.AudioBarFrameListener {
            override fun visibilityChanged(visibility: Int) {
                if (visibility == View.INVISIBLE) {
                    audioMediaPlayer?.pause()
                }
            }

            override fun onBackPressed() {
                release()
            }
        }

        progressBar = audioPlayerLayout.find(R.id.audio_view_progress_bar)

        mediaController = object : MediaController(context, false) {
            override fun hide() {
                if (audioMediaPlayer == null) {
                    super.hide()
                } else {
                    show(0)
                }
            }

            override fun setAnchorView(view: View) {
                super.setAnchorView(view)

                // Change media controller background color
                val mediaControllerRoot = getChildAt(0) as LinearLayout
                mediaControllerRoot.setBackgroundColor(Color.TRANSPARENT)

                // Change Time text color
                val progressLayout = mediaControllerRoot.getChildAt(1) as LinearLayout

                val currentPositionText = progressLayout.getChildAt(0) as TextView
                currentPositionText.setTextColor(Color.WHITE)

                val durationText = progressLayout.getChildAt(2) as TextView
                durationText.setTextColor(Color.WHITE)

                // Add a close button
                val closeButton = ImageView(context)
                closeButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_clear_white_24dp))

                val params = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                params.marginStart = (6 * context.resources.displayMetrics.density).toInt() // 6 dp
                params.gravity = Gravity.START
                addView(closeButton, params)

                closeButton.setOnClickListener { release() }
            }

            override fun dispatchKeyEvent(event: KeyEvent): Boolean {
                // onBackPressed
                if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                    release()
                    return true
                }
                return super.dispatchKeyEvent(event)
            }
        }

        mediaController.setAnchorView(audioPlayerLayout)
    }

    private fun removePlayer() {
        audioMediaPlayer?.stop()
        audioMediaPlayer?.release()
        audioMediaPlayer = null
        if (mediaController.isShowing) {
            mediaController.hide()
        }
    }

    fun setAudioURI(uri: Uri) {
        removePlayer()
        if (audioPlayerLayout.parent == null) {
            parentViewGroup.addView(audioPlayerLayout)
        }

        try {
            audioMediaPlayer = AudioMediaPlayer()
            audioMediaPlayer?.setAudioStreamType(AudioManager.STREAM_MUSIC)
            audioMediaPlayer?.setDataSource(context, uri)
            audioMediaPlayer?.prepareAsync()

            audioPlayerLayout.requestFocus()

            mediaController.setMediaPlayer(audioMediaPlayer)

            progressBar.visibility = View.VISIBLE

            audioMediaPlayer?.setOnPreparedListener {
                // Set audioMediaPlayer = null cancels this callback
                progressBar.visibility = View.GONE
                mediaController.show(0)
            }
        } catch (e: IOException) {
            Toast.makeText(context, "Cannot open audio file", Toast.LENGTH_SHORT).show()
        }
    }

    fun release() {
        removePlayer()
        parentViewGroup.removeView(audioPlayerLayout)
    }

    /**
     * Copied from Snackbar
     */
    private fun findSuitableParent(entryView: View?): ViewGroup? {
        var view = entryView
        var fallback: ViewGroup? = null
        do {
            if (view is CoordinatorLayout) {
                // We've found a CoordinatorLayout, use it
                return view
            } else if (view is FrameLayout) {
                if (view.id == android.R.id.content) {
                    // If we've hit the decor content view, then we didn't find a CoL in the
                    // hierarchy, so use it.
                    return view
                } else {
                    // It's not the content view but we'll use it as our fallback
                    fallback = view as ViewGroup?
                }
            }

            if (view != null) {
                // Else, we will loop and crawl up the view hierarchy and try to find a parent
                val parent = view.parent
                view = if (parent is View) parent else null
            }
        } while (view != null)

        // If we reach here then we didn't find a CoL or a suitable content view so we'll fallback
        return fallback
    }

    class AudioMediaPlayer : MediaPlayer(), MediaController.MediaPlayerControl {

        override fun getBufferPercentage(): Int {
            return 0
        }

        override fun canPause(): Boolean {
            return true
        }

        override fun canSeekBackward(): Boolean {
            return true
        }

        override fun canSeekForward(): Boolean {
            return true
        }
    }
}
