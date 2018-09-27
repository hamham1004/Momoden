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

	// ログ出力用タグ名
	private final String TAG = "SplashActivity";

    // このアクティビティ自身
	private Activity activity = this;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);

        // 画面上部のタイトルを無しにする
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.splash);

		// 2秒後にお知らせの有無をチェックした後、次の画面へ移る
        Handler handler = new Handler();
        handler.postDelayed(new SplashRunnable(), 2000);
    }

    /**
     * お知らせの有無をチェックした後、次の画面へ遷移する処理。Handlerにpostして呼び出す。
     */
    private class SplashRunnable implements Runnable {

		public void run() {
			// タスクからの戻り値、処理が終わるとキーendFlagに1が入る
	        HashMap<String, String> resultMap = new HashMap<String, String>();

	        // お知らせメッセージを取得する
	        new GetInformationTask(resultMap).execute(AppConstants.GET_INFORMATION_URL);

	        // 非同期呼び出しなので、終了フラグが立つまで待つ
	        while (resultMap.containsKey("endFlag") == false) {
				SystemClock.sleep(1000);
			}
	        Log.i(TAG, "GetInformationTask return, info=<" + resultMap.get("info") + ">");

	        if (resultMap.containsKey("info")) {

	        	// お知らせが存在する場合は、お知らせをダイアログで表示する
				AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
				dialog.setTitle("お知らせ");
				dialog.setMessage(resultMap.get("info"));
				dialog.setCancelable(false);
				dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					// OKボタンが押されたら、次の画面に移動する
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

	        	// お知らせは存在しない場合は、すぐ次の画面に移動
				Intent intent = new Intent(SplashActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
	        }

		}

    }
}
