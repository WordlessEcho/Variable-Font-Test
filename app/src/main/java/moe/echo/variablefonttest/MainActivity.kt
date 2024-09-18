package moe.echo.variablefonttest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.fragment.app.FragmentContainerView
import com.google.android.material.appbar.AppBarLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val navigationBarsInsets = windowInsets.getInsets(
                WindowInsetsCompat.Type.navigationBars())

            var isGestureNavAtTop = false
            var isGestureNavAtBottom = false
            var isGestureNavAtLeft = false
            var isGestureNavAtRight = false

            // https://gist.github.com/Thorsten1976/07d61b3f697364e5f1c08ae076641d58
            val navBarInteractionModeID = resources.getIdentifier(
                "config_navBarInteractionMode", "integer", "android")
            if (navBarInteractionModeID > 0) {
                val navBarInteractionMode = resources.getInteger(navBarInteractionModeID)
                if (navBarInteractionMode == 2) {
                    isGestureNavAtTop = navigationBarsInsets.top > 0
                    isGestureNavAtBottom = navigationBarsInsets.bottom > 0
                    isGestureNavAtLeft = navigationBarsInsets.left > 0
                    isGestureNavAtRight = navigationBarsInsets.right > 0
                }
            }

            val topInsetsHeight = if (!isGestureNavAtTop) systemBarsInsets.top else 0
            val bottomInsetsHeight = if (!isGestureNavAtBottom) systemBarsInsets.bottom else 0
            val leftInsetsWidth = if (!isGestureNavAtLeft) systemBarsInsets.left else 0
            val rightInsetsWidth = if (!isGestureNavAtRight) systemBarsInsets.right else 0

            val topInsetsBackground: View? = findViewById(R.id.top_insets_background)
            topInsetsBackground?.apply {
                layoutParams.height = topInsetsHeight
                requestLayout()
            }

            val bottomInsetsBackground: View? = findViewById(R.id.bottom_insets_background)
            bottomInsetsBackground?.apply {
                layoutParams.height = bottomInsetsHeight
                requestLayout()
            }

            val leftInsetsBackground: View? = findViewById(R.id.left_insets_background)
            leftInsetsBackground?.let {
                val layoutParams = it.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.width = leftInsetsWidth
                layoutParams.topMargin = topInsetsHeight
                layoutParams.bottomMargin = bottomInsetsHeight
                it.requestLayout()
            }

            val rightInsetsBackground: View? = findViewById(R.id.right_insets_background)
            rightInsetsBackground?.let {
                val layoutParams = it.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.width = rightInsetsWidth
                layoutParams.topMargin = topInsetsHeight
                layoutParams.bottomMargin = bottomInsetsHeight
                it.requestLayout()
            }

            windowInsets
        }

        val appBarLayout: AppBarLayout? = findViewById(R.id.app_bar_layout)
        appBarLayout?.apply {
            ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->
                val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val displayCutout = windowInsets.displayCutout

                v.updatePadding(
                    top = systemBarsInsets.top
                        .coerceAtLeast(displayCutout?.safeInsetTop ?: 0),
                    left = systemBarsInsets.left
                        .coerceAtLeast(displayCutout?.safeInsetLeft ?: 0),
                    right = systemBarsInsets.right
                        .coerceAtLeast(displayCutout?.safeInsetRight ?: 0))

                windowInsets
            }
        }

        val mainContainer: FragmentContainerView? = findViewById(R.id.main_container)
        if (mainContainer != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainContainer) { v, windowInsets ->
                val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val displayCutout = windowInsets.displayCutout

                v.updatePadding(
                    left = systemBarsInsets.left
                        .coerceAtLeast(displayCutout?.safeInsetLeft ?: 0),
                    right = systemBarsInsets.right
                        .coerceAtLeast(displayCutout?.safeInsetRight ?: 0))

                windowInsets
            }
        }
    }
}
