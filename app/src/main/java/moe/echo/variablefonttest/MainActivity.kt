package moe.echo.variablefonttest

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // https://blog.csdn.net/QQxiaoqiang1573/article/details/79888186
        // https://cs.android.com/android/_/android/platform/frameworks/base/+/refs/tags/android-5.1.1_r38:packages/SystemUI/res/values/colors.xml;l=24;drc=bb4a702e6fe44cb026097db13492f8345b38ee97
        // https://cs.android.com/android/platform/superproject/+/android-5.1.1_r38:frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/phone/NavigationBarTransitions.java;l=31;drc=c3056190ae1a6d29ce5943d45ab4711e1e49620c
        val rootLayout: FrameLayout? = findViewById(R.id.root_layout)
        val statusBarBackground: View? = findViewById(R.id.status_bar_background)

        if (rootLayout != null) {
            ViewCompat.setOnApplyWindowInsetsListener(rootLayout) { _, windowInsets ->
                val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

                if (statusBarBackground != null) {
                    statusBarBackground.layoutParams.height = insets.top
                    statusBarBackground.requestLayout()
                }

                // https://developer.android.com/develop/ui/views/touch-and-input/gestures/gesturenav
                // https://developer.android.com/develop/ui/views/layout/edge-to-edge-manually#change-color
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    val navigationBarBackground: View? =
                        findViewById(R.id.navigation_bar_background)

                    if (navigationBarBackground != null) {
                        navigationBarBackground.layoutParams.height = insets.bottom
                        navigationBarBackground.requestLayout()
                    }
                }

                windowInsets
            }
        }
    }
}
