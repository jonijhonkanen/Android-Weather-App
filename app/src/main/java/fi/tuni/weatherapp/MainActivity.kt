package fi.tuni.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    //UI Functionalities
    lateinit var button : Button

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        apikey = getString(R.string.api_key)
        lang = "en"
        locText = "Tampere"
        unit = "metric"

        val url = "https://api.openweathermap.org/data/2.5/weather?q=${locText}&units=${unit}&lang=${lang}&appid=${apikey}"
        //https://api.openweathermap.org/data/2.5/weather?q=Tampere&units=metric&lang=en&appid=fd3e1dc8b00f86224410cd96b97454eb

        location = findViewById(R.id.location_name)
        desc = findViewById(R.id.description)
        windText = findViewById(R.id.wind)
        temperatureText = findViewById(R.id.temperature)

        button = findViewById(R.id.button)

    }


}