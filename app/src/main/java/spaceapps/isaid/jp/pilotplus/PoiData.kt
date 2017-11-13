package spaceapps.isaid.jp.pilotplus

import java.io.Serializable

/**
 * Created by iwsbrfts on 2017/04/30.
 */
data class PoiData(
        val name: String,
        val lng: Float,
        val lat: Float,
        val description: String,
        val image: String)
    : Serializable
