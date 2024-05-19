package com.websarva.wings.android.gourmetsearch

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class StoreNameSearch : AppCompatActivity() {
    var prefectures = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_name_search)

        val prefecturesItem = arrayOf(
            "東京",
            "大阪",
            "北海道",
            "青森",
            "秋田",
            "岩手",
            "山形",
            "宮城",
            "福島",
            "群馬",
            "栃木",
            "茨城",
            "埼玉",
            "神奈川",
            "千葉",
            "山梨",
            "静岡",
            "新潟",
            "富山",
            "石川",
            "福井",
            "長野",
            "岐阜",
            "愛知",
            "三重",
            "滋賀",
            "奈良",
            "和歌山",
            "京都",
            "兵庫",
            "香川",
            "徳島",
            "高知",
            "愛媛",
            "鳥取",
            "島根",
            "岡山",
            "広島",
            "山口",
            "福岡",
            "佐賀",
            "長崎",
            "大分",
            "熊本",
            "宮崎",
            "鹿児島",
            "沖縄"
        )

        val adapter = ArrayAdapter(
            applicationContext,
            android.R.layout.simple_spinner_item,
            prefecturesItem
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)


        var prefecturesSp = findViewById<Spinner>(R.id.prefectures)
        var prefecturesTx = findViewById<TextView>(R.id.prefecturesText)
        prefecturesSp.adapter = adapter
        prefecturesSp.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            //　アイテムが選択された時
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?, position: Int, id: Long
            ) {
                val spinnerParent = parent as Spinner
                val item = spinnerParent.selectedItem as String
                // View Binding
                prefecturesTx.text = item
                prefectures = prefecturesItem[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
                //
                prefectures = "東京"
            }

        }

        val listener = searchListener()
        val storenamesearchbutton = findViewById<Button>(R.id.Keywordsearch)
        storenamesearchbutton.setOnClickListener(listener)
    }

    private inner class searchListener : View.OnClickListener {
        override fun onClick(view: View) {
            //idのR値に応じて処理を分岐する
            var keyword = findViewById<EditText>(R.id.KeywordText)
            var keyText = keyword.text.toString()
            keyText = keyText.replace("\\s+".toRegex(), "AAA")
            if(keyword.text.toString() == ""){
                keyText = "検索された文字が存在しません。"
            }
            when (view.id) {
                R.id.Keywordsearch -> {
                    val intent = Intent(this@StoreNameSearch, StoreNameList::class.java)
                    intent.putExtra("keyword",keyText.toString())        //半径データを渡す
                    intent.putExtra("prefectures",prefectures.toString())
                    startActivity(intent)   //飛ぶ
                }
            }
        }
    }
}