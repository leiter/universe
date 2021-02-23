package com.together.profile

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.NumberPicker
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.together.base.UiState
import com.together.databinding.FragmentPickDayBinding


class PickDayFragment : DialogFragment() {

    private lateinit var viewBinding: FragmentPickDayBinding

    private val viewModel: ProfileViewModel by viewModels({ requireParentFragment() })

    private lateinit var market: UiState.Market

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            market = it.getParcelable(BundleKey)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewBinding = FragmentPickDayBinding.inflate(inflater, container, false)
        viewBinding.tpDays.setIs24HourView(true)

        val i =
            (((viewBinding.tpDays as FrameLayout).children.iterator().next() as LinearLayout)
                .children.iterator().next() as LinearLayout).children.iterator()
        val minutePicker = i.next() as NumberPicker
        minutePicker.minValue = 0
        minutePicker.maxValue = 6
        minutePicker.displayedValues = arrayOf(
            "Sonntag", "Montag", "Dienstag", "Mittwoch",
            "Donnerstag", "Freitag", "Samstag"
        )

        val f = i.next()
        f.visibility = View.GONE
        val h = i.next()
        h.visibility = View.GONE

        viewBinding.btnCancel.setOnClickListener { dismiss() }
        viewBinding.btnSet.setOnClickListener {
            val pr = viewModel.profileLive.value?.copy()!!
            val index = viewBinding.tpDays.currentHour
            market.dayIndicator = index + 1
            market.dayOfWeek = minutePicker.displayedValues[index]
            val rePlaceInt=  pr.marketList.indexOfFirst { market.id == it.id }
            pr.marketList.removeAt(pr.marketList.indexOfFirst { market.id == it.id })
            pr.marketList.add(rePlaceInt,market)
            val p = pr.marketList.toMutableList()
            p.add(rePlaceInt, market)
            viewModel.markets.value = p
            viewModel.profileLive.value = pr
            dismiss()
        }


        return viewBinding.root
    }

    companion object {
        const val BundleKey = "market"
        @JvmStatic
        fun newInstance(param1: Parcelable) =
            PickDayFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(BundleKey, param1)
                }
            }
    }

}