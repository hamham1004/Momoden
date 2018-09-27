package foo.bar.momoden;

import java.text.ParseException;
import java.util.HashMap;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class CallActivity extends Activity {

	// ログ出力用タグ名
	private final String TAG = "CallActivity";

	// SIP通信用オブジェクト
	SipManager manager = null;
    SipProfile sp = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;
    SipAudioCall.Listener listener;

    // 自分の電話番号、MainActivityから渡される
    String thisDN;

    // 電話をかけている最中か否かのフラグ
    Boolean onCalling = false;
    
    // 終了処理が始まったか否かのフラグ
    Boolean onEnding = false;

    // 画面表示部品
    LinearLayout layout;
    TextView textView;
    TextView memberTextCaption;
    public TextView memberText;  // 相手の推しメン、GetProfileTaskから更新するからpublic
    LinearLayout innerLayout3;   // 残り時間、延長ボタンを入れる
    LinearLayout innerLayout4;   // 残り時間のタイトル、残り時間のTextViewを入れる
    TextView timerTextCaption;
    TextView timerText;
    Button extendButton;
    LinearLayout innerLayout;    // ミュート、スピーカーボタンを入れる
    Button muteButton;
//  Button holdButton;
    Button speakerButton;
    LinearLayout innerLayout2;   // ミュート、スピーカーボタンのタイトルを入れる
    SeekBar volumeControler;
    Button endcallButton;

    // 画面描画用ハンドラー
    public Handler handler;

    // 残り時間が後少しになった時に鳴らすアラーム
	public SoundPool soundPool;

	// 発信中、着信待ちのダイアログ
	ProgressDialog progressDialog;

	callEndTimerRunnable callEndTimerRunnable;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);

        // 画面上部のタイトルを無しにする
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // 画面がスリープ状態になるのを抑止する
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // 発信中、着信待ちのダイアログの初期化
        progressDialog = new ProgressDialog(this);

        // 最上位のLinearLayoutの初期設定
        initParentLayout();

        // メッセージ表示用のTextViewの初期設定
        initTextView();

        // 相手の推しメン表示用のTextViewの初期設定
        initMemberText();

        // 残り時間表示、延長ボタン用のレイアウトの初期設定
        initInnerLayout3();
        
        // 残り時間表示用のレイアウトの初期設定
        initInnerLayout4();

        // 残り時間表示用のTextViewの初期設定
        initTimerText();
        
        // 延長ボタンの初期設定
        initExtendButton();

        // ミュート、保留、スピーカーボタンの初期設定
        initMuteHoldSpeakerBlock();

        // ボリューム調整バーの初期設定
        initVolumeControler();

        // 電話を切るボタンの初期設定
        initEndCallButton();

        // イベントハンドラから、画面描画や次画面遷移を要求するためのハンドラーの初期化
        handler = new Handler();

        // 前画面からインテントで渡された自分の電話番号を取得する
        thisDN = getThisDN();
		Log.d(TAG, "dn=<" + thisDN + ">");

		// SoundPoolを初期化する
		initSoundPool();

        // SipManagerインスタンス生成
        manager = SipManager.newInstance(this);
        SipProfile.Builder builder;
        // Sipに対応している端末かをチェック
        if ( SipManager.isVoipSupported(this) ){

        	// SipProfileを作成
        	try {
        		// 発信中、着信待ちダイアログを表示する
        		showProgressDialog();

        		builder = new SipProfile.Builder(thisDN, AppConstants.SIPSERVER_IP);
        		builder.setPassword(thisDN + "pass");
        		sp = builder.build();

        		// SIPコール受信用インテントを作成
        		Intent i = new Intent();
        		i.setAction("android.SipDemo.INCOMING_CALL");
        		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);

        		// SIPサーバとの接続
        		manager.open(sp, pi, null);

        		// リスナーを設定
        		manager.setRegistrationListener(sp.getUriString(), new mySipRegistrationListener());

        	} catch (ParseException e) {
        		Log.e(TAG, "SipProfile.Builder error (ParseException)", e);
        	} catch (SipException e) {
        		Log.e(TAG, "manager.open error (SipException)", e);
        	}
        }else{
        	layout.addView(textView);
        	textView.setText("申し訳ありませんがこの端末ではご利用になれません。（Galaxy S2など）");
        	backToMainActivity();
        }
    }

	/**
	 * 最上位のLinearLayoutの初期設定
	 */
	@SuppressWarnings("deprecation")
	private void initParentLayout() {
		// ボタンを途中から表示する必要があるため、レイアウトはxmlを使わず、プログラムで設定する
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundDrawable(getResources().getDrawable(foo.bar.momoden.R.drawable.maple));
        setContentView(layout);
	}

	/**
	 * メッセージ表示用のTextViewの初期設定
	 */
	private void initTextView() {
		// メッセージ表示用のtextViewを作る
        textView = new TextView(this);
        LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams.height = getPixels(110);
        layoutParams.setMargins(0, 0, 0, 0);
        layoutParams.gravity = Gravity.CENTER;
        textView.setLayoutParams(layoutParams);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        textView.setBackgroundColor(Color.argb(127, 32, 32, 32));
        textView.setTextColor(Color.WHITE);
	}

	/**
	 * 相手の推しメン表示用のTextViewの初期設定
	 */
	private void initMemberText() {
		// 「相手は」という文字を出すためのtextViewを作る
        memberTextCaption = new TextView(this);
        LayoutParams layoutParams2 = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams2.setMargins(getPixels(30), 0, 0, 0);
        memberTextCaption.setLayoutParams(layoutParams2);
        memberTextCaption.setText("相手は");

        // 推しメン表示用のtextViewを作る
        memberText = new TextView(this);
        LayoutParams layoutParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, getPixels(53));
        layoutParams.setMargins(getPixels(30), 0, getPixels(30), getPixels(10));
        layoutParams.gravity = Gravity.CENTER;
        memberText.setLayoutParams(layoutParams);
        memberText.setTextColor(Color.BLACK);
        memberText.setGravity(Gravity.CENTER);
        memberText.setTextSize(30.0f);
	}

	/**
	 * 残り時間表示、延長ボタンを格納する子階層のLinearLayoutの初期設定
	 */
	private void initInnerLayout3() {
		// 残り時間表示、延長ボタンを入れるレイアウト
        innerLayout3 = new LinearLayout(this);
        LayoutParams innerlayoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayoutParams.gravity = Gravity.CENTER;
        innerlayoutParams.setMargins(0, 0, 0, 0);
        innerLayout3.setLayoutParams(innerlayoutParams);
        innerLayout3.setOrientation(LinearLayout.HORIZONTAL);
	}

	/**
	 * 残り時間表示、延長ボタンを格納する子階層のLinearLayoutの初期設定
	 */
	private void initInnerLayout4() {
		// 残り時間表示、延長ボタンを入れるレイアウト
        innerLayout4 = new LinearLayout(this);
        LayoutParams innerlayoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        innerlayoutParams.setMargins(0, 0, 0, 0);
        innerLayout4.setLayoutParams(innerlayoutParams);
        innerLayout4.setOrientation(LinearLayout.VERTICAL);
        innerLayout3.addView(innerLayout4);
	}

	/**
	 * 残り時間表示用のTextViewの初期設定
	 */
	private void initTimerText() {
		// 残り時間という文字を出すためのtextViewを作る
        timerTextCaption = new TextView(this);
        LayoutParams layoutParams8 = new LayoutParams(getPixels(180), LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams8.gravity = Gravity.CENTER;
        timerTextCaption.setLayoutParams(layoutParams8);
        timerTextCaption.setGravity(Gravity.CENTER);
        timerTextCaption.setText("残り時間");
        innerLayout4.addView(timerTextCaption);

        // タイマー表示用のtextViewを作る
        timerText = new TextView(this);
        LayoutParams layoutParams7 = new LayoutParams(getPixels(150), getPixels(53));
        layoutParams7.setMargins(0, 0, 0, 0);
        layoutParams7.gravity = Gravity.CENTER;
        timerText.setLayoutParams(layoutParams7);
        timerText.setTextAppearance(this, android.R.style.TextAppearance_Large);
        timerText.setBackgroundColor(Color.LTGRAY);
        timerText.setTextColor(Color.BLACK);
        timerText.setGravity(Gravity.CENTER);
        timerText.setTextSize(40.0f);
        innerLayout4.addView(timerText);
	}

	/**
	 * 通話時間を延長するボタンの初期設定
	 */
	private void initExtendButton() {
	    extendButton = new Button(this);
	    LayoutParams layoutParams2 = new LayoutParams(getPixels(100), getPixels(70));
	    layoutParams2.setMargins(0, getPixels(5), 0, 0);
	    layoutParams2.gravity = Gravity.CENTER;
	    extendButton.setLayoutParams(layoutParams2);
	    extendButton.setTextAppearance(this, android.R.style.TextAppearance_Large);
	    extendButton.setText("延長");
	    innerLayout3.addView(extendButton);
	    extendButton.setOnClickListener(new OnClickListener() {
	
			public void onClick(View arg0) {
		    	Log.i(TAG, "extendButton onClickListener start");
		    	Log.d(TAG, "restTime = " + callEndTimerRunnable.getRestTime());
		    	
		    	if (callEndTimerRunnable.getRestTime() < AppConstants.FIRST_ALARM) {
		    		callEndTimerRunnable.setRestTime(callEndTimerRunnable.getRestTime() + AppConstants.CALL_END_EXTEND_TIME);
		    		changeTimerTextBackgroundColor(Color.LTGRAY);
		    	}
			}
	
	    });
	}

	/**
	 * ミュート、保留、スピーカーボタンの初期設定
	 */
	private void initMuteHoldSpeakerBlock() {
		// ミュート、保留、スピーカーボタンを格納する子階層のLinearLayoutの初期設定
		initInnerLayout();

        // ミュートボタンの初期設定
        initMuteButton();

        // 保留ボタンの初期設定
//        initHoldButton();

        // スピーカーボタンの初期設定
        initSpeakerButton();

        // ミュート、保留、スピーカーという文字の初期設定
        initMuteHoldSpeakerCaption();
	}

	/**
	 * ミュート、保留、スピーカーボタンを格納する子階層のLinearLayoutの初期設定
	 */
	private void initInnerLayout() {
		// ミュート、保留、スピーカー、ボリュームコントロールを入れるレイアウト
        innerLayout = new LinearLayout(this);
        LayoutParams innerlayoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        innerlayoutParams.setMargins(0, 0, 0, 0);
        innerLayout.setLayoutParams(innerlayoutParams);
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
	}

	/**
	 * ミュートボタンの初期設定
	 */
	@SuppressWarnings("deprecation")
	private void initMuteButton() {
		// ミュートボタンを作る、ここは設定だけで表示はしない
        muteButton = new Button(this);
		LayoutParams layoutParams3 = new LayoutParams(getPixels(100), getPixels(100));
        layoutParams3.gravity = Gravity.CENTER;
        muteButton.setLayoutParams(layoutParams3);
        getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_sound_off).clearColorFilter();
        muteButton.setBackgroundDrawable(getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_sound_off));
        muteButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View arg0) {
    	    	Log.i(TAG, "muteButton onClickListener start");
	    		call.toggleMute();
    			Drawable drawable = getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_sound_off);
	    		if (call.isMuted()) {
	    			drawable.setColorFilter(Color.DKGRAY, android.graphics.PorterDuff.Mode.DST_ATOP);
	    			muteButton.setBackgroundDrawable(drawable);
	    		} else {
	    			drawable.clearColorFilter();
	    			muteButton.setBackgroundDrawable(drawable);
	    		}
    		}

        });
        innerLayout.addView(muteButton);
	}

	/**
	 * 保留ボタンの初期設定
	 */
