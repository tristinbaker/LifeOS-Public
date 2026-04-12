package com.lifeos.core

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

interface LifeOSModule {
    val id: String
    val name: String
    val icon: ImageVector
    val description: String
    val version: String

    @Composable
    fun Content(
        onNavigateBack: () -> Unit,
        initialNoteId: Long?
    )
}

object ModuleRegistry {
    private val _modules = mutableListOf<LifeOSModule>()
    val modules: List<LifeOSModule> = _modules

    fun register(module: LifeOSModule) {
        if (_modules.none { it.id == module.id }) {
            _modules.add(module)
        }
    }

    fun getModule(id: String): LifeOSModule? = _modules.find { it.id == id }
}
