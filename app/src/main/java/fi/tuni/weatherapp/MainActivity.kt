package fi.tuni.weatherapp

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
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
    lateinit var textField : EditText
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //UI Image
    lateinit var weatherImage : ImageView
    var weatherIcon : Bitmap? = null

    //UI Text
    lateinit var locationTextView : TextView
    lateinit var desc : TextView
    lateinit var windText : TextView
    lateinit var temperatureText : TextView

    //UI Variables
    private lateinit var apikey : String
    lateinit var lang : String
    lateinit var locText : String
    lateinit var unit : String
    lateinit var iconCode : String
    //Using Tampere as default
    var lat : Double? = 61.4991
    var lon : Double? = 23.7871
    var url : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        weatherImage = findViewById(R.id.icon)

        apikey = getString(R.string.api_key)
        lang = "en"
        locText = "Tampere"
        unit = "metric"
        iconCode = ""

        //url = "https://api.openweathermap.org/data/2.5/weather?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}"
        //https://api.openweathermap.org/data/2.5/weather?q=Tampere&units=metric&lang=en&appid=fd3e1dc8b00f86224410cd96b97454eb

        //https://api.openweathermap.org/data/2.5/weather?lat=${gpsLocation.lat}&lon=${gpsLocation.lon}&units=metric&lang=${lang}&appid=${APIKey}

        locationTextView = findViewById(R.id.location_name)
        desc = findViewById(R.id.description)
        windText = findViewById(R.id.wind)
        temperatureText = findViewById(R.id.temperature)

        textField = findViewById(R.id.editText)

        //val locationManager : LocationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        button = findViewById(R.id.button)
        button.setOnClickListener() {
            makeRequest()
        }
    }

    //Operate UI changes
    private fun makeRequest() {
        //Log.d("valmisjson", locText)
        //Log.d("valmisjson", textField.text.toString())

        /*
        if(textField.text.isEmpty()) {
            Log.d("valmisjson", "Use current location!")
        }*/

        //Location update here
        //var currentLoc : Location =
        fetchLocation()

        this.url = when(textField.text.isEmpty()) {
            false -> "https://api.openweathermap.org/data/2.5/weather?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}"
            true -> "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=${unit}&lang=${lang}&appid=${apikey}"
        }
        Log.d("valmisjson", url)
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
                //Log.d("valmisjson", result.toString())

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
                    //Log.d("valmisjson", iconCode)
                    weatherIcon = fetchImage(iconCode)
                }

                //Update UI with fetched data
                runOnUiThread() {
                    if(weatherIcon != null) {
                        //Log.d("valmisjson", "Weather icon not null!")
                        weatherImage.setImageBitmap(weatherIcon)
                    } else {
                        Log.d("valmisjson", "No valid icon!")
                    }

                    //UI Texts
                    desc.text = test
                    temperatureText.text = t
                    windText.text = windSpeed

                    if (textField.text.isEmpty()) "At current location" else locText = textField.text.toString()

                }
            }
        }
    }

    //Fetch data
    private fun fetchData(url : String) : String? {
        //Log.d("haku", "Fetching data!")

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

    private fun fetchLocation() {
        Log.d("valmisjson", "Fetching current location!")
        //Request permission for location

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                Log.d("valmisjson", location.toString())
                if (location != null) {
                    lat = location.latitude
                    lon = location.longitude

                    Log.d("valmisjson", lat.toString())
                    Log.d("valmisjson", lon.toString())
                } else {
                    lat = null
                    lon = null
                }
            }



    }

    /*
        try {
            if (ContextCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    101
                )
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }*/

}