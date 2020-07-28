package com.e.trivia.utils.collection


 fun  <T>ArrayList<T>.take(n: Int): ArrayList<T> {
     require(n >= 0) { "Requested element count $n is less than zero." }
     if (n == 0) return ArrayList()
     var count = 0
     val list = ArrayList<T>(n)
     for (item in this) {
         list.add(item)
         if (++count == n)
             break
     }
     return list
 }