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
import kotlin.math.roundToInt

/*
* Main activity of the application.
* This activity contains the main view for displaying current weather.
*/
class MainActivity : AppCompatActivity() {

    //UI Functionalities
    lateinit var button : Button
    lateinit var forecastButton : Button
    lateinit var textField : EditText

    //Device location provider
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    //UI Image
    lateinit var weatherImage : ImageView
    var weatherIcon : Bitmap? = null

    //UI Texts
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
    private var count : Int = 16
    var lat : Double? = null
    var lon : Double? = null
    var url : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Default image
        weatherImage = findViewById(R.id.icon)

        //UI Components
        locationTextView = findViewById(R.id.location_name)
        desc = findViewById(R.id.description)
        windText = findViewById(R.id.wind)
        temperatureText = findViewById(R.id.temperature)
        textField = findViewById(R.id.editText)

        //UI function variables
        apikey = getString(R.string.api_key)
        lang = "en"
        locText = ""
        unit = "metric"
        iconCode = ""

        //Location service
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        //Current weather button
        button = findViewById(R.id.button)
        button.setOnClickListener() {
            makeRequest()
        }

        //Forecast button
        forecastButton = findViewById(R.id.forecast_button)
        forecastButton.setOnClickListener() {
            fetchForecast()
        }
        fetchLocation()

    }

    /*
    * Make a request for current weather data.
    *
    * This function calls the fetchData() function with a specific url and updates the UI based on the
    * received json string. The json is parsed and the relevant data is stored into the data classes
    * in the Dataclasses.kt file. The UI texts are based on the same data and they are updated
    * if the json is successfully received.
    */
    private fun makeRequest() {

        //Disable button while processing data fetch
        runOnUiThread {
            button.isEnabled = false
        }

        //Use current text input for location
        locText = textField.text.toString()

        //Location update here, if no location given
        if (locText.isEmpty()){
            Log.d("valmisjson", "Fetching location...")
            fetchLocation()
        }

        Log.d("valmisjson", lon.toString())
        Log.d("valmisjson", lat.toString())

        //Choose between specific location query or query with
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
            var temp : Double? = null
            var windSpeed : Double? = null

            //Process json if not null
            if (json != null) {
                Log.d("valmisjson", json)
                val result : Weather? = ObjectMapper().readValue(json, Weather::class.java)
                list = result?.weather
                temp = result?.main?.temp
                windSpeed = result?.wind?.speed
            }

            //Process only if json result list is valid
            if(list != null){

                //Capitalize the first letter of description
                val uiDescText = list[0].description.replaceFirstChar { it.uppercase() }

                //Set icon code
                val iconCode = list[0].icon

                //Check icon code validity
                if (iconCode != null && iconCode.isNotEmpty()) {
                    //Log.d("valmisjson", iconCode)
                    //Fetch a weather icon
                    weatherIcon = fetchImage(iconCode)
                }

                //Update UI with fetched data
                runOnUiThread() {

                    //Set weather icon image
                    if(weatherIcon != null) {
                        //Log.d("valmisjson", "Weather icon not null!")
                        weatherImage.setImageBitmap(weatherIcon)
                    } else {
                        Log.d("valmisjson", "No valid icon!")
                    }

                    //UI Texts
                    desc.text = uiDescText
                    if (temp != null) {
                        temperatureText.text = getString(R.string.temp_cel, temp.roundToInt().toString())
                    }
                    if (windSpeed != null) {
                        windText.text = getString(R.string.wind_metric, windSpeed.roundToInt().toString())
                    }
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

    /*
    * Fetch weather data from OpenWeatherMap
    *
    * @param url A url string for data query.
    * @return Returns a string of json data or null
    */
    private fun fetchData(url : String) : String? {
        //Log.d("haku", "Fetching data!")

        var result: String?
        val buffer = StringBuffer()
        val myUrl = URL(url)

        //Create connection and get json response as string
        try {
            val conn = myUrl.openConnection() as HttpURLConnection
            val inputStream = conn.inputStream
            val reader = BufferedReader(InputStreamReader(inputStream))

            //Construct a string of text via buffer
            reader.use {
                var line: String?
                do {
                    line = it.readLine()
                    buffer.append(line)
                } while (line != null)
                result = buffer.toString()
            }

            return result
        } catch (e : Exception) {
            e.printStackTrace()
            return null
        }

    }

    /*
    * Fetch the respective weather icon based on given icon code.
    *
    * @param imageCode Icon id code for the url.
    * @return Returns a bitmap image or null.
    */
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
    * Fetch current latitude and longitude of the device.
    */
    private fun fetchLocation() {
        Log.d("locFetch", "Fetching current location!")
        //Request permission for location
        if(checkPermissions()) {

            //Check if device location is enabled
            if(isLocationEnabled()) {

                //Check location access permissions
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //Open permission dialogue
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                        PERMISSION_REQUEST_ACCESS_LOCATION
                    )
                    return
                }
                //Get last known location
                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    val location : Location? = task.result
                    if(location != null) {
                        //Log.d("locFetch", location.toString())

                        //Set latitude and longitude
                        lon = location.longitude
                        lat = location.latitude

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

    //Used for permission requests
    companion object {
        const val PERMISSION_REQUEST_ACCESS_LOCATION = 100
    }

    //To be removed
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

    /*
    * Makes requests for location permissions.
    */
    private fun makePermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf( Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_REQUEST_ACCESS_LOCATION
        )
    }

    /*
    * Notifies the user about the permission status
    *
    * @param requestCode Request code id as integer.
    * @param permissions Array of request permissions.
    * @param grantResults Grant results for the corresponding permissions
    */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == PERMISSION_REQUEST_ACCESS_LOCATION) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "Permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(applicationContext, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }


    /*
    * Gives a confirmation of location provider, either GPS or Network based.
    *
    * @return A boolean result whether the GPS or Network provider location is enabled
    */
    private fun isLocationEnabled() : Boolean {
        val locationManager : LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //Return either GPS or Internet
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }


    /*
    * Fetches the forecast and sends the json string and unit string to Forecast activity.
    * This function calls the fetchData() function with a specific url and
    * sends the received json string forward to Forecast activity, if the string is not
    * null. Otherwise, the user is notified about the failed fetch with a Toast message.
    */
    private fun fetchForecast() {
        locText = textField.text.toString()

        forecastButton.isEnabled = false
        Log.d("forecast", "Fetching forecast")
        Log.d("forecast", locText)
        //Check if the location is defined
        if (locText.isEmpty()){
            Log.d("valmisjson", "Fetching location...")
            fetchLocation()
        }

        //Define the url needed
        this.url = when(locText.isEmpty()) {
            false -> "https://api.openweathermap.org/data/2.5/forecast?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}&cnt=${count}"
            true -> "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&units=${unit}&lang=${lang}&appid=${apikey}&cnt=${count}"
        }
        //Log.d("forecast", url)

        thread {
            //Get the json
            val forecastJson : String? = fetchData(url)

            if(forecastJson != null) {
                //Send json data to another activity
                //Log.d("forecast", forecastJson)
                val forecastIntent = Intent(this, ForecastActivity::class.java)

                //Set extras
                forecastIntent.putExtra("forecast", forecastJson)
                forecastIntent.putExtra("unit", unit)

                //Re-enable the button
                runOnUiThread { forecastButton.isEnabled = true }
                startActivity(forecastIntent)
            } else {
                Log.d("forecast", "Forecast fetch failed!")
                runOnUiThread {
                    Toast.makeText(this, "Forecast fetch failed!", Toast.LENGTH_SHORT).show()
                    forecastButton.isEnabled = true
                }
            }
        }
    }
}