package com.example.weatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.IOException
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {
    lateinit var rlmain:RelativeLayout
    lateinit var rlzip:RelativeLayout
    lateinit var llerrorcontainer:LinearLayout
    lateinit var llRefresh:LinearLayout
    lateinit var progressBar: ProgressBar
    lateinit var tvlocation:TextView
    lateinit var tvlastupdate:TextView
    lateinit var tvstatus:TextView
    lateinit var tvtemperature:TextView
    lateinit var tvtempmin:TextView
    lateinit var tvtempmax:TextView
    lateinit var tvsunrais:TextView
    lateinit var tvsunset:TextView
    lateinit var tvwind:TextView
    lateinit var tvpressure:TextView
    lateinit var tvhumidity:TextView
    lateinit var tvrefresh:TextView
    lateinit var etzip:EditText
    lateinit var btzip:Button
    lateinit var bterror:Button
    private var city = "10001"
    private val API = "9dd73f229b085ce5fd11ed747f1d4252"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rlmain=findViewById(R.id.rlMain)
        rlzip=findViewById(R.id.rlZip)
        llRefresh=findViewById(R.id.llRefresh)
        llerrorcontainer=findViewById(R.id.llErrorContainer)
        progressBar=findViewById(R.id.progressBar)
        tvlocation=findViewById(R.id.tvlocation)
        tvlastupdate=findViewById(R.id.tvlastupdate)
        tvstatus=findViewById(R.id.tvstatus)
        tvtemperature=findViewById(R.id.tvTemperature)
        tvtempmin=findViewById(R.id.tvtempmin)
        tvtempmax=findViewById(R.id.tvtempmax)
        tvsunset=findViewById(R.id.tvsunset)
        tvsunrais=findViewById(R.id.tvsunrise)
        tvwind=findViewById(R.id.tvwind)
        tvpressure=findViewById(R.id.tvpressure)
        tvhumidity=findViewById(R.id.tvhumidity)
        tvrefresh=findViewById(R.id.tvrefresh)
        etzip=findViewById(R.id.etZip)
        btzip=findViewById(R.id.btZip)
        bterror=findViewById(R.id.btError)

        bterror.setOnClickListener {
            city="10001"
            requestApi()
        }

        btzip.setOnClickListener {
            city=etzip.text.toString()
            requestApi()
            rlzip.isVisible=false
        }

        requestApi()

    }

  fun fetchdata():String{
      var data=""
      try {
          data=URL("https://api.openweathermap.org/data/2.5/weather?zip=$city&units=metric&appid=$API").readText(Charsets.UTF_8)
      }catch (e:IOException){
          println("Error: $e")
      }
      return data
  }

    private suspend fun weatherData( Data:String){
        withContext(Dispatchers.Main){
            val js=JSONObject(Data)
            val main = js.getJSONObject("main")
            val sys = js.getJSONObject("sys")
            val wind = js.getJSONObject("wind")
            val weather = js.getJSONArray("weather").getJSONObject(0)

            val address=js.getString("name")+","+sys.getString("country")
            val lastUpdate=js.getLong("dt")
            val lastUpdateF="updated at :"+ SimpleDateFormat( "dd/mm/yyy hh:mm", Locale.ENGLISH).format(
                Date(lastUpdate*1000)
            )
            val temperature=main.getString("temp")
            val temperatureF=temperature.substring(0,temperature.indexOf("."))+ "°C"
            val tempMin=main.getString("temp_min")
            val tempMinF="Low:"+tempMin.substring(0,tempMin.indexOf("."))+ "°C"
            val tempMax=main.getString("temp_max")
            val tempMaxF="High:"+tempMax.substring(0,tempMax.indexOf("."))+ "°C"
            val pressure=main.getString("pressure")
            val humidity=main.getString("humidity")
            val sunrise=sys.getLong("sunrise")
            val sunriseF=SimpleDateFormat("hh:mm a",Locale.ENGLISH).format(Date(sunrise*1000))
            val sunset=sys.getLong("sunset")
            val sunsetF=SimpleDateFormat("hh:mm a",Locale.ENGLISH).format(Date(sunset*1000))
            val windSpeed=wind.getDouble("speed")
            val description=weather.getString("description")

            tvlocation.text=address
            tvlocation.setOnClickListener { rlzip.isVisible=true }
            tvlastupdate.text=lastUpdateF
            tvtemperature.text=temperatureF
            tvtempmin.text=tempMinF
            tvtempmax.text=tempMaxF
            tvpressure.text=pressure
            tvhumidity.text=humidity
            tvsunrais.text=sunriseF
            tvsunset.text=sunsetF
            tvwind.text=windSpeed.toString()
            tvstatus.text=description
            llRefresh.setOnClickListener { requestApi() }

        }
    }

    private fun requestApi(){
        CoroutineScope(Dispatchers.IO).launch {
            updateStatus(-1)
            val data=async {
                fetchdata()
            }.await()
            if (data.isNotEmpty()){
                weatherData(data)
                updateStatus(0)
            }else{
                updateStatus(1)
            }
        }
    }

    private suspend fun updateStatus(state: Int){
//        states: -1 = loading, 0 = loaded, 1 = error
        withContext(Main){
            when{
                state < 0 -> {
                    progressBar.visibility = VISIBLE
                    rlmain.visibility = GONE
                    llerrorcontainer.visibility = GONE
                }
                state == 0 -> {
                    progressBar.visibility = GONE
                    rlmain.visibility = VISIBLE
                }
                state > 0 -> {
                    progressBar.visibility = GONE
                    llerrorcontainer.visibility =VISIBLE
                }
            }
        }
    }

}

//http://api.openweathermap.org/data/2.5/weather?q=$city,uk&units=metric&APPID=$API