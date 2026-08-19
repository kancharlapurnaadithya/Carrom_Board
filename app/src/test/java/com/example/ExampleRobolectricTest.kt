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
      boardTheme = BoardTheme.NEON_CYBERPUNK,
      strikerDesign = StrikerDesign.CYBER_VORTEX_NEON
    )
    assertEquals(BoardTheme.NEON_CYBERPUNK, engine.boardTheme)
    assertEquals(StrikerDesign.CYBER_VORTEX_NEON, engine.strikerDesign)
    assertNotNull(engine.striker)
    assertEquals(19, engine.coins.size)
  }
}

