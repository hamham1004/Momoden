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

	// ログ出力用タグ名
	private final String TAG = "MainActivity";

    // 非同期で相談したい人のDNを取得するためのタスクオブジェクト生成
	private MainActivity activity = this;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate start");
        super.onCreate(savedInstanceState);

        // 画面上部のタイトルを無しにする
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.main);

		// 既に保存されている推しメンを取得するため、プリファレンスを開く
		final SharedPreferences preferences = getSharedPreferences(AppConstants.PREFERENCE_FILE_NAME, MODE_PRIVATE);

        // 推しメン選択Spinner、プリファレンスに保存されている値を取得してセット
        Spinner memberSpinner = (Spinner)findViewById(R.id.member_spinner);
        // プリファレンスに保存されている値は、画面表示位置＋1になるので、1を引いてセット
        memberSpinner.setSelection(preferences.getInt("MEMBER", 0) - 1);
        // Spinnerのリスナー設定
        memberSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

        	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        		// 選択された推しメンを取得
        		String selectedMmeber = (String) parent.getSelectedItem();
        		Log.d(TAG, "selected member = " + selectedMmeber + ",position = " + position);

        		// 推しメンをプリファレンスに保存する
        		SharedPreferences.Editor editor = preferences.edit();
        		editor.putInt("MEMBER", position + 1);
        		editor.commit();
        	}

			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});

        // 相談したいボタンのリスナー設定
        Button callerButton = (Button)findViewById(R.id.caller_button);
        callerButton.setOnClickListener(new OnClickListener() {

        	public void onClick(View v) {
    	        Log.i(TAG, "CallerButtonOnClickListener start");
    	        // 相談したい人用の電話番号を取得し、次画面に進む
    	    	getDnAndStartActivity();
    		}

        });

        // twitterのツイートボタンを表示する
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
	 * 自分用の電話番号を取得し、次画面のActivityを呼び出す
	 */
	private void getDnAndStartActivity() {
		// プリファレンスから保存されている推しメンを取得する
		SharedPreferences preferences = getSharedPreferences(AppConstants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        int savedMember = preferences.getInt("MEMBER", 1);

        // GetDnTaskからの戻り値、キーdnに取得した電話番号が入る、処理が終わるとキーendFlagに1が入る
        HashMap<String, String> resultMap = new HashMap<String, String>();

        // 電話番号取得処理を呼び出す
        new GetDnTask(activity, resultMap).execute(AppConstants.GET_CALLER_DN_URL, savedMember);

        // 非同期呼び出しなので、終了フラグが立つまで待つ
        while (resultMap.containsKey("endFlag") == false) {
			SystemClock.sleep(1000);
		}
        Log.i(TAG, "GetCallerDnTask return, dn=<" + resultMap.get("dn") + ">");

		if (resultMap.containsKey("dn") == false) {
			// http通信がエラー
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
			dialog.setTitle("エラー");
			dialog.setMessage("サーバーに接続できません。インターネット接続が有効でないか、システムがメンテナンス中の場合があります。");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// OKボタンを押された時の処理は特になし
				}
			});
			dialog.show();
		} else if (resultMap.get("dn").equals("NG")) {
			// 空き番号がない
			AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
			dialog.setTitle("エラー");
			dialog.setMessage("混雑しているため、ただいまご利用になれません。しばらく待ってからご利用ください。");
			dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					// OKボタンを押された時の処理は特になし
				}
			});
			dialog.show();
		} else {
			// 正常に電話番号が取得できたので、通話するアクティビティを呼び出す
			Intent intent = new Intent(MainActivity.this, CallActivity.class);
			// 自分の電話番号
			intent.putExtra("thisDN", resultMap.get("dn"));
			// 相談したい人
			intent.putExtra("isCaller", true);  // ホントは不要だが受け側のCallActivityで参照している箇所が残っているので残してある
			startActivity(intent);
		}
	}

	@Override
    public void onResume() {
        Log.i(TAG, "onResume start");
        super.onResume();

        // ログに記録された推しメンを集計したポイントを取得して、表示する
        new GetPointTask(activity).execute(AppConstants.GET_POINT_URL);

	}

}
