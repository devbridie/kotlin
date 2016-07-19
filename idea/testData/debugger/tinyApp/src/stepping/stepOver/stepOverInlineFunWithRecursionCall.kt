package stepOverInlineFunWithRecursionCall

fun foo(v: Int): Int {
    if (v == 2) {
        //Breakpoint!
        inlineCall { foo(1) }
    }

    return 2 // This line should be visited once in normal step over
}


fun main(args: Array<String>) {
    foo(2)
}

inline fun inlineCall(l: () -> Unit) {
    l()
}

// STEP_OVER: 3