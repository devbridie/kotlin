interface I {
    fun foo()
    val someVal: Int
    var someVar: Int
}

class Base1 {
    protected open fun bar(){}
}

open class Base2 : Base1() {
}

class A : Base2(), I {
    o<caret>
}

// EXIST: { lookupString: "override", itemText: "override" }
// EXIST: { itemText: "override fun bar() {...}", lookupString: "override", allLookupStrings: "override, bar", tailText: null, typeText: "Base1", attributes: "" }
// EXIST: { itemText: "override operator fun equals(other: Any?): Boolean {...}", lookupString: "override", allLookupStrings: "override, equals", tailText: null, typeText: "Any", attributes: "" }
// EXIST: { itemText: "override fun foo() {...}", lookupString: "override", allLookupStrings: "override, foo", tailText: null, typeText: "I", attributes: "bold" }
// EXIST: { itemText: "override fun hashCode(): Int {...}", lookupString: "override", allLookupStrings: "override, hashCode", tailText: null, typeText: "Any", attributes: "" }
// EXIST: { itemText: "override val someVal: Int", lookupString: "override", allLookupStrings: "override, someVal", tailText: null, typeText: "I", attributes: "bold" }
// EXIST: { itemText: "override var someVar: Int", lookupString: "override", allLookupStrings: "override, someVar", tailText: null, typeText: "I", attributes: "bold" }
