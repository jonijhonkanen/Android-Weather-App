package fi.tuni.weatherapp

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

//Main activity data classes

//Contains a list of Main objects, a Temperature object and Wind object
@JsonIgnoreProperties(ignoreUnknown = true)
data class Weather(
    var weather : MutableList<Main>? = null,
    var main: Temperature? = null,
    var wind: Wind? = null,
)

//Contains description text and icon id text
@JsonIgnoreProperties(ignoreUnknown = true)
data class Main(
    var description: String = "",
    var icon : String? = null
)

//Contains a temperature value as double
@JsonIgnoreProperties(ignoreUnknown = true)
data class Temperature(
    var temp: Double? = null,
)

//Contains a wind speed value as double
@JsonIgnoreProperties(ignoreUnknown = true)
data class Wind(
    var speed: Double? = null,
)

//Forecast list contains WeatherItem objects
@JsonIgnoreProperties(ignoreUnknown = true)
data class Forecast(
    var list : MutableList<WeatherItem>? = null
)

//Forecast single card
//Contains a list of Info objects, a Temperature object and a timestamp text
@JsonIgnoreProperties(ignoreUnknown = true)
data class WeatherItem(
    var weather : MutableList<Info>? = null,
    var main : Temperature? = null,
    var dt_txt : String? = null
)

//Contains Forecast card description text
@JsonIgnoreProperties(ignoreUnknown = true)
data class Info(
    var description : String? = null,
)