# Eden

再也不需要 kapt/ksp 的编译浪费时间！实时扫描带注解的类、方法、属性，并生成我们想生成的类，同时给予 IDE 实时提示！

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

## 协议与参考

此项目使用到了如下开源项目：

- JetBrains IDEA
- JetBrains Kotlin
- Android IDEA Plugin