package spaceapps.isaid.jp.pilotplus

import java.io.Serializable

/**
 * Created by iwsbrfts on 2017/04/30.
 */

class PoiData : Serializable {
    var name: String? = null
    var lng: Float = 0.toFloat()
    var lat: Float = 0.toFloat()
    var description: String? = null
    var image: String? = null
}
