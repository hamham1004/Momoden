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

	// ���O�o�͗p�^�O��
	private final String TAG = "CallActivity";

	// SIP�ʐM�p�I�u�W�F�N�g
	SipManager manager = null;
    SipProfile sp = null;
    public SipAudioCall call = null;
    public IncomingCallReceiver callReceiver;
    SipAudioCall.Listener listener;

    // �����̓d�b�ԍ��AMainActivity����n�����
    String thisDN;

    // �d�b�������Ă���Œ����ۂ��̃t���O
    Boolean onCalling = false;
    
    // �I���������n�܂������ۂ��̃t���O
    Boolean onEnding = false;

    // ��ʕ\�����i
    LinearLayout layout;
    TextView textView;
    TextView memberTextCaption;
    public TextView memberText;  // ����̐��������AGetProfileTask����X�V���邩��public
    LinearLayout innerLayout3;   // �c�莞�ԁA�����{�^��������
    LinearLayout innerLayout4;   // �c�莞�Ԃ̃^�C�g���A�c�莞�Ԃ�TextView������
    TextView timerTextCaption;
    TextView timerText;
    Button extendButton;
    LinearLayout innerLayout;    // �~���[�g�A�X�s�[�J�[�{�^��������
    Button muteButton;
//  Button holdButton;
    Button speakerButton;
    LinearLayout innerLayout2;   // �~���[�g�A�X�s�[�J�[�{�^���̃^�C�g��������
    SeekBar volumeControler;
    Button endcallButton;

    // ��ʕ`��p�n���h���[
    public Handler handler;

    // �c�莞�Ԃ��㏭���ɂȂ������ɖ炷�A���[��
	public SoundPool soundPool;

	// ���M���A���M�҂��̃_�C�A���O
	ProgressDialog progressDialog;

	callEndTimerRunnable callEndTimerRunnable;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);

        // ��ʏ㕔�̃^�C�g���𖳂��ɂ���
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // ��ʂ��X���[�v��ԂɂȂ�̂�}�~����
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // ���M���A���M�҂��̃_�C�A���O�̏�����
        progressDialog = new ProgressDialog(this);

        // �ŏ�ʂ�LinearLayout�̏����ݒ�
        initParentLayout();

        // ���b�Z�[�W�\���p��TextView�̏����ݒ�
        initTextView();

        // ����̐��������\���p��TextView�̏����ݒ�
        initMemberText();

        // �c�莞�ԕ\���A�����{�^���p�̃��C�A�E�g�̏����ݒ�
        initInnerLayout3();
        
        // �c�莞�ԕ\���p�̃��C�A�E�g�̏����ݒ�
        initInnerLayout4();

        // �c�莞�ԕ\���p��TextView�̏����ݒ�
        initTimerText();
        
        // �����{�^���̏����ݒ�
        initExtendButton();

        // �~���[�g�A�ۗ��A�X�s�[�J�[�{�^���̏����ݒ�
        initMuteHoldSpeakerBlock();

        // �{�����[�������o�[�̏����ݒ�
        initVolumeControler();

        // �d�b��؂�{�^���̏����ݒ�
        initEndCallButton();

        // �C�x���g�n���h������A��ʕ`��⎟��ʑJ�ڂ�v�����邽�߂̃n���h���[�̏�����
        handler = new Handler();

        // �O��ʂ���C���e���g�œn���ꂽ�����̓d�b�ԍ����擾����
        thisDN = getThisDN();
		Log.d(TAG, "dn=<" + thisDN + ">");

		// SoundPool������������
		initSoundPool();

        // SipManager�C���X�^���X����
        manager = SipManager.newInstance(this);
        SipProfile.Builder builder;
        // Sip�ɑΉ����Ă���[�������`�F�b�N
        if ( SipManager.isVoipSupported(this) ){

        	// SipProfile���쐬
        	try {
        		// ���M���A���M�҂��_�C�A���O��\������
        		showProgressDialog();

        		builder = new SipProfile.Builder(thisDN, AppConstants.SIPSERVER_IP);
        		builder.setPassword(thisDN + "pass");
        		sp = builder.build();

        		// SIP�R�[����M�p�C���e���g���쐬
        		Intent i = new Intent();
        		i.setAction("android.SipDemo.INCOMING_CALL");
        		PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);

        		// SIP�T�[�o�Ƃ̐ڑ�
        		manager.open(sp, pi, null);

        		// ���X�i�[��ݒ�
        		manager.setRegistrationListener(sp.getUriString(), new mySipRegistrationListener());

        	} catch (ParseException e) {
        		Log.e(TAG, "SipProfile.Builder error (ParseException)", e);
        	} catch (SipException e) {
        		Log.e(TAG, "manager.open error (SipException)", e);
        	}
        }else{
        	layout.addView(textView);
        	textView.setText("�\���󂠂�܂��񂪂��̒[���ł͂����p�ɂȂ�܂���B�iGalaxy S2�Ȃǁj");
        	backToMainActivity();
        }
    }

	/**
	 * �ŏ�ʂ�LinearLayout�̏����ݒ�
	 */
	@SuppressWarnings("deprecation")
	private void initParentLayout() {
		// �{�^����r������\������K�v�����邽�߁A���C�A�E�g��xml���g�킸�A�v���O�����Őݒ肷��
        layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setBackgroundDrawable(getResources().getDrawable(foo.bar.momoden.R.drawable.maple));
        setContentView(layout);
	}

	/**
	 * ���b�Z�[�W�\���p��TextView�̏����ݒ�
	 */
	private void initTextView() {
		// ���b�Z�[�W�\���p��textView�����
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
	 * ����̐��������\���p��TextView�̏����ݒ�
	 */
	private void initMemberText() {
		// �u����́v�Ƃ����������o�����߂�textView�����
        memberTextCaption = new TextView(this);
        LayoutParams layoutParams2 = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		layoutParams2.setMargins(getPixels(30), 0, 0, 0);
        memberTextCaption.setLayoutParams(layoutParams2);
        memberTextCaption.setText("�����");

        // ���������\���p��textView�����
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
	 * �c�莞�ԕ\���A�����{�^�����i�[����q�K�w��LinearLayout�̏����ݒ�
	 */
	private void initInnerLayout3() {
		// �c�莞�ԕ\���A�����{�^�������郌�C�A�E�g
        innerLayout3 = new LinearLayout(this);
        LayoutParams innerlayoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayoutParams.gravity = Gravity.CENTER;
        innerlayoutParams.setMargins(0, 0, 0, 0);
        innerLayout3.setLayoutParams(innerlayoutParams);
        innerLayout3.setOrientation(LinearLayout.HORIZONTAL);
	}

	/**
	 * �c�莞�ԕ\���A�����{�^�����i�[����q�K�w��LinearLayout�̏����ݒ�
	 */
	private void initInnerLayout4() {
		// �c�莞�ԕ\���A�����{�^�������郌�C�A�E�g
        innerLayout4 = new LinearLayout(this);
        LayoutParams innerlayoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        innerlayoutParams.setMargins(0, 0, 0, 0);
        innerLayout4.setLayoutParams(innerlayoutParams);
        innerLayout4.setOrientation(LinearLayout.VERTICAL);
        innerLayout3.addView(innerLayout4);
	}

	/**
	 * �c�莞�ԕ\���p��TextView�̏����ݒ�
	 */
	private void initTimerText() {
		// �c�莞�ԂƂ����������o�����߂�textView�����
        timerTextCaption = new TextView(this);
        LayoutParams layoutParams8 = new LayoutParams(getPixels(180), LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams8.gravity = Gravity.CENTER;
        timerTextCaption.setLayoutParams(layoutParams8);
        timerTextCaption.setGravity(Gravity.CENTER);
        timerTextCaption.setText("�c�莞��");
        innerLayout4.addView(timerTextCaption);

        // �^�C�}�[�\���p��textView�����
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
	 * �ʘb���Ԃ���������{�^���̏����ݒ�
	 */
	private void initExtendButton() {
	    extendButton = new Button(this);
	    LayoutParams layoutParams2 = new LayoutParams(getPixels(100), getPixels(70));
	    layoutParams2.setMargins(0, getPixels(5), 0, 0);
	    layoutParams2.gravity = Gravity.CENTER;
	    extendButton.setLayoutParams(layoutParams2);
	    extendButton.setTextAppearance(this, android.R.style.TextAppearance_Large);
	    extendButton.setText("����");
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
	 * �~���[�g�A�ۗ��A�X�s�[�J�[�{�^���̏����ݒ�
	 */
	private void initMuteHoldSpeakerBlock() {
		// �~���[�g�A�ۗ��A�X�s�[�J�[�{�^�����i�[����q�K�w��LinearLayout�̏����ݒ�
		initInnerLayout();

        // �~���[�g�{�^���̏����ݒ�
        initMuteButton();

        // �ۗ��{�^���̏����ݒ�
//        initHoldButton();

        // �X�s�[�J�[�{�^���̏����ݒ�
        initSpeakerButton();

        // �~���[�g�A�ۗ��A�X�s�[�J�[�Ƃ��������̏����ݒ�
        initMuteHoldSpeakerCaption();
	}

	/**
	 * �~���[�g�A�ۗ��A�X�s�[�J�[�{�^�����i�[����q�K�w��LinearLayout�̏����ݒ�
	 */
	private void initInnerLayout() {
		// �~���[�g�A�ۗ��A�X�s�[�J�[�A�{�����[���R���g���[�������郌�C�A�E�g
        innerLayout = new LinearLayout(this);
        LayoutParams innerlayoutParams = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        innerlayoutParams.setMargins(0, 0, 0, 0);
        innerLayout.setLayoutParams(innerlayoutParams);
        innerLayout.setOrientation(LinearLayout.HORIZONTAL);
	}

	/**
	 * �~���[�g�{�^���̏����ݒ�
	 */
	@SuppressWarnings("deprecation")
	private void initMuteButton() {
		// �~���[�g�{�^�������A�����͐ݒ肾���ŕ\���͂��Ȃ�
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
	 * �ۗ��{�^���̏����ݒ�
	 */
/*	@SuppressWarnings("deprecation")
	private void initHoldButton() {
		// �ۗ��{�^�������A�����͐ݒ肾���ŕ\���͂��Ȃ�
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
	 * �X�s�[�J�[�{�^���̏����ݒ�
	 */
	@SuppressWarnings("deprecation")
	private void initSpeakerButton() {
		// �X�s�[�J�[�{�^�������A�����͐ݒ肾���ŕ\���͂��Ȃ�
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
	 * �~���[�g�A�ۗ��A�X�s�[�J�[�Ƃ��������̏����ݒ�
	 */
	private void initMuteHoldSpeakerCaption() {
		// �~���[�g�A�ۗ��A�X�s�[�J�[�A�{�����[���R���g���[���̕��������郌�C�A�E�g
        innerLayout2 = new LinearLayout(this);
        LayoutParams innerlayout2Params = new LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        innerlayout2Params.gravity = Gravity.CENTER_HORIZONTAL;
        innerlayout2Params.setMargins(0, 0, 0, getPixels(10));
        innerLayout2.setLayoutParams(innerlayout2Params);
        innerLayout2.setOrientation(LinearLayout.HORIZONTAL);

        TextView muteText = new TextView(this);
        muteText.setLayoutParams(new LayoutParams(getPixels(100), LinearLayout.LayoutParams.WRAP_CONTENT));
        muteText.setText("�~���[�g");
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
        speakerText.setText("�X�s�[�J�[");
        speakerText.setGravity(Gravity.CENTER);
        innerLayout2.addView(speakerText);
	}

	/**
	 * �{�����[�������o�[�̏����ݒ�
	 */
	private void initVolumeControler() {
		// �{�����[�������o�[�����
        volumeControler = new SeekBar(this);
        LayoutParams layoutParams6 = new LayoutParams(getPixels(300), LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams6.setMargins(0, 0, 0, getPixels(20));
        layoutParams6.gravity = Gravity.CENTER;
        volumeControler.setLayoutParams(layoutParams6);

        // �{�����[���̌��ݒl�ƍő�l���擾���A������{�����[�������o�[�̏����l�Ƃ��Đݒ肷��
        final AudioManager audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        int volume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL);
        Log.d(TAG, "current volume=" + volume + ", max volume=" + maxVolume);
        volumeControler.setProgress(volume);
        volumeControler.setMax(maxVolume);

        // �{�����[�������o�[�Œl���ύX���ꂽ���̏���
        volumeControler.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onStopTrackingTouch(SeekBar arg0) {
			}

			public void onStartTrackingTouch(SeekBar arg0) {
			}

			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// �ύX��̒l���Z�b�g����
				audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, arg1, 0);
				Log.d(TAG, "volume=" + audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL));
			}
		});
	}

	/**
	 * �d�b��؂�{�^���̏����ݒ�
	 */
	private void initEndCallButton() {
		// �d�b��؂�{�^�������A�����͐ݒ肾���ŕ\���͂��Ȃ�
	    endcallButton = new Button(this);
	    LayoutParams layoutParams2 = new LayoutParams(getPixels(180), getPixels(60));
	    layoutParams2.gravity = Gravity.CENTER;
	    endcallButton.setLayoutParams(layoutParams2);
	    endcallButton.setTextAppearance(this, android.R.style.TextAppearance_Large);
	    endcallButton.setText("�d�b��؂�");
	    endcallButton.setOnClickListener(new OnClickListener() {
	
			public void onClick(View arg0) {
		    	Log.i(TAG, "endCallButton onClickListener start");
	
		    	// textView�̃��b�Z�[�W���w�ʘb���I�����܂����x�ɕς��āA�ʘb�ؒf�{�^�����B��
		        showCallEndMessage();
	
				// �d�b��؂��ă��C����ʂɖ߂�
				endCallAndBackToMainActivity();
			}
	
	    });
	}

	/**
	 * �ʘb�J�n���A�ʘb�I�����A�ʘb���Ԃ��c�菭�Ȃ��Ȃ������ɖ炷��������������
	 * @return
	 */
	private void initSoundPool() {
		// �c�莞�Ԃ����Ȃ��Ȃ������ɖ炷���̗p��
		soundPool = new SoundPool(1, AudioManager.STREAM_VOICE_CALL, 0);
		soundPool.load(getApplicationContext(), R.raw.boatbell, 1);
		soundPool.load(getApplicationContext(), R.raw.ring, 1);
		soundPool.load(getApplicationContext(), R.raw.end, 1);
		return;
	}

	/**
	 * @return �O��ʂ���n���ꂽ�����̓d�b�ԍ�
	 */
	private String getThisDN() {
        // �O��ʁiMainActivity�j����n���ꂽ�C���e���g�擾
        Intent intent = getIntent();

        // �C���e���g�̕t�����擾
        Bundle extras = intent.getExtras();

        // �����̓d�b�ԍ����擾
        String thisDN = extras != null ? extras.getString("thisDN") : "";

        return thisDN;
	}

    // ���M
    public void makeCall(String targetDN) {
    	Log.i(TAG, "makeCall start");

    	// �d�b�����ɂ����Ă���Œ��ł���Ή������Ȃ�
    	if (onCalling) {
        	Log.i(TAG, "do nothing and quit since call is already running, ");
    		return;
    	} else {
    		onCalling = true;
    	}

    	// Listener�N���X�̒����炱�̃A�N�e�B�r�e�B���Q�Ƃ��邽�߂̕ϐ�
    	final CallActivity callActivity = this;
    	
    	// ���X�i�[���쐬
        listener = new SipAudioCall.Listener() {

        	// �ʐM�m�����̏���
            @Override
            public void onCallEstablished(SipAudioCall call) {
            	Log.i(TAG, "onCallEstablished");

            	if (call.isInCall()) {
            		// ���ɒʘb���̏�Ԃ�onCallEstablished�ɗ���̂́A�ۗ������̎������Ǝv����
            		Log.i(TAG, "do nothing since already in call");
            	} else {
            		// �ʏ�̒ʘb�m�����̏���
            		Log.i(TAG, "not in call so start audio for established call");

            		// �ʘb���n�܂������Ƃ�m�点�鉹��炷
            		soundPool.play(2, 1.0F, 1.0F, 0, 0, 1.0F);

            		// ���M���_�C�A���O������
            		dismissProgressDialog();

            		// �����ʘb���J�n
                    call.startAudio();

                    // �X�s�[�J�[���[�h���I�t�ɂ���
					call.setSpeakerMode(false);

                    // textView�ɒʘb���̃��b�Z�[�W���o��
                    showCallStartMessage();

                	// ����̐����������擾���A��ʂɕ\������
                	new GetProfileTask(callActivity).execute(AppConstants.GET_PROFILE_URL, callActivity.call.getPeerProfile().getUserName());

                	// �ʘb���O����������
                	new InsertLogTask().execute(AppConstants.INSERT_LOG_URL, thisDN, getMyFavoriteMember());

                    // �ʘb�I���^�C�}�[�̊J�n
                    callEndTimer();
            	}

            }

            // �ʘb�I�����̏���
            @Override
            public void onCallEnded(SipAudioCall call) {
            	Log.i(TAG, "onCallEnded start");

            	// �ʘb�I�����̉���炷
            	soundPool.play(3, 1.0F, 1.0F, 0, 0, 1.0F);

                // textView�̃��b�Z�[�W���w�ʘb���I�����܂����x�ɕς��āA�ʘb�ؒf�{�^�����B��
                showCallEndMessage();

				// �d�b��؂��ă��C����ʂɖ߂�
				endCallAndBackToMainActivity();
            }

            // �b�����̎��̏���
            // ���M������N������N���X(IncomingCallReceiver)��o�^���A��莞�Ԓ��M�҂��𑱂���
            // ��莞�ԑ҂��Ă����M���Ȃ�������A���C����ʂɖ߂�
            @Override
            public void onCallBusy(SipAudioCall call) {
            	Log.i(TAG, "onCallBusy start");

            	// �d�b���M�p�̃N���X��o�^���āA���M���\�ȏ�Ԃɂ���
                registerReceiver();

                // �����̐���������5000�Ԃ�isUse�ɏ�������
    	        new PutProfileTask().execute(AppConstants.PUT_PROFILE_URL, "5000", getMyFavoriteMember());

                // ���M�҂��^�C�}�[���N�����A�^�C���A�E�g�����烁�C����ʂɖ߂�
                waitForCall();
            }
        };

        // �����ʘb���M
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
     * �v���t�@�����X����ۑ�����Ă��鐄���������擾����A���ۑ��̏ꍇ�́i�������j�ɂ���
	 * @return int �����̐���������\���ԍ��A1�F�������A2�`6�F�e�����o�[
	 */
	public int getMyFavoriteMember() {
		// �v���t�@�����X����ۑ�����Ă��鐄���������擾����A���ۑ��̏ꍇ�́i�������j�ɂ���
		SharedPreferences preferences = getSharedPreferences(AppConstants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
		int myFavoriteMember = preferences.getInt("MEMBER", 1);
		return myFavoriteMember;
	}

	@Override
	public void onPause() {
		super.onPause();
		Log.i(TAG, "onPause start");

		// ���M���A���M�҂��_�C�A���O������
		dismissProgressDialog();

        // textView�̃��b�Z�[�W���w�ʘb���I�����܂����x�ɕς��āA�ʘb�ؒf�{�^�����B��
        showCallEndMessage();

		// �d�b��؂��ă��C����ʂɖ߂�
		endCallAndBackToMainActivity();
    }

    /**
     * �d�b�̒��M���󂯂邽�߁A�Ăяo������M���邽�߂̃C���e���g�t�B���^�[���Z�b�g�A�b�v���A�d�b���M�p�̃N���X��o�^<br>
     * �d�b�����M�����IncomingCallReceiver�N���X���Ăяo�����
     */
    public void registerReceiver() {
    	Log.i(TAG, "registerReceiver start");

    	IntentFilter filter = new IntentFilter();
        filter.addAction("android.SipDemo.INCOMING_CALL");
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
    }
    
	/**
	 * �w�莞�ԓd�b�̒��M��҂��A�������Ă��Ȃ�������O��ʂɖ߂�
	 */
	public void waitForCall() {

		Runnable run = new Runnable() {

			public void run() {
				Log.i(TAG, "waitForCall start");

				// �w�莞�ԑ҂����������ۂ�
				boolean loopEnd = true;

				for (int i=0; i<(Integer)AppConstants.CALL_WAIT_TIMER; i++) {
					// ���ɒʘb���n�܂��Ă����珈���𔲂���
					if (call != null) {
						if (call.isInCall()) {
							Log.i(TAG, "waitForCall quit since call is started");
							loopEnd = false;
							break;
						}
					}

					// ���M�҂����Ƀ��C����ʂɖ߂��Ă����珈���𔲂���
					if (manager == null) {
						Log.i(TAG, "waitForCall quit since SipManager is null");
						loopEnd = false;
						break;
					}

//					callActivity.updateProgressDialog(i);

					// 1�b�X���[�v
					SystemClock.sleep(1000);
				}

				if (loopEnd) {
					Log.i(TAG, "waitForCall timeover");

					// ���M�҂��_�C�A���O������
					dismissProgressDialog();

					// textView�̃��b�Z�[�W���w���k�҂��݂���܂���x�ɕς��āA�ʘb�ؒf�{�^�����B��
					showCallerBusyMessage();

					// �d�b��؂��ă��C����ʂɖ߂�
					endCallAndBackToMainActivity();
				}
			}
		};
		new Thread(run).start();
	}

	/**
	 * �I���������s���A�O�̉�ʂɖ߂�<br>
	 * �����ӏ�����Ă΂�邪�A�ŏ���1�񂾂������΂����̂ŁA2��ڈȍ~�͑����^�[��
	 */
	public void endCallAndBackToMainActivity() {
		Log.i(TAG, "endCallAndBackToMainActivity start");
		
		// ���ɑ�����Ă΂�ďI���������n�܂��Ă����ꍇ�́A�����Ƀ��^�[������
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

		// �ʘb���Ԃ��c�菭�Ȃ��Ȃ������ɖ炷�����������
		if (soundPool != null) {
			soundPool.release();
		}
		
		backToMainActivity();
	}
	
	/**
	 * �ȍ~���M���Ȃ��悤�ɂ��邽�߁A���M�p�̃N���X���o�^����Ă�����A�o�^�𖕏�����
	 */
	private void _unregisterReceiver() {
    	Log.i(TAG, "_unregisterReceiver start");
	    // ���M�p�̃N���X�̓o�^�𖕏�����
		if (callReceiver != null) {
	    	Log.d(TAG, "unregisterReceiver");
	        this.unregisterReceiver(callReceiver);
	        callReceiver = null;
	    }
	}

	/**
	 * �ʘb���ł���Γd�b��؂�
	 */
	private void _endCall() {
    	Log.i(TAG, "_endCall start");
		// �ʘb���ł���ΐؒf����
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
	 * ���蓖�Ă�ꂽDN���������http���N�G�X�g�𑗐M����
	 */
	private void releaseDn() {
    	Log.i(TAG, "releaseDn start");
		if (! thisDN.equals("")) {
	    	// DN����^�X�N����̖߂�l������A�L�[result�Ɏ��s���ʂ�����A�������I���ƃL�[endFlag��1������
	        HashMap<String, String> resultMap = new HashMap<String, String>();
	
	        // ���蓖�Ă�ꂽDN���������http���N�G�X�g�𑗐M����^�X�N���Ăяo��
	        new ReleaseDnTask(resultMap).execute(AppConstants.RELEASE_CALLER_DN_URL, thisDN);
	
	        thisDN = "";
		}
	}

	/**
	 * �A�����W�X�g�ASipManager�̃N���[�Y
	 */
	private void unregistAndClose() {
    	Log.i(TAG, "unregistAndClose start");
	    // �A�����W�X�g������ASipManager���N���[�Y����
	    if (manager != null) {
	    	if (sp != null) {
	    		try {
	        		Log.d(TAG, "manager.unregister");
					manager.unregister(sp, null);
	
	        		SystemClock.sleep(1000); // ��������Ȃ��ƂȂ���unregister��SIP�T�[�o�ɓ͂��Ȃ�
	
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
	 * �w��b����ɂ��̃A�N�e�B�r�e�B���I�����A���C����ʂɖ߂�
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
	 * textView�̃��b�Z�[�W���w�ʘb���I�����܂����x�ɕς��āA�ʘb�ؒf�{�^�����B��
	 */
	public void showCallEndMessage() {
		// textView�̕\�����b�Z�[�W��ς��āA�ʘb�ؒf�{�^��������
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCallEndMessage start");
				textView.setText("�ʘb���I�����܂����B");
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
	 * textView�̃��b�Z�[�W���w���k�ł��鑊�肪�݂���܂���x�ɕς��āA�ʘb�ؒf�{�^�����B��
	 */
	public void showCallerBusyMessage() {
		// textView�̕\�����b�Z�[�W��ς��āA�ʘb�ؒf�{�^��������
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCallerBusyMessage start");
				layout.addView(textView);
				textView.setText("�b�����肪�݂���܂���B���΂炭�҂��Ă��������x�T���Ă݂Ă��������B");
			}
		});
	}

	/**
	 * textView�̃��b�Z�[�W���w�����𒆒f���܂����x�ɕς���
	 */
	public void showCancelMessage() {
		// textView�̕\�����b�Z�[�W��ς��āA�ʘb�ؒf�{�^��������
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCancelMessage start");
				layout.addView(textView);
				textView.setText("�����𒆒f���܂����B");
			}
		});
	}

	/**
	 * �ʘb���n�܂������_�ŌĂяo���AtextView�̃��b�Z�[�W��ʘb���ɕς���
	 */
	public void showCallStartMessage() {
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.i(TAG, "showCallStartMessage start");
		        layout.addView(textView);
				textView.setText("���肪������܂����B�i�ʘb���j");
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
	 * �ʘb���n�܂������_�ŌĂяo���A�ʘb�I���܂ł̎��Ԃ��J�E���g�_�E������B�ʘb�I�����ԂɒB������A�ʘb��ؒf���A�O��ʂɖ߂�B
	 */
	public void callEndTimer() {

		callEndTimerRunnable = new callEndTimerRunnable();

		new Thread(callEndTimerRunnable).start();

	}

	/**
	 * �c�莞�Ԃ̕\�����X�V����
	 */
	public void updateTimerText(final int value) {
		this.runOnUiThread(new Runnable() {

			public void run() {
				// timerText�ɕ\������Ă��鎞�Ԃ��X�V����
				int tempTime = value;
				int intMinute = tempTime / 60;
				int intSecond = tempTime - intMinute * 60;
				StringBuffer strSecond = new StringBuffer("0" + String.valueOf(intSecond));
				timerText.setText(String.valueOf(intMinute) + ":" + strSecond.substring(strSecond.length() - 2));
			}

		});
	}

	/**
	 * �c�莞�ԕ\���̔w�i�F��ύX����
	 */
	public void changeTimerTextBackgroundColor(final int color) {
		this.runOnUiThread(new Runnable() {

			public void run() {
				timerText.setBackgroundColor(color);
			}

		});
	}

	/**
	 * ���M���A���M�҂��̃_�C�A���O��\������
	 */
	public void showProgressDialog() {
		this.runOnUiThread(new Runnable() {

			public void run() {
				Log.d(TAG, "showProgressDialog start");
				progressDialog.setTitle("�T����");
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.setIndeterminate(true);
				progressDialog.setMessage("�b�������T���Ă��܂��B ���΂炭���҂����������B");
				progressDialog.setCancelable(false);

				// �L�����Z���{�^�������������̏����A���\�[�X���J�����ă��C����ʂɖ߂�
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						Log.i(TAG, "progressDialog cancel button onClickListener start");
						progressDialog.cancel();

						// textView�̃��b�Z�[�W���w�����𒆒f���܂����x�ɕς���
						showCancelMessage();

						// �d�b��؂��ă��C����ʂɖ߂�
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
//				progressDialog.setMessage("���k�����T���Ă��܂��B ���΂炭���҂����������B(" + String.valueOf(60-value) + ")");
//			}
//		});
//
//	}

	/**
	 * ���M���A���M�҂��_�C�A���O������
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
     * dp����pixel�ɕϊ�����
     * @param dipValue dp�ł̒���
     * @return pixel�ł̒���
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

		// ���W�X�g���[�V�������N�G�X�g���M��
        public void onRegistering(String localProfileUri) {
        	Log.i(TAG, "onRegistering");
        }

        // ���W�X�g���[�V����������
        public void onRegistrationDone(String localProfileUri, long expiryTime) {
        	Log.i(TAG, "onRegistrationDone");

    		if (isRunning) {
				Log.i(TAG, "do nothing since already calling");
    		} else {
    			isRunning = true;
        		// ���M�O��Wait����������
        		SystemClock.sleep(3000);
            	makeCall(AppConstants.CALLER_TARGET_DN);
    		}
        }

        // ���W�X�g���[�V�������s��
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

			// �ʘb�I�����ԂɒB�������ۂ�
			boolean loopEnd = true;

			// �ʘb�������ԂɂȂ�܂�1�b���J�E���g�A�b�v���Ă������[�v
			for (restTime = AppConstants.CALL_END_TIMER ; restTime > 0 ; restTime--) {

				// �r���Œʘb���I����Ă����ꍇ�͏����𔲂���A���̎��͓d�b�ؒf�A���C����ʂɖ߂鏈���͍s��Ȃ��AonCallEnded�ōs����̂�
				if (call == null || ! call.isInCall()) {
					Log.i(TAG, "callEndTimer quit since call is not in call");
					loopEnd = false;
					break;
				}

				// �c�莞�Ԃ�����3���܂���1���ɂȂ�����A���[������炵�A�w�i�F��ς���
				if (restTime == AppConstants.FIRST_ALARM) {
					Log.d(TAG, "alarm play");
					soundPool.play(1, 1.0F, 1.0F, 0, 0, 1.0F);
					changeTimerTextBackgroundColor(Color.YELLOW);
				} else if (restTime == AppConstants.SECOND_ALARM) {
					Log.d(TAG, "alarm play");
					soundPool.play(1, 1.0F, 1.0F, 0, 0, 1.0F);
					changeTimerTextBackgroundColor(Color.RED);
				}

				// ��ʂ̎c�莞�ԕ\�����X�V����
				updateTimerText(restTime);

				// 1�b�X���[�v
				SystemClock.sleep(1000);
			}

			// �c�莞�Ԃ�0�ɂȂ������̏����A�d�b��؂��ă��C����ʂɖ߂�
			if (loopEnd) {
				Log.i(TAG, "callEndTimer timeover");
				// textView�̃��b�Z�[�W���w�ʘb���I�����܂����x�ɕς��āA�ʘb�ؒf�{�^�����B��
				showCallEndMessage();

				// �d�b��؂��ă��C����ʂɖ߂�
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
