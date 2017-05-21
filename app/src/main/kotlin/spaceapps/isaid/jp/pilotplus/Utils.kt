package spaceapps.isaid.jp.pilotplus

import android.content.Context
import android.location.Location
import android.util.Log

import com.google.gson.reflect.TypeToken
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.LinkedList
import java.util.Locale

/**
 * Created by iwsbrfts on 17/04/29.
 */

object Utils {
    private val TAG = Utils::class.java!!.getSimpleName()

    private val TIME_DIVIDE = 30
    private val TIME_SPEED = 1000 / 8


    fun loadCsv(context: Context, filename: String): List<FlightDataPoint>? {

        try {
            return loadCsvReadAndReverse(context.assets.open(filename))

        } catch (e: Exception) {
            Log.d(TAG, "failed", e)
        }

        return null
    }


    fun loadCsvDummyData(list: List<FlightDataPoint>): List<FlightDataPoint>? {
        try {
            //            List<FlightDataPoint> list = loadCsvReadAndReverse(in);

            val retData = LinkedList<FlightDataPoint>()

            var old: FlightDataPoint? = null
            for (point in list) {

                if (old == null) {
                    old = point
                    retData.add(point)
                    continue
                }

                val time = (point.timestamp - old.timestamp).toInt()
                val results = FloatArray(3)
                Location.distanceBetween(old.lat.toDouble(), old.lon.toDouble(), point.lat.toDouble(), point.lon.toDouble(), results)

                val distance = results[0]
                val y = distance / (time * 1f)
                Log.d(TAG, "distance:$distance waittime:$time y:$y")

                val count = Math.floor((time / TIME_DIVIDE).toDouble()).toInt()

                val floatLat = point.lat - old.lat
                val diffLat = floatLat / count

                val floatLon = point.lon - old.lon
                val diffLon = floatLon / count

                Log.d(TAG, "x:$count diffLat:$diffLat diffLon$diffLon")

                var i = 1
                val max = count + 1
                while (i < max) {

                    val next = old!!.clone()
                    next.speed = point.speed
                    next.altitude = point.altitude
                    next.timestamp = next.timestamp + TIME_DIVIDE
                    next.waittime = (next.timestamp - old.timestamp) * TIME_SPEED
                    next.lat = next.lat + diffLat
                    next.lon = next.lon + diffLon
                    Location.distanceBetween(old.lat.toDouble(), old.lon.toDouble(), next.lat.toDouble(), next.lon.toDouble(), results)

                    old.direction = results[1].toInt()

                    next.isDummy = true

                    if (point.timestamp < next.timestamp) {
                        break
                    }
                    Log.d(TAG, next.toString())

                    retData.add(next)

                    old = next
                    i++

                }


            }

            return retData
        } catch (e: Exception) {
            Log.d(TAG, "failed", e)
        }

        return null
    }

    fun formattedTimestamp(timestamp: Long): String {
        val timeFormat = "HH:mm"
        Log.d(TAG, timestamp.toString())
        val ts = Timestamp(timestamp * 1000)
        return SimpleDateFormat(timeFormat, Locale.JAPAN).format(ts)
    }


    fun loadCsvReadAndReverse(`in`: InputStream): List<FlightDataPoint>? {
        try {
            val list = LinkedList<FlightDataPoint>()
            val br = BufferedReader(InputStreamReader(`in`))
            var line: String

            line = br.readLine()
            while (line != null) {
                if (line.contains("Timestamp")) continue
                line = line.replace("\"", "")
                val data = line.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()

                val point = FlightDataPoint()
                point.timestamp = java.lang.Long.valueOf(data[0])!!
                point.callSigne = data[2]
                point.lat = java.lang.Float.valueOf(data[3])!!
                point.lon = java.lang.Float.valueOf(data[4])!!
                point.altitude = java.lang.Long.valueOf(data[5])!!
                point.speed = java.lang.Long.valueOf(data[6])!!
                point.direction = Integer.valueOf(data[7])!!


                list.add(point)
                line = br.readLine()
            }

            Collections.reverse(list)

            return list

        } catch (e: IOException) {
            Log.d(TAG, "failed", e)
        }

        return null
    }


    fun getDataList(context: Context, lat: Float, lon: Float, callback: FutureCallback<List<PoiData>>) {
        val url = "https://spaceapp-i-said.mybluemix.net/api/poi-sample?lat=$lat&lng=$lon"
        Log.d(TAG, "url:" + url)
        Ion.with(context)
                .load(url)
                .`as`(object : TypeToken<List<PoiData>>() {

                })
                .setCallback(callback)
    }


}
