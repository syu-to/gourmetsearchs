package com.websarva.wings.android.gourmetsearch

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class StoreNamedetail : AppCompatActivity() {
    companion object {
        private  const val DEBUG_TAG = "AsyncSample"    //ログメッセージ
        private const val RESTAURANT_URL = "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/" //APIのアドレス
        private const val API_ID = "59c533b8ba2a26ed"   //APIキー
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_namedetail)

        val id = intent.getStringExtra("id").toString()

        val urlFull = "${StoreNamedetail.RESTAURANT_URL}?key=${StoreNamedetail.API_ID}&id=$id&format=json"
        println(urlFull)
        ShopDetaileInfo(urlFull)
    }

    @UiThread
    private fun ShopDetaileInfo(urlFull:String){
        val backgroundReceiver = ShopDetaileInfoBackgroundReceiver(urlFull)
        val executeService = Executors.newSingleThreadExecutor()
        val future =executeService.submit(backgroundReceiver)
        val result = future.get()
        ShowDetaileShopInfo(result)
    }

    private inner class ShopDetaileInfoBackgroundReceiver(url: String): Callable<String> {
        private val _url = url
        @WorkerThread
        override fun call(): String{
            var result = ""
            //URLオブジェクト生成
            val url = URL(_url)
            //URLオブジェクトからHttpURLConnectionオブジェクト取得
            val con = url.openConnection() as HttpURLConnection
            //接続に使ってよい時間を設定
            con.connectTimeout = 1000
            //データ取得に使ってよい時間
            con.readTimeout = 1000
            //HTTP接続メソッドをGETに設定
            con.requestMethod = "GET"
            try {
                //接続。
                con.connect()
                //HttpURLConnectionオブジェクトからレスポンスデータ取得
                val stream = con.inputStream
                //レスポンスデータであるInputStreamオブジェクトを文字列に変換。
                result = is2String(stream)
                //InputStreamオブジェクトを解放
                stream.close()
            }
            catch (ex: SocketTimeoutException){
                Log.w(StoreNamedetail.DEBUG_TAG,"通信タイムアウト",ex)
            }
            con.disconnect()
            //WebAPIに接続するコード
            return  result
        }

        private fun is2String(stream: InputStream): String{
            val sb = StringBuilder()
            val reader =  BufferedReader(
                InputStreamReader(
                    stream, StandardCharsets.UTF_8)
            )
            var line = reader.readLine()
            while (line != null){
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }

    }

    @UiThread
    private fun ShowDetaileShopInfo(result: String){
        val rootJSON = JSONObject(result)
        //レストラン情報JSON配列オブジェクトを取得
        val ShopJSONArrays = rootJSON.getJSONObject("results")
        val ShopJSONArray = ShopJSONArrays.getJSONArray("shop")
        val ShopImage = findViewById<ImageView>(R.id.ShoprestaurantImage)
        if(ShopJSONArray.length() != 0) {
        var ShopJSON = ShopJSONArray.getJSONObject(0)

            var name = ShopJSON.getString("name")
            var address = ShopJSON.getString("address")
            var station = ShopJSON.getString("station_name")
            var open = ShopJSON.getString("open")
            var photo = ShopJSON.getJSONObject("photo").getJSONObject("mobile").getString("l")
            val Shopname = findViewById<TextView>(R.id.ShoprestaurantnameTextView)
            Shopname.text = name
            val Shopaddress = findViewById<TextView>(R.id.ShopaddressTextView)
            Shopaddress.text = address
            val Shopstation = findViewById<TextView>(R.id.ShopstationTextView)
            Shopstation.text = station
            val Shopopen = findViewById<TextView>(R.id.ShopopenTextView)
            Shopopen.text = open

            val imageTask: RestaurantDetail.GetImage = RestaurantDetail.GetImage(ShopImage)
            imageTask.execute(photo)
        }
        else
        {
            Snackbar.make(
                ShopImage,
                "データが存在しませんでした。",
                Snackbar.LENGTH_SHORT
            )    //バーで追加したことを知らせる
                .setAction("戻る") { finish() }  //戻るボタン
                .setActionTextColor(Color.BLUE)   //テキストカラー
                .show()
        }
    }

    class GetImage(private val image: ImageView) : AsyncTask<String, Void, Bitmap>() {
        override fun doInBackground(vararg params: String): Bitmap? {
            val image: Bitmap   //Bitmap形式の格納場所
            try {
                val imageUrl = URL(params[0])   //ウェブ画像を取得
                val imageIs: InputStream
                imageIs = imageUrl.openStream()
                image = BitmapFactory.decodeStream(imageIs)
                return image
            } catch (e: IOException) {
                return null
            }
        }

        override fun onPostExecute(result: Bitmap) {
            // 取得した画像をImageViewに設定します。
            image.setImageBitmap(result)
        }
    }
    fun onShopMapButtonClick(view: View){

    }
}