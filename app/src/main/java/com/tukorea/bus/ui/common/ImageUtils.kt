package com.tukorea.bus.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.naver.maps.map.overlay.OverlayImage
import com.tukorea.bus.R

/**
 * 이미지 관련 유틸리티 함수
 * UI 레이어에서 사용하는 이미지 처리 로직을 담당합니다.
 */
object ImageUtils {
    /**
     * 리소스에서 이미지를 로드하고 지정된 크기로 리사이즈하여 OverlayImage로 변환합니다.
     *
     * @param context Android Context
     * @param resourceId 리소스 ID
     * @param sizeInDp 리사이즈할 크기 (dp 단위)
     * @return 리사이즈된 OverlayImage
     */
    fun createResizedOverlayImage(
        context: Context,
        resourceId: Int,
        sizeInDp: Int = 64
    ): OverlayImage {
        val density = context.resources.displayMetrics.density
        val sizeInPx = (sizeInDp * density).toInt()
        val originalBitmap = BitmapFactory.decodeResource(context.resources, resourceId)
        val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, sizeInPx, sizeInPx, true)
        originalBitmap.recycle() // 원본 비트맵 메모리 해제
        return OverlayImage.fromBitmap(resizedBitmap)
    }
}

