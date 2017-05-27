package spaceapps.isaid.jp.pilotplus.flightrader24

import android.content.Context
import com.google.android.gms.maps.model.LatLngBounds
import com.google.gson.JsonObject
import com.koushikdutta.async.future.FutureCallback
import com.koushikdutta.ion.Ion

/**
 * Created by iwsbrfts on 17/05/20.
 */
class Api {
    fun getFeed(context: Context, bounds:LatLngBounds,callback: FutureCallback<JsonObject>) {
        val url = getUrl(bounds)
        Ion.with(context)
                .load(url)
                .asJsonObject()
                .setCallback(callback)
    }


    private fun getUrl(bounds:LatLngBounds) : String {

        val boundsStr = "${bounds.northeast.latitude.toString()},${bounds.southwest.latitude.toString()},${bounds.northeast.longitude.toString()},${bounds.southwest.longitude.toString()}"

        val url = "https://data-live.flightradar24.com/zones/fcgi/feed.js?bounds=${boundsStr}&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=1&estimated=1&maxage=7200&gliders=1&stats=1"

        return url
    }
}