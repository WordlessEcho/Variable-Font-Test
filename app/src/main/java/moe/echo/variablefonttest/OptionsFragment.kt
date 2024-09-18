package moe.echo.variablefonttest

import android.app.AlertDialog
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import androidx.preference.SwitchPreference
import androidx.preference.SwitchPreferenceCompat
import androidx.preference.forEach
import rikka.preference.SimpleMenuPreference
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.collections.MutableMap
import kotlin.collections.contains
import kotlin.collections.filter
import kotlin.collections.get
import kotlin.collections.joinToString
import kotlin.collections.mutableMapOf
import kotlin.collections.set
import kotlin.collections.toList
import kotlin.collections.toMap
import kotlin.collections.zip
import kotlin.math.abs
import kotlin.math.pow

private const val TAG = "OptionsFragment"

class OptionsFragment : PreferenceFragmentCompat() {

    private val fontVariationSettings = mutableMapOf<String, String>()
    private val fontFeatureSettings = mutableMapOf<String, String>()

    private val getFont =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) changeFontFromUri(uri)
        }

    private fun createAddPreferenceDialog(
        context: Context,
        preferences: PreferenceCategory,
        setSetting: (tagName: String, value: String) -> Unit
    ) = AlertDialog.Builder(context).apply {
        // https://developer.android.com/develop/ui/views/components/dialogs#CustomLayout
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        val dialogLayout = View.inflate(context, R.layout.add_preference_dialog, null)

        val spinner = dialogLayout.findViewById<Spinner>(R.id.tagType)
        val typeValues = resources.getStringArray(R.array.font_feature_type_values)

        val seekBarMin = dialogLayout.findViewById<EditText>(R.id.tagSeekBarMin)
        val seekBarMax = dialogLayout.findViewById<EditText>(R.id.tagSeekBarMax)
        val seekBarStep = dialogLayout.findViewById<EditText>(R.id.tagSeekBarStep)

        ArrayAdapter.createFromResource(
            context,
            R.array.font_feature_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            spinner.adapter = adapter
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    when (typeValues[position]) {
                        Constants.ADD_FEATURE_TYPE_SEEK_BAR -> {
                            seekBarMin.isVisible = true
                            seekBarMax.isVisible = true
                            seekBarStep.isVisible = true
                        }
                        else -> {
                            seekBarMin.isVisible = false
                            seekBarMax.isVisible = false
                            seekBarStep.isVisible = false
                        }
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) { return }
            }
        }

        setView(dialogLayout)

        setPositiveButton(android.R.string.ok) { _, _ ->
            val tagNameEditText = dialogLayout.findViewById<EditText>(R.id.tagName)
            val tagName = tagNameEditText.text.toString()

            val preference = when (typeValues[spinner.selectedItemPosition]) {
                Constants.ADD_FEATURE_TYPE_SWITCH ->
                    SwitchPreference(preferenceScreen.context).apply {

                        setOnPreferenceChangeListener { _, _ ->
                            setSetting(tagName, if (!isChecked) "1" else "0")
                            true
                        }
                    }
                Constants.ADD_FEATURE_TYPE_SEEK_BAR -> {
                    SeekBarPreference(preferenceScreen.context).apply {
                        val rawMin = seekBarMin.text.toString()
                        val rawMax = seekBarMax.text.toString()
                        val rawStep = seekBarStep.text.toString()

                        val minSetting = rawMin.toFloatOrNull() ?: 0F
                        val maxSetting = rawMax.toFloatOrNull() ?: 0F

                        val minimum = minSetting.coerceAtMost(maxSetting)
                        val maximum = minSetting.coerceAtLeast(maxSetting)
                        val step = rawStep.toFloatOrNull() ?: 0F

                        var offset = 0F
                        var multiplier = 1F

                        if (rawStep.contains(".")) {
                            val decimalWithDot = rawStep
                                .substring(rawStep.indexOf("."))
                            val decimalWithDotLength = decimalWithDot.length

                            if (decimalWithDotLength > 1) {
                                if (decimalWithDot.substring(1).toInt() > 0) {
                                    multiplier *= 10F.pow(decimalWithDotLength - 1)
                                }
                            }
                        }

                        if (minimum < 0) {
                            offset += abs(minimum)
                        }

                        min = ((minimum + offset) * multiplier).toInt()
                        max = ((maximum + offset) * multiplier).toInt()
                        seekBarIncrement = (step * multiplier).toInt()

                        Log.i(TAG, "createAddPreferenceDialog: $tagName: minimum: $minimum")
                        Log.i(TAG, "createAddPreferenceDialog: $tagName: maximum: $maximum")
                        Log.i(TAG, "createAddPreferenceDialog: $tagName: step: $step")

                        Log.i(TAG, "createAddPreferenceDialog: $tagName: offset: $offset")
                        Log.i(TAG, "createAddPreferenceDialog: $tagName: multiplier: $multiplier")

                        Log.i(TAG, "createAddPreferenceDialog: $tagName: seekBar.min: $min")
                        Log.i(TAG, "createAddPreferenceDialog: $tagName: seekBar.max: $max")
                        Log.i(
                            TAG,
                            "createAddPreferenceDialog: $tagName: seekBar.seekBarIncrement: $seekBarIncrement"
                        )

                        updatesContinuously = true

                        setOnPreferenceChangeListener { _, newValue ->
                            val value = newValue.toString().toFloatOrNull()

                            if (value != null) {
                                setSetting(
                                    tagName,
                                    ((value - offset * multiplier) / multiplier).toString()
                                )
                                true
                            } else false
                        }
                    }
                }
                Constants.ADD_FEATURE_TYPE_EDIT_TEXT ->
                    EditTextPreference(preferenceScreen.context).apply {
                        dialogTitle = tagName

                        setOnPreferenceChangeListener { _, newValue ->
                            try {
                                summary = newValue.toString()
                                setSetting(tagName, newValue.toString())
                                return@setOnPreferenceChangeListener true
                            } catch (e: IllegalArgumentException) {
                                Toast.makeText(
                                    context,
                                    e.message.toString(),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            false
                        }
                    }
                else -> null
            } ?: return@setPositiveButton

            preference.apply {
                key = tagName
                title = tagName

                isPersistent = false
            }

            val duplicateKeyPreference = findPreference<Preference>(tagName)
            if (duplicateKeyPreference != null) {
                preferences.removePreference(duplicateKeyPreference)
            }
            preferences.addPreference(preference)

            // Reorganize preferences to make add & edit preference always at bottom
            preferences.forEach {
                when (it.key) {
                    preference.key -> it.order = preferences.preferenceCount - 3
                    Constants.PREF_ADD_FONT_VARIATION, Constants.PREF_ADD_FONT_FEATURE ->
                        it.order = preferences.preferenceCount - 2
                    Constants.PREF_EDIT_VARIATION, Constants.PREF_EDIT_FEATURE ->
                        it.order = preferences.preferenceCount - 1
                }
            }
        }
        setNegativeButton(android.R.string.cancel) { _, _ -> return@setNegativeButton }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.options, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val fontFamilyOptions = resources.getStringArray(R.array.font_family_values)
        // Drop custom font option
        val fontFamilyValues = fontFamilyOptions.filter { s -> s != Constants.OPTION_CUSTOM_VALUE }
        val fontFamilyList = arrayOf(
            Typeface.DEFAULT, Typeface.DEFAULT_BOLD, Typeface.MONOSPACE,
            Typeface.SANS_SERIF, Typeface.SERIF
        )
        // Pair options and typefaces
        val valueToTypeface = fontFamilyValues.zip(fontFamilyList).toMap()

        val previewContent: EditText? = view.findViewById(R.id.preview_content)

        val textSize: EditTextPreference? = findPreference(Constants.PREF_TEXT_SIZE)
        val fontFamilies: SimpleMenuPreference? = findPreference(Constants.PREF_FONT_FAMILIES)
        val ttcIndex: EditTextPreference? = findPreference(Constants.PREF_TTC_INDEX)
        val customFont: Preference? = findPreference(Constants.PREF_CUSTOM_FONT)

        val variations: PreferenceCategory? = findPreference(Constants.PREF_CATEGORY_VARIATIONS)
        val ital: SeekBarPreference? = findPreference(Constants.PREF_VARIATION_ITALIC)
        val opsz: SeekBarPreference? = findPreference(Constants.PREF_VARIATION_OPTICAL_SIZE)
        val slnt: SeekBarPreference? = findPreference(Constants.PREF_VARIATION_SLANT)
        val wdth: SeekBarPreference? = findPreference(Constants.PREF_VARIATION_WIDTH)
        val wght: SeekBarPreference? = findPreference(Constants.PREF_VARIATION_WEIGHT)
        val variationEditor: EditTextPreference? = findPreference(Constants.PREF_VARIATION_EDITOR)
        val addVariation: Preference? = findPreference(Constants.PREF_ADD_FONT_VARIATION)
        val editVariation: Preference? = findPreference(Constants.PREF_EDIT_VARIATION)

        val fontFeatures: PreferenceCategory? = findPreference(Constants.PREF_CATEGORY_FONT_FEATURES)
        val chws: SwitchPreferenceCompat? = findPreference(Constants.PREF_FEATURE_CHWS)
        val halt: SwitchPreferenceCompat? = findPreference(Constants.PREF_FEATURE_HALT)
        val frac: SwitchPreferenceCompat? = findPreference(Constants.PREF_FEATURE_FRAC)
        val featureEditor: EditTextPreference? = findPreference(Constants.PREF_FEATURE_EDITOR)
        val addFeature: Preference? = findPreference(Constants.PREF_ADD_FONT_FEATURE)
        val editFeatures: Preference? = findPreference(Constants.PREF_EDIT_FEATURE)

        // https://developer.android.com/develop/ui/views/layout/edge-to-edge
        // https://medium.com/androiddevelopers/gesture-navigation-handling-gesture-conflicts-8ee9c2665c69#eaaa
        listView.clipToPadding = false
        ViewCompat.setOnApplyWindowInsetsListener(listView) { v, windowInsets ->
            val insets = WindowInsetsUtil.safeDrawing(windowInsets)

            // Apply the insets as a margin to the view. This solution sets
            // only the bottom, left, and right dimensions, but you can apply whichever
            // insets are appropriate to your layout. You can also update the view padding
            // if that's more appropriate.
            v.updatePadding(bottom = insets.bottom)

            // Return CONSUMED if you don't want want the window insets to keep passing
            // down to descendant views.
            WindowInsetsCompat.CONSUMED
        }

        fun setVariation(settings: String) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                previewContent?.fontVariationSettings = settings
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            variations?.isEnabled = true

            val unsupportedAndroid = findPreference<Preference>(Constants.PREF_UNSUPPORTED_ANDROID)
            unsupportedAndroid?.isVisible = false
        }

        ttcIndex?.setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        textSize?.apply {
            setOnBindEditTextListener { editText ->
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
            setOnPreferenceChangeListener { _, newValue ->
                if (newValue.toString().toFloatOrNull() != null) {
                    previewContent?.textSize = newValue.toString().toFloat()
                    true
                } else false
            }
        }

        fontFamilies?.setOnPreferenceChangeListener { _, newValue ->
            when {
                valueToTypeface.contains(newValue) -> {
                    customFont?.isVisible = false
                    ttcIndex?.isVisible = false
                    previewContent?.typeface = valueToTypeface[newValue]
                    setVariation(fontVariationSettings.toFeatures())
                    true
                }
                newValue == Constants.OPTION_CUSTOM_VALUE -> {
                    customFont?.isVisible = true
                    ttcIndex?.isVisible = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ttcIndex?.isEnabled = true
                    }
                    true
                }
                else -> false
            }
        }

        customFont?.setOnPreferenceClickListener {
            getFont.launch("font/*")
            true
        }

        // SeekBar only support positive integers
        ital?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue.toString().toFloatOrNull()

            if (value != null) {
                fontVariationSettings[Constants.VARIATION_AXIS_ITALIC] = (value / 10).toString()
                setVariation(fontVariationSettings.toFeatures())
                true
            } else false
        }

        opsz?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue.toString().toFloatOrNull()

            if (value != null) {
                fontVariationSettings[Constants.VARIATION_AXIS_OPTICAL_SIZE] = (value / 10).toString()
                setVariation(fontVariationSettings.toFeatures())
                true
            } else false
        }

        slnt?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue.toString().toFloatOrNull()

            if (value != null) {
                fontVariationSettings[Constants.VARIATION_AXIS_SLANT] = (value - 90).toString()
                setVariation(fontVariationSettings.toFeatures())
                true
            } else false
        }

        wdth?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue.toString().toFloatOrNull()

            if (value != null) {
                fontVariationSettings[Constants.VARIATION_AXIS_WIDTH] = (value / 10).toString()
                setVariation(fontVariationSettings.toFeatures())
                true
            } else false
        }

        wght?.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue.toString().toFloatOrNull()

            if (value != null) {
                fontVariationSettings[Constants.VARIATION_AXIS_WEIGHT] = value.toString()
                setVariation(fontVariationSettings.toFeatures())
                true
            } else false
        }

        variationEditor?.setOnPreferenceChangeListener { _, newValue ->
            try {
                setVariation(newValue.toString())
                return@setOnPreferenceChangeListener true
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
            }
            false
        }

        addVariation?.setOnPreferenceClickListener {
            if (variations != null) {
                createAddPreferenceDialog(view.context, variations) { tagName, value ->
                    fontVariationSettings[tagName] = value
                    setVariation(fontVariationSettings.toFeatures())
                }.apply {
                    setTitle(R.string.add_font_variation)
                    show()
                }
                true
            } else false
        }

        editVariation?.setOnPreferenceClickListener {
            variationEditor?.apply {
                text = fontVariationSettings.toFeatures()

                if (isVisible) {
                    variations?.forEach {
                        if (
                            it.key !in setOf(
                                Constants.PREF_ADD_FONT_VARIATION,
                                Constants.PREF_EDIT_VARIATION,
                                Constants.PREF_UNSUPPORTED_ANDROID
                            )
                        ) {
                            it.isVisible = true
                        }
                    }

                    editVariation.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_edit_24)
                    editVariation.title = getString(R.string.edit_variation_text)
                } else {
                    variations?.forEach {
                        if (
                            it.key !in setOf(
                                Constants.PREF_ADD_FONT_VARIATION,
                                Constants.PREF_EDIT_VARIATION,
                                Constants.PREF_UNSUPPORTED_ANDROID
                            )
                        ) {
                            it.isVisible = false
                        }
                    }

                    editVariation.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_build_24)
                    editVariation.title = getString(R.string.edit_variation_ui)
                }

                isVisible = !isVisible
            }
            true
        }

        chws?.apply {
            // `chws` is disabled by default when SDK < 33
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                isChecked = false
            }

            summary = String.format(
                getString(R.string.mojikumi_description),
                Constants.FEATURE_CHWS
            )

            setOnPreferenceChangeListener { _, _ ->
                fontFeatureSettings[Constants.FEATURE_CHWS] = if (!isChecked) "1" else "0"
                previewContent?.fontFeatureSettings = fontFeatureSettings.toFeatures()
                true
            }
        }

        halt?.apply {
            summary = String.format(
                getString(R.string.mojikumi_description),
                Constants.FEATURE_HALT
            )

            setOnPreferenceChangeListener { _, _ ->
                fontFeatureSettings[Constants.FEATURE_HALT] = if (!isChecked) "1" else "0"
                previewContent?.fontFeatureSettings = fontFeatureSettings.toFeatures()
                true
            }
        }

        frac?.apply {
            setOnPreferenceChangeListener { _, _ ->
                fontFeatureSettings[Constants.FEATURE_FRAC] = if (!isChecked) "1" else "0"
                previewContent?.fontFeatureSettings = fontFeatureSettings.toFeatures()
                true
            }
        }

        featureEditor?.setOnPreferenceChangeListener { _, newValue ->
            try {
                previewContent?.fontFeatureSettings = newValue.toString()
                return@setOnPreferenceChangeListener true
            } catch (e: IllegalArgumentException) {
                Toast.makeText(context, e.message.toString(), Toast.LENGTH_LONG).show()
            }
            false
        }

        addFeature?.setOnPreferenceClickListener {
            if (fontFeatures != null) {
                createAddPreferenceDialog(view.context, fontFeatures) { tagName, value ->
                    fontFeatureSettings[tagName] = value
                    previewContent?.fontFeatureSettings = fontFeatureSettings.toFeatures()
                }.apply {
                    setTitle(R.string.add_font_feature)
                    show()
                }
                true
            } else false
        }

        editFeatures?.setOnPreferenceClickListener {
            featureEditor?.apply {
                text = fontFeatureSettings.toFeatures()

                if (isVisible) {
                    fontFeatures?.forEach {
                        if (
                            it.key !in setOf(
                                Constants.PREF_ADD_FONT_FEATURE,
                                Constants.PREF_EDIT_FEATURE
                            )
                        ) {
                            it.isVisible = true
                        }
                    }

                    editFeatures.icon =
                        ContextCompat.getDrawable(context, R.drawable.ic_baseline_edit_24)
                    editFeatures.title = getString(R.string.edit_feature_text)
                } else {
                    fontFeatures?.forEach {
                        if (
                            it.key !in setOf(
                                Constants.PREF_ADD_FONT_FEATURE,
                                Constants.PREF_EDIT_FEATURE
                            )
                        ) {
                            it.isVisible = false
                        }
                    }

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

    private fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    private fun changeFontFromUri(uri: Uri) {
        activity?.runOnUiThread {
            val previewContent: EditText? = parentFragment?.view?.findViewById(R.id.preview_content)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val ttcIndex: EditTextPreference? = findPreference(Constants.PREF_TTC_INDEX)

                activity?.contentResolver?.openFileDescriptor(uri, "r")?.use {
                    val builder = Typeface.Builder(it.fileDescriptor)
                    builder.setFontVariationSettings(fontVariationSettings.toFeatures())
                    builder.setTtcIndex(ttcIndex?.text?.toInt() ?: 0)
                    previewContent?.typeface = builder.build()
                    return@runOnUiThread
                } ?: {
                    Log.w(TAG, "changeFontFromUri: Failed to set font.")
                    Log.w(TAG, "changeFontFromUri: Uri: $uri")
                    Log.w(TAG, "changeFontFromUri: Uri?.path: ${uri.path}")
                    Log.w(TAG, "changeFontFromUri: activity == null? ${activity == null}")
                    Log.w(
                        TAG,
                        "changeFontFromUri: activity?.contentResolver == null? ${activity?.contentResolver == null}"
                    )
                }
            } else {
                val cacheDir = context?.cacheDir

                if (cacheDir != null) {
                    val font = File.createTempFile("font", "ByVFT")

                    val inputStream = activity?.contentResolver?.openInputStream(uri)

                    if (inputStream != null) {
                        copyStreamToFile(inputStream, font)
                        previewContent?.typeface = Typeface.createFromFile(font)
                        return@runOnUiThread
                    } else {
                        Log.w(TAG, "changeFontFromUri: Failed to openInputStream to set font.")
                        Log.w(TAG, "changeFontFromUri: Uri: $uri")
                        Log.w(TAG, "changeFontFromUri: context?.cacheDir: $cacheDir")
                        Log.w(TAG, "changeFontFromUri: activity == null? ${activity == null}")
                        Log.w(
                            TAG,
                            "changeFontFromUri: activity?.contentResolver == null? ${activity?.contentResolver == null}"
                        )
                    }
                } else {
                    Log.w(TAG, "changeFontFromUri: Failed to set font.")
                    Log.w(TAG, "changeFontFromUri: Uri: $uri")
                }
            }

            Toast.makeText(context, R.string.font_import_failed, Toast.LENGTH_LONG).show()
        }
    }
}
