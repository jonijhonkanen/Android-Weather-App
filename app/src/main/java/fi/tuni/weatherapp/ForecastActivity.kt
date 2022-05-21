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
    private var extras: Bundle? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forecast_main)

        recyclerView = findViewById(R.id.recyclerView)


        extras= intent.extras

        if(extras != null) {
            forecast = extras!!.getString("forecast")
            if(forecast != null) {
                Log.d("forecast", forecast!!)
                processForecast(forecast!!)
            }
        }

    }

    //This can be an independent file
    class ViewAdapter(private var itemList : MutableList<WeatherItem>) : RecyclerView.Adapter<ViewAdapter.MyViewHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.forecast_row,
                parent, false)
            return MyViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val currentItem = itemList[position]
            holder.descText.text = currentItem.weather?.get(0)?.description ?: "Not found"
            holder.tempText.text = currentItem.main?.temp.toString()
        }

        override fun getItemCount() = itemList.size

        class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
            val descText : TextView = itemView.findViewById(R.id.forecast_description)
            val tempText : TextView = itemView.findViewById(R.id.forecast_temp)
        }
    }

    private fun processForecast(forecast : String) {

        val result : Forecast = ObjectMapper().readValue(forecast, Forecast::class.java)
        Log.d("forecast", result.toString())
        val list = result.list

        if (list != null) {
            for (item in list) {
                val descText = item.weather!![0].description
                if (descText != null) {
                    item.weather!![0].description = descText.replaceFirstChar { it.uppercase() }
                }
                //.replaceFirstChar { it.uppercase() }
                item.weather?.get(0)?.let { it.description?.let { it1 -> Log.d("forecast", it1) } }
                Log.d("forecast", item.toString())

                //Send list to adapter
                runOnUiThread {

                    //Set adapter with person list
                    recyclerView.adapter = ViewAdapter(list)
                    recyclerView.layoutManager = LinearLayoutManager(this)
                    recyclerView.setHasFixedSize(true)

                    /*
                    list.forEach {
                        //adapter.add(it)
                        //callback(it.toString())
                    }*/
                }
            }
        }
    }



}