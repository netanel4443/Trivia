package com.e.trivia.realmdbobjects

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PlayerDetailsRealmObject:RealmObject() {
    //when a primary key is a string, Realm will auto annotate it with @index
    @PrimaryKey
    var name:String=""
    var level:Int=0
    var score:Int=0
    var coins:Int=0
}