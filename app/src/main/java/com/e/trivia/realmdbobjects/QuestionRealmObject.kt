package com.e.trivia.realmdbobjects

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

open class QuestionRealmObject :RealmObject() {

    @PrimaryKey
    var question:String=""
    var answer:Boolean=false
}