# 简介 #
该SlidingMenuLayout主要实现了左边侧边栏、主界面和多个Fragment功能。该控件主要是参考酷狗界面的功能，左侧菜单可设置缩放动画，及打开多个Fragment功能。该控件在后期会整合到[乐乐音乐播放器项目](https://github.com/zhangliangming/HappyPlayer5)中去。

# 日志 #

## v1.1 ##
- 添加对外接口

## v1.0 ##
- 初始导入

# 设计思路图 #

![](https://i.imgur.com/atqEkCf.png)

![](https://i.imgur.com/9faCtfh.png)

![](https://i.imgur.com/r8D827t.png)


# 截图 #

![](https://i.imgur.com/uABcAiK.png)

![](https://i.imgur.com/QzmOk0P.png)

![](https://i.imgur.com/25hBtio.png)

![](https://i.imgur.com/BPdwxB2.png)


# Gradle #
1.root build.gradle

	`allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}`
	
2.app build.gradle

`dependencies {
	         compile 'com.github.zhangliangming:SlidingMenuLayout:v1.2'
	}`

# 混淆注意 #

-keep class com.zlm.libs.widget.** { *; }

# 调用Demo #

链接: [https://pan.baidu.com/s/1Z5nrsdmwiz3Gdmj-HvE2_A](https://pan.baidu.com/s/1Z5nrsdmwiz3Gdmj-HvE2_A) 密码: d72c

# 功能 #

- 处理与viewpager控件的冲突
- 多个Fragment功能

# 捐赠 #
如果该项目对您有所帮助，欢迎您的赞赏

- 微信

![](https://i.imgur.com/e3hERHh.png)

- 支付宝

![](https://i.imgur.com/29AcEPA.png)