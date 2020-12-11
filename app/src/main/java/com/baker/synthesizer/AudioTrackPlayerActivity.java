package com.baker.synthesizer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.baker.sdk.basecomponent.BakerBaseConstants;
import com.baker.sdk.basecomponent.bean.BakerError;
import com.databaker.synthesizer.BakerCallback;
import com.databaker.synthesizer.BakerSynthesizer;

public class AudioTrackPlayerActivity extends AppCompatActivity {
    //私有化部署时，无clientId和secret，使用标贝公有云合成需要拿到这个授权信息
    private final String clientId = "Your clientId";
    private final String clientSecret = "Your clientSecret";
    private BakerSynthesizer bakerSynthesizer;
    private AudioTrackPlayer audioTrackPlayer;
    private EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_track_player);

        editText = findViewById(R.id.edit_content);

        //初始化sdk
        bakerSynthesizer = new BakerSynthesizer(AudioTrackPlayerActivity.this, clientId, clientSecret);
        //私有化部署时，初始化方式
//        bakerSynthesizer = new BakerSynthesizer();
    }

    BakerCallback bakerCallback = new BakerCallback() {
        /**
         * 开始合成
         */
        @Override
        public void onSynthesisStarted() {
        }

        /**
         * 合成完成。
         * 当onBinaryReceived方法中最后一条消息返回后，会回调此方法。
         */
        @Override
        public void onSynthesisCompleted() {
        }

        /**
         * 合成失败
         * @param bakerError 返回msg内容格式为：{"code":40000,"message":"…","trace_id":" 1572234229176271"}
         */
        @Override
        public void onTaskFailed(BakerError bakerError) {
            Log.d("baker", "errorCode==" + bakerError.getCode() + ",errorMsg==" + bakerError.getMessage() + ",traceId==" + bakerError.getTrace_id());
        }

        /**
         * 第一帧数据返回时的回调
         */
        @Override
        public void onPrepared() {
            //清除掉播放器之前的缓存数据
            audioTrackPlayer.cleanAudioData();
        }

        /**
         * 流式持续返回数据的接口回调
         *
         * @param data 合成的音频数据
         * @param audioType  音频类型，如audio/pcm
         * @param interval  音频interval信息，
         * @param endFlag  是否时最后一个数据块，false：否，true：是
         */
        @Override
        public void onBinaryReceived(byte[] data, String audioType, String interval, boolean endFlag) {
            audioTrackPlayer.setAudioData(data);
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
        //私有化部署时，必须设置token和url，使用标贝公有云合成不需要设置这2个参数
//        bakerSynthesizer.setTtsToken("default");
//        bakerSynthesizer.setUrl("wss://xxxxx");
        //设置要转为语音的合成文本
        bakerSynthesizer.setText(editText.getText().toString().trim());
        //设置返回数据的callback
        bakerSynthesizer.setBakerCallback(bakerCallback);
        /**********************以下是选填参数**************************/
        //设置发音人声音名称，默认：标准合成_模仿儿童_果子
        bakerSynthesizer.setVoice("特色合成_儿童声音_小恐龙");
        //合成请求文本的语言，目前支持ZH(中文和中英混)和ENG(纯英文，中文部分不会合成),默认：ZH
        bakerSynthesizer.setLanguage(BakerBaseConstants.LANGUAGE_ZH);
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
        bakerSynthesizer.setAudioType(BakerBaseConstants.AUDIO_TYPE_PCM_16K);
    }

    public void startSynthesizer(View view) {
//        //开始合成，合成结束后会自动stop
        audioTrackPlayer = new AudioTrackPlayer();
        setParams();
        bakerSynthesizer.start();
    }

    public void stopSynthesizer(View view) {
        audioTrackPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        audioTrackPlayer.stop();
        if (bakerSynthesizer != null) {
            bakerSynthesizer.onDestroy();
        }
        super.onDestroy();
    }
}
