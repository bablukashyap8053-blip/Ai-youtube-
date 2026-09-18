package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.UserProfileStore
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
    assertEquals("AI Studio", appName)
  }

  @Test
  fun `verify user profile initialization`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val profileStore = UserProfileStore(context)
    val profile = profileStore.profile.value
    assertNotNull(profile)
    assertEquals("Bablu Kashyap", profile.name)
    assertEquals("bablukashyap8053@gmail.com", profile.email)
  }
}
