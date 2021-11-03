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

        val fontVariationSettings = mutableMapOf<String, String>()

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

        val ital: SwitchPreferenceCompat? = findPreference("ital")
        val opsz: EditTextPreference? = findPreference("opsz")
        val slnt: EditTextPreference? = findPreference("slnt")
        val wdth: EditTextPreference? = findPreference("wdth")
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

        ital?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                fontVariationSettings["ital"] = if (!isChecked) "1" else "0"
                previewContent.fontVariationSettings = fontVariationSettings.toVariation()
                true
            }
        }

        opsz?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                if (newValue.toString().toDoubleOrNull() != null) {
                    fontVariationSettings["opsz"] = newValue.toString()
                    true
                } else false
            }
        }

        slnt?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                if (newValue.toString().toDoubleOrNull() != null) {
                    fontVariationSettings["slnt"] = newValue.toString()
                    true
                } else false
            }
        }

        wdth?.apply {
            setOnPreferenceChangeListener { preference, newValue ->
                if (newValue.toString().toDoubleOrNull() != null) {
                    fontVariationSettings["wdth"] = newValue.toString()
                    true
                } else false
            }
        }

        wght?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toFloatOrNull() != null) {
                    fontVariationSettings["wght"] = newValue.toString()
                    previewContent.fontVariationSettings = fontVariationSettings.toVariation()
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

    private fun MutableMap<String, String>.toVariation(): String {
        var variationSettings = ""
        this.forEach { (t, u) -> variationSettings = "$variationSettings, '$t' $u" }
        return variationSettings
    }
}