package ir.divarfiling.mobile.feature.contentstudio.canvas

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.RectF

object PcsImageCrop {
    fun computeCropSrc(bmp: Bitmap, dst: RectF, style: PcsTextStyle?, objectFit: String?): Rect =
        computeCropSrc(bmp.width, bmp.height, dst, style, objectFit)

    fun computeCropSrc(
        bmpWidth: Int,
        bmpHeight: Int,
        dst: RectF,
        style: PcsTextStyle?,
        objectFit: String?,
    ): Rect {
        val crop = PcsImageCropMath.computeCropSrc(
            bmpWidth = bmpWidth,
            bmpHeight = bmpHeight,
            dstWidth = dst.width(),
            dstHeight = dst.height(),
            style = style,
            objectFit = objectFit,
        )
        return Rect(crop.left, crop.top, crop.right, crop.bottom)
    }
}
