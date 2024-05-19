package com.websarva.wings.android.gourmetsearch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    var radius = 1
    var genrecode = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val radiusItems = arrayOf(
            "300m",
            "500m",
            "1000m",
            "2000m",
            "3000m"
        )
        val genreItems = arrayOf(
                "ジャンル未選択",
                "居酒屋",
                "ダイニングバー・バル",
                "創作料理",
                "和食",
                "洋食",
                "イタリアン・フレンチ",
                "中華",
                "焼肉・ホルモン",
                "アジア・エスニック料理",
                "各国料理",
                "カラオケ・パーティ",
                "バー・カクテル",
                "ラーメン",
                "カフェ・スイーツ",
                "その他グルメ",
                "お好み焼き・もんじゃ",
                "韓国料理"
        )

        val genrecodeList = arrayOf(
                "G000",
                "G001",
                "G002",
                "G003",
                "G004",
                "G005",
                "G006",
                "G007",
                "G008",
                "G009",
                "G010",
                "G011",
                "G012",
                "G013",
                "G014",
                "G015",
                "G016",
                "G017"
        )

        val adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            radiusItems
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        var radiusSp = findViewById<Spinner>(R.id.radius)
        var radiusTx = findViewById<TextView>(R.id.radiustext)
        radiusSp.adapter = adapter
        radiusSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                // View Binding
                radiusTx.text = item
                radius = position + 1
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
                radius = 1
            }

        }

        var genreSP = findViewById<Spinner>(R.id.genre)
        var genreTx = findViewById<TextView>(R.id.genreTextView)



        val genreadapter = ArrayAdapter(
                applicationContext,
                android.R.layout.simple_spinner_item,
                genreItems
        )
        genreadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        genreSP.adapter = genreadapter
        genreSP.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?, position: Int, id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                // View Binding
                genreTx.text = item
                genrecode = genrecodeList[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                genrecode = genrecodeList[0]
            }

        }




        val listener = searchListener()
        val Search = findViewById<Button>(R.id.search)  //サーチボタンオブジェクトの取得
        Search.setOnClickListener(listener)       //リスナ設定

    }

    private inner class searchListener : View.OnClickListener {
        override fun onClick(view: View) {
            //idのR値に応じて処理を分岐する
            var midnight = findViewById<CheckBox>(R.id.midnight)
            when (view.id) {
                R.id.search -> {
                    val intent = Intent(this@SearchActivity, restaurantlist::class.java)
                    intent.putExtra("RADIUS_TEXT",radius.toString())        //半径データを渡す
                    intent.putExtra("midnight_SELECT",midnight.isChecked)
                    intent.putExtra("genre_CODE",genrecode)
                    startActivity(intent)   //飛ぶ
                }
            }
        }
    }
}