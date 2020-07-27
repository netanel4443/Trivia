package com.e.trivia.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.TextView
import com.e.trivia.R
import com.e.trivia.data.PlayerDetails
import kotlinx.android.synthetic.main.game_over_dialog.view.*


class GameOverDialog {

    fun show(context: Context, playerDetails:PlayerDetails, gameScore:Int, returnToMainScreen:()->Unit){
        val alertDialog=AlertDialog.Builder(context)
        val inflater=LayoutInflater.from(context)
        val view=inflater.inflate(R.layout.game_over_dialog,null)
        val okBtn=view.okBtnGameOverDialog
        val gameDetailsLayout=view.gameDetailsLayout
        val playerNameTview=view.playerNameGameOverdialog
        val bestScoreTview=view.playerBestScoreGameOverdialog
        val gameScoreTview=view.gameScoreGameOverdialog

        alertDialog.setView(view)

        determineTextsColorAccordingToScore(playerDetails,gameScore,gameScoreTview)

        playerNameTview.text= playerDetails.name
        gameScoreTview.text=spanText("Score: ",gameScore.toString())
        bestScoreTview.text=spanText("Best score: ",playerDetails.highestScore.toString())

        val alert=alertDialog.create()

        okBtn.setOnClickListener {
            alert.dismiss()
        }
        
        alert.setOnDismissListener { returnToMainScreen() }

        alert.show()
    }

    private fun determineTextsColorAccordingToScore(playerDetails: PlayerDetails,gameScore:Int,gameScoreTview:TextView){
       val color= if  (playerDetails.highestScore>gameScore) { Color.RED } else { Color.GREEN }

        gameScoreTview.animate().
                scaleY(1.5f)
                    .scaleX(1.5f)
                        .setDuration(2000)
            .withEndAction { gameScoreTview.setTextColor(color) }

    }

    private fun spanText(textToPaint: String,additionalText:String):String{
        val stringBuilder=StringBuilder()
        stringBuilder.append(textToPaint).append(additionalText)
//        val wordtoSpan = SpannableString(stringBuilder.toString())
//        wordtoSpan.setSpan(
//            ForegroundColorSpan(Color.WHITE),
//            0,
//            textToPaint.length,
//            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
//        )
        return stringBuilder.toString()
    }
}