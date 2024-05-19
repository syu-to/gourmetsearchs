package com.websarva.wings.android.gourmetsearch

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import io.realm.Realm
import io.realm.kotlin.createObject
import io.realm.kotlin.where
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.net.URLEncoder

class FavotiteDetaile : AppCompatActivity() {
    private  lateinit var realm: Realm
    var restaurant_name = ""
    var address = ""
    var station = ""
    var photo = ""
    var open = ""
    var logo_image = ""
    var access = ""
    var id:Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favotite_detaile)

        restaurant_name = intent.getStringExtra("restaurant").toString()
        address = intent.getStringExtra("address").toString()
        station = intent.getStringExtra("station_name").toString()
        photo = intent.getStringExtra("photo").toString()
        open = intent.getStringExtra("open").toString()
        logo_image = intent.getStringExtra("logo_image").toString()
        access = intent.getStringExtra("access").toString()
        id = intent.getLongExtra("id",0)

        val restaurantnametext = findViewById<TextView>(R.id.FavotiterestaurantnameTextView)
        val addresstext = findViewById<TextView>(R.id.FavotiteaddressTextView)
        val stationtext = findViewById<TextView>(R.id.FavotitestationTextView)
        val photoimage = findViewById<ImageView>(R.id.FavotiterestaurantImage)
        val opentext = findViewById<TextView>(R.id.FavotiteopenTextView)

        restaurantnametext.text = restaurant_name.toString()
        addresstext.text = address.toString()
        stationtext.text = station.toString()
        opentext.text = open

        val listener = FavotiteListener()
        val favoritedelete = findViewById<Button>(R.id.favotitedelete)
        favoritedelete.setOnClickListener(listener)       //リスナ設定

        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        //イメージ画像をBitmapに変換して表示するクラスを呼び出す
        if(networkCapabilities != null) {
            val imageTask: RestaurantDetail.GetImage = RestaurantDetail.GetImage(photoimage)
            imageTask.execute(photo)

        }
    }

    fun onFavotiteMapButtonClick(view: View){
        //店の名前をURLエンコードで取得
        val SearchWord = URLEncoder.encode(address,"UTF-8")
        //マップアプリと連携するURI文字列作成
        val uriStr = "geo:0,0?q=${SearchWord}"
        //URI文字列からURIオブジェクトを生成
        val uri = Uri.parse(uriStr)
        //Intentオブジェクトを生成
        val intent = Intent(Intent.ACTION_VIEW,uri)
        //アクティビティ起動
        startActivity(intent)
    }
    private inner class FavotiteListener : View.OnClickListener {
        override fun onClick(view: View) {
            //idのR値に応じて処理を分岐する
            when (view.id) {
                R.id.favotitedelete -> {
                    realm = Realm.getDefaultInstance()  //Realmのデフォルトインスタンス
                    realm.executeTransaction { db: Realm ->
                        db.where<favorite>().equalTo("id", id)  //指定したidに合っている項目の削除
                                ?.findFirst()
                                ?.deleteFromRealm()
                    }

                    Snackbar.make(view, "削除しました。", Snackbar.LENGTH_SHORT)
                            .setActionTextColor(Color.YELLOW)   //メッセージ
                            .show()
                    onResume()  //リスト更新
                    finish()
                }
            }
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



    private inner class TopListener : View.OnClickListener{
        override fun onClick(view: View) {
            //idのR値に応じて処理を分岐する
            when(view.id){
                R.id.favotitedelete->{
                    realm = Realm.getDefaultInstance()  //Realmのデフォルトインスタンス
                    realm.executeTransaction { db: Realm ->
                        val maxId = db.where<favorite>().max("id")  // 現在の最大IDを取得
                        val nextId = (maxId?.toLong() ?: 0L) + 1L
                        val favorite = db.createObject<favorite>(nextId)
                        favorite.name = restaurant_name
                        favorite.address = address
                        favorite.station = station
                        favorite.open = open
                        favorite.photo = photo
                        favorite.image = logo_image
                        favorite.access = access
                    }
                    Snackbar.make(view, "削除しました。", Snackbar.LENGTH_SHORT)    //バーで追加したことを知らせる
                            .setAction("戻る") { finish() }  //戻るボタン
                            .setActionTextColor(Color.BLUE)   //テキストカラー
                            .show()
                }
            }
        }
    }
}