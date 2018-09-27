package foo.bar.momoden;

import java.util.HashMap;
import foo.bar.momoden.R;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;

@SuppressLint("SetJavaScriptEnabled")
public class MainActivity extends Activity {

	// ���O�o�͗p�^�O��
	private final String TAG = "MainActivity";

    // �񓯊��ő��k�������l��DN���擾���邽�߂̃^�X�N�I�u�W�F�N�g����
	private MainActivity activity = this;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);

        // ��ʏ㕔�̃^�C�g���𖳂��ɂ���
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

		// ���ɕۑ�����Ă��鐄���������擾���邽�߁A�v���t�@�����X���J��
		final SharedPreferences preferences = getSharedPreferences(AppConstants.PREFERENCE_FILE_NAME, MODE_PRIVATE);

        // ���������I��Spinner�A�v���t�@�����X�ɕۑ�����Ă���l���擾���ăZ�b�g
        Spinner memberSpinner = (Spinner)findViewById(R.id.member_spinner);
        // �v���t�@�����X�ɕۑ�����Ă���l�́A��ʕ\���ʒu�{1�ɂȂ�̂ŁA1�������ăZ�b�g
        memberSpinner.setSelection(preferences.getInt("MEMBER", 0) - 1);
        // Spinner�̃��X�i�[�ݒ�
        memberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		// �I�����ꂽ�����������擾
        		String selectedMmeber = (String) parent.getSelectedItem();
        		Log.d(TAG, "selected member = " + selectedMmeber + ",position = " + position);

        		// �����������v���t�@�����X�ɕۑ�����
        		SharedPreferences.Editor editor = preferences.edit();
        		editor.putInt("MEMBER", position + 1);
        		editor.commit();
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

        // ���k�������{�^���̃��X�i�[�ݒ�
        Button callerButton = (Button)findViewById(R.id.caller_button);
        callerButton.setOnClickListener(new OnClickListener() {

        	public void onClick(View v) {
    	        Log.i(TAG, "CallerButtonOnClickListener start");
    	        // ���k�������l�p�̓d�b�ԍ����擾���A����ʂɐi��
    	    	getDnAndStartActivity();
    		}

        });

        // twitter�̃c�C�[�g�{�^����\������
        WebView webView = (WebView)findViewById(R.id.webview1);
        webView.setBackgroundColor(Color.TRANSPARENT);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        webView.loadUrl(AppConstants.TWEET_BUTTON_URL);
        webView.setHorizontalScrollBarEnabled(false);
        webView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return(event.getAction() == MotionEvent.ACTION_MOVE);
			}
        });

    }

	/**
	 * �����p�̓d�b�ԍ����擾���A����ʂ�Activity���Ăяo��
	 */
	private void getDnAndStartActivity() {
		// �v���t�@�����X����ۑ�����Ă��鐄���������擾����
		SharedPreferences preferences = getSharedPreferences(AppConstants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        int savedMember = preferences.getInt("MEMBER", 1);

        // GetDnTask����̖߂�l�A�L�[dn�Ɏ擾�����d�b�ԍ�������A�������I���ƃL�[endFlag��1������
        HashMap<String, String> resultMap = new HashMap<String, String>();

        // �d�b�ԍ��擾�������Ăяo��
        new GetDnTask(activity, resultMap).execute(AppConstants.GET_CALLER_DN_URL, savedMember);

        // �񓯊��Ăяo���Ȃ̂ŁA�I���t���O�����܂ő҂�
        while (resultMap.containsKey("endFlag") == false) {
			SystemClock.sleep(1000);
		}
        Log.i(TAG, "GetCallerDnTask return, dn=<" + resultMap.get("dn") + ">");

		if (resultMap.containsKey("dn") == false) {
			// http�ʐM���G���[
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
			dialog.setTitle("�G���[");
			dialog.setMessage("�T�[�o�[�ɐڑ��ł��܂���B�C���^�[�l�b�g�ڑ����L���łȂ����A�V�X�e���������e�i���X���̏ꍇ������܂��B");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// OK�{�^���������ꂽ���̏����͓��ɂȂ�
				}
			});
			dialog.show();
		} else if (resultMap.get("dn").equals("NG")) {
			// �󂫔ԍ����Ȃ�
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
			dialog.setTitle("�G���[");
			dialog.setMessage("���G���Ă��邽�߁A�������܂����p�ɂȂ�܂���B���΂炭�҂��Ă��炲���p���������B");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// OK�{�^���������ꂽ���̏����͓��ɂȂ�
				}
			});
			dialog.show();
		} else {
			// ����ɓd�b�ԍ����擾�ł����̂ŁA�ʘb����A�N�e�B�r�e�B���Ăяo��
			Intent intent = new Intent(MainActivity.this, CallActivity.class);
			// �����̓d�b�ԍ�
			intent.putExtra("thisDN", resultMap.get("dn"));
			// ���k�������l
			intent.putExtra("isCaller", true);  // �z���g�͕s�v�����󂯑���CallActivity�ŎQ�Ƃ��Ă���ӏ����c���Ă���̂Ŏc���Ă���
			startActivity(intent);
		}
	}

	@Override
    public void onResume() {
        Log.i(TAG, "onResume start");
        super.onResume();

        // ���O�ɋL�^���ꂽ�����������W�v�����|�C���g���擾���āA�\������
        new GetPointTask(activity).execute(AppConstants.GET_POINT_URL);

	}

}