/*	@SuppressWarnings("deprecation")
	private void initHoldButton() {
		// 保留ボタンを作る、ここは設定だけで表示はしない
        holdButton = new Button(this);
        LayoutParams layoutParams5 = new LayoutParams(getPixels(100), getPixels(100));
        layoutParams5.gravity = Gravity.CENTER;
        holdButton.setLayoutParams(layoutParams5);
        getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_answer_and_hold).clearColorFilter();
        holdButton.setBackgroundDrawable(getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_answer_and_hold));
        holdButton.setOnClickListener(new OnClickListener() {
    		public void onClick(View arg0) {
    	    	Log.i(TAG, "holdButton onClickListener start");
    			Drawable drawable = getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_answer_and_hold);
	    		if (call.isOnHold()) {
	    			try {
						call.continueCall(0);
		    			drawable.clearColorFilter();
				        holdButton.setBackgroundDrawable(drawable);
					} catch (SipException e) {
						Log.e(TAG, "call.continueCall error", e);
					}
	    			holdButton.setTextColor(Color.BLACK);
	    		} else {
    	    		try {
						call.holdCall(0);
		    			drawable.setColorFilter(Color.DKGRAY, android.graphics.PorterDuff.Mode.DST_ATOP);
				        holdButton.setBackgroundDrawable(drawable);
					} catch (SipException e) {
						Log.e(TAG, "call.holdCall error", e);
					}
	    			holdButton.setTextColor(Color.GRAY);
	    		}
    		}

        });
        innerLayout.addView(holdButton);
	}
*/
	/**
	 * スピーカーボタンの初期設定
	 */
	@SuppressWarnings("deprecation")
	private void initSpeakerButton() {
		// スピーカーボタンを作る、ここは設定だけで表示はしない
        speakerButton = new Button(this);
        LayoutParams layoutParams4 = new LayoutParams(getPixels(100), getPixels(100));
        layoutParams4.gravity = Gravity.CENTER;
        speakerButton.setLayoutParams(layoutParams4);
        getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_sound_on).clearColorFilter();
        speakerButton.setBackgroundDrawable(getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_sound_on));
        speakerButton.setOnClickListener(new OnClickListener() {
            boolean inSpeakerMode = false;
    		public void onClick(View arg0) {
    	    	Log.i(TAG, "speakerButton onClickListener start");
    			Drawable drawable = getResources().getDrawable(foo.bar.momoden.R.drawable.ic_jog_dial_sound_on);
    	    	if (inSpeakerMode) {
    	    		Log.d(TAG, "change speaker mode from true to false");
    	    		inSpeakerMode = false;
        	    	call.setSpeakerMode(false);
	    			drawable.clearColorFilter();
        	        speakerButton.setBackgroundDrawable(drawable);
    	    	} else {
    	    		Log.d(TAG, "change speaker mode from false to true");
    	    		inSpeakerMode = true;
        	    	call.setSpeakerMode(true);
	    			drawable.setColorFilter(Color.DKGRAY, android.graphics.PorterDuff.Mode.DST_ATOP);
        	        speakerButton.setBackgroundDrawable(drawable);
    	    	}
    		}

        });
        innerLayout.addView(speakerButton);
	}

	/**
	 * ミュート、保留、スピーカーという文字の初期設定
	 */
	private void initMuteHoldSpeakerCaption() {
		// ミュート、保留、スピーカー、ボリュームコントロールの文字を入れるレイアウト
        innerLayout2 = new LinearLayout(this);
        LayoutParams innerlayout2Params = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayout2Params.gravity = Gravity.CENTER_HORIZONTAL;
        innerlayout2Params.setMargins(0, 0, 0, getPixels(10));
        innerLayout2.setLayoutParams(innerlayout2Params);
        innerLayout2.setOrientation(LinearLayout.HORIZONTAL);

        TextView muteText = new TextView(this);
        muteText.setLayoutParams(new LayoutParams(getPixels(100), LinearLayout.LayoutParams.WRAP_CONTENT));
        muteText.setText("ミュート");
        muteText.setGravity(Gravity.CENTER);
        innerLayout2.addView(muteText);

/*      TextView holdText = new TextView(this);
        holdText.setLayoutParams(new LayoutParams(getPixels(100), LinearLayout.LayoutParams.WRAP_CONTENT));
        holdText.setText("HOLD");
        holdText.setGravity(Gravity.CENTER);
        innerLayout2.addView(holdText);
*/
        TextView speakerText = new TextView(this);
        speakerText.setLayoutParams(new LayoutParams(getPixels(100), LinearLayout.LayoutParams.WRAP_CONTENT));
        speakerText.setText("スピーカー");
        speakerText.setGravity(Gravity.CENTER);
        innerLayout2.addView(speakerText);
	}

	/**
	 * ボリューム調整バーの初期設定
	 */
	private void initVolumeControler() {
		// ボリューム調整バーを作る
        volumeControler = new SeekBar(this);
        LayoutParams layoutParams6 = new LayoutParams(getPixels(300), LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams6.setMargins(0, 0, 0, getPixels(20));
        layoutParams6.gravity = Gravity.CENTER;
        volumeControler.setLayoutParams(layoutParams6);

        // ボリュームの現在値と最大値を取得し、それをボリューム調整バーの初期値として設定する
        final AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        Log.d(TAG, "current volume=" + volume + ", max volume=" + maxVolume);
        volumeControler.setProgress(volume);
        volumeControler.setMax(maxVolume);

        // ボリューム調整バーで値が変更された時の処理
        volumeControler.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar arg0) {
			}

			public void onStartTrackingTouch(SeekBar arg0) {
			}

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// 変更後の値をセットする
				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, arg1, 0);
				Log.d(TAG, "volume=" + audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
			}
		});
	}

	/**
	 * 電話を切るボタンの初期設定
	 */
	private void initEndCallButton() {
		// 電話を切るボタンを作る、ここは設定だけで表示はしない
	    endcallButton = new Button(this);
	    LayoutParams layoutParams2 = new LayoutParams(getPixels(180), getPixels(60));
	    layoutParams2.gravity = Gravity.CENTER;
	    endcallButton.setLayoutParams(layoutParams2);
	    endcallButton.setTextAppearance(this, android.R.style.TextAppearance_Large);
	    endcallButton.setText("電話を切る");
	    endcallButton.setOnClickListener(new OnClickListener() {
	
			public void onClick(View arg0) {
		    	Log.i(TAG, "endCallButton onClickListener start");
	
		    	// textViewのメッセージを『通話が終了しました』に変えて、通話切断ボタンを隠す
		        showCallEndMessage();
	
				// 電話を切ってメイン画面に戻る
				endCallAndBackToMainActivity();
			}
	
	    });
	}

	/**
	 * 通話開始時、通話終了時、通話時間が残り少なくなった時に鳴らす音を初期化する
	 * @return
	 */
	private void initSoundPool() {
		// 残り時間が少なくなった時に鳴らす音の用意
		soundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0);
		soundPool.load(getApplicationContext(), R.raw.boatbell, 1);
		soundPool.load(getApplicationContext(), R.raw.ring, 1);
		soundPool.load(getApplicationContext(), R.raw.end, 1);
		return;
	}

	/**
	 * @return 前画面から渡された自分の電話番号
	 */
	private String getThisDN() {
        // 前画面（MainActivity）から渡されたインテント取得
        Intent intent = getIntent();

        // インテントの付加情報取得
        Bundle extras = intent.getExtras();

        // 自分の電話番号を取得
        String thisDN = extras != null ? extras.getString("thisDN") : "";

        return thisDN;
	}

    // 発信
    public void makeCall(String targetDN) {
    	Log.i(TAG, "makeCall start");

    	// 電話を既にかけている最中であれば何もしない
    	if (onCalling) {
        	Log.i(TAG, "do nothing and quit since call is already running, ");
    		return;
    	} else {
    		onCalling = true;
    	}

    	// Listenerクラスの中からこのアクティビティを参照するための変数
    	final CallActivity callActivity = this;
    	
    	// リスナーを作成
        listener = new SipAudioCall.Listener() {

        	// 通信確立時の処理
            @Override
            public void onCallEstablished(SipAudioCall call) {
            	Log.i(TAG, "onCallEstablished");

            	if (call.isInCall()) {
            		// 既に通話中の状態でonCallEstablishedに来るのは、保留解除の時だけと思われる
            		Log.i(TAG, "do nothing since already in call");
            	} else {
            		// 通常の通話確立時の処理
            		Log.i(TAG, "not in call so start audio for established call");

            		// 通話が始まったことを知らせる音を鳴らす
            		soundPool.play(2, 1.0F, 1.0F, 0, 0, 1.0F);

            		// 発信中ダイアログを消す
            		dismissProgressDialog();

            		// 音声通話を開始
                    call.startAudio();

                    // スピーカーモードをオフにする
					call.setSpeakerMode(false);

                    // textViewに通話中のメッセージを出す
                    showCallStartMessage();

                	// 相手の推しメンを取得し、画面に表示する
                	new GetProfileTask(callActivity).execute(AppConstants.GET_PROFILE_URL, callActivity.call.getPeerProfile().getUserName());

                	// 通話ログを書き込む
                	new InsertLogTask().execute(AppConstants.INSERT_LOG_URL, thisDN, getMyFavoriteMember());

                    // 通話終了タイマーの開始
                    callEndTimer();
            	}

            }

            // 通話終了時の処理
            @Override
            public void onCallEnded(SipAudioCall call) {
            	Log.i(TAG, "onCallEnded start");

            	// 通話終了時の音を鳴らす
            	soundPool.play(3, 1.0F, 1.0F, 0, 0, 1.0F);

                // textViewのメッセージを『通話が終了しました』に変えて、通話切断ボタンを隠す
                showCallEndMessage();

				// 電話を切ってメイン画面に戻る
				endCallAndBackToMainActivity();
            }

            // 話し中の時の処理
            // 着信したら起動するクラス(IncomingCallReceiver)を登録し、一定時間着信待ちを続ける
            // 一定時間待っても着信しなかったら、メイン画面に戻る
            @Override
            public void onCallBusy(SipAudioCall call) {
            	Log.i(TAG, "onCallBusy start");

            	// 電話着信用のクラスを登録して、着信が可能な状態にする
                registerReceiver();

                // 自分の推しメンを5000番のisUseに書き込む
    	        new PutProfileTask().execute(AppConstants.PUT_PROFILE_URL, "5000", getMyFavoriteMember());

                // 着信待ちタイマーを起動し、タイムアウトしたらメイン画面に戻る
                waitForCall();
            }
        };

        // 音声通話発信
        try {
			call = manager.makeAudioCall(sp.getUriString(), targetDN + "@" + AppConstants.SIPSERVER_IP, listener, 20);

		} catch (SipException e) {
			Log.e(TAG, "SipManager.makeAudioCall error (SipException)", e);
			if (sp != null) {
				try {
					manager.close(sp.getUriString());
				} catch (SipException e1) {
					Log.e(TAG, "SipManager.close error (SipException)", e1);
				}
			}
			if (call != null) {
				call.close();
			}
		}
    }

    /**
     * プリファレンスから保存されている推しメンを取得する、未保存の場合は（箱推し）にする
	 * @return int 自分の推しメンを表す番号、1：箱推し、2～6：各メンバー
	 */
	public int getMyFavoriteMember() {
		// プリファレンスから保存されている推しメンを取得する、未保存の場合は（箱推し）にする
		SharedPreferences preferences = getSharedPreferences(AppConstants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
		int myFavoriteMember = preferences.getInt("MEMBER", 1);
		return myFavoriteMember;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause start");

		// 発信中、着信待ちダイアログを消す
		dismissProgressDialog();

        // textViewのメッセージを『通話が終了しました』に変えて、通話切断ボタンを隠す
        showCallEndMessage();

		// 電話を切ってメイン画面に戻る
		endCallAndBackToMainActivity();
    }

    /**
     * 電話の着信を受けるため、呼び出しを受信するためのインテントフィルターをセットアップし、電話着信用のクラスを登録<br>
     * 電話が着信するとIncomingCallReceiverクラスが呼び出される
     */
    public void registerReceiver() {
    	Log.i(TAG, "registerReceiver start");

    	IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
    }
    
	/**
	 * 指定時間電話の着信を待ち、かかってこなかったら前画面に戻る
	 */
	public void waitForCall() {

		Runnable run = new Runnable() {

			public void run() {
				Log.i(TAG, "waitForCall start");

				// 指定時間待ち続けたか否か
				boolean loopEnd = true;

				for (int i=0; i<(Integer)AppConstants.CALL_WAIT_TIMER; i++) {
					// 既に通話が始まっていたら処理を抜ける
					if (call != null) {
						if (call.isInCall()) {
							Log.i(TAG, "waitForCall quit since call is started");
							loopEnd = false;
							break;
						}
					}

					// 着信待ち中にメイン画面に戻っていたら処理を抜ける
					if (manager == null) {
						Log.i(TAG, "waitForCall quit since SipManager is null");
						loopEnd = false;
						break;
					}

//					callActivity.updateProgressDialog(i);

					// 1秒スリープ
					SystemClock.sleep(1000);
				}

				if (loopEnd) {
					Log.i(TAG, "waitForCall timeover");

					// 着信待ちダイアログを消す
					dismissProgressDialog();

					// textViewのメッセージを『相談者がみつかりません』に変えて、通話切断ボタンを隠す
					showCallerBusyMessage();

					// 電話を切ってメイン画面に戻る
					endCallAndBackToMainActivity();
				}
			}
		};
		new Thread(run).start();
	}

	/**
	 * 終了処理を行い、前の画面に戻る<br>
	 * 複数箇所から呼ばれるが、最初の1回だけ動けばいいので、2回目以降は即リターン
	 */
	public void endCallAndBackToMainActivity() {
		Log.i(TAG, "endCallAndBackToMainActivity start");
		
		// 既に他から呼ばれて終了処理が始まっていた場合は、すぐにリターンする
		synchronized (onEnding) {
			if (onEnding) {
				Log.d(TAG, "endCallAndBackToMainActivity quit since already onEnding");
				return;
			} else {
				onEnding = true;
			}
		}

		_unregisterReceiver();
		_endCall();
		releaseDn();
		unregistAndClose();

		// 通話時間が残り少なくなった時に鳴らす音を解放する
		if (soundPool != null) {
			soundPool.release();
		}
		
		backToMainActivity();
	}
	
	/**
	 * 以降着信しないようにするため、着信用のクラスが登録されていたら、登録を抹消する
	 */
	private void _unregisterReceiver() {
    	Log.i(TAG, "_unregisterReceiver start");
	    // 着信用のクラスの登録を抹消する
		if (callReceiver != null) {
	    	Log.d(TAG, "unregisterReceiver");
	        this.unregisterReceiver(callReceiver);
	        callReceiver = null;
	    }
	}

	/**
	 * 通話中であれば電話を切る
	 */
	private void _endCall() {
    	Log.i(TAG, "_endCall start");
		// 通話中であれば切断する
        if (call != null) {
        	try {
        		Log.d(TAG, "call.endCall");
        		call.endCall();
        	} catch (SipException e) {
    			Log.e(TAG, "SipAudioCall.endCall error (SipException)", e);
        	}
    		Log.d(TAG, "call.close");
        	call.close();
        	call = null;
        }
	}
	
	/**
	 * 割り当てられたDNを解放するhttpリクエストを送信する
	 */
	private void releaseDn() {
    	Log.i(TAG, "releaseDn start");
		if (! thisDN.equals("")) {
	    	// DN解放タスクからの戻り値を入れる、キーresultに実行結果が入る、処理が終わるとキーendFlagに1が入る
	        HashMap<String, String> resultMap = new HashMap<String, String>();
	
	        // 割り当てられたDNを解放するhttpリクエストを送信するタスクを呼び出す
	        new ReleaseDnTask(resultMap).execute(AppConstants.RELEASE_CALLER_DN_URL, thisDN);
	
	        thisDN = "";
		}
	}

	/**
	 * アンレジスト、SipManagerのクローズ
	 */
	private void unregistAndClose() {
    	Log.i(TAG, "unregistAndClose start");
	    // アンレジストした後、SipManagerをクローズする
	    if (manager != null) {
	    	if (sp != null) {
	    		try {
	        		Log.d(TAG, "manager.unregister");
					manager.unregister(sp, null);
	
	        		SystemClock.sleep(1000); // これを入れないとなぜかunregisterがSIPサーバに届かない
	
	        		Log.d(TAG, "manager.close");
	        		manager.close(sp.getUriString());
	        		manager = null;
				} catch (SipException e) {
	    			Log.e(TAG, "SipManager.unregist/close error (SipException)", e);
				} catch (NullPointerException e) {
					Log.e(TAG, "SipManager.unregist/close error (NullPointerException)", e);
				}
	    	}
	    }
	}

	/**
	 * 指定秒数後にこのアクティビティを終了し、メイン画面に戻る
	 */
	private void backToMainActivity() {
		Log.i(TAG, "backToMainActivity start");
		Runnable runnable2 = new Runnable() {
			public void run() {
				finish();
			}
		};
		handler.postDelayed(runnable2, AppConstants.AFTER_CALL_TIMER);
	}

	/**
	 * textViewのメッセージを『通話が終了しました』に変えて、通話切断ボタンを隠す
	 */
	public void showCallEndMessage() {
		// textViewの表示メッセージを変えて、通話切断ボタンを消す
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCallEndMessage start");
				textView.setText("通話が終了しました。");
		        layout.removeView(memberTextCaption);
		        layout.removeView(memberText);
		        layout.removeView(innerLayout3);
		        layout.removeView(innerLayout);
		        layout.removeView(innerLayout2);
		        layout.removeView(volumeControler);
		        layout.removeView(endcallButton);
			}
		});
	}

	/**
	 * textViewのメッセージを『相談できる相手がみつかりません』に変えて、通話切断ボタンを隠す
	 */
	public void showCallerBusyMessage() {
		// textViewの表示メッセージを変えて、通話切断ボタンを消す
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCallerBusyMessage start");
				layout.addView(textView);
				textView.setText("話し相手がみつかりません。しばらく待ってからもう一度探してみてください。");
			}
		});
	}

	/**
	 * textViewのメッセージを『処理を中断しました』に変える
	 */
	public void showCancelMessage() {
		// textViewの表示メッセージを変えて、通話切断ボタンを消す
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCancelMessage start");
				layout.addView(textView);
				textView.setText("処理を中断しました。");
			}
		});
	}

	/**
	 * 通話が始まった時点で呼び出し、textViewのメッセージを通話中に変える
	 */
	public void showCallStartMessage() {
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCallStartMessage start");
		        layout.addView(textView);
				textView.setText("相手が見つかりました。（通話中）");
		        layout.addView(memberTextCaption);
		        layout.addView(memberText);
		        layout.addView(innerLayout3);
				updateTimerText(AppConstants.CALL_END_TIMER);
		        layout.addView(innerLayout);
		        layout.addView(innerLayout2);
		        layout.addView(volumeControler);
		        layout.addView(endcallButton);
			}
		});
	}

	/**
	 * 通話が始まった時点で呼び出し、通話終了までの時間をカウントダウンする。通話終了時間に達したら、通話を切断し、前画面に戻る。
	 */
	public void callEndTimer() {

		callEndTimerRunnable = new callEndTimerRunnable();

		new Thread(callEndTimerRunnable).start();

	}

	/**
	 * 残り時間の表示を更新する
	 */
	public void updateTimerText(final int value) {
		this.runOnUiThread(new Runnable() {

			public void run() {
				// timerTextに表示されている時間を更新する
				int tempTime = value;
				int intMinute = tempTime / 60;
				int intSecond = tempTime - intMinute * 60;
				StringBuffer strSecond = new StringBuffer("0" + String.valueOf(intSecond));
				timerText.setText(String.valueOf(intMinute) + ":" + strSecond.substring(strSecond.length() - 2));
			}

		});
	}

	/**
	 * 残り時間表示の背景色を変更する
	 */
	public void changeTimerTextBackgroundColor(final int color) {
		this.runOnUiThread(new Runnable() {

			public void run() {
				timerText.setBackgroundColor(color);
			}

		});
	}

	/**
	 * 発信中、着信待ちのダイアログを表示する
	 */
	public void showProgressDialog() {
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.d(TAG, "showProgressDialog start");
				progressDialog.setTitle("探索中");
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setIndeterminate(true);
				progressDialog.setMessage("話し相手を探しています。 しばらくお待ちください。");
				progressDialog.setCancelable(false);

				// キャンセルボタンを押した時の処理、リソースを開放してメイン画面に戻る
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						Log.i(TAG, "progressDialog cancel button onClickListener start");
						progressDialog.cancel();

						// textViewのメッセージを『処理を中断しました』に変える
						showCancelMessage();

						// 電話を切ってメイン画面に戻る
						endCallAndBackToMainActivity();
					}
				});
				progressDialog.show();
			}
		});
	}

