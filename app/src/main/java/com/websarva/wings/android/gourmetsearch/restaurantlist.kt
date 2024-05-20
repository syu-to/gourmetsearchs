package com.websarva.wings.android.gourmetsearch

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class restaurantlist : AppCompatActivity() {

    companion object {
        private  const val DEBUG_TAG = "AsyncSample"    //ログメッセージ
        private const val RESTAURANT_URL = "https://webservice.recruit.co.jp/hotpepper/gourmet/v1/" //APIのアドレス
        private const val API_ID = "59c533b8ba2a26ed"   //APIキー
    }


    var count = 0
    var radiusText = ""
    var midnight_check = false
    var searchgenrecode = ""
    private var _latitude = 0.0 //緯度プロパティ
    private var _longitude = 0.0    //経度プロパティ
    //FusedLocationProviderClientオブジェクトプロパティ
    private lateinit var _fusedLocationClient: FusedLocationProviderClient
    //locationRequestオブジェクトプロパティ
    private lateinit var _locationRequest: LocationRequest
    //位置情報が変更されたときの処理を行うコールバックオブジェクトプロパティ
    private lateinit var _onUpdateLocation: OnUpdateLocation
    private inner class OnUpdateLocation: LocationCallback(){
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            if(count == 0) {
                val location = locationResult.lastLocation
                location?.let {
                    //locationオブジェクトから緯度を取得
                    _latitude = it.latitude
                    //locationオブジェクトから経度を取得
                    _longitude = it.longitude
                }
                val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
                if(networkCapabilities != null) {
                    println(_latitude)
                    if(searchgenrecode == "G000") {
                        val urlFull = "$RESTAURANT_URL?key=$API_ID&lat=$_latitude&lng=$_longitude&range=$radiusText&count=100&format=json" //様々な条件をつけてAPIを呼び出す
                        println(urlFull)
                        restaurantInfo(urlFull)
                    }
                    else
                    {
                        val urlFull = "$RESTAURANT_URL?key=$API_ID&lat=$_latitude&lng=$_longitude&range=$radiusText&count=100&genre=$searchgenrecode&format=json"
                        println(urlFull)
                        restaurantInfo(urlFull)
                    }

                }
                else
                {
                    val restaurantList = findViewById<ListView>(R.id.restaurantlist)
                    Snackbar.make(restaurantList, "Wi-Fiに接続出来ません。", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                            .setAction("再試行") { count = 0 }  //戻るボタン
                            .setActionTextColor(Color.BLUE)   //テキストカラー
                            .show()
                }
                count = 1   //検索は開いてから一度のみにする
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_restaurantlist)
        //FusedLocationProviderClientオブジェクト取得
        _fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@restaurantlist)
        //LocationRequestのビルダーオブジェクトを生成
        val builder = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,5000)
        //LocationRequestオブジェクトを生成
        _locationRequest = builder.build()
        //位置情報が変更されたときの処理を行うコールバックオブジェクトを生成
        _onUpdateLocation = OnUpdateLocation()

        radiusText = intent.getStringExtra("RADIUS_TEXT").toString()   //半径取得
        midnight_check = intent.getBooleanExtra("midnight_SELECT",false)    //23時以降営業にチェックが入っていたかの取得
        searchgenrecode = intent.getStringExtra("genre_CODE").toString()   //指定ジャンル
        println(_latitude)


    }

    override fun onResume(){
        super.onResume()

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val permission = arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION)
            ActivityCompat.requestPermissions(this,permission,1000)
            return
        }
        _fusedLocationClient.requestLocationUpdates(_locationRequest,_onUpdateLocation,mainLooper)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1000 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.checkSelfPermission(this@restaurantlist,android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                return
            }

            _fusedLocationClient.requestLocationUpdates(_locationRequest, _onUpdateLocation, mainLooper)
        }
    }
    override fun onPause(){
        super.onPause()

        _fusedLocationClient.removeLocationUpdates(_onUpdateLocation)
    }


    //レストランの情報取得を行うメソッド
    @UiThread
    private fun restaurantInfo(urlFull:String){
        val backgroundReceiver = RestaurantInfoBackgroundReceiver(urlFull)
        val executeService = Executors.newSingleThreadExecutor()
        val future =executeService.submit(backgroundReceiver)
        val result = future.get()
        ShowRestaurantInfo(result)
    }

    @UiThread
    private fun ShowRestaurantInfo(result: String){
        var logoimage = ""
        val logolist:MutableList<String> = mutableListOf()
        val photolist:MutableList<String> = mutableListOf()
        //listview取得
        val restaurantList = findViewById<ListView>(R.id.restaurantlist)

        //ルートオブジェクトを生成

        val rootJSON = JSONObject(result)
        //レストラン情報JSON配列オブジェクトを取得
        val restaurantJSONArrays = rootJSON.getJSONObject("results")
        val restaurantJSONArray = restaurantJSONArrays.getJSONArray("shop")
        if(restaurantJSONArray.length() != 0) {
            var restaurantJSON = restaurantJSONArray.getJSONObject(0)
            logoimage = restaurantJSON.getString("logo_image")
            var photo = restaurantJSON.getJSONObject("photo").getJSONObject("mobile").getString("l")
            var midnight = restaurantJSON.getString("midnight")

                if (midnight_check) {
                    if (midnight == "営業している") {
                        logolist.add(logoimage)
                        photolist.add(photo)
                    }
                } else {
                    logolist.add(logoimage)
                    photolist.add(photo)
                }


            var count = 1
            var size = restaurantJSONArray.length()
            while (count <= size - 1) {
                //すべての情報を取得取得
                restaurantJSON = restaurantJSONArray.getJSONObject(count)
                logoimage = restaurantJSON.getString("logo_image")
                var photo =
                        restaurantJSON.getJSONObject("photo").getJSONObject("mobile").getString("l")
                var midnight = restaurantJSON.getString("midnight")
                var genrecode = restaurantJSON.getJSONObject("genre").getString("code")
                println(genrecode)
                    if (midnight_check) {
                        if (midnight == "営業している") {
                            logolist.add(logoimage)
                            photolist.add(photo)
                        }
                    } else {
                        logolist.add(logoimage)
                        photolist.add(photo)
                    }
                count += 1
            }

            if(logolist.size != 0) {
                val imageTask: restaurantlist.GetImage = restaurantlist.GetImage(
                        this,
                        restaurantJSONArray,
                        restaurantList,
                        logolist,
                        photolist,
                        midnight_check,
                        searchgenrecode
                )
                imageTask.execute(logoimage)
                restaurantList.onItemClickListener = ListItemclickListener()
            }
            else{
                Snackbar.make(restaurantList, "検索結果が0件でした", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                        .setAction("戻る") { finish() }  //戻るボタン
                        .setActionTextColor(Color.BLUE)   //テキストカラー
                        .show()
            }
        }
        else
        {
            Snackbar.make(restaurantList, "検索結果が0件でした", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                    .setAction("戻る") { finish() }  //戻るボタン
                    .setActionTextColor(Color.BLUE)   //テキストカラー
                    .show()
        }
    }

    private inner class ListItemclickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = parent.getItemAtPosition(position) as MutableMap<String,String>
            //レストラン名と住所と最寄り駅の取得
            var name = item["name"]
            var address = item["address"]
            var station = item["station_name"]
            var image = item["logo_image"]
            var photo = item["photo"]
            var open = item["open"]
            var logoimage = item["logo_image"]
            var access = item["access"]
            //インテリテントオブジェクトを生成
            var intents = Intent(this@restaurantlist,RestaurantDetail::class.java)
            //レストラン詳細画面に送るデータを格納
            intents.putExtra("restaurant",name)
            intents.putExtra("address",address)
            intents.putExtra("station_name",station)
            intents.putExtra("photo",photo)
            intents.putExtra("open",open)
            intents.putExtra("logo_image",logoimage)
            intents.putExtra("access",access)
            //レストラン詳細画面の起動
            startActivity(intents)
        }
    }
    //非同期でAPIに接続するためのクラス
    private inner class RestaurantInfoBackgroundReceiver(url: String): Callable<String> {
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
                Log.w(DEBUG_TAG,"通信タイムアウト",ex)
            }
            con.disconnect()
            //WebAPIに接続するコード
            return  result
        }

        private fun is2String(stream: InputStream): String{
            val sb = StringBuilder()
            val reader =  BufferedReader(InputStreamReader(
                stream, StandardCharsets.UTF_8))
            var line = reader.readLine()
            while (line != null){
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            return sb.toString()
        }

    }
    class GetImage(val context: Context, val restaurantJSONArray: JSONArray,var restaurantList: ListView,var logolist: MutableList<String>,var photolist: MutableList<String>,var midnight_check: Boolean,var searchgenrecode: String) : AsyncTask<String, Void, Bitmap>() {
        var image: MutableList<Bitmap> = mutableListOf()
        override fun doInBackground(vararg params: String): Bitmap? {

            try {
                var size = logolist.size
                val imageUrl = URL(logolist[0])
                val imageIs: InputStream
                imageIs = imageUrl.openStream()
                image.add(BitmapFactory.decodeStream(imageIs))

                var count = 1
                while (count <= size - 1) {
                    val imageUrl = URL(logolist[count])
                    val imageIs: InputStream
                    imageIs = imageUrl.openStream()
                    image.add(BitmapFactory.decodeStream(imageIs))
                    count += 1
                }
                return image[0]
            } catch (e: IOException) {
                return null
            }

        }

        override fun onPostExecute(result: Bitmap) {
            // 取得した画像をImageViewに設定します。
            var imgcnt = 0
            val alllist: MutableList<MutableMap<String, Any>> = mutableListOf()
            var restaurantJSON = restaurantJSONArray.getJSONObject(0)
            var restaurant = restaurantJSON.getString("name")
            var logoimage = restaurantJSON.getString("logo_image")
            var address = restaurantJSON.getString("address")
            var station = restaurantJSON.getString("station_name")
            var access = restaurantJSON.getString("access")
            var open = restaurantJSON.getString("open")
            var photo = restaurantJSON.getJSONObject("photo").getJSONObject("mobile").getString("l")
            var menu = mutableMapOf("name" to restaurant, "logo_image" to logoimage, "address" to address, "station_name" to station, "image" to image[0],"photo" to photo,"access" to access,"open" to open)
            var midnight = restaurantJSON.getString("midnight")
                if (midnight_check) {
                    if (midnight == "営業している") {
                        alllist.add(menu)

                    }
                } else {
                    alllist.add(menu)
                    imgcnt += 1
                }
            var count = 1

            var size = restaurantJSONArray.length()
            while (count <= size - 1) {
                restaurantJSON = restaurantJSONArray.getJSONObject(count)
                restaurant = restaurantJSON.getString("name")
                logoimage = restaurantJSON.getString("logo_image")
                address = restaurantJSON.getString("address")
                station = restaurantJSON.getString("station_name")
                photo = restaurantJSON.getJSONObject("photo").getJSONObject("mobile").getString("l")
                access = restaurantJSON.getString("access")
                open = restaurantJSON.getString("open")
                var midnight = restaurantJSON.getString("midnight")
                println("onPostExecute:" + R.drawable.resource_default)
                println(image.size)
                println(size)
                    if (midnight_check) {
                        if (midnight == "営業している") {
                            println(count)
                            menu = mutableMapOf("name" to restaurant, "logo_image" to logoimage, "address" to address, "station_name" to station, "image" to image[imgcnt], "photo" to photo, "access" to access, "open" to open)
                            alllist.add(menu)
                            imgcnt += 1
                            println(image.size)
                        }
                    } else {
                        println(count)
                        menu = mutableMapOf("name" to restaurant, "logo_image" to logoimage, "address" to address, "station_name" to station, "image" to image[imgcnt], "photo" to photo, "access" to access, "open" to open)
                        alllist.add(menu)
                        imgcnt += 1
                    }
                count += 1
            }

            //第4引数form用データの用意
            val from = arrayOf("name", "image","access")
            //第5引数to用データ
            val to = intArrayOf(R.id.restaurantText, R.id.logoImageView,R.id.accessTextView)
            //adapter作成
            val adapter = restaurantlist.CustomAdapter(context, alllist, R.layout.list_item, from, to)
            restaurantList.adapter = adapter
        }

    }

    class CustomAdapter(
        context: Context,   //表示するxmlファイル
        private val data: List<Map<String, Any>>,   //データの一覧
        resource: Int,  //表示形式
        from: Array<out String>,    //配列のアドレス
        to: IntArray    //どのViewに表示するか
    ) : SimpleAdapter(context, data, resource, from, to) {  //SimpleAdapterを継承する

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            //ロゴイメージを表示するImageViewの取得
            val imageView =
                view.findViewById<ImageView>(R.id.logoImageView) // imageViewはレイアウトファイルに存在するImageViewのID

            // 画像の表示ロジックを実装する
            val image = data[position]["image"] as Bitmap // imageにはBitmap形式のロゴの画像が入っている
            imageView.setImageBitmap(image) //bitmapの画像をsetImageBitmapで表示する

            // その他のデータの表示ロジックを実装する
            val textView =
                view.findViewById<TextView>(R.id.restaurantText) // textViewはレイアウトファイルに存在するTextViewのID
            val text = data[position]["name"] as String // レストラン名を取得
            textView.text = text    //表示する

            return view
        }
    }


}