package moe.echo.variablefonttest

import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat

class WindowInsetsUtil {
    companion object {
        fun safeDrawing(windowInsets: WindowInsetsCompat): Insets {
            val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val displayCutout = windowInsets.displayCutout
            val ime = windowInsets.getInsets(WindowInsetsCompat.Type.ime())

            val maxOfSystemBarsAndIME = Insets.max(systemBars, ime)

            return if (displayCutout == null) {
                maxOfSystemBarsAndIME
            } else {
                val displayCutoutInsets = Insets.of(
                    displayCutout.safeInsetLeft, displayCutout.safeInsetTop,
                    displayCutout.safeInsetRight, displayCutout.safeInsetBottom)

                Insets.max(maxOfSystemBarsAndIME, displayCutoutInsets)
            }
        }

        fun safeGestures(windowInsets: WindowInsetsCompat): Insets {
            val systemGestures = windowInsets.getInsets(WindowInsetsCompat.Type.systemGestures())
            val waterfall = windowInsets.displayCutout?.waterfallInsets
            val tappableElement = windowInsets.getInsets(WindowInsetsCompat.Type.tappableElement())

            val maxOfSystemGesturesAndTappableElement = Insets.max(systemGestures, tappableElement)

            return if (waterfall == null) {
                maxOfSystemGesturesAndTappableElement
            } else {
                Insets.max(maxOfSystemGesturesAndTappableElement, waterfall)
            }
        }

        fun safeContent(windowInsets: WindowInsetsCompat) =
            Insets.max(safeDrawing(windowInsets), safeGestures(windowInsets))
    }
}
