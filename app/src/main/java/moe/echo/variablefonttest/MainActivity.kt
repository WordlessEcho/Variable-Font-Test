package moe.echo.variablefonttest

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.slider.Slider

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

//    should be same order as res/values/font_families_array.xml
    private val fontFamilyList = arrayOf(
        Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.MONOSPACE,
        Typeface.SANS_SERIF, Typeface.SERIF
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val previewContent: EditText = findViewById(R.id.previewContent)
        val textSize: EditText = findViewById(R.id.textSize)
        val chws: SwitchCompat = findViewById(R.id.chws)
        val wght: Slider = findViewById(R.id.wght)
        val preview: TextView = findViewById(R.id.preview)
        val fontFamilies: Spinner = findViewById(R.id.fontFamilies)

        previewContent.addTextChangedListener { text ->
            preview.text = text
        }

        textSize.addTextChangedListener { text ->
            preview.textSize = if (text.toString().isEmpty()) 20.toFloat() else text.toString().toFloat()
        }

        wght.addOnChangeListener { _, value, _ ->
            preview.fontVariationSettings = "'wght' $value"
        }

        chws.setOnCheckedChangeListener { buttonView, isChecked ->
            preview.fontFeatureSettings = if (isChecked) "'chws' 1" else ""
        }

        ArrayAdapter.createFromResource(
            this,
            R.array.font_families_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            fontFamilies.adapter = adapter
        }

        fontFamilies.onItemSelectedListener = this
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val preview: TextView = findViewById(R.id.preview)
        preview.typeface = fontFamilyList[pos]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show()
    }
}
