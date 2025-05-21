package com.ilyavorontsov.lab18

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class CurrencyListFragment : Fragment() {

    lateinit var currencies: ArrayList<Currency>
    private lateinit var list: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            currencies = (it.getSerializable("CURRENCIES") as ArrayList<Currency>)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_currency_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("CURRENCY LIST", "CREATED")

        val adapter = CurrencyListAdapter(currencies)
        list = view.findViewById<RecyclerView>(R.id.rvCurrencyList)
        list.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance(currencies: MutableList<Currency>) =
            CurrencyListFragment().apply {
                arguments = Bundle().apply {
                    putSerializable("CURRENCIES", ArrayList(currencies))
                }
            }
    }
}