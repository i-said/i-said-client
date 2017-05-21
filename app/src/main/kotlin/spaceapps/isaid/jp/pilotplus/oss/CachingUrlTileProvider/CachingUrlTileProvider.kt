package spaceapps.isaid.jp.pilotplus.oss.CachingUrlTileProvider

/*******************************************************************************
 * Copyright 2014 Maurice Wohlkönig

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 * http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.Context
import android.graphics.Bitmap

import com.google.android.gms.maps.model.Tile
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.android.gms.maps.model.TileProvider
import com.nostra13.universalimageloader.core.DisplayImageOptions
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

import java.io.ByteArrayOutputStream

/**
 *
 * Google Maps Android V2 tile overlay provider for cached URL tiles. Caches in memory and/or on disk.
 *
 * Uses the famous [Android Universal Image Loader](https://github.com/nostra13/Android-Universal-Image-Loader/) library for downloading and caching.
 *
 * Usage example:<pre>
 * googleMap.addTileOverlay(new CachingUrlTileProvider(this, 256, 256) {
 * @Override
 * * public String getTileUrl(int x, int y, int z) {
 * * return String.format("https://a.tile.openstreetmap.org/%3$s/%1$s/%2$s.png",x,y,z);
 * * }
 * * }.createTileOverlayOptions());
 * * </pre>
 */
abstract class CachingUrlTileProvider(ctx: Context, private val mTileWidth: Int, private val mTileHeight: Int) : TileProvider {
    private val mOptions: DisplayImageOptions

    init {

        // if ImageLoader has not been instantiated by parent application yet
        if (!ImageLoader.getInstance().isInited) {
            // Create global configuration and initialize ImageLoader with this config
            val config = ImageLoaderConfiguration.Builder(ctx).build()
            ImageLoader.getInstance().init(config)
        }

        // init ImageLoader display options
        val builder = DisplayImageOptions.Builder()
        builder.cacheInMemory(true).cacheOnDisk(true)
        setDisplayImageOptions(builder)
        mOptions = builder.build()
    }

    override fun getTile(x: Int, y: Int, z: Int): Tile {
        val tileImage = getTileImage(x, y, z)
        if (tileImage != null) {
            return Tile(mTileWidth / 2, mTileHeight / 2, tileImage)
        }
        return TileProvider.NO_TILE
    }

    /**
     * Synchronously loads the requested Tile image either from cache or from the web.
     * Background threading/pooling is done by the google maps api so we can do it all synchronously.

     * @param x x coordinate of the tile
     * *
     * @param y y coordinate of the tile
     * *
     * @param z the zoom level
     * *
     * @return byte data of the image or *null* if the image could not be loaded.
     */
    private fun getTileImage(x: Int, y: Int, z: Int): ByteArray? {
        val bitmap = ImageLoader.getInstance().loadImageSync(getTileUrl(x, y, z), mOptions) ?: return null
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    /**
     * Creates a new TileOverlayOptions object to be used with [map.addTileOverlay()][com.google.android.gms.maps.GoogleMap.addTileOverlay]

     * @return a TileOverlayOptions with this object set as a tile provider plus fadeIn set to false
     */
    fun createTileOverlayOptions(): TileOverlayOptions {
        val tileOverlayOptions = TileOverlayOptions().tileProvider(this)

        // set fadeIn to false for all GMS versions that support it
        try {
            Class.forName("com.google.android.gms.maps.model.TileOverlayOptions")
                    .getMethod("fadeIn", Boolean::class.javaPrimitiveType)
                    .invoke(tileOverlayOptions, false)
        } catch (e: Exception) {
        }

        return tileOverlayOptions
    }

    /**
     * Allows you to set additional ImageLoader display options.
     *
     *
     * See https://github.com/nostra13/Android-Universal-Image-Loader/wiki/Display-Options for possible options.
     *
     *
     * Disabling the cache option DOES NOT MAKE SENSE.

     * @param optionsBuilder options builder for setting de downloader settings
     */
    protected fun setDisplayImageOptions(optionsBuilder: DisplayImageOptions.Builder) {

    }

    /**
     * Return the url to your tiles. For example:
     * <pre>
     * public String getTileUrl(int x, int y, int z) {
     * return String.format("https://a.tile.openstreetmap.org/%3$s/%1$s/%2$s.png",x,y,z);
     * }
    </pre> *
     * See [http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames](http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames) for more details

     * @param x x coordinate of the tile
     * *
     * @param y y coordinate of the tile
     * *
     * @param z the zoom level
     * *
     * @return the url to the tile specified by the parameters
     */
    abstract fun getTileUrl(x: Int, y: Int, z: Int): String
}