package com.ilyavorontsov.lab18

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_POSITION
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.Serializable
import java.io.StringReader
import java.net.URL
import java.nio.charset.Charset

data class Currency(
    val name: String,
    val value: String,
) : Serializable

class CurrencyListItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var tvName: TextView
    var tvValue: TextView

    init {
        tvName = itemView.findViewById(R.id.tvCurrencyName)
        tvValue = itemView.findViewById(R.id.tvCurrencyValue)
    }
}

class CurrencyListAdapter(val currencies: MutableList<Currency>) : RecyclerView.Adapter<CurrencyListItemHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyListItemHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.currency_list_item, parent, false)

        val holder = CurrencyListItemHolder(view)
        view.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != NO_POSITION) {
                onItemClickListener?.invoke(pos)
            }
        }
        return holder
    }

    override fun onBindViewHolder(holder: CurrencyListItemHolder, position: Int) {
        holder.tvName.text = currencies[position].name
        holder.tvValue.text = currencies[position].value
    }

    override fun getItemCount(): Int {
        return currencies.size
    }

    private var onItemClickListener: ((Int) -> Unit)? = null
    fun setOnItemClickListener(f: (Int) -> Unit) {
        onItemClickListener = f
    }
}

class MainActivity : AppCompatActivity() {

    private var currencies: MutableList<Currency> = mutableListOf()
    private lateinit var btn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btn = findViewById(R.id.loadButton)

        btn.setOnClickListener {
            val scope = CoroutineScope(Dispatchers.IO)
            scope.launch {
                val xml = URL("https://www.cbr.ru/scripts/XML_daily.asp")
                    .readText(Charset.forName("Windows-1251"))
                val parser = XmlPullParserFactory.newInstance().newPullParser()
                parser.setInput(StringReader(xml))
                var parseTag = ""
                var parseNext = false
                var currencyName = ""
                var currencyValue = ""
                while (parser.eventType != XmlPullParser.END_DOCUMENT) {
                    if (parser.eventType == XmlPullParser.START_TAG)
                        when (parser.name) {
                            "Name" -> {
                                parseNext = true
                                parseTag = "Name"
                            }
                            "Value" -> {
                                parseNext = true
                                parseTag = "Value"
                            }
                        }
                    else if (parser.eventType == XmlPullParser.TEXT && parseNext)
                        when (parseTag) {
                            "Name" -> {
                                Log.d("PARSE", parser.text)
                                parseNext = false
                                currencyName = parser.text
                            }
                            "Value" -> {
                                parseNext = false
                                currencyValue = parser.text
                            }
                        }
                    else if (parser.eventType == XmlPullParser.END_TAG && parser.name == "Valute") {
                        currencies.add(Currency(currencyName, currencyValue))
                        currencyName = ""
                        currencyValue = ""
                    }
                    parser.next()
                }
                withContext(Dispatchers.Main) {
                    supportFragmentManager.beginTransaction().replace(R.id.fragmment_container_view, CurrencyListFragment.newInstance(currencies)).commit()
                }
            }
            supportFragmentManager.beginTransaction().replace(R.id.fragmment_container_view, LoaderFragment()).commit()
        }


    }
}