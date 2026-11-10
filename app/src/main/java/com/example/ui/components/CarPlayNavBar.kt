package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CockpitBackground
import com.example.ui.theme.CockpitCardBorder
import com.example.ui.theme.HighContrastWhite

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@Composable
fun CarPlayNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val navItems = listOf(
        NavItem("Inicio", Icons.Default.Home, "nav_inicio"),
        NavItem("Viajes", Icons.Default.Navigation, "nav_viajes"),
        NavItem("Taller", Icons.Default.Build, "nav_taller"),
        NavItem("Gasolina", Icons.Default.LocalGasStation, "nav_gasolina"),
        NavItem("Datos", Icons.Default.Tune, "nav_datos")
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CockpitBackground)
            .border(width = 0.5.dp, color = CockpitCardBorder)
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("carplay_bottom_nav_bar")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = selectedTab == index
                val itemColor = if (isSelected) accentColor else Color.Gray

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onTabSelected(index) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag(item.testTag)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.title,
                        tint = itemColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.title,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = itemColor
                    )
                }
            }
        }
    }
}
