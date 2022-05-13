package fi.tuni.weatherapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.concurrent.thread


//Provide data classes
/*
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherResult(
    var list : MutableList<WeatherItem>? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherItem(
    var weather : Weather? = null,
)
*/
@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(
    var weather : MutableList<Main>? = null,
    var main: Temperature? = null,
    var wind: Wind? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Main(
    var description: String = "",
    var icon : String? = null
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Temperature(
    var temp: Double? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Wind(
    var speed: Double? = null,
)

class MainActivity : AppCompatActivity() {

    //UI Functionalities
    lateinit var button : Button

    //UI Image
    lateinit var weatherImage : ImageView
    var weatherIcon : Bitmap? = null

    //UI Text
    lateinit var location : TextView
    lateinit var desc : TextView
    lateinit var windText : TextView
    lateinit var temperatureText : TextView

    //UI Variables
    private lateinit var apikey : String
    lateinit var lang : String
    lateinit var locText : String
    lateinit var unit : String
    lateinit var iconCode : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherImage = findViewById(R.id.icon)

        apikey = getString(R.string.api_key)
        lang = "en"
        locText = "Tampere"
        unit = "metric"
        iconCode = ""

        val url = "https://api.openweathermap.org/data/2.5/weather?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}"
        //https://api.openweathermap.org/data/2.5/weather?q=Tampere&units=metric&lang=en&appid=fd3e1dc8b00f86224410cd96b97454eb

        location = findViewById(R.id.location_name)
        desc = findViewById(R.id.description)
        windText = findViewById(R.id.wind)
        temperatureText = findViewById(R.id.temperature)

        button = findViewById(R.id.button)
        button.setOnClickListener() {
            makeRequest(url)
        }
    }

    //Operate UI changes
    private fun makeRequest(url : String) {
        Log.d("valmisjson", locText)

        thread() {

            //This works
            val json : String? = fetchData(url)
            if (json != null) {
                Log.d("valmisjson", json)
            }

            val result : Weather? = ObjectMapper().readValue(json, Weather::class.java)
            val list = result?.weather
            val t = result?.main?.temp.toString()
            val windSpeed = result?.wind?.speed.toString()


            if(list != null){
                Log.d("valmisjson", result.toString())

                /*
                for(item : Main? in list) {
                    Log.d("valmisjson", item.toString())
                }*/

                val test = list[0].description.replaceFirstChar { it.uppercase() }
                val iconCode = list[0].icon
                //Log.d("valmisjson", test)
                //Log.d("valmisjson", t)
                //Log.d("valmisjson", windSpeed)

                //Check icon code validity
                if (iconCode != null && iconCode.isNotEmpty()) {
                    Log.d("valmisjson", iconCode)
                    weatherIcon = fetchImage(iconCode)
                }

                //Update UI with fetched data
                runOnUiThread() {
                    if(weatherIcon != null) {
                        Log.d("valmisjson", "Weather icon not null!")
                        weatherImage.setImageBitmap(weatherIcon)
                    } else {
                        Log.d("valmisjson", "No valid icon!")
                    }

                    //UI Texts
                    desc.text = test
                    temperatureText.text = t
                    windText.text = windSpeed

                }
            }
        }
    }

    //Fetch data
    private fun fetchData(url : String) : String? {
        Log.d("haku", "Fetching data!")

        var result: String?
        val buffer = StringBuffer()
        val myUrl = URL(url)
        val conn = myUrl.openConnection() as HttpURLConnection
        val inputStream = conn.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.use {
            var line: String?
            do {
                line = it.readLine()
                buffer.append(line)
            } while (line != null)
            result = buffer.toString()
        }

        if (result != null) {
            Log.d("haku", result!!)

        }

        return result

    }

    private fun fetchImage(imageCode : String) : Bitmap? {
        //Icon from URL
        val imageUrl = "https://openweathermap.org/img/w/$imageCode.png"

        var bm : Bitmap?

        try {
            bm = BitmapFactory.decodeStream(URL(imageUrl).content as InputStream)
        } catch (e: Exception) {
            e.printStackTrace()
            bm = null
            return bm
        }

        return bm

    }

    /*
    *
    *
    * */

}