package spaceapps.isaid.jp.pilotplus;

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
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.integration.okhttp.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.koushikdutta.async.future.FutureCallback;
import com.squareup.okhttp.OkHttpClient;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.WorkerThread;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;

import spaceapps.isaid.jp.pilotplus.databinding.ActivityMainBinding;
import spaceapps.isaid.jp.pilotplus.oss.CachingUrlTileProvider.CachingUrlTileProvider;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener, GoogleMap.OnCameraMoveCanceledListener, GoogleMap.OnCameraIdleListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String EXTRA_AIRPLANE = "extra_airplane";
    //    private static final String OSM_MAP_URL_FORMAT = "http://tile.openstreetmap.org/%d/%d/%d.png";
    private static final String OSM_SATELITE = "http://mt1.google.com/vt/lyrs=y&x=%d&y=%d&z=%d";

    private boolean isZoomControl = false;

    private GoogleMap mMap;
    private ImageButton mImageButton;

    private SparseArray mMarkerArray = new SparseArray();

    private OkHttpClient mOkHttpClient;
    private Marker mAirplaneMarker = null;

    private List<FlightDataPoint> mList;
    private boolean mIsAuto = true;
    private String mAirplaneName;


    private TextView mSpeedInfoView;
    private TextView mAltitudeInfoView;
    private TextView mCurrentTimeInfoView;
    private ActivityMainBinding mBinding;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mImageButton = mBinding.btnAuto;
        mImageButton.setOnClickListener(v -> {
            mIsAuto = !mIsAuto;
            mImageButton.setImageDrawable(
                    ContextCompat.getDrawable(getApplicationContext(), mIsAuto ? R.drawable.heading_btn : R.drawable.heading_btn_disable));
        });

        mSpeedInfoView = mBinding.speed;
        mAltitudeInfoView = mBinding.meter;
        mCurrentTimeInfoView = mBinding.currentTime;

        Intent intent = getIntent();
        mAirplaneName = intent.getStringExtra(EXTRA_AIRPLANE);


        mOkHttpClient = new OkHttpClient();
        mOkHttpClient.setFollowRedirects(true);
        mOkHttpClient.setFollowSslRedirects(true);

        Glide.get(this).register(GlideUrl.class, InputStream.class,
                new OkHttpUrlLoader.Factory(mOkHttpClient));

        mBinding.flightNumber.setText(mAirplaneName);
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
        mMap.setMapType(GoogleMap.MAP_TYPE_NONE);

        TileProvider tileProvider = new CachingUrlTileProvider(getApplicationContext(), 256, 256) {

            @Override
            public synchronized String getTileUrl(int x, int y, int z) {

//                int reversedY = (1 << z) - y - 1;
                String s = String.format(Locale.US, OSM_SATELITE, x, y, z);

//                Log.d(TAG,"s:" + s + " reY:" + reversedY);
                //String s = String.format(Locale.US, OSM_MAP_URL_FORMAT, z, x, y);
                return s;
            }
        };


        mMap.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider).zIndex(0));

        task.execute();

        mMap.setInfoWindowAdapter(new PoiInfoWindowAdapter(getApplicationContext()));
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
                        .zIndex(10)
                        .width(3);

                mMap.addPolyline(straight);

            }
        });
    }

    private void addMarker(final LatLng latlng, final String title) {
        addMarker(latlng, title, null);
    }

    private void addMarker(final LatLng latlng, final String title, final PoiData poiData) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MarkerOptions options = new MarkerOptions()
                        .position(latlng)
                        .title(title)
                        .zIndex(10)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.pin));
                Marker marker = mMap.addMarker(options);
                marker.setTag(poiData);

                if (poiData != null) {
                    mMarkerArray.append(marker.hashCode(), poiData);
                }

            }
        });
    }

    private void addAirPlaneMarker(final LatLng latlng) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                MarkerOptions options = new MarkerOptions().position(latlng).icon(BitmapDescriptorFactory.fromResource(R.drawable.airplane)).zIndex(10);
                mAirplaneMarker = mMap.addMarker(options);
            }
        });
    }

    @WorkerThread
    private void addLine(List<FlightDataPoint> list) {

        LatLng oldLatLon = null;

        for (final FlightDataPoint point : list) {

            final LatLng fOld = oldLatLon;
            final LatLng latlon = new LatLng(point.lat, point.lon);

            if (oldLatLon == null) {
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

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            isZoomControl = false;


        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_API_ANIMATION) {

        } else if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION) {

        }

    }


    class CameraRunnable implements Runnable {
        private Marker mOldMarker = null;
        private List<FlightDataPoint> mData;
        private int mMax = 0;
        private int mCurrent = 0;

        public CameraRunnable(List<FlightDataPoint> data) {
            mData = data;
            mMax = mData.size();

        }

        @Override
        public void run() {
            if (mMax <= mCurrent) return;
            FlightDataPoint point = mData.get(mCurrent);


            int mNext = mCurrent + 1;

            if (mNext >= mMax) {
                mNext = mMax - 1;
            }

            FlightDataPoint nextPoint = mData.get(mNext);
            Log.d(TAG, "wait:" + point.toString());
//            LatLng nextLatLon = new LatLng(nextPoint.lat, nextPoint.lon);


            LatLng nowLatLon = new LatLng(point.lat, point.lon);

            mAirplaneMarker.setPosition(nowLatLon);
//            mAirplaneMarker.setRotation(nextPoint.direction);


//            int width = getResources().getDisplayMetrics().widthPixels;
//            int height = getResources().getDisplayMetrics().heightPixels;

//            if(mOldMarker != null) {
//                mOldMarker.remove();
//            }

//            mOldMarker = addMarker(nowLatLon, "camera");
//            mOldMarker = addAirPlaneMarker(nowLatLon);
            float zoom = mMap.getCameraPosition().zoom;
            if (isZoomControl) {

                if (point.speed < 80) {
                } else if (point.speed < 200) {
                    zoom -= 2f;
                } else {
                    zoom -= 3f;
                }
            } else {

            }

//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlon, zoom));
//            LatLngBounds.Builder builder = LatLngBounds.builder();
//            builder.include(nowLatLon);
//            builder.include(nextLatLon);
//            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(),200));


            if (mIsAuto) {
                animateCamera(nextPoint, point.direction, zoom);
                //            mMap.moveCamera(CameraUpdateFactory.zoomTo(zoom));
            }

            if (mCurrent == 0 || mCurrent % 5 == 0) {
                Utils.getDataList(getApplicationContext(), point.lat, point.lon, new FutureCallback<List<PoiData>>() {
                    @Override
                    public void onCompleted(Exception e, List<PoiData> pois) {
                        if (pois == null) return;
                        for (PoiData data : pois) {
                            Log.d(TAG, "name:" + data.getName() + " lat:" + data.getLat() + " lng:" + data.getLng() + " url:" + data.getImage());

                            addMarker(new LatLng(data.getLat(), data.getLng()), data.getName(), data);
                        }
                    }
                });

            }


            mHandler.postDelayed(this, point.waittime);

            mCurrent += 1;

            long speed = point.speed;
            long altitude = point.altitude;
            long timestamp = point.timestamp;
            String timeStr = Utils.formattedTimestamp(timestamp);
            Log.d(TAG, timeStr);

            if (mSpeedInfoView != null) {
                int knot = (int) (speed / 1.852);
                mSpeedInfoView.setText(Integer.toString((int) knot));
            }
            if (mAltitudeInfoView != null) {
                mAltitudeInfoView.setText(Integer.toString((int) altitude));
            }
            if (mCurrentTimeInfoView != null) {
                mCurrentTimeInfoView.setText(timeStr);
            }
        }
    }

    ;


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


    private GoogleMap.OnInfoWindowClickListener mOnInfoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {

        }
    };


}
