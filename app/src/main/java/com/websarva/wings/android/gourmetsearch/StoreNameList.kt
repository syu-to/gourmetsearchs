package com.websarva.wings.android.gourmetsearch

import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.annotation.UiThread
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.paging.*
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.Callable
import java.util.concurrent.Executors

class StoreNameList : AppCompatActivity() {
    companion object {
        private  const val DEBUG_TAG = "AsyncSample"    //ログメッセージ
        private const val RESTAURANT_URL = "https://webservice.recruit.co.jp/hotpepper/shop/v1/" //店名サーチAPIのアドレス
        private const val API_ID = "59c533b8ba2a26ed"   //APIキー
    }


    var keyword = ""
    var prefectures = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_name_list)

        keyword = intent.getStringExtra("keyword").toString()
        prefectures = intent.getStringExtra("prefectures").toString()

        val keywordByte = URLEncoder.encode(keyword, StandardCharsets.UTF_8.toString())
        val prefecturesByte = URLEncoder.encode(prefectures, StandardCharsets.UTF_8.toString())

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(networkCapabilities != null) {
            val urlFull =
                "${StoreNameList.RESTAURANT_URL}?key=${StoreNameList.API_ID}&keyword=${keywordByte}%2C${prefecturesByte}&count=30&format=json"
            println(urlFull)
            ShopInfo(urlFull)
        }
        else
        {
            val shopList = findViewById<ListView>(R.id.storenamelist)
            Snackbar.make(shopList, "Wi-Fiに接続出来ません。", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                .setAction("戻る") { finish() }  //戻るボタン
                .setActionTextColor(Color.BLUE)   //テキストカラー
                .show()
        }

    }

    @UiThread
    private fun ShopInfo(urlFull:String){
        val backgroundReceiver = ShopInfoBackgroundReceiver(urlFull)
        val executeService = Executors.newSingleThreadExecutor()
        val future =executeService.submit(backgroundReceiver)
        val result = future.get()
        ShowShopInfo(result)
    }

    private inner class ShopInfoBackgroundReceiver(url: String): Callable<String> {
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
                Log.w(StoreNameList.DEBUG_TAG,"通信タイムアウト",ex)
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
    private fun ShowShopInfo(result: String){
        val alllist: MutableList<MutableMap<String, String>> = mutableListOf()
        val namelist:MutableList<String> = mutableListOf()
        val storenameList = findViewById<ListView>(R.id.storenamelist)
        //ルートオブジェクトを生成
        val rootJSON = JSONObject(result)
        //レストラン情報JSON配列オブジェクトを取得
        val ShopJSONArrays = rootJSON.getJSONObject("results")
        println(ShopJSONArrays.length())
        if(ShopJSONArrays.length() != 2) {
            val ShopJSONArray = ShopJSONArrays.getJSONArray("shop")
            if (ShopJSONArray.length() != 0) {
                var ShopJSON = ShopJSONArray.getJSONObject(0)
                var name = ShopJSON.getString("name")
                var id = ShopJSON.getString("id")


                var menu = mutableMapOf("name" to name, "id" to id)
                alllist.add(menu)
                var count = 1
                var size = ShopJSONArray.length()
                while (count <= size - 1) {
                    //すべての情報を取得取得
                    ShopJSON = ShopJSONArray.getJSONObject(count)
                    var name = ShopJSON.getString("name")
                    var id = ShopJSON.getString("id")
                    var menu = mutableMapOf("name" to name, "id" to id)
                    alllist.add(menu)
                    count += 1
                }

                if (alllist.size != 0) {
                    //第4引数form用データの用意
                    val from = arrayOf("name")
                    //第5引数to用データ
                    val to = intArrayOf(R.id.searchitems)
                    //adapter作成
                    val adapter =
                        SimpleAdapter(this@StoreNameList, alllist, R.layout.list_item2, from, to)
                    storenameList.adapter = adapter
                    storenameList.onItemClickListener = ListItemclickListener()
                } else {
                    Snackbar.make(
                        storenameList,
                        "検索結果が0件でした",
                        Snackbar.LENGTH_SHORT
                    )    //バーで追加したことを知らせる
                        .setAction("戻る") { finish() }  //戻るボタン
                        .setActionTextColor(Color.BLUE)   //テキストカラー
                        .show()
                }
            } else {
                Snackbar.make(
                    storenameList,
                    "検索結果が0件でした",
                    Snackbar.LENGTH_SHORT
                )    //バーで追加したことを知らせる
                    .setAction("戻る") { finish() }  //戻るボタン
                    .setActionTextColor(Color.BLUE)   //テキストカラー
                    .show()
            }
        }
        else
        {
            Snackbar.make(
                storenameList,
                "検索結果が30件超えでした。キーワードを増やしてみてください",
                Snackbar.LENGTH_SHORT
            )    //バーで追加したことを知らせる
                .setAction("戻る") { finish() }  //戻るボタン
                .setActionTextColor(Color.BLUE)   //テキストカラー
                .show()
        }
    }

    private inner class ListItemclickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
            val item = parent.getItemAtPosition(position) as MutableMap<String,String>
            //レストラン名と住所と最寄り駅の取得
            var id = item["id"]
            //インテリテントオブジェクトを生成
            var intents = Intent(this@StoreNameList,StoreNamedetail::class.java)
            //レストラン詳細画面に送るデータを格納
            intents.putExtra("id",id)
            //レストラン詳細画面の起動
            startActivity(intents)
        }
    }

}
