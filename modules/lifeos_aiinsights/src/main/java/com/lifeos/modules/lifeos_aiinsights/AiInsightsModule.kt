package com.lifeos.modules.lifeos_aiinsights

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifeos.core.LifeOSModule
import com.lifeos.modules.lifeos_aiinsights.ui.AiInsightsScreen
import com.lifeos.modules.lifeos_aiinsights.ui.AiInsightsViewModel

class AiInsightsModule : LifeOSModule {
    override val id = "aiinsights"
    override val name = "AI Insights"
    override val icon: ImageVector = Icons.Default.AutoAwesome
    override val description = "AI-powered analysis connecting your sleep, habits, nutrition, and finances"
    override val shortDescription = "AI life analysis"
    override val version = "1.0"

    @Composable
    override fun Content(onNavigateBack: () -> Unit, initialId: Long?) {
        val viewModel: AiInsightsViewModel = hiltViewModel()
        AiInsightsScreen(viewModel = viewModel, onNavigateBack = onNavigateBack)
    }
}
