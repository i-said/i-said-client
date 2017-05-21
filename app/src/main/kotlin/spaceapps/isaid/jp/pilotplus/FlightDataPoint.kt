package spaceapps.isaid.jp.pilotplus

import java.io.Serializable

/**
 * Created by iwsbrfts on 17/04/29.
 */

class FlightDataPoint : Cloneable, Serializable {
    var timestamp: Long = 0
    var waittime: Long = 0
    var lat: Float = 0.toFloat()
    var lon: Float = 0.toFloat()
    var callSigne: String? = null //コールサイン
    var speed: Long = 0 //スピード
    var altitude: Long = 0 //高さ
    var direction: Int = 0 //方向
    var isDummy = false

    public override fun clone(): FlightDataPoint {

        var point = FlightDataPoint()
        try {
            point = super.clone() as FlightDataPoint
        } catch (e: CloneNotSupportedException) {
            e.printStackTrace()
        }

        return point

    }

    override fun toString(): String {
        return "time:$timestamp lat:$lat lon:$lon direction:$direction wait:$waittime speed:$speed isDummy:$isDummy"
    }
}
