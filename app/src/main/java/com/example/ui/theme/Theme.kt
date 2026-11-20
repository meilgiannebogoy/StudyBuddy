package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val StudyBuddyLightColorScheme = lightColorScheme(
    primary = PastelBluePrimary,
    onPrimary = PureWhiteSurface,
    primaryContainer = BabyBlueContainer,
    onPrimaryContainer = NavyHeading,
    secondary = SkyBlueAccent,
    onSecondary = PureWhiteSurface,
    secondaryContainer = SoftSkyTint,
    onSecondaryContainer = NavyHeading,
    tertiary = BabyBlueSecondary,
    onTertiary = NavyHeading,
    tertiaryContainer = BabyBlueContainer,
    onTertiaryContainer = NavyHeading,
    background = SoftIceBackground,
    onBackground = NavyHeading,
    surface = PureWhiteSurface,
    onSurface = NavyHeading,
    surfaceVariant = SoftSkyTint,
    onSurfaceVariant = BodySlateGray,
    outline = CardBorderBlue,
    outlineVariant = Color(0xFFE5F0FC),
    error = SoftCoralUrgent,
    onError = PureWhiteSurface,
    errorContainer = SoftCoralContainer,
    onErrorContainer = Color(0xFF7D1F2D)
)

val StudyBuddyShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun StudyBuddyTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = StudyBuddyLightColorScheme,
        typography = StudyBuddyTypography,
        shapes = StudyBuddyShapes,
        content = content
    )
}
