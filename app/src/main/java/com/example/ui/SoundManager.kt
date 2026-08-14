package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object SoundManager {
    @Volatile
    private var toneGenerator: ToneGenerator? = null
    var isSoundEnabled = true
    var isMusicEnabled = true
    var isVibrationEnabled = true

    private fun getToneGen(): ToneGenerator? {
        if (toneGenerator == null) {
            synchronized(this) {
                if (toneGenerator == null) {
                    try {
                        toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
                    } catch (e: Exception) {
                        toneGenerator = null
                    }
                }
            }
        }
        return toneGenerator
    }

    private fun playToneSafely(toneType: Int, durationMs: Int) {
        if (!isSoundEnabled) return
        try {
            getToneGen()?.startTone(toneType, durationMs)
        } catch (e: Exception) {
            // Ignored to avoid breaking audio thread
        }
    }

    fun playStrikeSound() {
        playToneSafely(ToneGenerator.TONE_PROP_BEEP, 60)
    }

    fun playBounceSound() {
        playToneSafely(ToneGenerator.TONE_CDMA_PIP, 35)
    }

    fun playPocketSound() {
        playToneSafely(ToneGenerator.TONE_SUP_DIAL, 120)
    }

    fun playFoulSound() {
        playToneSafely(ToneGenerator.TONE_SUP_ERROR, 200)
    }

    fun playWarningTickSound() {
        playToneSafely(ToneGenerator.TONE_PROP_PROMPT, 50)
    }

    fun playTimeoutBuzzerSound() {
        playToneSafely(ToneGenerator.TONE_SUP_ERROR, 300)
    }

    fun playVictorySound() {
        playToneSafely(ToneGenerator.TONE_SUP_CONGESTION, 350)
    }

    fun triggerVibration(context: Context) {
        if (!isVibrationEnabled) return
        try {
            val attributionContext = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                try {
                    context.createAttributionContext("vibrator")
                } catch (e: Exception) {
                    context
                }
            } else {
                context
            }

            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = attributionContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                attributionContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            
            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(50)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun triggerHapticFeedback(haptic: HapticFeedback) {
        if (!isVibrationEnabled) return
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
