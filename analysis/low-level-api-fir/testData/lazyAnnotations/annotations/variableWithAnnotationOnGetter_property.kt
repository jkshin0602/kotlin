// QUERY: annotations
package pack

@get:MyAnno("str")
var var<caret>iable: Int = 0
    get() = 1

annotation class MyAnno(val s: String)