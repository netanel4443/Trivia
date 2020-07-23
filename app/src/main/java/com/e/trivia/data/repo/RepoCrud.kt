package com.e.trivia.data.repo

import com.e.VoiceAssistant.utils.printIfDebug
import com.e.trivia.data.PlayerDetails
import com.e.trivia.data.repo.configurations.QuestionConfiguration
import com.e.trivia.data.repo.configurations.RealmMainConfiguration
import com.e.trivia.realmdbobjects.PlayerDetailsRealmObject
import com.e.trivia.realmdbobjects.QuestionRealmObject
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import java.lang.Exception

class RepoCrud(){

    fun readPlayerDetails(): Single<PlayerDetails> {

       return Single.fromCallable {
           val realm= Realm.getInstance(RealmMainConfiguration().config())
           val playerDetails=PlayerDetails()

           val k= realm.use {
                val realmObject =
                    realm.where(PlayerDetailsRealmObject::class.java)
                        .findFirst()

                realmObject?.let { details->
                    playerDetails.Highestlevel=details.level
                    playerDetails.highestScore=details.score
                    playerDetails.name=details.name
                    playerDetails.diamonds=details.coins
                }
            }
           playerDetails
        }
    }

    fun savePlayerDetails(playerDetails:PlayerDetails):Completable{
        return Completable.fromAction {
            val realm=Realm.getInstance(RealmMainConfiguration().config())
            realm.executeTransaction {
                try {
                    var rlmObj= realm.where(PlayerDetailsRealmObject::class.java)
                        .equalTo("name",playerDetails.name)
                        .findFirst()
                    if (rlmObj==null){
                        rlmObj= realm.createObject(PlayerDetailsRealmObject::class.java,playerDetails.name)
                    }
                    realm.insertOrUpdate(rlmObj)
                }catch (e:Exception){ printIfDebug("savePlayer",e.message)}
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