package fi.tuni.weatherapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import kotlin.math.roundToInt


@JsonIgnoreProperties(ignoreUnknown = true)
data class Forecast(
    var list : MutableList<WeatherItem>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherItem(

    var weather : MutableList<Info>? = null,
    var main : Temperature? = null,
    var dt_txt : String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Info(
    var description : String? = null,
)

class ForecastActivity : AppCompatActivity() {

    private lateinit var recyclerView : RecyclerView
    private var forecast : String? = null
    var unit : String? = null
    var tempLabel : String? = null

    private var extras: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forecast_main)

        recyclerView = findViewById(R.id.recyclerView)
        extras= intent.extras

        if(extras != null) {
            forecast = extras!!.getString("forecast")
            unit = extras!!.getString("unit")
            if(forecast != null) {
                Log.d("forecast", forecast!!)
                processForecast(forecast!!)
            }
        }
    }

    //Adapter for displaying forecast info cards
    inner class ViewAdapter(private var itemList : MutableList<WeatherItem>) : RecyclerView.Adapter<ViewAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.forecast_row,
                parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val currentItem = itemList[position]
            holder.descText.text = currentItem.weather?.get(0)?.description ?: "Not found"
            holder.timeStamp.text = currentItem.dt_txt

            //Set unit label for temperature
            when(unit) {
                "metric" -> tempLabel = "°C"
                "imperial" -> tempLabel = "°F"
            }

            val tempLabelText = currentItem.main?.temp?.roundToInt().toString() + tempLabel
            holder.tempText.text = tempLabelText
        }

        override fun getItemCount() = itemList.size

        inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
            val timeStamp : TextView = itemView.findViewById(R.id.forecast_time)
            val descText : TextView = itemView.findViewById(R.id.forecast_description)
            val tempText : TextView = itemView.findViewById(R.id.forecast_temp)
        }
    }

    private fun processForecast(forecast : String) {

        val result : Forecast = ObjectMapper().readValue(forecast, Forecast::class.java)
        //Log.d("forecast", result.toString())
        val list = result.list

        if (list != null) {
            for (item in list) {

                //Capitalize the first letter of each description
                val descText = item.weather!![0].description
                if (descText != null) {
                    item.weather!![0].description = descText.replaceFirstChar { it.uppercase() }
                }

                Log.d("forecast", item.dt_txt!!)
                //Send list to adapter
                runOnUiThread {
                    //Set adapter with person list
                    recyclerView.adapter = ViewAdapter(list)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.setHasFixedSize(true)
                }
            }
        }
    }

}