/*
 * Copyright (c) 2025 Proton Technologies AG
 * This file is part of Proton Technologies AG and Proton Mail.
 *
 * Proton Mail is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Mail is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Mail. If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("DEPRECATION")

package ch.protonmail.android.design.compose.modifiers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Picture
import android.os.Build
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.createBitmap
import ch.protonmail.android.design.compose.modifiers.BlurBuilder.blurBitmap

/**
 * Blurs a portion of the given composable
 */
@Composable
fun ListBlurEffect(
    areaToApplyBlur: Size,
    modifier: Modifier = Modifier,
    listState: LazyListState,
    content: @Composable (modifier: Modifier) -> Unit
) {
    val graphicsLayer = rememberGraphicsLayer()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        content(modifier.applyBlur(areaToApplyBlur, graphicsLayer))
    } else {
        val picture = remember { Picture() }
        // The overscroll effect on a column doesn't look good when applying the blur, it creates a judder
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
            content(
                modifier
                    .applyBlurLessThanAndroid15Supported(
                        areaToApplyBlur = areaToApplyBlur,
                        context = LocalContext.current,
                        picture = picture,
                        listState = listState
                    )
            )
        }
    }
}


/**
 * Only available on android 12 and above
 */
private fun Modifier.applyBlur(
    areaToApplyBlur: Size,
    graphicsLayer: GraphicsLayer,
    blurRadius: Float = 30f
) = this.drawWithContent {
    graphicsLayer.record {
        this@drawWithContent.drawContent()
    }
    drawContent()
    graphicsLayer.clip = true

    val clipPath = Path().apply {
        addRect(
            rect = Rect(
                offset = Offset(0f, 0f),
                size = areaToApplyBlur
            )
        )
    }
    graphicsLayer.blendMode
    graphicsLayer.renderEffect = BlurEffect(
        radiusX = blurRadius,
        radiusY = blurRadius,
        edgeTreatment = TileMode.Decal
    )

    clipPath(clipPath) {
        drawLayer(graphicsLayer)
    }
}

/**
 * Supporting android versions less than 15
 */
fun Modifier.applyBlurLessThanAndroid15Supported(
    areaToApplyBlur: Size,
    context: Context,
    picture: Picture,
    blurRadius: Float = 15f,
    @Suppress("UNUSED_PARAMETER")
    listState: LazyListState
) = this.drawWithCache {
    val width = this.size.width.toInt()
    onDrawWithContent {
        val pictureCanvas =
            Canvas(
                picture.beginRecording(
                    width,
                    areaToApplyBlur.height.toInt()
                )
            )
        draw(this, layoutDirection, pictureCanvas, size) {
            this@onDrawWithContent.drawContent()
        }
        picture.endRecording()

        // offset is used to retrigger drawing as we need to redraw when the offset changes
        // i.e the list is scrolled
        val bitmap = with(listState.firstVisibleItemScrollOffset) {
            blurBitmap(
                context,
                createHeightConstrainedBitmapFromPicture(
                    picture = picture,
                    height = areaToApplyBlur.height.toInt()
                ),
                blurRadius
            )
        }
        drawContent()
        drawImage(
            bitmap.asImageBitmap()
        )
    }
}

private fun createHeightConstrainedBitmapFromPicture(picture: Picture, height: Int): Bitmap {
    val bitmap = createBitmap(picture.width, height)

    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawPicture(picture)
    return bitmap
}

private object BlurBuilder {

    @Suppress("DEPRECATION")
    fun blurBitmap(
        context: Context,
        bitmap: Bitmap,
        blurRadius: Float
    ): Bitmap {
        val rs = RenderScript.create(context)

        val softwareBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val input = Allocation.createFromBitmap(rs, softwareBitmap)
        val output = Allocation.createTyped(rs, input.type)
        val script = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))

        script.setRadius(blurRadius)
        script.setInput(input)
        script.forEach(output)
        output.copyTo(softwareBitmap) // Copy blurred data back to original bitmap
        rs.destroy()
        return softwareBitmap
    }
}
