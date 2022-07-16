import com.zsu.eden.dsl.Eden
import com.zsu.eden.dsl.java
import junit.framework.TestCase
import org.junit.Test

class FakeClassGenTest : TestCase() {
    @Test
    fun testGen() {
        val fakeClass = Eden.fakeClass("Test") {
//            imports("java.util.ArrayList", "java.util.HashMap")
            isPublic = false
            typeParam("A")
            typeParam("B", "String")
            typeParam("C", "Integer")
            extends("HashMap<B,C>")
            implements("ArrayList<A>")
//            field("f", "ArrayList<B>", isPublic = false, isStatic = false)
//            field("fs", "int", isPublic = true, isStatic = true)
            constructor {
                property("c1", "String")
                property("c2", "int", isField = true)
                property("c3", "long", isField = true, isFinalField = false)
                property("c4", "char", isField = true, isFinalField = false, isPublicField = false)
            }
            constructor {
                isPublic = false
                property("c2", "int", isField = true)
                property("c5", "int", isField = true)
            }
            method("m") {
                isStatic = true
//                param("p0" to "int", "p1" to "String")
            }
            method("m") {
                returnType("ArrayList<D>")
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
        assertEquals(
            java(
                """
            import java.util.ArrayList;
            import java.util.HashMap;
            private class Test<A, B extends String, C extends Integer> extends HashMap<B,C> implements ArrayList<A> {
            final private ArrayList<B> f;
            final public static int fs;
            final public int c2;
            public long c3;
            private char c4;
            final public int c5;
            public Test(String c1, int c2, long c3, char c4){ throw new Exception(); }
            private Test(int c2, int c5){ throw new Exception(); }
            public static  void m(int p0, String p1){ throw new Exception(); }
            private <D extends String> ArrayList<D> m(){ throw new Exception(); }
            private class I {
            public  void iA(){ throw new Exception(); }
            
            }
            public static class J {
            
            }
            }""".trimIndent()
            ), fakeClass.toString()
        )
    }

}
