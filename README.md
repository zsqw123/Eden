# Eden

再也不需要等待 kapt/ksp 生成类的编译浪费时间！实时扫描带注解的类、方法、属性，并生成我们想生成的类，同时给予 IDE 实时提示！

## Preview

![](https://cdn.jsdelivr.net/gh/zsqw123/cdn@master/picCDN/202206111257981.gif)
而实现这些，只需要编写非常简单的 DSL:

```kotlin
Eden.fakeClass("Fake${ktDeclaration.capitalizeAsciiOnly()}", ktDeclaration.packageName) {
    field("fakeFieldStatic", "int", isStatic = true)
    clazz("FakeInnerClassStatic") { isStatic = true }
    method("fakeMethod")
}
```

## Usage

### 1. 添加依赖

maven-central
release
version: [![Maven Central](https://img.shields.io/maven-central/v/io.github.zsqw123/eden-idea)](https://search.maven.org/artifact/io.github.zsqw123/eden-idea)

```groovy
repositories {
    mavenCentral()
}
dependencies {
    implementation("io.github.zsqw123:eden-idea:$version")
}
```

### 2. 实现注解处理

1. 先定义好要处理的注解
2. 处理被指定注解所注解 `KtDeclaration`
3. 生成供 IDE 提示的虚拟类

```kotlin
val fakeFqn = "com.fake.FakeClass" // the annotation full qualified name which need to process

@Service // Regist a IDEA light service
class FakeClassCache(project: Project) : EdenCache(project, fakeFqn) {
    // 需要在这里处理被指定注解所注解的 KtDeclaration, 然后生成相应的虚拟类
    override fun processAnnotation(annotations: Sequence<KtDeclaration>): Sequence<FakeClass> =
        annotations.mapNotNull { ktDeclaration ->
            val name = ktDeclaration.name ?: return@mapNotNull null
            val className = "Fake${name.capitalizeAsciiOnly()}"
            Eden.fakeClass(className, ktDeclaration.packageName) {
                method("fakeMethod") // 生成在这个类里面的虚拟方法
                field("fakeField", "String") // 生成在这个类里面的虚拟变量
                clazz("FakeInnerClass") // 虚拟内部类
            }
        }

    companion object {
        fun getInstance(project: Project) = project.getService(FakeClassCache::class.java)
    }
}
```

### 3. 实现缓存及实时提示

1. 实现在输入字符时实时提示
2. 避免在 IDE 解析时虚拟类飘红
3. 在注解元素变动的时候销毁之前生成的虚拟类缓存

```kotlin
// 输入字符时的实时提示框
class FakeShortNameCache(project: Project) :
    EdenClassNamesCache(FakeClassCache.getInstance(project), FakeTracker.getInstance(project))

// 这个是为了让类不爆红
class FakeClassFinder(project: Project) :
    EdenClassFinder(FakeClassCache.getInstance(project), FakeTracker.getInstance(project))

// 注解变动监听器
class FakeChangeListener(project: Project) :
    EdenAnnotatedChange(fakeFqn.substringAfterLast('.'), FakeTracker.getInstance(project))

@Service // 用于刷新缓存
class FakeTracker : EdenModificationTracker() {
    companion object {
        fun getInstance(project: Project) = project.getService(FakeTracker::class.java)
    }
}
```

并在 `plugin.xml` 中注册:

```xml

<idea-plugin>
    <extensions defaultExtensionNs="com.intellij">
        <psi.treeChangeListener implementation="com.zsu.eden.sample.FakeChangeListener"/>
        <java.shortNamesCache implementation="com.zsu.eden.sample.FakeShortNameCache"/>
        <java.elementFinder implementation="com.zsu.eden.sample.FakeClassFinder"/>
    </extensions>
</idea-plugin>
```

## Fake DSL

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
    }
    clazz("I") { // 内部类
        method("iA")
        isPublic = false
    }
    clazz("J") {
        isStatic = true // 静态内部类
    }
}
```

> 这些方法其实背后生成了对应的 java psi，暂时不支持声明类型是否可空，方法的结构体和变量的初始化体均被省略，因为生成过重的结构会加重 IDE 解析的负担

## 更多

- 具体的例子可以参考 sample 模块: [sample](/sample)
- 关于 `Fake DSL` 的使用，可以参照: [FakeClassGenTest](/eden-ide/src/test/java/FakeClassGenTest.kt)

## 协议与参考

此项目使用到了如下开源项目：

- JetBrains IDEA
- JetBrains Kotlin
- Android IDEA Plugin
