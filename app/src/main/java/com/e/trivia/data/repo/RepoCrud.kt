package com.e.trivia.data.repo

import com.e.VoiceAssistant.utils.printIfDebug
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.repo.configurations.QuestionConfiguration
import com.e.trivia.data.repo.configurations.RealmMainConfiguration
import com.e.trivia.data.repo.realmdbobjects.PlayerDetailsRealmObject
import com.e.trivia.data.repo.realmdbobjects.QuestionRealmObject
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import java.lang.Exception

class RepoCrud(){

    fun readPlayerDetails(): Single<PlayerDetails> {

       return Single.fromCallable {
           val realm= Realm.getInstance(RealmMainConfiguration().config())
           val playerDetails=PlayerDetails()

           realm.use {
                val realmObject =
                    realm.where(PlayerDetailsRealmObject::class.java)
                        .findFirst()

                realmObject?.let { details->
                    playerDetails.highestlevel=details.highestLevel
                    playerDetails.highestScore=details.highestScore
                    playerDetails.name=details.name
                    playerDetails.diamonds=details.diamonds
                }
            }
           playerDetails
        }
    }

    fun deletePlayerDetails(playerName: String):Completable{
        return Completable.fromAction {
            val realm=Realm.getInstance(RealmMainConfiguration().config())
            realm.use {
                realm.where(PlayerDetailsRealmObject::class.java)
                    .equalTo("name",playerName)
                    .findFirst()?.deleteFromRealm()
            }
        }
    }

    fun savePlayerDetails(playerDetails:PlayerDetails):Completable{
        return Completable.fromAction {
            val realm=Realm.getInstance(RealmMainConfiguration().config())
            try {
            realm.executeTransaction {

                  val rlmObj=  realm.where(PlayerDetailsRealmObject::class.java)
                        .equalTo("name",playerDetails.name)
                        .findFirst()
                     rlmObj?.run {
                        highestLevel=playerDetails.highestlevel
                        diamonds=playerDetails.diamonds
                        highestScore=playerDetails.highestScore
                        realm.insertOrUpdate(this)
                     }
                }
            }  catch (e:Exception){ printIfDebug("savePlayer",e.message)}
            finally {
                realm?.close()
            }
        }
    }

    fun updatePlayerDetails(){

    }

    fun createQuestionsDb(question:String,answer:Boolean): Completable {

        return Completable.fromAction {
            val config=QuestionConfiguration().config()
            val realm=Realm.getInstance(config)

            realm.use {
                val realmObject=realm.createObject(QuestionRealmObject::class.java,question)
                realmObject.answer=answer
            }
        }
    }
}