package spaceapps.isaid.jp.pilotplus;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private GoogleMap mMap;

    private List<FlightDataPoint> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    protected void onDestroy() {

        mHandler.removeCallbacks(mCameraRun);

        super.onDestroy();

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
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

//        // Add a marker in Sydney and move the camera
//        LatLng tokyo = new LatLng(35.652832, 139.839478);
//        mMap.addMarker(new MarkerOptions().position(tokyo).title("Marker in Tokyo"));
//
//        LatLng Frankfurt = new LatLng(50.037933, 8.562152);
//        mMap.addMarker(new MarkerOptions().position(Frankfurt).title("Marker in Frankfurt"));


        mList = Utils.loadCsv(getApplicationContext(), "Flight_NH203.csv");

        for(FlightDataPoint data:mList) {
            Log.d(TAG,data.toString());
        }

        addLine();

        mCameraRun = new CameraRunnable(mList);
        mHandler.post(mCameraRun);

    }

    private void addLine(LatLng from , LatLng to) {
        PolylineOptions straight = new PolylineOptions()
                .add(from, to)
                .geodesic(false)   // 直線
                .color(Color.RED)
                .width(3);
        mMap.addPolyline(straight);
    }

    private Marker addMarker(LatLng latlng , String title) {
        MarkerOptions options = new MarkerOptions().position(latlng).title(title);
        return mMap.addMarker(options);
    }

    private Marker addAirPlaneMarker(LatLng latlng) {
        MarkerOptions options = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane));
        return mMap.addMarker(options);
    }

    private void addLine() {

        LatLng oldLatLon = null;

        for(FlightDataPoint point:mList) {
            LatLng latlon = new LatLng(point.lat,point.lon);

            if(oldLatLon == null) {
//                mMap.addMarker(new MarkerOptions().position(latlon).title("start"));
                addMarker(latlon, "Start");
                oldLatLon = latlon;
                continue;
            }

//            mMap.addMarker(new MarkerOptions().position(latlon).title("latlong:" + latlon.toString()));
            addLine(oldLatLon,latlon);
            oldLatLon = latlon;

        }
//        mMap.addMarker(new MarkerOptions().position(oldLatLon).title("end"));
        addMarker(oldLatLon, "end");



    }


    private CameraRunnable mCameraRun;


    class CameraRunnable implements Runnable {
        private Marker mOldMarker = null;
        private List<FlightDataPoint> mData;
        private int mMax = 0;
        private int mCurrent = 1;

        public CameraRunnable(List<FlightDataPoint> data) {
            mData = data;
            mMax = mData.size();

        }

        @Override
        public void run() {
            if(mMax <= mCurrent) return;
            FlightDataPoint point =  mData.get(mCurrent);

            int mNext = mCurrent + 5;
            if(mNext <= mCurrent) {
                mNext = mMax - 1;
            }

            FlightDataPoint nextPoint = mData.get(mNext);
            Log.d(TAG,"wait:" + point.waittime);


            LatLng nowLatLon = new LatLng(point.lat,point.lon);
            LatLng nextLatLon = new LatLng(nextPoint.lat , nextPoint.lon);


//            int width = getResources().getDisplayMetrics().widthPixels;
//            int height = getResources().getDisplayMetrics().heightPixels;

            if(mOldMarker != null) {
                mOldMarker.remove();
            }

//            mOldMarker = addMarker(nowLatLon, "camera");
            mOldMarker = addAirPlaneMarker(nowLatLon);

//            float zoom = 12f;
//            if(point.altitude < 5000) {
//                zoom -= 2f;
//
//            } else if(point.altitude < 10000) {
//                zoom -= 2f;
//
//            } else if(point.altitude < 30000) {
//                zoom -= 1f;
//
//            }

//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, zoom));
//            LatLngBounds.Builder builder = LatLngBounds.builder();
//            builder.include(nowLatLon);
//            builder.include(nextLatLon);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));


            CameraPosition.Builder cpBuilder = CameraPosition.builder();
            cpBuilder.bearing(point.direction);
            cpBuilder.target(nowLatLon);
            cpBuilder.tilt(60);
            cpBuilder.zoom(15f);

            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpBuilder.build()));

 //            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            mHandler.postDelayed(this,point.waittime);

            mCurrent += 1;

        }
    };




//    private void addKML(String filename) {
//
//        try {
//
//
//            KmlLayer layer = new KmlLayer(mMap,getAssets().open(filename), getApplicationContext());
//            layer.addLayerToMap();
//
//            for(KmlPlacemark placemark : layer.getPlacemarks()) {
//                Geometry geometry = placemark.getGeometry();
//                Log.d(TAG,"geometry:" + geometry.getClass().getName());
//                if(geometry instanceof KmlPoint) {
//                    KmlPoint point = (KmlPoint) geometry;
//
//                    mMap.addMarker(new MarkerOptions().position(point.getGeometryObject()).title("aaa:" + point.getGeometryObject().toString()));
//                }
//            }
//
//
//
////
////            for(KmlContainer container : layer.getContainers()) {
////                Log.d(TAG,"container:" + container);
////                for(KmlPlacemark placemark : container.getPlacemarks()) {
////                    Geometry geometry = placemark.getGeometry();
////                    Log.d(TAG,"geometry:" + geometry.getClass().getName());
////                    if(geometry instanceof KmlPoint) {
////                        KmlPoint point = (KmlPoint) geometry;
////
////                        mMap.addMarker(new MarkerOptions().position(point.getGeometryObject()).title("aaa:" + point.getGeometryObject().toString()));
////                    }
////                }
////            }
//
//            layer.removeLayerFromMap();
//
//
//        } catch(IOException e) {
//            Log.d(TAG,"failed", e);
//        } catch (XmlPullParserException e) {
//            Log.d(TAG,"failed", e);
//        }
//
//
//
//    }


//    private void moveCameraToKml(KmlLayer kmlLayer) {
//        //Retrieve the first container in the KML layer
//        KmlContainer container = kmlLayer.getContainers().iterator().next();
//        //Retrieve a nested container within the first container
//        container = container.getContainers().iterator().next();
//        //Retrieve the first placemark in the nested container
//        KmlPlacemark placemark = container.getPlacemarks().iterator().next();
//        //Retrieve a polygon object in a placemark
//        KmlPolygon polygon = (KmlPolygon) placemark.getGeometry();
//        //Create LatLngBounds of the outer coordinates of the polygon
//        LatLngBounds.Builder builder = new LatLngBounds.Builder();
//        for (LatLng latLng : polygon.getOuterBoundaryCoordinates()) {
//            builder.include(latLng);
//        }
//
//        int width = getResources().getDisplayMetrics().widthPixels;
//        int height = getResources().getDisplayMetrics().heightPixels;
//        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), width, height, 1));
//    }


    private Handler mHandler = new Handler();




}
