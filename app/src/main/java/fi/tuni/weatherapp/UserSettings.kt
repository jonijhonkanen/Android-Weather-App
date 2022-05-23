package fi.tuni.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/*
* Provides simple user settings.
* Currently only setting option available is the unit of measurement.
*/
class UserSettings : AppCompatActivity() {

    //UI Components
    private lateinit var title : TextView
    private lateinit var buttonsTitle : TextView
    lateinit var saveButton: Button
    lateinit var unitButtons : RadioGroup
    lateinit var metricButton : RadioButton
    lateinit var imperialButton : RadioButton
    lateinit var sharedPreferences: SharedPreferences

    //Data values for Shared Preferences
    private var buttonUnit : String? = null
    private var buttonChecked : Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_main)

        //Get data from shared preferences
        sharedPreferences = getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE)
        buttonUnit = sharedPreferences.getString("UNIT_KEY", "metric")
        buttonChecked = sharedPreferences.getInt("buttonChecked", 0)

        //UI Components
        title = findViewById(R.id.settingsTitle)
        buttonsTitle = findViewById(R.id.buttonsTitle)
        saveButton = findViewById(R.id.saveButton)
        unitButtons = findViewById(R.id.unitChoice)
        metricButton = findViewById(R.id.unit_metric)
        imperialButton = findViewById(R.id.unit_imperial)
        Log.d("settings", buttonChecked.toString())

        //Set button checked
        if(buttonChecked == 1) {
            metricButton.isChecked = true
        } else if (buttonChecked == 0) {
            imperialButton.isChecked = true
        }

        //For data saving
        saveButton.setOnClickListener {
            saveData()
        }
    }

    /*
    * Function keeps track on changes to UI buttons.
    *
    * @param view Current view (button) that calls the method
    */
    fun onRadioButtonClicked(view : View) {
        if(view is RadioButton) {
            val checked = view.isChecked

            when(view.getId()) {
                R.id.unit_metric -> if(checked) {
                    //Set unit to metric, save button as checked
                    buttonUnit = "metric"
                    buttonChecked = 1
                }
                R.id.unit_imperial -> if(checked) {
                    //Set unit to imperial, save button as checked
                    buttonUnit = "imperial"
                    buttonChecked = 0
                }
            }
        }
    }

    /*
    * Saves current changes to shared preferences.
    */
    private fun saveData() {

        val unitText : String? = buttonUnit
        if (unitText != null) {
            Log.d("settings", unitText)
        }

        //Save changes with editor
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("UNIT_KEY", unitText)
            buttonChecked?.let { putInt("buttonChecked", it) }
        }.apply()

        Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
    }

}