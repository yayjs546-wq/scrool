package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
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
    assertEquals("DreamScroll", appName)
  }

  @Test
  fun `verify accessibility service configuration defaults`() {
    com.example.service.DreamScrollAccessibilityService.setAutoScrollEnabled(true)
    assertEquals(true, com.example.service.DreamScrollAccessibilityService.isAutoScrollEnabled.value)

    com.example.service.DreamScrollAccessibilityService.setScrollDelaySeconds(12)
    assertEquals(12, com.example.service.DreamScrollAccessibilityService.scrollDelaySeconds.value)
    assertEquals(12, com.example.service.DreamScrollAccessibilityService.countdownSeconds.value)

    com.example.service.DreamScrollAccessibilityService.setSkipLivesEnabled(true)
    assertEquals(true, com.example.service.DreamScrollAccessibilityService.skipLivesEnabled.value)
  }
}
