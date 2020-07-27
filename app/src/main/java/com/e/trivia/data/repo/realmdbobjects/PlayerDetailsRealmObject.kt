package com.e.trivia.data.repo.realmdbobjects

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class PlayerDetailsRealmObject:RealmObject() {
    //when a primary key is a string, Realm will auto annotate it with @index
    @PrimaryKey
    var name:String=""
    var highestLevel:Int=0
    var highestScore:Int=0
    var diamonds:Int=0
}