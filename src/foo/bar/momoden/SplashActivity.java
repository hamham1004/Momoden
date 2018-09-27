package foo.bar.momoden;

import java.util.HashMap;

import foo.bar.momoden.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Window;

public class SplashActivity extends Activity {

	// ���O�o�͗p�^�O��
	private final String TAG = "SplashActivity";

    // ���̃A�N�e�B�r�e�B���g
	private Activity activity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);

        // ��ʏ㕔�̃^�C�g���𖳂��ɂ���
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash);

		// 2�b��ɂ��m�点�̗L�����`�F�b�N������A���̉�ʂֈڂ�
        Handler handler = new Handler();
        handler.postDelayed(new SplashRunnable(), 2000);
    }

    /**
     * ���m�点�̗L�����`�F�b�N������A���̉�ʂ֑J�ڂ��鏈���BHandler��post���ČĂяo���B
     */
    private class SplashRunnable implements Runnable {

		public void run() {
			// �^�X�N����̖߂�l�A�������I���ƃL�[endFlag��1������
	        HashMap<String, String> resultMap = new HashMap<String, String>();

	        // ���m�点���b�Z�[�W���擾����
	        new GetInformationTask(resultMap).execute(AppConstants.GET_INFORMATION_URL);

	        // �񓯊��Ăяo���Ȃ̂ŁA�I���t���O�����܂ő҂�
	        while (resultMap.containsKey("endFlag") == false) {
				SystemClock.sleep(1000);
			}
	        Log.i(TAG, "GetInformationTask return, info=<" + resultMap.get("info") + ">");

	        if (resultMap.containsKey("info")) {

	        	// ���m�点�����݂���ꍇ�́A���m�点���_�C�A���O�ŕ\������
				AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
				dialog.setTitle("���m�点");
				dialog.setMessage(resultMap.get("info"));
				dialog.setCancelable(false);
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					// OK�{�^���������ꂽ��A���̉�ʂɈړ�����
					public void onClick(DialogInterface dialog, int which) {
						Intent intent = new Intent(SplashActivity.this, MainActivity.class);
						startActivity(intent);
						finish();
					}
				});
				if (! activity.isFinishing()) {
					dialog.show();
				}

	        } else {

	        	// ���m�点�͑��݂��Ȃ��ꍇ�́A�������̉�ʂɈړ�
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
	        }

		}

    }
}
