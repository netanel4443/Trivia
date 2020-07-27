package com.e.trivia.utils

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.e.trivia.R
import com.e.trivia.ui.fragments.FragmentsTag

//Animations
const val slideUp= R.anim.slide_up_dialog
const val slideDown= R.anim.slide_down_dialog

inline fun FragmentManager.transaction(func: FragmentTransaction.() -> FragmentTransaction) {
    beginTransaction().func().commit()
}

/**
 * cheks if fragment's backStack is empty or not.
 * if empty, then, return null
 * else return fragment's name as supplied to
 * [androidx.fragment.app.FragmentTransaction#addToBackStack(String)]
 * @return fragment's tag or null if no fragments in backStack
 */
fun AppCompatActivity.handleBackPressWhenFragmentAttached():String?{
    val fragmentManager=supportFragmentManager
    val backStackCount=fragmentManager.backStackEntryCount
    if (backStackCount!=0){
     return fragmentManager.getBackStackEntryAt(fragmentManager.backStackEntryCount-1 ).name
    }
    return null
}

fun FragmentActivity.removeFragment(tag: String){
    val fragmentt =supportFragmentManager.findFragmentByTag(tag)
    supportFragmentManager.beginTransaction().remove(fragmentt!!).commit()
    supportFragmentManager.popBackStack()
}

fun AppCompatActivity.addFragment(fragment: Fragment, container: Int,tag:String) {
    val currentFragment=supportFragmentManager.findFragmentByTag(tag)
    if (currentFragment == null) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(slideUp, slideUp, slideDown, slideDown)
            .add(container, fragment, tag)
            .addToBackStack(tag)
            .commit()
        }
    }

fun FragmentActivity.addFragment(fragment: Fragment, container: Int,tag:String){
    val currentFragment=supportFragmentManager.findFragmentByTag(tag)
    if (currentFragment == null) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(slideUp, slideUp, slideDown, slideDown)
            .add(container, fragment, tag)
            .addToBackStack(tag)
            .commit()
    }
}

