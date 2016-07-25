package extensionFunctionInObject

object ExtensionFunctionInObjectKt {
    fun String.foo() = 1

    @JvmStatic
    fun main(args: Array<String>) {
        //Breakpoint!
        2
    }
}

// EXPRESSION: "".foo()
// RESULT: 1: Z
