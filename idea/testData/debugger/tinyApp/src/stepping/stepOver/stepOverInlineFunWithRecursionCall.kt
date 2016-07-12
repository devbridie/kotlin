package stepOverInlineFunWithRecursionCall

fun foo(v: Int): Int {
    if (v == 2) {
        //Breakpoint!
        inlineCall { foo(1) }
    }

    return 2
}


fun main(args: Array<String>) {
    foo(2)
}

inline fun inlineCall(l: () -> Unit) {
    l()
}
