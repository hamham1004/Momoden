package foo.bar.momoden;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import android.os.AsyncTask;
import android.util.Log;

/**
 * 割り当てられたDNを解放するhttpリクエストを送信するタスク
 */
public class ReleaseDnTask extends AsyncTask<Object, Void, Void> {

	// ログ出力用タグ名
	private final String TAG = "ReleaseDnTask";

	// 呼び出し元への戻り値、処理が終わるとキーendFlagに1が入る
	private HashMap<String, String> resultMap;

	/**
	 * 割り当てられたDNを解放するhttpリクエストを送信するタスクのコンストラクタ
	 * @param resultMap 処理結果を返すHashMap、キーresultにokかng、キーendFlagに処理終了時1が入る
	 */
	public ReleaseDnTask(HashMap<String, String> resultMap) {
        super();
        this.resultMap = resultMap;
	}

	// 割り当てられたDNを解放するhttpリクエスト送信処理
	// executeの引数1個目は送信先URL、引数2個目は解放するDN
	// httpへの送信はPOSTで、"dn"に解放するDNをセットして送る
	// httpからの応答はJSON形式で、{"result":"ok"}、または{"result":"ng"}
	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground start");

		// POSTリクエスト用のインスタンス生成
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST用のパラメータ
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("dn", (String) params[1]));
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

		// http通信しレスポンス情報取得
		try {
			HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
	        // HTTPレスポンスが正常な場合（受信電文は参照しない）
	        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            resultMap.put("result", "ok");
	        } else {
	            resultMap.put("result", "ng");
	            Log.e(TAG, "http statusCode = " + httpResponse.getStatusLine().getStatusCode());
	        }
		} catch (Exception e) {
			Log.e(TAG, "http error", e);
		} finally {
			defaultHttpClient.getConnectionManager().shutdown();
		}

		Log.i(TAG, "doInBackground end, dn=<" + (String)params[1] + ">, result=<" + resultMap.get("result") + ">");
        resultMap.put("endFlag", "1");
		return null;
	}

}
