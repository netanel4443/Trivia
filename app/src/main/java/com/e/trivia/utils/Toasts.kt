package com.e.trivia.utils

import android.content.Context
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Context.toast(message: String) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun  Context.toast(message: Int) =
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toastLong(message: String)=
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Context.toastLong(message: Int)=
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()

fun Fragment.toast(message: String) =
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

fun Fragment.toast(message: Int) =
    Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()


