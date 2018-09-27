package foo.bar.momoden;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 非同期で相談したい人のDNを取得するためのタスク
 */
public class GetDnTask extends AsyncTask<Object, Void, Void> {

	// ログ出力用タグ名
	private final String TAG = "GetCallerDnTask";

	// 呼び出し元のアクティビティ取得用（MainActivity）
//	private Activity activity;

	// 呼び出し元への戻り値、キーdnに取得した電話番号が入る、処理が終わるとキーendFlagに1が入る
	private HashMap<String, String> resultMap;

	// プログレスダイアログ
//	private ProgressDialog dialog;

    // コンストラクタ
	public GetDnTask(Activity activity, HashMap<String, String> resultMap) {
        super();
//        this.activity = activity;
        this.resultMap = resultMap;
	}

    // onPreExecuteメソッド(バックグラウンド処理前処理)
//	@Override
//	protected void onPreExecute() {
//		Log.i(TAG, "onPreExecute started");
//		super.onPreExecute();
//		// プログレスダイアログ表示
//		dialog = new ProgressDialog(activity);
//		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		dialog.setMessage("しばらくお待ちください");
//		dialog.setCancelable(true);
//		dialog.show();
//	}

	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground stard");

		// POSTリクエスト用のインスタンス生成
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST用のパラメータ
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("pass", String.valueOf((Integer)params[1])));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(paramList,HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UrlEncodedFormEntity error (UnsupportedEncodingException", e);
		}

		// httpのタイムアウトパラメータの設定
		HttpParams httpParams = new BasicHttpParams();
		// コネクション確率のタイムアウト
		httpParams.setIntParameter(AllClientPNames.CONNECTION_TIMEOUT, AppConstants.CONNECTION_TIMEOUT);
		// データ待ちのタイムアウト
		httpParams.setIntParameter(AllClientPNames.SO_TIMEOUT, AppConstants.SO_TIMEOUT);

		// クライアントオブジェクト生成
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParams);

        // HTTP通信しレスポンス情報取得
		try {
			HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
	        // HTTPレスポンスが正常な場合
	        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            // HTTPレスポンスのコンテンツを取得
	            HttpEntity entity = httpResponse.getEntity();
	            JSONObject json = new JSONObject(EntityUtils.toString(entity));
	            resultMap.put("dn", json.getString("dn"));
	        }
		} catch (Exception e) {
			Log.e(TAG, "http error", e);
		} finally {
			defaultHttpClient.getConnectionManager().shutdown();
		}

		Log.i(TAG, "doInBackground end, result=<" + resultMap.get("dn") + ">");
        resultMap.put("endFlag", "1");
		return null;
	}

    // onPostExecuteメソッド(バックグラウンド終了後処理)
//    @Override
//    protected void onPostExecute(Void result) {
//		Log.i(TAG, "onPostExecute started");
//    	super.onPostExecute(result);
//
//    	// プログレスダイアログ終了
//    	dialog.dismiss();
//    }

}
