package com.jamal2367.styx.browser

import com.jamal2367.styx.view.StyxView
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages the list of open browser tabs (StyxView instances).
 * This is a stub for Phase 2. Full tab management logic is in Phase 3.
 */
@Singleton
class TabsManager @Inject constructor() {

    private val tabs = mutableListOf<StyxView>()

    fun getTabAtPosition(position: Int): StyxView? = tabs.getOrNull(position)

    fun size(): Int = tabs.size

    fun indexOfTab(tab: StyxView): Int = tabs.indexOf(tab)

    fun getCurrentTab(): StyxView? = tabs.firstOrNull()

}
