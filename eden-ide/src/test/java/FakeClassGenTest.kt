import com.zsu.eden.dsl.Eden
import org.junit.Test

class FakeClassGenTest {
    @Test
    fun testGen() {
        val fakeClass = Eden.fakeClass("Test", "com.fake.test") {
            imports("java.util.ArrayList", "java.util.HashMap")
            isPublic = false
            typeParam("A")
            typeParam("B", "String")
            typeParam("C", "Integer")
            extends("HashMap<B,C>")
            implements("ArrayList<A>")
            field("f", "ArrayList<B>", isPublic = false, isStatic = false)
            field("fs", "int", isPublic = true, isStatic = true)
            method("m") {
                isStatic = true
                param("p0" to "int", "p1" to "String")
            }
            method("m") {
                returnType = "ArrayList<D>"
                isPublic = false
                typeParam("D", "String")
            }
            clazz("I") {
                method("iA")
                isPublic = false
            }
            clazz("J") {
                isStatic = true
            }
        }
        assert(
            fakeClass.toString() == """
           import java.util.ArrayList;
           import java.util.HashMap;
           private class Test<A, B extends String, C extends Integer> extends HashMap<B,C> implements ArrayList<A> {
           private ArrayList<B> f;
           public static int fs;
           public static  void m(int p0, String p1){ throw new Exception(); }
           private <D extends String> ArrayList<D> m(){ throw new Exception(); }
           private class I {
           public  void iA(){ throw new Exception(); }

           }
           public static class J {

           }
           }
       """.trimIndent()
        )
    }

}