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
    private var toneGenerator: ToneGenerator? = null
    var isSoundEnabled = true
    var isMusicEnabled = true
    var isVibrationEnabled = true

    init {
        try {
            // Priority normal streams
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun playStrikeSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 80)
            } catch (e: Exception) {
                // fallbacks
            }
        }
    }

    fun playBounceSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 40)
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun playPocketSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_DIAL, 150)
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun playFoulSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 220)
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun playWarningTickSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 60)
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun playTimeoutBuzzerSound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_ERROR, 350)
            } catch (e: Exception) {
                // fallback
            }
        }
    }

    fun playVictorySound() {
        if (!isSoundEnabled) return
        CoroutineScope(Dispatchers.IO).launch {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_SUP_CONGESTION, 400)
            } catch (e: Exception) {
                // fallback
            }
        }
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
