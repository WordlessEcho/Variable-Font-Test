package moe.echo.variablefonttest

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.fragment.app.Fragment

class MainFragment : Fragment(R.layout.main_fragment) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val toggleOptions: ImageButton? = view.findViewById(R.id.toggle_options)
        val options: View? = view.findViewById(R.id.options_fragment)

        toggleOptions?.setOnClickListener {
            options?.apply {
                visibility = if (visibility == View.INVISIBLE) {
                    toggleOptions.setImageResource(R.drawable.ic_baseline_expand_less_24)
                    View.VISIBLE
                } else {
                    toggleOptions.setImageResource(R.drawable.ic_baseline_expand_more_24)
                    View.INVISIBLE
                }
            }
        }
    }
}
