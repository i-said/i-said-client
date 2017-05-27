package spaceapps.isaid.jp.pilotplus

import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient


/**
 * Created by iwsbrfts on 17/05/27.
 */
abstract class BaseActivity : FragmentActivity() ,GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private val TAG = this.localClassName

    private val REQUEST_LOCATION = 1;


    protected var mGoogleApiClient: GoogleApiClient? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .build()
    }


    //#GoogleAPI
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        when(connectionResult.errorCode) {
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                //TODO create version up code


            }
            else -> {

            }
        }

    }

    override fun onConnectionSuspended(result: Int) {

        when(result){
            GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST -> Log.d(TAG,"CAUSE_NETWORK_LOST")
            GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED -> Log.d(TAG,"CAUSE_SERVICE_DISCONNECTED")
        }

    }

    override fun onConnected(bundle: Bundle?) {


    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
            }
        }
    }

//#GoogleAPI


    override fun onStart() {
        super.onStart()
        mGoogleApiClient!!.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient!!.disconnect()
    }

}