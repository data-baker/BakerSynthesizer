# 标贝合成安卓SDK & demo 
# 1.Android Studio集成lib（参考demo）
## 1.1将jar包添加至工程主module下，lib文件夹里。
![在这里插入图片描述](https://img-blog.csdnimg.cn/20191204173225864.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L2hzaHVhaWp1bjU1,size_16,color_FFFFFF,t_70)
## 1.2在主module的build.gradle文件里，添加以下代码。
```java
dependencies {
    implementation 'com.squareup.okhttp3:okhttp:4.2.2'
    implementation "io.reactivex.rxjava2:rxjava:2.2.0"
    implementation "io.reactivex.rxjava2:rxandroid:2.1.0"
    implementation 'com.squareup.retrofit2:retrofit:2.6.2'
    implementation 'com.squareup.retrofit2:adapter-rxjava2:2.6.2'
    implementation 'com.squareup.retrofit2:converter-gson:2.6.2'
}
```
## 1.3在主Module的AndroidManifest.xml文件中添加网络权限。
```java
<uses-permission android:name="android.permission.INTERNET" />
```
## 1.4在主Module的AndroidManifest.xml文件中的application节点添加以下属性。
```java
android:usesCleartextTraffic="true"
```
**Eclipse环境也遵循相关集成jar包的方式即可。**

# 2.SDK关键类
1. BakerSynthesizer：语音合成关键业务处理类，全局只需一个实例即可。
2. BakerCallback：合成结果回调类。在获得合成音频数据，发生错误等事件发生时会触发回调。您应当实现该类，在回调方法中加入自己的处理逻辑。
3. BakerConstants：参数等常量类。

# 3.调用顺序
1. 初始化BakerSynthesizer类，得到BakerSynthesizer的实例。
2. 定义BakerCallback实现类。
3. 设置BakerSynthesizer合成参数，包括必填参数和非必填参数。
4. 调用BakerSynthesizer.start()方法开始与云端服务连接
5. 在BakerCallback回调中获得合成的音频数据并按您自己的业务需要处理合成结果或错误情况。
6. 如果需要发起新的请求，可以重复第3-5步。
7. 在业务完全处理完毕，或者页面关闭时，调用bakerSynthesizer.stop();结束websocket服务，释放资源。

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
setEnableTimestamp | 	是否返回时间戳内容 | 	否 | 	设置是否返回时间戳内容。true=支持返回，false=不需要返回。不设置默认为false不返回。
**注意：如果调整了参数中的采样率或码率，记得注意(Demo中示例的)播放器的采样率也要同步调整。**

## 4.2 BakerCallback 回调类方法说明
参数	     |    参数名称   | 说明
------ | --------------- | --------- 
onSynthesisStarted | 	开始合成 | 	开始合成
onBinaryReceived | 	流式持续返回数据的接口回调 | 	idx  数据块序列号，请求内容会以流式的数据块方式返回给客户端。服务器端生成，从1递增。data 合成的音频数据，已使用base64加密，客户端需进行base64解密。audioType  音频类型，如audio/pcm。interval  音频interval信息，可能为空。endFlag  是否时最后一个数据块，false：否，true：是。
onSynthesisCompleted | 	合成完成 | 	当onBinaryReceived方法中endFlag参数=true，即最后一条消息返回后，会回调此方法。
onTaskFailed | 	合成失败	 | 返回msg内容格式为：{"code":40000,"message":"…","trace_id":" 1572234229176271"} trace_id是引擎内部合成任务ID。
## 4.3失败时返回的code对应表
### 4.3.1失败时返回的msg格式
参数名称	     |    类型   | 描述
------ | --------------- | --------- 
code  | 	int  | 	错误码9xxxx表示SDK相关错误，1xxxx参数相关错误，2xxxx合成引擎相关错误，3xxxx授权及其他错误
message  | 	string	  | 错误描述
trace_id  | 	string  | 	引擎内部合成任务id
### 4.3.2 对应code值

错误码 | 	含义
------ | ---------------
90001 | 	合成SDK初始化失败
90002	 | 合成文本内容为空
90003 | 	参数格式错误
90004 | 	返回结果解析错误
90005 | 	合成失败，失败信息相关错误。
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

# 附：万次合成测试情况
1. 总合成次数：10052次
2. 合成失败：12次
3. 合成失败占比：0.119%
4. 平均首包返回时长：1093.65毫秒
5. 合成参数：
{"access_token":"1d36b097-2f77-49f8-b1e0-a629b9336e8e","tts_params":{"volume":"5","rate":"2","audiotype":"4","domain":"1","voice_name":"标准合成_模仿儿童_果子","language":"ZH","text":"200字合成文本编码后内容省略......","pitch":"5","speed":"5.0"},"version":"1.0"}

6. 测试日志详见：logInfo.txt
7. Demo测试内存及CPU消耗情况见图：内存及CPU消耗情况.png

8. 12次合成失败错误日志信息：
1)发生错误：errorCode=50002,errorMsg=Internal rpc failed,traceId=1575366856831514
2)发生错误：errorCode=50002,errorMsg=Internal rpc failed,traceId=1575360213440525
3)发生错误：errorCode=50002,errorMsg=Internal rpc failed,traceId=1575349249281115
4)发生错误：errorCode=50002,errorMsg=Internal rpc failed,traceId=1575354250343803
5)发生错误：errorCode=50002,errorMsg=Internal rpc failed,traceId=1575355649964829
6)发生错误：errorCode=90005,errorMsg=合成失败：Read error: ssl=0x79ca541308: Failure in SSL library, usually a protocol error error:100000d7:SSL routines:OPENSSL_internal:SSL_HANDSHAKE_FAILURE (external/boringssl/src/ssl/ssl_lib.cc:988 0x79e25bce07:0x00000000)  
7)发生错误：errorCode=90005,errorMsg=合成失败：Read error: ssl=0x79ca541308: Failure in SSL library, usually a protocol error error:1000042e:SSL routines:OPENSSL_internal:TLSV1_ALERT_PROTOCOL_VERSION (external/boringssl/src/ssl/tls_record.cc:592 0x79b3701548:0x00000001)
8)发生错误：errorCode=90005,errorMsg=合成失败：Read error: ssl=0x79e584cf88: I/O error during system call, Bad file descriptor
9)发生错误：errorCode=90005,errorMsg=合成失败：Read error: ssl=0x79ca541308: I/O error during system call, Bad file descriptor
10)发生错误：errorCode=90005,errorMsg=合成失败：Connection closed by peer
11)发生错误：errorCode=90005,errorMsg=合成失败：null
12)发生错误：errorCode=90005,errorMsg=合成失败：null

	错误原因分析：前5次是因为server端内部BUG错误，此BUG现在已修复。错误码90005的意思是sdk在发起网络请求时，因各类网络原因导致错误。此时还没有进入到引擎合成业务环节。BUG 6-10，大意是websocket协议在握手时，ssl中某环节出错，比如不合法文件描述符等。与整个网络中网络交换机、移动设备等都有原因。11-12错误原因暂无法追踪。
