package spaceapps.isaid.jp.pilotplus.flightrader24

/**
 * Created by iwsbrfts on 17/05/20.
 */
class Feed {
    var fullcount: Int = 0
    var version : Int = 0
    var flightDatas = linkedMapOf<String, FlightData>()

    class FlightData {
        var id: String = ""
        var ssrModeS : String = ""
        var lat :Float = 0.0f
        var lng :Float = 0.0f
        var direction : Int = 0
        var heightFeet : Int = 0
        var speedKnot : Int = 0
        var squawk :String = ""
        var rcvRadar :String = ""
        var aircraftType : String = ""
        var icaoCode = ""
        var time = 0
        var startAirport = ""
        var endAirport = ""
        var iataCode = ""
        var temp1 = 0
        var temp2 = 0
        var callSign = ""
        var temp3 = 0
    }

}