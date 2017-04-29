package spaceapps.isaid.jp.pilotplus;

import java.util.Date;

/**
 * Created by iwsbrfts on 17/04/29.
 */

public class FlightDataPoint implements Cloneable {
    public long timestamp;
    public long waittime;
    public float lat;
    public float lon;
    public String callSigne; //コールサイン
    public long speed; //スピード
    public long altitude; //高さ
    public int direction; //方向

    @Override
    public FlightDataPoint clone() {

        FlightDataPoint point = new FlightDataPoint();
        try {
            point = (FlightDataPoint)super.clone();
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
        }
        return point;

    }

    @Override
    public String toString() {
        return "time:" + timestamp + " lat:" + lat + " lon:" + lon + " direction:" + direction + " wait:" + waittime;
    }
}
