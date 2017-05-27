package spaceapps.isaid.jp.pilotplus

import android.os.Bundle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment


class AircraftActivity : BaseActivity(), OnMapReadyCallback {
    private val TAG = this.localClassName

    private var mMap: GoogleMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_aircraft)


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap



    }

    override fun onConnected(bundle: Bundle?) {
        super.onConnected(bundle)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }




}
