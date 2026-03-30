package com.jamal2367.styx.browser

import com.jamal2367.styx.AppTheme
import com.jamal2367.styx.ThemedActivity

abstract class BrowserActivity : ThemedActivity() {
    override fun themeStyle(aTheme: AppTheme): Int = 0
}
