package spaceapps.isaid.jp.pilotplus

import android.content.Context
import android.databinding.DataBindingUtil
import android.provider.ContactsContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView

import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

import java.util.Objects

import spaceapps.isaid.jp.pilotplus.databinding.ImageInfoWindowBinding


class PoiInfoWindowAdapter internal constructor(context: Context) : GoogleMap.InfoWindowAdapter {
    private val mContext: Context

    init {
        mContext = Objects.requireNonNull(context)
    }


    override fun getInfoWindow(marker: Marker): View? {
        return null
    }

    /**
     * initiates loading the info window and makes sure the new image is used in case it changed
     */

    override fun getInfoContents(marker: Marker): View? {
        Log.d(TAG, "getInfoContents:" + marker.hashCode())

        val o = marker.tag
        Log.d(TAG, "o:" + o!!)
        if (o == null) {
            return null
        }

        if (o !is PoiData) {
            return null
        }

        Log.d(TAG, "o:" + o.javaClass)
        val poi = o
        val binding = DataBindingUtil.inflate<ImageInfoWindowBinding>(LayoutInflater.from(mContext), R.layout.image_info_window, null, false)
        val image = binding.image
        val name = binding.name
        name.text = poi.name

        Glide.with(mContext)
                .load(poi.image)
                //TODO ローディング画像にしたい
                //                        .placeholder(R.drawable.dummy)
                .error(R.drawable.dummy)
                .listener(object : RequestListener<String, GlideDrawable> {
                    override fun onException(e: Exception?, s: String, glideDrawableTarget: Target<GlideDrawable>, b: Boolean): Boolean {
                        Log.d(TAG, "Error in Glide listener")
                        Log.d(TAG, s)
                        if (e != null) {
                            Log.d(TAG, e.message)
                        }
                        return false
                    }

                    override fun onResourceReady(glideDrawable: GlideDrawable, s: String, glideDrawableTarget: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                        if (!isFromMemoryCache) {
                            marker.showInfoWindow()
                        }
                        return false
                    }
                })
                .into(image)
        return binding.root
    }

    companion object {
        private val TAG = "poiInfo"
    }
}
