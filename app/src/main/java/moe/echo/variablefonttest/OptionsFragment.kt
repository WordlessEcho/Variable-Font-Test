package moe.echo.variablefonttest

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.preference.*
import rikka.preference.SimpleMenuPreference

class OptionsFragment: PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.options, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.overScrollMode = View.OVER_SCROLL_NEVER

        val fontFamilyValues = resources.getStringArray(R.array.font_families_value)
        val fontFamilyList = arrayOf(
            Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.MONOSPACE,
            Typeface.SANS_SERIF, Typeface.SERIF
        )
        val valueToTypeface = fontFamilyValues.zip(fontFamilyList).toMap()

        val previewContent: EditText =
            requireParentFragment().requireView().findViewById(R.id.preview_content)
        val textSize: EditTextPreference? = findPreference("text_size")
        val fontFamilies: SimpleMenuPreference? = findPreference("font_families")
        val wght: SeekBarPreference? = findPreference("wght")
        val chws: SwitchPreferenceCompat? = findPreference("chws")

        textSize?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toFloatOrNull() != null) {
                    previewContent.textSize = newValue.toString().toFloat()
                    true
                } else false
            }
        }

        fontFamilies?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (valueToTypeface.contains(newValue)) {
                    previewContent.typeface = valueToTypeface[newValue]
                    wght?.value = 400
                    true
                } else false
            }
        }

        wght?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toFloatOrNull() != null) {
                    previewContent.fontVariationSettings = "'wght' $newValue"
                    true
                } else false
            }
        }

        chws?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                previewContent.fontFeatureSettings = if (!isChecked) "'chws' 1" else ""
                true
            }
        }
    }
}