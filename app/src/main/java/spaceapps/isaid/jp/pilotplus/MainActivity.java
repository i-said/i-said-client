package spaceapps.isaid.jp.pilotplus;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.koushikdutta.async.future.FutureCallback;

import java.util.List;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_AIRPLANE = "extra_airplane";

    private GoogleMap mMap;
    private ImageButton mImageButton;


    private Marker mAirplaneMarker = null;

    private List<FlightDataPoint> mList;
    private boolean mIsAuto = true;
    private String mAirplaneName;


    private TextView mSpeedInfoView;
    private TextView mAltitudeInfoView;
    private TextView mCurrentTimeInfoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mImageButton = (ImageButton) findViewById(R.id.btn_auto);
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIsAuto = !mIsAuto;

            }
        });

        mSpeedInfoView = (TextView) findViewById(R.id.speed);
        mAltitudeInfoView = (TextView) findViewById(R.id.meter);
        mCurrentTimeInfoView = (TextView) findViewById(R.id.current_time);

        Intent intent = getIntent();
        mAirplaneName = intent.getStringExtra(EXTRA_AIRPLANE);
        ((TextView)findViewById(R.id.flight_number)).setText(mAirplaneName);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        task.execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void addLine(final LatLng from, final LatLng to, final boolean isDumy) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                PolylineOptions straight = new PolylineOptions()
                        .add(from, to)
                        .geodesic(false)   // 直線
                        .color((isDumy ? Color.RED : Color.CYAN))
                        .width(3);
                mMap.addPolyline(straight);

            }
        });
    }

    private void addMarker(final LatLng latlng ,final  String title) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MarkerOptions options = new MarkerOptions().position(latlng).title(title);
                mMap.addMarker(options);
            }
        });
    }

    private void addAirPlaneMarker(final LatLng latlng) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MarkerOptions options = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane));
                mAirplaneMarker = mMap.addMarker(options);
            }
        });
    }

    @WorkerThread
    private void addLine(List<FlightDataPoint> list) {

        LatLng oldLatLon = null;

        for (final FlightDataPoint point : list) {

            final LatLng fOld = oldLatLon;
            final LatLng latlon = new LatLng(point.lat,point.lon);

            if(oldLatLon == null) {
                oldLatLon = latlon;
//                mMap.addMarker(new MarkerOptions().position(latlon).title("start"));
                addMarker(latlon, "Start");
                addAirPlaneMarker(latlon);
                moveCamera(point, 15f);
                continue;
            }

//            mMap.addMarker(new MarkerOptions().position(latlon).title("latlong:" + latlon.toString()));
            addLine(fOld, latlon, point.isDummy);

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


            int mNext = mCurrent + 1;

            if (mNext >= mMax) {
                mNext = mMax - 1;
            }

            FlightDataPoint nextPoint = mData.get(mNext);
            Log.d(TAG, "wait:" + point.toString());
//            LatLng nextLatLon = new LatLng(nextPoint.lat, nextPoint.lon);


            LatLng nowLatLon = new LatLng(point.lat,point.lon);

            mAirplaneMarker.setPosition(nowLatLon);
//            mAirplaneMarker.setRotation(nextPoint.direction);



//            int width = getResources().getDisplayMetrics().widthPixels;
//            int height = getResources().getDisplayMetrics().heightPixels;

//            if(mOldMarker != null) {
//                mOldMarker.remove();
//            }

//            mOldMarker = addMarker(nowLatLon, "camera");
//            mOldMarker = addAirPlaneMarker(nowLatLon);

            float zoom = 15f;
            if (point.speed < 80) {
            } else if (point.speed < 200) {
                zoom -= 2f;
            } else {
                zoom -= 3f;
            }

//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, zoom));
//            LatLngBounds.Builder builder = LatLngBounds.builder();
//            builder.include(nowLatLon);
//            builder.include(nextLatLon);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));


            if(mIsAuto) {
                animateCamera(nextPoint, point.direction, zoom);
                //            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            }
            if (!point.isDummy) {
                Utils.getDataList(getApplicationContext(), point.lat, point.lon, new FutureCallback<List<PoiData>>() {
                    @Override
                    public void onCompleted(Exception e, List<PoiData> pois) {
                        if (pois == null) return;
                        for (PoiData data : pois) {
                            addMarker(new LatLng(data.lat, data.lng), data.name);
                        }
                    }
                });

            }


            mHandler.postDelayed(this,point.waittime);

            mCurrent += 1;

            long speed = point.speed;
            long altitude = point.altitude;
            long timestamp = point.timestamp;
            String timeStr = Utils.formattedTimestamp(timestamp);
            Log.d(TAG, timeStr);

            if (mSpeedInfoView !=null) {
                mSpeedInfoView.setText(Integer.toString((int) speed));
            }
            if (mAltitudeInfoView !=null) {
                mAltitudeInfoView.setText(Integer.toString((int) altitude));
            }
            if (mCurrentTimeInfoView !=null) {
                mCurrentTimeInfoView.setText(timeStr);
            }
        }
    };


    private void animateCamera(final FlightDataPoint point, final float direction, final float zoom) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LatLng nowLatLon = new LatLng(point.lat, point.lon);

                CameraPosition.Builder cpBuilder = CameraPosition.builder();
                cpBuilder.bearing(direction);
                cpBuilder.target(nowLatLon);
                cpBuilder.tilt(60);
                cpBuilder.zoom(zoom);

                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cpBuilder.build()));
            }
        });

    }

    private void moveCamera(final FlightDataPoint point, final float zoom) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                LatLng nowLatLon = new LatLng(point.lat, point.lon);

                CameraPosition.Builder cpBuilder = CameraPosition.builder();
                cpBuilder.bearing(point.direction);
                cpBuilder.target(nowLatLon);
                cpBuilder.tilt(60);
                cpBuilder.zoom(zoom);

                mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cpBuilder.build()));
            }
        });

    }


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


    private AsyncTask task = new AsyncTask() {


        @Override
        protected Object doInBackground(Object[] params) {

            List<FlightDataPoint> list = Utils.loadCsv(getApplicationContext(), "Flight_" + mAirplaneName + ".csv");
            mList = Utils.loadCsvDummyData(list);

            addLine(list);

            return null;
        }

        @Override
        protected void onProgressUpdate(Object[] values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);



//            for(FlightDataPoint data:mList) {
//                Log.d(TAG,data.toString());
//            }
//
//            addLine();

            mCameraRun = new CameraRunnable(mList);
            mHandler.post(mCameraRun);



        }


    };



}
