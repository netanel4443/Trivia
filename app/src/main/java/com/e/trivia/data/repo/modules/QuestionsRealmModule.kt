package com.e.trivia.data.repo.modules

import com.e.trivia.data.repo.realmdbobjects.QuestionRealmObject
import io.realm.annotations.RealmModule

@RealmModule(classes = [QuestionRealmObject::class])
class QuestionsRealmModule()
