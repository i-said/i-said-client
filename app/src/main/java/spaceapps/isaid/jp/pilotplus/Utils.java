package spaceapps.isaid.jp.pilotplus;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by iwsbrfts on 17/04/29.
 */

public class Utils {
    private static final String TAG = Utils.class.getSimpleName();

    private static final int TIME_DIVIDE = 30;
    private static final int TIME_SPEED = 1000 / 100;


    public static  List<FlightDataPoint> loadCsv(Context context, String filename) {

        try {
            return loadCsv(context.getAssets().open(filename));

        } catch(Exception e) {
            Log.d(TAG,"failed", e);
        }

        return null;
    }


    public static List<FlightDataPoint> loadCsv(InputStream in) {
        try {
            List<FlightDataPoint> list = loadCsvReadAndReverse(in);

            List<FlightDataPoint> retData = new LinkedList<>();

            FlightDataPoint old = null;
            for(FlightDataPoint point:list) {

                if(old == null) {
                    old = point;
                    retData.add(point);
                    continue;
                }

                int time = (int)(point.timestamp - old.timestamp);
                float[] results = new float[1];
                Location.distanceBetween(old.lat,old.lon,point.lat,point.lon,results);

                final float distance = results[0];
                final float y = (distance / (time * 1f));
                Log.d(TAG, "distance:" + distance + " waittime:" + time + " y:" + y);

                if (time % TIME_DIVIDE > 1) {

                    final int count = (int) Math.floor(time / TIME_DIVIDE);

                    float floatLat = point.lat - old.lat;
                    float diffLat = floatLat / count;

                    float floatLon = point.lon - old.lon;
                    float diffLon = floatLon / count;

                    Log.d(TAG, "x:" + count + " diffLat:" + diffLat + " diffLon" + diffLon);

                    for (int i = 1, max = count + 1; i < max; i++) {

                        FlightDataPoint next = old.clone();
                        next.timestamp = next.timestamp + TIME_DIVIDE;
                        next.waittime = (next.timestamp - old.timestamp) * TIME_SPEED;
                        next.lat = next.lat + diffLat;
                        next.lon = next.lon + diffLon;

                        next.isDummy = true;

                        if(point.timestamp < next.timestamp) {
                            break;
                        }
                        Log.d(TAG, next.toString());

                        retData.add(next);

                        old = next;

                    }


                } else {
                    point.waittime = time * TIME_SPEED;

                    retData.add(point);
                    old = point;

                }


            }

            return retData;
        } catch (Exception e) {
            Log.d(TAG,"failed", e);
        }

        return null;
    }


    public static List<FlightDataPoint> loadCsvDummyData(InputStream in) {
        List<FlightDataPoint> list = loadCsvReadAndReverse(in);

        List<FlightDataPoint> retData = new LinkedList<>();

        FlightDataPoint old = null;
        for (FlightDataPoint point : list) {

            if (old == null) {
                old = point;
                retData.add(point);
                continue;
            }

            int time = (int) (point.timestamp - old.timestamp);
            float[] results = new float[1];
            Location.distanceBetween(old.lat, old.lon, point.lat, point.lon, results);

            final float distance = results[0];
            final float y = (distance / (time * 1f));
            Log.d(TAG, "distance:" + distance + " waittime:" + time + " y:" + y);

            final int count = (int) Math.floor(time / TIME_DIVIDE);

            float floatLat = point.lat - old.lat;
            float diffLat = floatLat / count;

            float floatLon = point.lon - old.lon;
            float diffLon = floatLon / count;

            Log.d(TAG, "x:" + count + " diffLat:" + diffLat + " diffLon" + diffLon);

            for (int i = 1, max = count + 1; i < max; i++) {

                FlightDataPoint next = old.clone();
                next.timestamp = next.timestamp + TIME_DIVIDE;
                next.waittime = (next.timestamp - old.timestamp) * TIME_SPEED;
                next.lat = next.lat + diffLat;
                next.lon = next.lon + diffLon;

                next.isDummy = true;

//                        if(point.lat < next.lat) {
//                            break;
//                        }
//
//                        if(point.lon < next.lon) {
//                            break;
//                        }

                if (point.timestamp < next.timestamp) {
                    break;
                }
                Log.d(TAG, next.toString());

                retData.add(next);

                old = next;

            }


        }

        return retData;


    }


    public static List<FlightDataPoint> loadCsvReadAndReverse(InputStream in) {
        try {
            List<FlightDataPoint> list = new LinkedList<>();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;

            while ((line = br.readLine()) != null) {
                if (line.contains("Timestamp")) continue;
                line = line.replace("\"", "");
                String[] data = line.split(",");

                FlightDataPoint point = new FlightDataPoint();
                point.timestamp = Long.valueOf(data[0]);
                point.callSigne = data[2];
                point.lat = Float.valueOf(data[3]);
                point.lon = Float.valueOf(data[4]);
                point.altitude = Long.valueOf(data[5]);
                point.speed = Long.valueOf(data[6]);
                point.direction = Integer.valueOf(data[7]);


                list.add(point);

            }

            Collections.reverse(list);

            return list;

        } catch (IOException e) {
            Log.d(TAG, "failed", e);
        }

        return null;
    }



}
