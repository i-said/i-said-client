package spaceapps.isaid.jp.pilotplus

import android.databinding.DataBindingUtil
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.support.annotation.WorkerThread
import android.support.v4.app.FragmentActivity
import android.util.Log
import android.util.SparseArray
import android.widget.ImageButton
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import com.koushikdutta.async.future.FutureCallback
import com.squareup.okhttp.OkHttpClient


import spaceapps.isaid.jp.pilotplus.databinding.ActivityMainBinding
import spaceapps.isaid.jp.pilotplus.oss.CachingUrlTileProvider.CachingUrlTileProvider
import java.io.InputStream
import java.util.*
import kotlin.String


class MainActivity : FragmentActivity(), OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener {

    private var isZoomControl = false

    private var mMap: GoogleMap? = null
    private var mImageButton: ImageButton? = null

    private val mMarkerArray: SparseArray<Any> = SparseArray()

    private var mOkHttpClient: OkHttpClient? = null
    private var mAirplaneMarker: Marker? = null

    private var mList: List<FlightDataPoint>? = null
    private var mIsAuto = true
    private var mAirplaneName: String? = null


    private var mSpeedInfoView: TextView? = null
    private var mAltitudeInfoView: TextView? = null
    private var mCurrentTimeInfoView: TextView? = null
    private var mBinding: ActivityMainBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mImageButton = mBinding!!.btnAuto
        mImageButton!!.setOnClickListener { mIsAuto = !mIsAuto }

        mSpeedInfoView = mBinding!!.speed
        mAltitudeInfoView = mBinding!!.meter
        mCurrentTimeInfoView = mBinding!!.currentTime

        val intent = intent
        mAirplaneName = intent.getStringExtra(EXTRA_AIRPLANE) as String


        mOkHttpClient = OkHttpClient()
        mOkHttpClient!!.followRedirects = true
        mOkHttpClient!!.followSslRedirects = true

        Glide.get(this).register(GlideUrl::class.java, InputStream::class.java,
                OkHttpUrlLoader.Factory(mOkHttpClient))

