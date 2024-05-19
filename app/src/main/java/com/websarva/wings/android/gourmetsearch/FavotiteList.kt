package com.websarva.wings.android.gourmetsearch

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.RealmResults
import io.realm.kotlin.where
import java.io.IOException
import java.io.InputStream
import java.net.URL

class FavotiteList : AppCompatActivity() {
    private  lateinit var realm: Realm

    var nameList: List<String> = mutableListOf()
    var addressList: List<String> = mutableListOf()
    var stationList: List<String> = mutableListOf()
    var openList: List<String> = mutableListOf()
    var imageList: List<String> = mutableListOf()
    var photoList: List<String> = mutableListOf()
    var accessList: List<String> = mutableListOf()
    var idlist: List<Long> = mutableListOf()
    var idArray: Array<Long> = arrayOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favotite_list)

        realm = Realm.getDefaultInstance()  //Realmのデフォルトインスタンス
        var results: RealmResults<favorite> = realm.where<favorite>().findAll()
        nameList = results.map { it.name }
        addressList = results.map { it.address }
        stationList = results.map { it.station }
        openList = results.map { it.open }
        imageList = results.map { it.image }
        photoList = results.map { it.photo }
        accessList = results.map{it.access}
        idlist = results.map{it.id}
        idArray = idlist.toTypedArray()
        val favotitelist = findViewById<ListView>(R.id.favotiteList)

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)

        if(nameList.size != 0) {
            val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if(networkCapabilities != null) {
                val imageTask: FavotiteList.GetImage =
                        FavotiteList.GetImage(this, imageList, nameList, favotitelist, accessList)
                var count = 0
                imageTask.execute(imageList[count])
            }
            else{
                val alllist: MutableList<MutableMap<String, String>> = mutableListOf()
                var menu = mutableMapOf("name" to nameList[0],"access" to accessList[0])
                alllist.add(menu)
                var count = 1
                var size = nameList.size
                while (count <= size - 1) {
                    menu = mutableMapOf("name" to nameList[count],"access" to accessList[count])
                    alllist.add(menu)
                    count += 1
                }
                println(alllist)

                //第4引数form用データの用意
                val from = arrayOf("name","access")
                //第5引数to用データ
                val to = intArrayOf(R.id.restaurantText,R.id.accessTextView)
                //adapter作成
                val adapter = FavotiteList.CustomAdapter(this, alllist, R.layout.list_item, from, to)
                favotitelist.adapter = adapter
            }

        }
        else
        {
            Snackbar.make(favotitelist, "キニナル登録がされていません。", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                .setAction("戻る") { finish() }  //戻るボタン
                .setActionTextColor(Color.BLUE)   //テキストカラー
                .show()
        }

        favotitelist.onItemClickListener = ListItemclickListener()
    }
    override fun onResume(){
        super.onResume()
        setContentView(R.layout.activity_favotite_list)

        realm = Realm.getDefaultInstance()  //Realmのデフォルトインスタンス
        var results: RealmResults<favorite> = realm.where<favorite>().findAll()
        nameList = results.map { it.name }
        addressList = results.map { it.address }
        stationList = results.map { it.station }
        openList = results.map { it.open }
        imageList = results.map { it.image }
        photoList = results.map { it.photo }
        accessList = results.map{it.access}
        idlist = results.map{it.id}
        idArray = idlist.toTypedArray()
        val favotitelist = findViewById<ListView>(R.id.favotiteList)

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if(nameList.size != 0) {
            if(networkCapabilities != null) {
                val imageTask: FavotiteList.GetImage =
                        FavotiteList.GetImage(this, imageList, nameList, favotitelist, accessList)
                var count = 0
                imageTask.execute(imageList[count])
            }else{
                val alllist: MutableList<MutableMap<String, String>> = mutableListOf()
                var menu = mutableMapOf("name" to nameList[0],"access" to accessList[0])
                alllist.add(menu)
                var count = 1
                var size = nameList.size
                while (count <= size - 1) {
                    menu = mutableMapOf("name" to nameList[count],"access" to accessList[count])
                    alllist.add(menu)
                    count += 1
                }
                println(alllist)

                //第4引数form用データの用意
                val from = arrayOf("name","access")
                //第5引数to用データ
                val to = intArrayOf(R.id.restaurantText,R.id.accessTextView)
                //adapter作成
                val adapter = FavotiteList.CustomAdapter(this, alllist, R.layout.list_item, from, to)
                favotitelist.adapter = adapter
            }

        }
        else
        {
            Snackbar.make(favotitelist, "キニナル登録がされていません。", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                    .setAction("戻る") { finish() }  //戻るボタン
                    .setActionTextColor(Color.BLUE)   //テキストカラー
                    .show()
        }

        favotitelist.onItemClickListener = ListItemclickListener()
    }

    private inner class ListItemclickListener : AdapterView.OnItemClickListener{
        override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {

            var name = nameList[position]
            var address = addressList[position]
            var station = stationList[position]
            var photo = photoList[position]
            var open = openList[position]
            var access = accessList[position]
            var id = idArray[position]

            var intents = Intent(this@FavotiteList,FavotiteDetaile::class.java)

            //レストラン詳細画面に送るデータを格納
            intents.putExtra("restaurant",name)
            intents.putExtra("address",address)
            intents.putExtra("station_name",station)
            intents.putExtra("photo",photo)
            intents.putExtra("open",open)
            intents.putExtra("access",access)
            intents.putExtra("id",id)
            //レストラン詳細画面の起動
            startActivity(intents)
        }
    }



    class GetImage(val context: Context,var logolist: List<String>, var namelist: List<String>,var favotiteList: ListView,var accesslist: List<String>) : AsyncTask<String, Void, Bitmap>() {
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
            val alllist: MutableList<MutableMap<String, Any>> = mutableListOf()
            var menu = mutableMapOf("name" to namelist[0], "logo_image" to image[0],"access" to accesslist[0])
            alllist.add(menu)
            var count = 1
            var size = namelist.size
            while (count <= size - 1) {
                menu = mutableMapOf("name" to namelist[count], "logo_image" to image[count],"access" to accesslist[count])
                alllist.add(menu)
                count += 1
            }
            println(alllist)

            //第4引数form用データの用意
            val from = arrayOf("name", "logo_image","access")
            //第5引数to用データ
            val to = intArrayOf(R.id.restaurantText, R.id.logoImageView,R.id.accessTextView)
            //adapter作成
            val adapter = FavotiteList.CustomAdapter(context, alllist, R.layout.list_item, from, to)
            favotiteList.adapter = adapter
        }
    }

        //ロゴ表示のために作成したアダプター
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
            if(data[position]["logo_image"] != null) {
                val image = data[position]["logo_image"] as Bitmap // imageにはBitmap形式のロゴの画像が入っている
                imageView.setImageBitmap(image) //bitmapの画像をsetImageBitmapで表示する
            }

            // その他のデータの表示ロジックを実装する
            val textView =
                view.findViewById<TextView>(R.id.restaurantText) // textViewはレイアウトファイルに存在するTextViewのID
            val text = data[position]["name"] as String // レストラン名を取得
            textView.text = text    //表示する

            return view
        }
    }
}