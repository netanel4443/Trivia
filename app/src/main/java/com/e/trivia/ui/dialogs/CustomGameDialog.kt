package com.e.trivia.ui.dialogs

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.e.trivia.R
import kotlinx.android.synthetic.main.custom_game_dialog.view.*

class CustomGameDialog(private val context: Context) {

    private var alert:AlertDialog?=null
    private val alertDialog:AlertDialog.Builder = AlertDialog.Builder(context)
    private val inflater:LayoutInflater = LayoutInflater.from(context)
    private val view:View=inflater.inflate(R.layout.custom_game_dialog,null)
    private val commentTview=view.comment_txtview_custom_game_dialog

    private fun createDialog(context: Context,numberOfQuestion:String,startGameAction:(Int?)->Unit){
        val numberOfQuestionsEditTxt=view.number_of_questions_editTxt_game_dialog
        val startGameBtn=view.start_btn_custom_game_dialog
        val numberOfQuestionsTxtView=view.number_of_question_txtview_custom_game_dialog
        alertDialog.setView(view)
        alert=alertDialog.create()

        numberOfQuestionsTxtView.text="number of questions allowed: $numberOfQuestion"

        startGameBtn.setOnClickListener {//todo handle numbers > int max
         startGameAction(numberOfQuestionsEditTxt.text.toString().toIntOrNull())
        }

        alert!!.show()
    }

    fun show(context: Context,numberOfQuestion:String,startGameAction:(Int?)->Unit){
        alert?.also {
            if (!it.isShowing){ it.show() }
        } ?:(createDialog(context,numberOfQuestion,startGameAction))
    }

    fun dismiss(){
        alert?.dismiss()
    }

    fun setCommentToUser(comment:String){
        commentTview.text=comment
    }
}