//	public void updateProgressDialog(final int value) {
//		this.runOnUiThread(new Runnable() {
//
//			public void run() {
//				progressDialog.setProgress(value);
//				progressDialog.setMessage("相談相手を探しています。 しばらくお待ちください。(" + String.valueOf(60-value) + ")");
//			}
//		});
//
//	}

	/**
	 * 発信中、着信待ちダイアログを消す
	 */
	public void dismissProgressDialog() {
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.d(TAG, "dismissProgressDialog start");
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			}
		});

	}
	
	/**
     * dpからpixelに変換する
     * @param dipValue dpでの長さ
     * @return pixelでの長さ
     */
    private int getPixels(int dipValue) {
    	Resources r = getResources();
    	int px = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    	return px;
    }

    public class mySipRegistrationListener implements SipRegistrationListener {

//    	private CallActivity callActivity;
    	boolean isRunning = false;

//    	public mySipRegistrationListener(CallActivity callActivity) {
//    		this.callActivity = callActivity;
//    	}

		// レジストレーションリクエスト送信時
        public void onRegistering(String localProfileUri) {
        	Log.i(TAG, "onRegistering");
        }

        // レジストレーション成功時
        public void onRegistrationDone(String localProfileUri, long expiryTime) {
        	Log.i(TAG, "onRegistrationDone");

    		if (isRunning) {
				Log.i(TAG, "do nothing since already calling");
    		} else {
    			isRunning = true;
        		// 発信前にWait処理をする
        		SystemClock.sleep(3000);
            	makeCall(AppConstants.CALLER_TARGET_DN);
    		}
        }

        // レジストレーション失敗時
        public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
        	if (errorCode == -4) {
        		Log.i(TAG, "unregistered (-4)");
        	} else if (errorCode == -9) {
        		Log.i(TAG, "registration overwrite (-9)");
        	} else {
            	Log.i(TAG, "onRegistrationFailed");
        	}
        }
    }
    
    public class callEndTimerRunnable implements Runnable {
			
		int restTime;

		public void run() {
			Log.i(TAG, "callEndTimer start");

			// 通話終了時間に達したか否か
			boolean loopEnd = true;

			// 通話制限時間になるまで1秒ずつカウントアップしていくループ
			for (restTime = AppConstants.CALL_END_TIMER ; restTime > 0 ; restTime--) {

				// 途中で通話が終わっていた場合は処理を抜ける、この時は電話切断、メイン画面に戻る処理は行わない、onCallEndedで行われるので
				if (call == null || ! call.isInCall()) {
					Log.i(TAG, "callEndTimer quit since call is not in call");
					loopEnd = false;
					break;
				}

				// 残り時間があと3分または1分になったらアラーム音を鳴らし、背景色を変える
				if (restTime == AppConstants.FIRST_ALARM) {
					Log.d(TAG, "alarm play");
					soundPool.play(1, 1.0F, 1.0F, 0, 0, 1.0F);
					changeTimerTextBackgroundColor(Color.YELLOW);
				} else if (restTime == AppConstants.SECOND_ALARM) {
					Log.d(TAG, "alarm play");
					soundPool.play(1, 1.0F, 1.0F, 0, 0, 1.0F);
					changeTimerTextBackgroundColor(Color.RED);
				}

				// 画面の残り時間表示を更新する
				updateTimerText(restTime);

				// 1秒スリープ
				SystemClock.sleep(1000);
			}

			// 残り時間が0になった時の処理、電話を切ってメイン画面に戻る
			if (loopEnd) {
				Log.i(TAG, "callEndTimer timeover");
				// textViewのメッセージを『通話が終了しました』に変えて、通話切断ボタンを隠す
				showCallEndMessage();

				// 電話を切ってメイン画面に戻る
				endCallAndBackToMainActivity();
			}
		}
		
		public int getRestTime() {
			return restTime;
		}
		
		public void setRestTime(int newRestTime) {
			restTime = newRestTime;
		}
    }
}
