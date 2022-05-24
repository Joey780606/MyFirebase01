package com.example.hardkotlincode

fun main() {
    val foo = try {
        throw Exception("try")  //會跑exception
    } catch(e: Exception) {
        "catch"
        //println("aa")
    } finally {
        "finally"
        //println("bb")
    }

    val bar = runCatching{
        throw Exception("runCatching")  //直接回應這個
    }.also{
        "also"
        //println("cc")
    }.onFailure {
        "onFailure"
        //println("dd")
    }

    println(foo)
    println("=====")
    println(bar)
}