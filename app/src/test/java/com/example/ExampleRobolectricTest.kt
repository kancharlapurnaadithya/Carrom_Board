package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.BoardTheme
import com.example.model.CarromEngine
import com.example.model.StrikerDesign
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Carrom Board", appName)
  }

  @Test
  fun `verify board themes and striker designs exist`() {
    assertEquals(8, BoardTheme.values().size)
    assertEquals(8, StrikerDesign.values().size)

    for (theme in BoardTheme.values()) {
      assertNotNull(theme.displayName)
      assertNotNull(theme.pattern)
    }

    for (design in StrikerDesign.values()) {
      assertNotNull(design.displayName)
      assertNotNull(design.style)
    }
  }

  @Test
  fun `verify carrom engine initialization with custom board and striker`() {
    val engine = CarromEngine(
      boardTheme = BoardTheme.NEON,
      strikerDesign = StrikerDesign.CYBER_PULSAR
    )
    assertEquals(BoardTheme.NEON, engine.boardTheme)
    assertEquals(StrikerDesign.CYBER_PULSAR, engine.strikerDesign)
    assertNotNull(engine.striker)
    assertEquals(19, engine.coins.size)
  }

  @Test
  fun `verify turn time limit and timeout penalty`() {
    val engine = CarromEngine()
    engine.setTurnTimeLimit(10f)
    assertEquals(10f, engine.turnTimeLimitSeconds)
    assertEquals(10f, engine.turnTimeRemaining)
    assertEquals(true, engine.isTimerEnabled)

    // Simulate tick of 7 seconds (remaining = 3s => warning active)
    engine.updatePhysicsTick(7f)
    assertEquals(true, engine.timerWarningActive)

    // Simulate timeout (tick past remaining time)
    engine.updatePhysicsTick(5f)
    assertEquals(true, engine.timeoutOccurredThisTick)
    assertEquals(1, engine.activePlayerIndex) // Turn passed to Player 2
  }

  @Test
  fun `verify striker launch physics with power calculation`() {
    val engine = CarromEngine()
    val powerFraction = 0.8f
    val maxDrag = 200f
    val basePowerFactor = 0.35f
    val powerBoostMultiplier = 1.5f
    val clampedDistance = powerFraction * maxDrag

    // Calculate launch vector as done in onEnd physics
    val finalVx = clampedDistance * basePowerFactor * powerBoostMultiplier
    val finalVy = 0f

    engine.launchStriker(finalVx, finalVy)
    assertEquals(true, engine.isMoving)
    assertEquals(false, engine.isStrikerPlaced)
    assertEquals(finalVx, engine.striker.vx, 0.001f)
    assertEquals(0f, engine.striker.vy, 0.001f)
  }
}

