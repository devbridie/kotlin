// WITH_RUNTIME

fun foo() {
    var array = arrayOf(1,2,3)
    for  (i in 0.<caret>rangeTo(array.size-1)) {

    }
}