package com.websarva.wings.android.gourmetsearch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listener = TopListener()
        val SearchButton = findViewById<Button>(R.id.searchbutton)  //サーチボタンオブジェクトの取得
        SearchButton.setOnClickListener(listener)       //リスナ設定
        val favotiteButton = findViewById<Button>(R.id.favotitebutton)
        favotiteButton.setOnClickListener(listener)
    }

    //ボタンクリックのリスナクラス
    private inner class TopListener : View.OnClickListener{
        override fun onClick(view: View) {
            //idのR値に応じて処理を分岐する
            when(view.id){
                R.id.searchbutton->{
                    val intent = Intent(this@MainActivity,SearchActivity::class.java)
                    startActivity(intent)   //飛ぶ
                }
                R.id.favotitebutton->{
                    val intent = Intent(this@MainActivity,FavotiteList::class.java)
                    startActivity(intent)   //飛ぶ
                }
            }
        }
    }
}