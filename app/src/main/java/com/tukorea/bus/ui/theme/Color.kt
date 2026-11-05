package com.tukorea.bus.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * TU Shuttle 디자인 토큰 - 색상 (기사 앱과 동일한 팔레트)
 * 새 화면에서는 이 값을 직접 쓰지 말고 MaterialTheme.colorScheme / MaterialTheme.busColors 를 쓴다.
 * 아래 이름들 중 기존 화면이 쓰던 것(Blue600, Green100, Gray50 ...)은 이름을 유지하고 값만 새 팔레트로 바꿨다.
 */

// Brand: 기본은 검정/흰색(우버 스타일). 파랑은 지도 위치 점·링크 같은 제한된 곳에만 쓴다.
val BrandPrimary = Color(0xFF000000)
val Blue500 = Color(0xFF3182F6)
val Blue600 = Color(0xFF1B64DA)

// 기존 화면이 쓰던 파랑 계열 이름. 모노크롬 디자인에 맞춰 중립 톤으로 바꿨다(이름만 레거시).
val Blue50 = Color(0xFFF2F4F6)
val Blue100 = Color(0xFFE5E8EB)
val Blue300 = Color(0xFFB0B8C1)
val Blue400 = Color(0xFF6B7684)
val Blue700 = Color(0xFF191F28)
val Blue900 = Color(0xFF000000)

// Neutral
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Gray50 = Color(0xFFF9FAFB)
val Gray100 = Color(0xFFF2F4F6)
val Gray200 = Color(0xFFE5E8EB)
val Gray300 = Color(0xFFD1D6DB)
val Gray400 = Color(0xFFB0B8C1)
val Gray500 = Color(0xFF8B95A1)
val Gray600 = Color(0xFF6B7684)
val Gray700 = Color(0xFF4E5968)
val Gray800 = Color(0xFF333D4B)
val Gray900 = Color(0xFF191F28)

// Status
val Green500 = Color(0xFF03B26C)
val Green600 = Green500
val Green700 = Color(0xFF028A55)
val Green900 = Color(0xFF04583A)
val Green100 = Color(0xFFC8EEDD)
val Green50 = Color(0xFFE6F7EF)
val Orange500 = Color(0xFFFE9800)
val Orange600 = Orange500
val Orange700 = Color(0xFFB76400)
val Orange100 = Color(0xFFFFE2B0)
val Orange50 = Color(0xFFFFF3DF)
val Red500 = Color(0xFFF04452)
val Red100 = Color(0xFFFFD4D8)
val Red700 = Color(0xFFC62C39)
val Red50 = Color(0xFFFEEBEC)
val Yellow700 = Color(0xFFA16207)
val Yellow100 = Color(0xFFFEF9C3)

// Dark surfaces
val DarkBackground = Color(0xFF17171C)
val DarkSurface = Color(0xFF202027)
val DarkSurfaceHigh = Color(0xFF2C2C35)
val DarkOutline = Color(0xFF3C3C47)
val DarkGreen50 = Color(0xFF12302A)
val DarkOrange50 = Color(0xFF3A2E17)
val DarkRed50 = Color(0xFF3A1D22)
val DarkBlue50 = Color(0xFF1B2F4D)

// Gradients (기존 화면용)
val GradientMapFrom = Color(0xFFE5E8EB)
val GradientMapVia = Color(0xFFE8F3FF)
val GradientMapTo = Color(0xFFF2F4F6)
val GradientReserveFrom = Gray900
val GradientReserveTo = Black

// Opacity (기존 화면용)
val WhiteOpacity90 = Color(0xE6FFFFFF)
val WhiteOpacity30 = Color(0x4DFFFFFF)
val WhiteOpacity20 = Color(0x33FFFFFF)
val WhiteOpacity10 = Color(0x1AFFFFFF)
val BgWhite95 = Color(0xF2FFFFFF)
val BgWhite20 = Color(0x33FFFFFF)

// Semantic (기존 화면용)
val PrimaryBlue = BrandPrimary
val SecondaryBlue = Blue400
val TertiaryBlue = Blue300
val BackgroundLight = Gray100
val SurfaceLight = White
val SurfaceVariantColor = Gray100
val OnPrimary = White
val OnSecondary = White
val OnBackground = Gray900
val OnSurface = Gray900
val OnSurfaceVariant = Gray600
val SuccessGreen = Green500
val WarningOrange = Orange500
val ErrorRed = Red500
val InfoBlue = Blue500
