# StatefulLayout
方便Android开发者根据不同的状态切换布局的自定义布局，基于FrameLayout，可同时存在多个同状态的子布局，可方便的自定义默认状态的而已，可自定义切换动画。

# 当前最新版本号：[![](https://jitpack.io/v/com.gitee.numeron/stateful.svg)](https://jitpack.io/#com.gitee.numeron/stateful)

### StatefulLayout
* 通过设置`state`的值切换不同的布局，共有4种状态：Empty, Loading, Failure, Success：
```kotlin
try {
    statefulLayout.state = State.Loading
    val list = getDateList()
    adapter.submitList(list)
    statefulLayout.state = if(list.isEmpty) State.Empty else State.Success
} catch (throwable: Throwable) {
    statefulLayout.state = State.Failure
}
```
* 设置页面中不同状态下的布局：

```xml
<!--引用failure状态下的视图，需要在该视图的根布局中添加app:layout_state="failure"属性-->
<!--同理，也可以引用empty、loading状态的布局，注意添加app:layout_state属性并指定对应的值-->
<StatefulLayout
    android:id="@+id/statefulLayout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:stateful_failure_layout="@layout/state_failure_layout">
        
    <!--默认是success状态-->
    <RecyclerView
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <!--设置empty状态下的视图-->
    <ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent" 
        app:layout_state="empty" />
    
    <!--设置loading状态下的视图-->
    <!--可单独设置某个视图显示或隐藏的动画-->
    <ConstraintLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent" 
        app:stateful_animation_show="@anim/stateful_loading_show"
        app:stateful_animation_hide="@anim/stateful_loading_hide"
        app:layout_state="loading" />
    
</StatefulLayout>
```
*  如果想统一修改除success以外所有状态的默认视图，可在当前正在应用的主题资源中指定：
```xml
<!--默认视图-->
<item="stateful_empty_layout">@layout/state_empty_layout</item>
<item="stateful_loading_layout">@layout/state_loading_layout</item>
<item="stateful_failure_layout">@layout/state_failure_layout</item>
<!--包括默认动画效果-->
<item="stateful_animation_show">@layout/state_show_anim</item>
<item="stateful_animation_hide">@layout/state_hide_anim</item>
```

### 引入
1.  在你的android工程的根目录下的build.gradle文件中的适当的位置添加以下代码：
```groovy
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
2. 在你的android工程中对应的android模块的build.gradle文件中的适当位置添加以下代码：
```groovy
implementation 'cn.numeron:stateful.layout:latest_version'
```
