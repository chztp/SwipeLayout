# SwipeLayout
# 可滑动布局

## 示意图

![image](https://github.com/chztp/SwipeLayout/screen/swipeLayout.gif)

## 快速集成 
### 混淆配置(proguard-rules)
+ -keep class com.ch.swipelayoutlib.SwipeLayout{*;}


### Android Studio
* 在你工程的根目录下的 **build.gradle**添加**jitpack**支持
   ```
   allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
   ```
* 在你的module的根目录下的**build.gradle**添加依赖
	```
	<!--这里的版本号，1.0.0 可以指定为任意release版本-->
	<!--如果希望一直使用最新版本可以替换 1.0.0 为 master-SNAPSHOT -->
	dependencies {
	        ...
	        compile 'com.github.chztp:SwipeLayout:1.0.0'
	}
