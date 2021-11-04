package moe.echo.variablefonttest

import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.preference.*
import rikka.preference.SimpleMenuPreference

class OptionsFragment: PreferenceFragmentCompat() {

    private val getFont =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) changeFontFromUri(uri)
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.options, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listView.overScrollMode = View.OVER_SCROLL_NEVER

        val fontVariationSettings = mutableMapOf<String, String>()

        // Drop custom font option
        val fontFamilyValues = resources.getStringArray(R.array.font_families_value).dropLast(1)
        val fontFamilyList = arrayOf(
            Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.MONOSPACE,
            Typeface.SANS_SERIF, Typeface.SERIF
        )
        // Pair options and typefaces
        val valueToTypeface = fontFamilyValues.zip(fontFamilyList).toMap()

        val previewContent: EditText =
            requireParentFragment().requireView().findViewById(R.id.preview_content)
        val textSize: EditTextPreference? = findPreference(Constants.PREF_TEXT_SIZE)
        val fontFamilies: SimpleMenuPreference? = findPreference(Constants.PREF_FONT_FAMILIES)
        val customFont: Preference? = findPreference(Constants.PREF_CUSTOM_FONT)

        val ital: SwitchPreferenceCompat? = findPreference(Constants.PREF_VARIATION_ITALIC)
        val opsz: EditTextPreference? = findPreference(Constants.PREF_VARIATION_OPTICAL_SIZE)
        val slnt: EditTextPreference? = findPreference(Constants.PREF_VARIATION_SLANT)
        val wdth: EditTextPreference? = findPreference(Constants.PREF_VARIATION_WIDTH)
        val wght: SeekBarPreference? = findPreference(Constants.PREF_VARIATION_WEIGHT)
        val variationEditor: EditTextPreference? = findPreference(Constants.PREF_VARIATION_EDITOR)
        val editVariation: Preference? = findPreference(Constants.PREF_EDIT_VARIATION)

        val chws: SwitchPreferenceCompat? = findPreference(Constants.PREF_FEATURE_CHWS)
        val featureEditor: EditTextPreference? = findPreference(Constants.PREF_FEATURE_EDITOR)
        val editFeatures: Preference? = findPreference(Constants.PREF_EDIT_FEATURE)

        textSize?.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toFloatOrNull() != null) {
                    previewContent.textSize = newValue.toString().toFloat()
                    true
                } else false
            }
        }

        fontFamilies?.setOnPreferenceChangeListener { _, newValue ->
            when {
                valueToTypeface.contains(newValue) -> {
                    customFont?.isVisible = false
                    previewContent.typeface = valueToTypeface[newValue]
                    wght?.value = 400
                    true
                }
                newValue == resources.getStringArray(R.array.font_families_value).last() -> {
                    customFont?.isVisible = true
                    wght?.value = 400
                    true
                }
                else -> false
            }
        }

        customFont?.setOnPreferenceClickListener {
            getFont.launch("font/*")
            true
        }

        ital?.setOnPreferenceChangeListener { _, _ ->
            fontVariationSettings[Constants.VARIATION_AXIS_ITALIC] =
                if (!ital.isChecked) "1" else "0"
            previewContent.fontVariationSettings = fontVariationSettings.toFeatures()
            true
        }

        opsz?.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toDoubleOrNull() != null) {
                    fontVariationSettings[Constants.VARIATION_AXIS_OPTICAL_SIZE] =
                        newValue.toString()
                    previewContent.fontVariationSettings = fontVariationSettings.toFeatures()
                    true
                } else false
            }
        }

        slnt?.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType =
                    InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            }
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toDoubleOrNull() != null) {
                    fontVariationSettings[Constants.VARIATION_AXIS_SLANT] = newValue.toString()
                    previewContent.fontVariationSettings = fontVariationSettings.toFeatures()
                    true
                } else false
            }
        }

        wdth?.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
            }
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toDoubleOrNull() != null) {
                    fontVariationSettings[Constants.VARIATION_AXIS_WIDTH] = newValue.toString()
                    previewContent.fontVariationSettings = fontVariationSettings.toFeatures()
                    true
                } else false
            }
        }

        wght?.apply {
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toFloatOrNull() != null) {
                    fontVariationSettings[Constants.VARIATION_AXIS_WEIGHT] = newValue.toString()
                    previewContent.fontVariationSettings = fontVariationSettings.toFeatures()
                    true
                } else false
            }
        }

        variationEditor?.setOnPreferenceChangeListener { _, newValue ->
            try {
                previewContent.fontVariationSettings = newValue.toString()
                return@setOnPreferenceChangeListener true
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
            }
            false
        }

        editVariation?.setOnPreferenceClickListener {
            variationEditor?.apply {
                text = ""

                if (isVisible) {
                    ital?.isVisible = true
                    opsz?.isVisible = true
                    slnt?.isVisible = true
                    wdth?.isVisible = true
                    wght?.isVisible = true

                    editVariation.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_edit_24)
                    editVariation.title = getString(R.string.edit_variation_text)
                } else {
                    ital?.isVisible = false
                    opsz?.isVisible = false
                    slnt?.isVisible = false
                    wdth?.isVisible = false
                    wght?.isVisible = false

                    editVariation.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_build_24)
                    editVariation.title = getString(R.string.edit_variation_ui)
                }

                isVisible = !isVisible
            }
            true
        }

        chws?.apply {
            setOnPreferenceChangeListener { _, _ ->
                // TODO: replace with list
                previewContent.fontFeatureSettings = if (!isChecked) "'chws' 1" else ""
                true
            }
        }

        featureEditor?.setOnPreferenceChangeListener { _, newValue ->
            try {
                previewContent.fontFeatureSettings = newValue.toString()
                return@setOnPreferenceChangeListener true
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
            }
            false
        }

        editFeatures?.setOnPreferenceClickListener {
            featureEditor?.apply {
                text = ""

                if (isVisible) {
                    chws?.isVisible = true

                    editFeatures.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_edit_24)
                    editFeatures.title = getString(R.string.edit_feature_text)
                } else {
                    chws?.isVisible = false

                    editFeatures.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_build_24)
                    editFeatures.title = getString(R.string.edit_feature_ui)
                }

                isVisible = !isVisible
            }

            true
        }
    }

    private fun MutableMap<String, String>.toFeatures(): String =
        this.toList().joinToString { "'${it.first}' ${it.second}" }

    private fun changeFontFromUri(uri: Uri) {
        activity?.runOnUiThread {
            val previewContent: EditText? = parentFragment?.view?.findViewById(R.id.preview_content)

            if (uri.path != null) {
                activity?.contentResolver?.openFileDescriptor(uri, "r")?.apply {
                    val builder = Typeface.Builder(fileDescriptor)
                    previewContent?.typeface = builder.build()
                }
            }
        }
    }
}