package fi.tuni.weatherapp

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
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
    /*
    * var lat : Double? = 61.4991
    var lon : Double? = 23.7871
    * */
    var lat : Double? = null
    var lon : Double? = null
    var url : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Default image
        weatherImage = findViewById(R.id.icon)

        locationTextView = findViewById(R.id.location_name)
        desc = findViewById(R.id.description)
        windText = findViewById(R.id.wind)
        temperatureText = findViewById(R.id.temperature)

        textField = findViewById(R.id.editText)

        apikey = getString(R.string.api_key)
        lang = "en"
        locText = ""
        unit = "metric"
        iconCode = ""

        //url = "https://api.openweathermap.org/data/2.5/weather?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}"
        //https://api.openweathermap.org/data/2.5/weather?q=Tampere&units=metric&lang=en&appid=fd3e1dc8b00f86224410cd96b97454eb

        //https://api.openweathermap.org/data/2.5/weather?lat=${gpsLocation.lat}&lon=${gpsLocation.lon}&units=metric&lang=${lang}&appid=${APIKey}

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        button = findViewById(R.id.button)
        button.setOnClickListener() {
            makeRequest()
        }
        fetchLocation()
    }

    //Operate UI changes
    private fun makeRequest() {

        //Disable button while processing data fetch
        runOnUiThread {
            button.isEnabled = false
        }
        //Log.d("valmisjson", locText)
        //Log.d("valmisjson", textField.text.toString())

        /*
        if(textField.text.isEmpty()) {
            Log.d("valmisjson", "Use current location!")
        }*/

        //Use current text input for location
        locText = textField.text.toString()

        //Location update here, if no location given
        if (locText.isEmpty() || (lat == null && lon == null)){
            Log.d("valmisjson", "Fetching location...")
            fetchLocation()
        }

        Log.d("valmisjson", lon.toString())
        Log.d("valmisjson", lat.toString())

        this.url = when(textField.text.isEmpty()) {
            false -> "https://api.openweathermap.org/data/2.5/weather?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}"
            true -> "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=${unit}&lang=${lang}&appid=${apikey}"
        }
        //Log.d("valmisjson", locText)
        Log.d("valmisjson", url)

        //Perform data fetch in a separate thread
        thread() {
            //Get json
            val json : String? = fetchData(url)

            var list : MutableList<Main>? = null
            var t : Double? = null
            var windSpeed : Double? = null

            //Process json if not null
            if (json != null) {
                Log.d("valmisjson", json)
                val result : Weather? = ObjectMapper().readValue(json, Weather::class.java)
                list = result?.weather
                t = result?.main?.temp
                windSpeed = result?.wind?.speed
            }

            //Process only if json result list is valid
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
                    temperatureText.text = getString(R.string.temp_cel, t.toString())
                    windText.text = getString(R.string.wind_metric, windSpeed.toString())
                    locationTextView.text = if (textField.text.isEmpty()) "At current location" else textField.text.toString()
                    button.isEnabled = true

                }
            } else {
                Log.d("valmisjson", "Non-valid data fetch!")
                runOnUiThread {
                    button.isEnabled = true
                    Toast.makeText(applicationContext, "Error while fetching data!", Toast.LENGTH_SHORT,).show()
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

        //This can fail
        try {
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
            /*
            if (result != null) {
                Log.d("haku", result!!)
            }*/

            return result
        } catch (e : Exception) {
            e.printStackTrace()
            return null
        }

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
        Log.d("locFetch", "Fetching current location!")
        //Request permission for location
        if(checkPermissions()) {
            if(isLocationEnabled()) {

                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_ACCESS_LOCATION
                    )
                    return
                }
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location : Location? = task.result
                    if(location != null) {
                        //Log.d("locFetch", location.toString())

                        lon = location.longitude
                        lat = location.latitude
                        url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&units=${unit}&lang=${lang}&appid=${apikey}"
                        Log.d("locFetch", lon.toString())
                        Log.d("locFetch", lat.toString())
                    } else {
                        Log.d("locFetch", "Location fetch failed")
                        Toast.makeText(this, "Location fetch failed!", Toast.LENGTH_SHORT).show()
                    }
                }

            } else {
                //Enable via settings
                Toast.makeText(this, "Allow location data", Toast.LENGTH_SHORT).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }

        } else {
            //Make permission request
            makePermissionRequest()
            return
        }
    }

    companion object {
        const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    private fun checkPermissions() : Boolean {
        //Check permissions first
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    private fun makePermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
                //Update location
                //fetchLocation()
            } else {
                Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun isLocationEnabled() : Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //Return either GPS or Internet
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /*
            //Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //return

            if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                101
            )
        }
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
        }

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.
                if (location != null) {
                    Log.d("locFetch", location.toString())
                    lat = location.latitude
                    lon = location.longitude

                    Log.d("locFetch", lat.toString())
                    Log.d("locFetch", lon.toString())
                } else {
                    lat = null
                    lon = null
                }
            }

        */

}