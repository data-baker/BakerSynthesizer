# 1.Android Studio集成lib（参考demo）
## 1.1集成aar包有多种方式，本文提供一种作为参考，采用其他正确集成方式都可行。将aar包拷贝至libs目录下，并执行下一步（1.2）。注意新增*.aar格式。

## 1.2在主module的build.gradle文件里，添加以下代码。
```java
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
    implementation 'com.squareup.okhttp3:okhttp:4.2.2'
    implementation 'com.google.code.gson:gson:2.8.6'
}
```
## 1.3在主Module的AndroidManifest.xml文件中添加网络权限。若使用离线合成SDK，在安卓6.0系统及以上版本需要申请写SDK权限。
```java
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```
## 1.4在主Module的AndroidManifest.xml文件中的application节点添加以下属性。
```java
android:usesCleartextTraffic="true"
```
**Eclipse环境也遵循相关集成jar包的方式即可。**

## 1.5 关于混淆
SDK中用到了okhttp和gson，所以需要将这两个包的混淆代码添加上。具体混淆代码可以去官方文档上查阅。如果项目中已经有这两个包的混淆代码，不必重复添加。请加上我们SDK其他类的混淆代码，如下：
```java
-keep class com.databaker.synthesizer.bean.** { *; } 
-keep public class com.databaker.synthesizer.BakerConstants{*;}
-keep public class com.databaker.synthesizer.BakerSynthesizer{*;}
-keep public class com.databaker.synthesizer.BakerCallback{*;}
-keep public class com.databaker.synthesizer.SynthesizerCallback{*;}
-keep public class com.databaker.synthesizer.BakerMediaCallback{*;}
-keep public class com.databaker.synthesizer.BaseMediaCallback{*;}
-keep public class com.databaker.synthesizer.offline.OfflineBakerSynthesizer{*;}
-keep public class com.databaker.synthesizer.offline.OffLineSynthesizerEngine{*;}
-keep public class com.databaker.synthesizer.authorization.AuthorizationCallback{*;}
```

# 2.SDK关键类
1. BakerSynthesizer：语音合成关键业务处理类，全局只需一个实例即可。
2. OfflineBakerSynthesizer: 离线合成关键业务处理类，全局只需要一个实例即可。
3. BakerCallback：合成结果源数据回调类。在获得合成音频源数据，或发生错误等情况发生时会触发此回调。如果您的应用场景中需要直接处理返回的字节类型源数据，您可以实现该类，并在回调方法中加入自己的处理逻辑。设置参数时请将此callback提交给BakerSynthesizer实例。
4. BakerMediaCallback：如果想直接使用SDK中的播放器来处理文本合成播放任务。您可以实现该类，此回调类中包含了播放器的各种状态回调，您可以在这些回调方法中实现自己的其他业务逻辑。设置参数时请将此callback提交给BakerSynthesizer实例。
5. BakerConstants：参数等常量类。

# 3.调用说明
1. 初始化BakerSynthesizer类，得到BakerSynthesizer的实例。
2. SDK中提供了2个回调类。如果想要自己处理合成返回的字节类型源数据，则可以定义BakerCallback实现类。如果想直接将合成文本数据交给SDK中的播放器处理，则可以定义BakerMediaCallback实现类。**如果选择了定义BakerCallback实现类，SDK中不会执行播放器等一整套业务代码，不用担心由此带来的各类附加资源开销。**
3. 设置BakerSynthesizer合成参数，包括必填参数和非必填参数。
4. 调用BakerSynthesizer.start()方法开始与云端服务连接
5. callback中的onPrepared()意义是合成的第一帧数据已取得。所以您可以在此回调方法中**调用bakerSynthesizer.bakerPlay()方法开启播放任务**。
6. 在callback其他回调方法中按照您的业务需求实现对应逻辑。
7. 如果需要发起新的请求，可以重复第3-6步。
8. 在业务完全处理完毕，或者页面关闭时，调用bakerSynthesizer.onDestroy();结束websocket服务，释放资源。 
9. 若使用离线合成功能，则初始化OfflineBakerSynthesizer实例即可，**其余调用方式皆不变**。

注意：若使用SDK中播放器执行合成音频播放任务，有以下方法可调用。  
+ bakerSynthesizer.bakerPlay() 播放音频，常在onPrepared()回调方法里调用此方法执行播放。
+ bakerSynthesizer.bakerPause() 暂停播放。
+ bakerSynthesizer.bakerStop() 停止播放。
+ bakerSynthesizer.isPlaying() 当前播放状态，boolean型，true=正在播放中，false=暂停或停止播放。
+ bakerSynthesizer.getCurrentPosition() 当前播放进度。
+ bakerSynthesizer.getDuration()文本合成音频的总长度。




