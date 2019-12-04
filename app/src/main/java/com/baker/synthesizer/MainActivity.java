package com.baker.synthesizer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.databaker.synthesizer.BakerCallback;
import com.databaker.synthesizer.BakerConstants;
import com.databaker.synthesizer.BakerSynthesizer;

public class MainActivity extends AppCompatActivity {
    private final String clientId = "your clientId";
    private final String clientSecret = "your clientSecret";
    private BakerSynthesizer bakerSynthesizer;
    private static AudioPlayer audioPlayer;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = findViewById(R.id.edit_content);

        //初始化sdk
        bakerSynthesizer = new BakerSynthesizer(clientId, clientSecret);
        audioPlayer = new AudioPlayer();
    }

    BakerCallback bakerCallback = new BakerCallback() {
        /**
         * 开始合成
         */
        @Override
        public void onSynthesisStarted() {
            Log.d("baker", "--onSynthesisStarted");
        }

        /**
         * 合成完成。
         * 当onBinaryReceived方法中endFlag参数=true，即最后一条消息返回后，会回调此方法。
         */
        @Override
        public void onSynthesisCompleted() {
            Log.d("baker", "--onSynthesisCompleted");
        }

        /**
         * 流式持续返回数据的接口回调
         *
         * @param idx  数据块序列号，请求内容会以流式的数据块方式返回给客户端。服务器端生成，从1递增
         * @param data 合成的音频数据
         * @param audioType  音频类型，如audio/pcm
         * @param interval  音频interval信息，
         * @param endFlag  是否时最后一个数据块，false：否，true：是
         */
        @Override
        public void onBinaryReceived(int idx, byte[] data, String audioType, String interval, boolean endFlag) {
            Log.d("baker", "onBinaryReceived idx = " + idx + ", data.length=" + data.length);
            if (idx == 1)
                audioPlayer.setAudioData(data, true);
            else
                audioPlayer.setAudioData(data, false);
        }

        /**
         * 合成失败
         * @param errorMsg 返回msg内容格式为：{"code":40000,"message":"…","trace_id":" 1572234229176271"}
         */
        @Override
        public void onTaskFailed(int errorCode, String errorMsg, String traceId) {
            Log.d("baker", "errorCode==" + errorCode + ",errorMsg==" + errorMsg + ",traceId==" + traceId);
        }
    };

    /**
     * 设置相关参数
     */
    private void setParams() {
        if (bakerSynthesizer == null) {
            return;
        }
        /**********************以下是必填参数**************************/
        //设置要转为语音的合成文本
        bakerSynthesizer.setText(editText.getText().toString().trim());
        //设置返回数据的callback
        bakerSynthesizer.setBakerCallback(bakerCallback);
        /**********************以下是选填参数**************************/
        //设置发音人声音名称，默认：标准合成_模仿儿童_果子
        bakerSynthesizer.setVoice(BakerConstants.VOICE_NORMAL);
        //合成请求文本的语言，目前支持ZH(中文和中英混)和ENG(纯英文，中文部分不会合成),默认：ZH
        bakerSynthesizer.setLanguage(BakerConstants.LANGUAGE_ZH);
        //设置播放的语速，在0～9之间（支持浮点值），不传时默认为5
        bakerSynthesizer.setSpeed(5.0f);
        //设置语音的音量，在0～9之间（只支持整型值），不传时默认值为5
        bakerSynthesizer.setVolume(5);
        //设置语音的音调，取值0-9，不传时默认为5中语调
        bakerSynthesizer.setPitch(5);
        /**
         * 可不填，不填时默认为4, 16K采样率的pcm格式
         * audiotype=4 ：返回16K采样率的pcm格式
         * audiotype=5 ：返回8K采样率的pcm格式
         * audiotype=6 ：返回16K采样率的wav格式
         * audiotype=6&rate=1 ：返回8K的wav格式
         */
        bakerSynthesizer.setAudioType(BakerConstants.AUDIO_TYPE_PCM_16K);
    }

    public void startSynthesizer(View view) {
        setParams();
        //开始合成，合成结束后会自动stop
        bakerSynthesizer.start();
    }

    public void stopSynthesizer(View view) {
        audioPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        audioPlayer.stop();
        if (bakerSynthesizer != null) {
            bakerSynthesizer.stop();
        }
        super.onDestroy();
    }
}
