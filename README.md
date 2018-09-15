# PatternLockView
九宫格手势图案锁第一版本

![](https://github.com/yuliangC/PatternLockView/blob/master/screenshots/qalxy-gg56q.gif)



How to
To get a Git project into your build:

Step 1. Add the JitPack repository to your build file

gradle
maven
sbt
leiningen
Add it in your root build.gradle at the end of repositories:

	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
Step 2. Add the dependency

	dependencies {
	        implementation 'com.github.yuliangC:SwitchButton:1.0.0'
	}