# 4.参数说明
## 4.1基本参数说明
参数	     | 参数名称 | 是否必填 | 说明
------ | --------- | --------- | -----------------
setText	 | 合成文本	 | 是 | 	设置要转为语音的合成文本
setBakerCallback | 	数据回调方法 | 	是	 | 设置返回数据的callback
setVoice	 | 发音人	 | 否 | 	设置发音人声音名称，默认：标准合成_模仿儿童_果子
setLanguage	 | 合并文本语言类型 | 	否 | 	合成请求文本的语言，目前支持ZH(中文和中英混)和ENG(纯英文，中文部分不会合成),默认：ZH
setSpeed | 	语速 | 	否	 | 设置播放的语速，在0～9之间（支持浮点值），不传时默认为5
setVolume | 	音量	 | 否	 | 设置语音的音量，在0～9之间（只支持整型值），不传时默认值为5
setPitch | 	音调 | 	否 | 	设置语音的音调，取值0-9，不传时默认为5中语调
setAudioType | 	返回数据文件格式 | 	否	 | 可不填，不填时默认为4，audiotype=4 ：返回16K采样率的pcm格式，audiotype=5 ：返回8K采样率的pcm格式，audiotype=6 ：返回16K采样率的wav格式， audiotype=6&rate=1 ：返回8K的wav格式

**注意：如果调整了参数中的采样率或码率，记得注意(Demo中示例的)播放器的采样率也要同步调整。**

## 4.2 BakerCallback 回调类方法说明
参数	     |    参数名称     | 说明
------ | --------------- | --------- 
onSynthesisStarted | 	开始合成   | 	开始合成
onPrepared | 	准备就绪     | 	第一帧数据返回时的回调，此时可以使用数据执行播放。
onBinaryReceived | 	流式持续返回数据的接口回调   | 	data 合成的音频数据，已使用base64加密，客户端需进行base64解密。audioType  音频类型，如audio/pcm。interval  音频interval信息，可能为空。endFlag  是否时最后一个数据块，false：否，true：是。
onSynthesisCompleted |   	合成完成    | 	当onBinaryReceived方法中endFlag参数=true，即最后一条消息返回后，会回调此方法。
onTaskFailed |   	合成失败	 | 返回msg内容格式为：{"code":40000,"message":"…","trace_id":" 1572234229176271"} trace_id是引擎内部合成任务ID。

## 4.3 BakerMediaCallback回调类方法说明
参数	     |    参数名称   | 说明
------ | --------------- | --------- 
onPrepared | 	准备就绪 | 	第一帧数据返回时的回调，此时可以使用数据执行播放。
onCacheAvailable | 	数据缓存进度回调 | 	percentsAvailable 已缓存的百分比，整数型，取值范围0-100。
playing | 	开始播放回调 | 	播放状态切换：开始播放时的回调。
noPlay | 	暂停或停止播放回调	 | 播放状态切换：暂停或停止播放时的回调。
onCompletion | 播放结束	 | 当数据播放完成时的回调。
onError | 	各类失败时的回调	 | 返回msg内容格式为：{"code":40000,"message":"…","trace_id":" 1572234229176271"} trace_id是引擎内部合成任务ID。

## 4.4失败时返回的code对应表
### 4.4.1失败时返回的msg格式
参数名称	     |    类型   | 描述
------ | --------------- | --------- 
code  | 	int  | 	错误码9xxxx表示SDK相关错误，1xxxx参数相关错误，2xxxx合成引擎相关错误，3xxxx授权及其他错误
message  | 	string	  | 错误描述
trace_id  | 	string  | 	引擎内部合成任务id
### 4.4.2 对应code值

错误码 | 	含义
------ | ---------------
90001 | 	合成SDK初始化失败
90002	 | 合成文本内容为空
90003 | 	参数格式错误
90004 | 	返回结果解析错误
90005 | 	合成失败，失败信息相关错误。
90006 | 	SDK中播放器相关错误。
90007 | 	离线SDK初始化相关错误
90008 | 	离线SDK授权错误
90009 | 	传入文本过长
90010 | 	文本编码不支持
90011 | 	引擎内部错误（离线合成引擎错误码）
90012 | 	离线授权成功
90013 | 	离线授权未知错误
90014 | 	引擎正在合成
90015 | 	引擎没有初始化
90016 | 	内存不足
90017 | 	引擎参数错误
90018 | 	引擎运行时错误
90019 | 	模型文件错误
90020 | 	动态库链接错误
90021 | 	正在初始化
90022 | 	释放离线合成SDK错误
90023 | 	无SD卡读写权限错误
10001	 | access_token参数获取失败或未传输
10002 | 	domain参数值错误
10003	 | language参数错误
10004 | 	voice_name参数错误
10005 | 	audiotype参数错误
10006	 | rate参数错误
10007 | 	idx错误
10008	 | single错误
10009 | 	text参数错误
10010 | 	文本太长
20000	 | 获取资源错误
20001 | 	断句失败
20002 | 	分段数错误
20003 | 	分段后的文本长度错误
20004 | 	获取引擎链接错误
20005	 | RPC链接失败错误
20006 | 	引擎内部错误
20007 | 	操作redis错误
20008 | 	音频编码错误
30000 | 	鉴权错误（access_token值不正确或已经失效）
30001 | 	并发错误
30002 | 	内部配置错误
30003 | 	json串解析错误
30004 | 	获取url失败
30005 | 	获取客户IP地址失败
30006 | 	任务队列错误
40001 | 	请求不是json结构
40002 | 	缺少必须字段
40003 | 	版本错误
40004 | 	字段值类型错误
40005 | 	参数错误
50001 | 	处理超时
50002 | 	内部rpc调用失败
50004 | 	其他内部错误
