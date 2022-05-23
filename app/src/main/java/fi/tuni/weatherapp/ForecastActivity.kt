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

/*
* Activity for displaying a weather forecast list.
* This activity receives a bundle with a json string and a unit label for
* determining the temperature unit to be displayed.
*/
class ForecastActivity : AppCompatActivity() {

    //Recycler View for the list display
    private lateinit var recyclerView : RecyclerView

    //Forecast json string from an intent extra
    private var forecast : String? = null

    //Used for determining temperature text unit
    var unit : String? = null
    var tempLabel : String? = null

    //For data received from Main activity
    private var extras: Bundle? = null

    //Create the activity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forecast_main)

        recyclerView = findViewById(R.id.recyclerView)
        extras= intent.extras

        //Process the extras if bundle is not null
        if(extras != null) {
            //Json string
            forecast = extras!!.getString("forecast")

            //Unit for temperature (celsius or fahrenheit)
            unit = extras!!.getString("unit")
            if(forecast != null) {
                Log.d("forecast", forecast!!)
                processForecast(forecast!!)
            }
        }
    }

    //Adapter for displaying forecast info cards
    /*
    * ViewAdapter is used to display the list of WeatherItem objects.
    * The adapter inherits an adapter with a custom ViewHolder for View display.
    *
    * @param itemList The list of WeatherItem objects that contain data for UI text views.
    */
    inner class ViewAdapter(private var itemList : MutableList<WeatherItem>) : RecyclerView.Adapter<ViewAdapter.MyViewHolder>() {

        /*
        * Required method of ViewAdapter.
        *
        * @param parent A ViewGroup needed for layout inflation
        * @param viewType An integer based value for a view type.
        * @return A custom ViewHolder with a specific View
        */
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
            //Use forecast_row.xml as the base component for views
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.forecast_row,
                parent, false)
            return MyViewHolder(itemView)
        }

        /*
        * Required method of ViewAdapter.
        *
        * @param holder A custom ViewHolder needed for the adapter.
        * @param position An index value for the item in the given list.
        * @return A custom ViewHolder with a specific View
        */
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            val currentItem = itemList[position]
            holder.descText.text = currentItem.weather?.get(0)?.description
            holder.timeStamp.text = currentItem.dt_txt

            //Set unit label for temperature
            when(unit) {
                "metric" -> tempLabel = "°C"
                "imperial" -> tempLabel = "°F"
            }

            val tempLabelText = currentItem.main?.temp?.roundToInt().toString() + tempLabel
            holder.tempText.text = tempLabelText
        }

        /*
        * Required method of ViewAdapter.
        *
        * @return The size of a given list.
        */
        override fun getItemCount() = itemList.size

        /*
        * A custom ViewHolder for displaying forecast cards and their text views.
        *
        * @param itemView A view that has different Text views.
        */
        inner class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) {
            val timeStamp : TextView = itemView.findViewById(R.id.forecast_time)
            val descText : TextView = itemView.findViewById(R.id.forecast_description)
            val tempText : TextView = itemView.findViewById(R.id.forecast_temp)
        }
    }

    /*
    * Used to process the given json and sending it to the adapter.
    *
    * @param forecast A Json string that is processed into data classes.
    */
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

                //Log.d("forecast", item.dt_txt!!)
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