        mBinding!!.flightNumber.text = mAirplaneName
    }


    override fun onDestroy() {

        mHandler.removeCallbacks(mCameraRun)

        super.onDestroy()

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.mapType = GoogleMap.MAP_TYPE_NONE

        val tileProvider = object : CachingUrlTileProvider(applicationContext, 256, 256) {

            @Synchronized override fun getTileUrl(x: Int, y: Int, z: Int): String {

                //                int reversedY = (1 << z) - y - 1;
                val s = String.format(Locale.US, OSM_SATELITE, x, y, z)

                //                Log.d(TAG,"s:" + s + " reY:" + reversedY);
                //String s = String.format(Locale.US, OSM_MAP_URL_FORMAT, z, x, y);
                return s
            }
        }


        mMap!!.addTileOverlay(TileOverlayOptions().tileProvider(tileProvider).zIndex(0f))

        task.execute()

        mMap!!.setInfoWindowAdapter(PoiInfoWindowAdapter(applicationContext))
    }

    override fun onPause() {
        super.onPause()
    }

    private fun addLine(from: LatLng?, to: LatLng, isDumy: Boolean) {
        mHandler.post {
            val straight = PolylineOptions()
                    .add(from, to)
                    .geodesic(false)   // 直線
                    .color(if (isDumy) Color.RED else Color.CYAN)
                    .zIndex(10f)
                    .width(3f)

            mMap!!.addPolyline(straight)
        }
    }

    private fun addMarker(latlng: LatLng, title: String, poiData: PoiData? = null) {
        mHandler.post {
            val options = MarkerOptions()
                    .position(latlng)
                    .title(title)
                    .zIndex(10f)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin))
            val marker = mMap!!.addMarker(options)
            marker.tag = poiData

            if (poiData != null) {
                mMarkerArray.append(marker.hashCode(), poiData)
            }
        }
    }

    private fun addAirPlaneMarker(latlng: LatLng) {
        mHandler.post {
            val options = MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane)).zIndex(10f)
            mAirplaneMarker = mMap!!.addMarker(options)
        }
    }

    @WorkerThread
    private fun addLine(list: List<FlightDataPoint>) {

        var oldLatLon: LatLng? = null

        for (point in list) {

            val fOld: LatLng? = oldLatLon
            val latlon = LatLng(point.lat.toDouble(), point.lon.toDouble())

            if (oldLatLon == null) {
                oldLatLon = latlon
                //                mMap.addMarker(new MarkerOptions().position(latlon).title("start"));
                addMarker(latlon, "Start")
                addAirPlaneMarker(latlon)
                moveCamera(point, 15f)
                continue
            }

            //            mMap.addMarker(new MarkerOptions().position(latlon).title("latlong:" + latlon.toString()));
            addLine(fOld, latlon, point.isDummy)

            oldLatLon = latlon

        }
        //        mMap.addMarker(new MarkerOptions().position(oldLatLon).title("end"));
        if (oldLatLon != null) {
            addMarker(oldLatLon, "end")
        }
    }


    private var mCameraRun: CameraRunnable? = null

    override fun onCameraIdle() {

    }

    override fun onCameraMoveCanceled() {

    }

    override fun onCameraMove() {

    }

    override fun onCameraMoveStarted(reason: Int) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            isZoomControl = false


        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {

        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {

        }

    }


    internal inner class CameraRunnable(private val mData: List<FlightDataPoint>) : Runnable {
        private val mOldMarker: Marker? = null
        private var mMax = 0
        private var mCurrent = 0

        init {
            mMax = mData.size

        }

        override fun run() {
            if (mMax <= mCurrent) return
            val point = mData[mCurrent]


            var mNext = mCurrent + 1

            if (mNext >= mMax) {
                mNext = mMax - 1
            }

            val nextPoint = mData[mNext]
            Log.d(TAG, "wait:" + point.toString())
            //            LatLng nextLatLon = new LatLng(nextPoint.lat, nextPoint.lon);


            val nowLatLon = LatLng(point.lat.toDouble(), point.lon.toDouble())

            mAirplaneMarker!!.setPosition(nowLatLon)
            //            mAirplaneMarker.setRotation(nextPoint.direction);


            //            int width = getResources().getDisplayMetrics().widthPixels;
            //            int height = getResources().getDisplayMetrics().heightPixels;

            //            if(mOldMarker != null) {
            //                mOldMarker.remove();
            //            }

            //            mOldMarker = addMarker(nowLatLon, "camera");
            //            mOldMarker = addAirPlaneMarker(nowLatLon);
            var zoom = mMap!!.cameraPosition.zoom
            if (isZoomControl) {

                if (point.speed < 80) {
                } else if (point.speed < 200) {
                    zoom -= 2f
                } else {
                    zoom -= 3f
                }
            } else {

            }

            //            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, zoom));
            //            LatLngBounds.Builder builder = LatLngBounds.builder();
            //            builder.include(nowLatLon);
            //            builder.include(nextLatLon);
            //            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));


            if (mIsAuto) {
                animateCamera(nextPoint, point.direction.toFloat(), zoom)
                //            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            }

            if (mCurrent == 0 || mCurrent % 5 == 0) {
                Utils.getDataList(applicationContext, point.lat, point.lon, FutureCallback<List<PoiData>> { e, pois ->
                    if (pois == null) return@FutureCallback
                    for (data in pois) {
                        Log.d(TAG, "name:" + data.name + " lat:" + data.lat + " lng:" + data.lng + " url:" + data.image)

                        val name = data.name
                        if (name != null) {
                            addMarker(LatLng(data.lat.toDouble(), data.lng.toDouble()), name, data)
                        }
                    }
                })

            }


            mHandler.postDelayed(this, point.waittime)

            mCurrent += 1

            val speed = point.speed
            val altitude = point.altitude
            val timestamp = point.timestamp
            val timeStr = Utils.formattedTimestamp(timestamp)
            Log.d(TAG, timeStr)

            if (mSpeedInfoView != null) {
                val knot = (speed / 1.852).toInt()
                mSpeedInfoView!!.text = Integer.toString(knot.toInt())
            }
            if (mAltitudeInfoView != null) {
                mAltitudeInfoView!!.text = Integer.toString(altitude.toInt())
            }
            if (mCurrentTimeInfoView != null) {
                mCurrentTimeInfoView!!.text = timeStr
            }
        }
    }


    private fun animateCamera(point: FlightDataPoint, direction: Float, zoom: Float) {
        mHandler.post {
            val nowLatLon = LatLng(point.lat.toDouble(), point.lon.toDouble())

            val cpBuilder = CameraPosition.builder()
            cpBuilder.bearing(direction)
            cpBuilder.target(nowLatLon)
            cpBuilder.tilt(60f)
            cpBuilder.zoom(zoom)

            mMap!!.animateCamera(CameraUpdateFactory.newCameraPosition(cpBuilder.build()))
        }

    }

    private fun moveCamera(point: FlightDataPoint, zoom: Float) {
        mHandler.post {
            val nowLatLon = LatLng(point.lat.toDouble(), point.lon.toDouble())

            val cpBuilder = CameraPosition.builder()
            cpBuilder.bearing(point.direction.toFloat())
            cpBuilder.target(nowLatLon)
            cpBuilder.tilt(60f)
            cpBuilder.zoom(zoom)

            mMap!!.moveCamera(CameraUpdateFactory.newCameraPosition(cpBuilder.build()))
        }

    }


    private val mHandler = Handler()


    private val task = object : AsyncTask<Any, Any, Any>() {


        protected override fun doInBackground(params: Array<Any>): Any? {

            val list = Utils.loadCsv(applicationContext, "Flight_$mAirplaneName.csv")
            if (list != null) {
                mList = Utils.loadCsvDummyData(list)
                addLine(list)
            }

            return null
        }

        protected override fun onProgressUpdate(values: Array<Any>) {
            super.onProgressUpdate(*values)

        }

        protected override fun onPostExecute(o: Any) {
            super.onPostExecute(o)


            //            for(FlightDataPoint data:mList) {
            //                Log.d(TAG,data.toString());
            //            }
            //
            //            addLine();

            if (mList != null) {
                mCameraRun = CameraRunnable(mList!!)
            }
            mHandler.post(mCameraRun)


        }


    }


    private val mOnInfoWindowClickListener = GoogleMap.OnInfoWindowClickListener { }

    companion object {
        private val TAG = MainActivity::class.java!!.getSimpleName()

        val EXTRA_AIRPLANE = "extra_airplane"
        //    private static final String OSM_MAP_URL_FORMAT = "http://tile.openstreetmap.org/%d/%d/%d.png";
        private val OSM_SATELITE = "http://mt1.google.com/vt/lyrs=y&x=%d&y=%d&z=%d"
    }


}
