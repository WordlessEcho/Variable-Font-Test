package moe.echo.variablefonttest

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
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
        val showOptions: ImageButton = findViewById(R.id.showOptions)
        val options: View = findViewById(R.id.options)
        val textSize: EditText = findViewById(R.id.textSize)
        val chws: SwitchCompat = findViewById(R.id.chws)
        val wght: Slider = findViewById(R.id.wght)
        val fontFamilies: Spinner = findViewById(R.id.fontFamilies)

        showOptions.setOnClickListener {
            options.apply {
                visibility = if (visibility == View.GONE) {
                    showOptions.setImageResource(R.drawable.ic_baseline_expand_less_24)
                    View.VISIBLE
                } else {
                    showOptions.setImageResource(R.drawable.ic_baseline_expand_more_24)
                    View.GONE
                }
            }
        }

        textSize.addTextChangedListener { text ->
            previewContent.textSize = if (text.toString().isEmpty()) 20.toFloat() else text.toString().toFloat()
        }

        wght.addOnChangeListener { _, value, _ ->
            previewContent.fontVariationSettings = "'wght' $value"
        }

        chws.setOnCheckedChangeListener { buttonView, isChecked ->
            previewContent.fontFeatureSettings = if (isChecked) "'chws' 1" else ""
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
        val previewContent: TextView = findViewById(R.id.previewContent)
        previewContent.typeface = fontFamilyList[pos]
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing selected", Toast.LENGTH_SHORT).show()
    }
}
