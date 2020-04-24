package com.baker.synthesizer;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.databaker.synthesizer.BakerConstants;
import com.databaker.synthesizer.BakerMediaCallback;
import com.databaker.synthesizer.BakerSynthesizer;

public class MediaPlayerActivity extends AppCompatActivity {
    private final String clientId = "Your clientId";
    private final String clientSecret = "Your clientSecret";
    private BakerSynthesizer bakerSynthesizer;
    private EditText editText;
    private TextView resultTv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_media_player);

        editText = findViewById(R.id.edit_content);
        resultTv = findViewById(R.id.tv);
        resultTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        //初始化sdk
        bakerSynthesizer = new BakerSynthesizer(clientId, clientSecret);
//        bakerSynthesizer.setDebug(MediaPlayerActivity.this, true);
//        bakerSynthesizer = new BakerSynthesizer(MediaPlayerActivity.this, clientId, clientSecret, 10);

    }

    BakerMediaCallback bakerMediaCallback = new BakerMediaCallback() {

        @Override
        public void onPrepared() {
            appendResult("\n合成准备就绪");
            if (bakerSynthesizer != null) {
                bakerSynthesizer.bakerPlay();
            }
        }

        @Override
        public void onCacheAvailable(int percentsAvailable) {
            appendResult("\n缓存进度：" + percentsAvailable + "%");
        }

        @Override
        public void onCompletion() {
            appendResult("\n播放结束");
        }

        @Override
        public void onError(int errorCode, String errorMsg, String traceId) {
            Log.d("baker", "--onError-- errorCode=" + errorCode + ", errorMsg=" + errorMsg);
        }

        @Override
        public void playing() {
            appendResult("\n播放啦");
        }

        @Override
        public void noPlay() {
            appendResult("\n没有播放啦");
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
        bakerSynthesizer.setBakerCallback(bakerMediaCallback);
        /**********************以下是选填参数**************************/
        //设置发音人声音名称，默认：标准合成_模仿儿童_果子
        bakerSynthesizer.setVoice(BakerConstants.VOICE_NORMAL);
        //合成请求文本的语言，目前支持ZH(中文和中英混)和ENG(纯英文，中文部分不会合成)、CAT(粤语),默认：ZH
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

    public void start(View view) {
        //开始合成
        setParams();
        bakerSynthesizer.start();
    }

    public void stop(View view) {
        if (bakerSynthesizer != null) {
            bakerSynthesizer.bakerStop();
            appendResult("\n停止播放");
        }
    }

    public void pauseOrPlay(View view) {
        if (bakerSynthesizer != null) {
            boolean isPlaying = bakerSynthesizer.isPlaying();
            if (isPlaying) {
                bakerSynthesizer.bakerPause();
                appendResult("\n暂停");
            } else {
                bakerSynthesizer.bakerPlay();
                appendResult("\n播放");
            }
        }
    }

    public void isPlay(View view) {
        if (bakerSynthesizer != null) {
            boolean isPlaying = bakerSynthesizer.isPlaying();
            appendResult("\n当前播放状态：" + isPlaying);
        }
    }

    public void playDuration(View view) {
        if (bakerSynthesizer != null) {
            int currentPosition = bakerSynthesizer.getCurrentPosition();
            appendResult("\n当前播放至：" + currentPosition + "秒");
        }
    }

    public void duration(View view) {
        if (bakerSynthesizer != null) {
            int duration = bakerSynthesizer.getDuration();
            appendResult("\n音频总长度：" + duration + "秒");
        }
    }

    private void appendResult(final String str) {
        resultTv.post(new Runnable() {
            @Override
            public void run() {
                resultTv.append(str);
                int scrollAmount = resultTv.getLayout().getLineTop(resultTv.getLineCount())
                        - resultTv.getHeight();
                if (scrollAmount > 0)
                    resultTv.scrollTo(0, scrollAmount);
                else
                    resultTv.scrollTo(0, 0);
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (bakerSynthesizer != null) {
            bakerSynthesizer.onDestroy();
        }
        super.onDestroy();
    }
}
