package moe.echo.variablefonttest

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewContent = findViewById<EditText>(R.id.previewContent)
        val textSize = findViewById<EditText>(R.id.textSize)
        val wght = findViewById<Slider>(R.id.wght)
        val preview = findViewById<TextView>(R.id.preview)

        previewContent.addTextChangedListener { text ->
            preview.text = text
        }

        textSize.addTextChangedListener { text ->
            preview.textSize = if (text.toString().isEmpty()) 20.toFloat() else text.toString().toFloat()
        }

        wght.addOnChangeListener { _, value, _ ->
            preview.fontVariationSettings = "'wght' $value"
        }
    }
}
