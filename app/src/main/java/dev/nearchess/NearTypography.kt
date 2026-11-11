package dev.nearchess

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// System sans-serif is Android's native Roboto-family mapping; no online font fetch.
private val nearFont=FontFamily.SansSerif
val NearTypography=Typography(
 displaySmall=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.Medium,fontSize=36.sp,lineHeight=44.sp),
 headlineSmall=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.Medium,fontSize=24.sp,lineHeight=32.sp),
 titleLarge=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.SemiBold,fontSize=22.sp,lineHeight=28.sp),
 titleMedium=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.Medium,fontSize=16.sp,lineHeight=24.sp,letterSpacing=.15.sp),
 titleSmall=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.Medium,fontSize=14.sp,lineHeight=20.sp,letterSpacing=.1.sp),
 bodyLarge=TextStyle(fontFamily=nearFont,fontSize=16.sp,lineHeight=24.sp,letterSpacing=.25.sp),
 bodyMedium=TextStyle(fontFamily=nearFont,fontSize=14.sp,lineHeight=20.sp,letterSpacing=.25.sp),
 bodySmall=TextStyle(fontFamily=nearFont,fontSize=12.sp,lineHeight=16.sp,letterSpacing=.4.sp),
 labelLarge=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.SemiBold,fontSize=14.sp,lineHeight=20.sp,letterSpacing=.1.sp),
 labelMedium=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.Medium,fontSize=12.sp,lineHeight=16.sp,letterSpacing=.4.sp),
 labelSmall=TextStyle(fontFamily=nearFont,fontWeight=FontWeight.Medium,fontSize=11.sp,lineHeight=16.sp,letterSpacing=.5.sp)
)
