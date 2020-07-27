package com.e.trivia.data.repo.modules

import com.e.trivia.data.repo.realmdbobjects.PlayerDetailsRealmObject
import io.realm.annotations.RealmModule

@RealmModule(classes = [PlayerDetailsRealmObject::class])
class PlayerDetailsRealmModule()
