// WITH_RUNTIME
// PROBLEM: none

fun foo() {
    var array = arrayOf(1,2,3)
    for  (i in 1 until <caret>array.size-1) {

    }
}