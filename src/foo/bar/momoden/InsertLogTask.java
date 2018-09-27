package foo.bar.momoden;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
 * 通話が始まったタイミングでログを書き込むタスク<br>
 * 引数1個目：String 送信先URL<br>
 * 引数2個め：String 自分の電話番号<br>
 * 引数3個目：int 自分の推しメンを表す番号、1：箱推し、2～6：各メンバー
 */
public class InsertLogTask extends AsyncTask<Object, Void, Void> {

	// ログ出力用タグ名
	private final String TAG = "InsertLogTask";

	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground start");

		// POSTリクエスト用のインスタンス生成
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST用のパラメータ
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("dn", (String) params[1]));
		paramList.add(new BasicNameValuePair("pass", String.valueOf((Integer)params[2])));
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
	            // レスポンスは返ってこないので何もしない
	        }
		} catch (Exception e) {
			Log.e(TAG, "http error", e);
		} finally {
			defaultHttpClient.getConnectionManager().shutdown();
		}

		Log.d(TAG, "doInBackground end, dn=<" + params[1] + ">, pass=<" + params[2] + ">");
		return null;
	}

}
