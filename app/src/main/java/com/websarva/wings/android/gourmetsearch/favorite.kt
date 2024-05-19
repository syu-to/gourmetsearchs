package com.websarva.wings.android.gourmetsearch

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey


open class favorite:RealmObject()  {
    @PrimaryKey
    var id: Long = 0
    var name: String = ""
    var address: String = ""
    var station: String = ""
    var open: String = ""
    var image:String = ""
    var photo: String = ""
    var access: String = ""
}