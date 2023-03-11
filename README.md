# Eden

No more wasted time waiting for kapt/ksp to generate classes for compilation!
Scan annotated elements and generating the classes we want to generate automatically, while
giving the IDE real-time hints!

再也不需要等待 kapt/ksp 生成类的编译浪费时间！使用 IDE 插件实时扫描带注解的类、方法、属性，并生成我们想生成的类，同时给予实时提示！

## Preview

![](https://cdn.jsdelivr.net/gh/zsqw123/cdn@master/picCDN/202206111257981.gif)
This is implemented by writing a very simple DSL:

```kotlin
Eden.fakeClass("Fake${ktDeclaration.capitalizeAsciiOnly()}", ktDeclaration.packageName) {
    field("fakeFieldStatic", "int", isStatic = true)
    clazz("FakeInnerClassStatic") { isStatic = true }
    method("fakeMethod")
}
```

## Usage

### 1. Add Dependencies

maven-central
release
version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.zsqw123/eden-idea)](https://central.sonatype.com/search?q=eden-idea)

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation("io.github.zsqw123:eden-idea:$version")
}
```

### 2. Implement Annotation Processor

1. Define the annotations to be processed
2. Handling the `KtDeclaration` which annotated by the specified annotation.
3. Generate virtual classes for IDE hints

```kotlin
val fakeFqn = "com.fake.FakeClass" // the annotation full qualified name which need to process

@Service // Regist a IDEA light service
class FakeClassCache(project: Project) : EdenCache(project, fakeFqn) {
    // The `KtDeclaration` annotated by the specified annotation needs to be processed here,
    // and the corresponding virtual class generated is returned.
    override fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass> =
        annotations.mapNotNull { ktDeclaration ->
            val name = ktDeclaration.name ?: return@mapNotNull null
            val className = "Fake${name.capitalizeAsciiOnly()}"
            Eden.fakeClass(className, ktDeclaration.packageName) {
                method("fakeMethod") // Generate virtual methods inside this class
                field("fakeField", "String") // Generate virtual variables inside this class
                clazz("FakeInnerClass") // virtual internal classes
            }
        }

    companion object {
        fun getInstance(project: Project) = project.getService(FakeClassCache::class.java)
    }
}
```

### 3. Implement Caching and Real-Time Hints

1. Real-time hints as characters are entered
2. Avoid virtual class report error when parsing in the IDE
3. Drop the previously generated virtual class cache when the annotated element changes

```kotlin
// Live alert box when entering characters
class FakeShortNameCache(project: Project) :
    EdenClassNamesCache(FakeClassCache.getInstance(project), FakeTracker.getInstance(project))

// Avoid virtual class report error when parsing in the IDE
class FakeClassFinder(project: Project) :
    EdenClassFinder(FakeClassCache.getInstance(project), FakeTracker.getInstance(project))

@Service // refreshing the cache by ModificationTracker
class FakeTracker : EdenModificationTracker() {
    companion object {
        fun getInstance(project: Project) = project.getService(FakeTracker::class.java)
    }
}
```

register it in `plugin.xml`, don't forget include `Eden-plugin.xml`!

```xml

<idea-plugin xmlns:xi="http://www.w3.org/2001/XInclude">
    <xi:include href="/META-INF/Eden-plugin.xml"/>
    <extensions defaultExtensionNs="com.intellij">
        <java.shortNamesCache implementation="com.zsu.eden.sample.FakeShortNameCache"/>
        <java.elementFinder implementation="com.zsu.eden.sample.FakeClassFinder"/>
    </extensions>
</idea-plugin>
```

## Fake DSL

In Eden, the process of generating a virtual class only requires `Eden.fakeClass` to generate a virtual class.
It supports the construction of virtual class structures using simple DSLs.

在 Eden 框架中，生成虚拟类的过程是简单的，只需要通过 `Eden.fakeClass` 即可生成一个虚拟类。它支持使用简单的 DSL 构建虚拟的类结构。

```kotlin
val fakeClass = Eden.fakeClass("Test", "com.fake.test") {
    imports("java.util.ArrayList", "java.util.HashMap") // import list
    isPublic = false // public / private
    typeParam("A") // 泛型参数
    typeParam("B", "String") // 再加一个泛型参数，但是继承了某个类型
    typeParam("C", "Integer")
    extends("HashMap<B,C>") // 继承某个类
    implements("ArrayList<A>") // 实现某个接口 (当然，这个 ArrayList 肯定不是接口 = =)
    field("f", "ArrayList<B>", isPublic = false, isStatic = false) // 增加一个变量
    field("fs", "int", isPublic = true, isStatic = true) // 增加一个静态变量
    method("m") { // 增加一个方法
        isStatic = true // 静态方法
        param("p0" to "int", "p1" to "String") // 方法参数
    }
    method("m") {
        returnType = "ArrayList<D>" // 返回值类型
        isPublic = false
        typeParam("D", "String")
        navigateTo = psiElement // used for idea navigation
    }
    clazz("I") { // 内部类
        method("iA")
        isPublic = false
    }
    clazz("J") {
        isStatic = true // 静态内部类
    }
    navigateTo = psiElement // used for idea navigation
}
```
> These methods actually generate the corresponding java psi behind the scenes, and for the time being do not support
> declaring the type nullable. The structure of the method and the initialization body of the variable are omitted, as
> generating an overly heavy structure would add to the parsing burden of the IDE
>
> 这些方法其实背后生成了对应的 java psi，暂时不支持声明类型是否可空，方法的结构体和变量的初始化体均被省略，因为生成过重的结构会加重 IDE 解析的负担 

## More

- For an example, see the `sample` module: [sample](/sample)
- About `Fake DSL` example: [FakeClassGenTest](/eden-ide/src/test/java/FakeClassGenTest.kt)

### Environment

| java | idea         |
|------|--------------|
| 11+  | 201.6858.69+ |

## License & References

[Apache License 2.0](./LICENCE)

This project uses the following open source projects：

- JetBrains IDEA
- JetBrains Kotlin
- Android IDEA Plugin
