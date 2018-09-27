/**
 *
 */
package foo.bar.momoden;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

/**
 * 通話が成立した時にログに記録された推しメンを集計したポイントを取得する
 * executeの引数1個目がURL
 */
public class GetPointTask extends AsyncTask<String, Void, HashMap<Integer, Integer>> {

	// ログ出力用タグ名
	private final String TAG = "GetPointTask";

	// 呼び出し元のアクティビティ取得用（MainActivity）
	private MainActivity activity;

    // コンストラクタ
	public GetPointTask(MainActivity activity) {
        super();
        this.activity = activity;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	protected HashMap<Integer, Integer> doInBackground(String... params) {
		Log.i(TAG, "doInBackground stard");

		// 取得した各メンバーごとのポイントを入れる、keyが推しメン、valueがポイント、これをonPostExecuteに渡す
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		// GET通信用オブジェクト生成
		HttpUriRequest request = new HttpGet(params[0]);

		// httpのタイムアウトパラメータの設定
		HttpParams httpParams = new BasicHttpParams();
		// コネクション確率のタイムアウト
		httpParams.setIntParameter(AllClientPNames.CONNECTION_TIMEOUT, AppConstants.CONNECTION_TIMEOUT);
		// データ待ちのタイムアウト
		httpParams.setIntParameter(AllClientPNames.SO_TIMEOUT, AppConstants.SO_TIMEOUT);

		// クライアントオブジェクト生成
		DefaultHttpClient client = new DefaultHttpClient(httpParams);

        // HTTP通信しレスポンス情報取得
		try {
			HttpResponse response = client.execute(request);
	        // HTTPレスポンスが正常な場合
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            // HTTPレスポンスのコンテンツを取得
	            HttpEntity entity = response.getEntity();
	            JSONObject json = new JSONObject(EntityUtils.toString(entity));
	            map.put(0, Integer.parseInt(json.getString("person1")));
	            map.put(1, Integer.parseInt(json.getString("person2")));
	            map.put(2, Integer.parseInt(json.getString("person3")));
	            map.put(3, Integer.parseInt(json.getString("person4")));
	            map.put(4, Integer.parseInt(json.getString("person5")));
	            map.put(5, Integer.parseInt(json.getString("person6")));
	        }
		} catch (Exception e) {
			Log.e(TAG, "http error", e);
		} finally {
			client.getConnectionManager().shutdown();
		}

		Log.i(TAG, "doInBackground end, map=<" + map.entrySet());
		return map;
	}

	protected void onPostExecute(HashMap<Integer, Integer> map) {
		Log.i(TAG, "onPostExecute start");

		// httpリクエストがタイムアウト等で失敗している場合はmapが空なので、描画処理は行わずリターン
		if (map.isEmpty()) {
			return;
		}

		// map内の要素をkeyに入っているポイント順に降順でソートする
		ArrayList<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer,Integer>>(map.entrySet());

		Collections.sort(list, new Comparator<Object>() {
			@SuppressWarnings("unchecked")
			public int compare(Object o1, Object o2) {
				Map.Entry<Integer, Integer> e1 = (Map.Entry<Integer, Integer>) o1;
				Map.Entry<Integer, Integer> e2 = (Map.Entry<Integer, Integer>) o2;
				Integer e1value = e1.getValue();
				Integer e2value = e2.getValue();
				return (e2value.compareTo(e1value));
			}
		});
		for (Map.Entry<Integer, Integer> entry:list) {
			Log.d(TAG, entry.getKey().toString());
		}

		// string.xmlからメンバー名を取得、この値がmapのkeyに入る
		String[] memberArray = activity.getResources().getStringArray(R.array.member_name);

		// 画面にメンバー名とポイントを表示する
		TextView rank1Name = (TextView)activity.findViewById(R.id.rank_1_name);
		rank1Name.setText(memberArray[list.get(0).getKey()]);
		setBackgroudColor(list.get(0).getKey(), rank1Name);
		TextView rank1Point = (TextView)activity.findViewById(R.id.rank_1_point);
		rank1Point.setText(list.get(0).getValue().toString() + "pt");

		TextView rank2Name = (TextView)activity.findViewById(R.id.rank_2_name);
		rank2Name.setText(memberArray[list.get(1).getKey()]);
		setBackgroudColor(list.get(1).getKey(), rank2Name);
		TextView rank2Point = (TextView)activity.findViewById(R.id.rank_2_point);
		rank2Point.setText(list.get(1).getValue().toString() + "pt");

		TextView rank3Name = (TextView)activity.findViewById(R.id.rank_3_name);
		rank3Name.setText(memberArray[list.get(2).getKey()]);
		setBackgroudColor(list.get(2).getKey(), rank3Name);
		TextView rank3Point = (TextView)activity.findViewById(R.id.rank_3_point);
		rank3Point.setText(list.get(2).getValue().toString() + "pt");

    }

	/**
	 * メンバーにあった背景色をセットする
	 * @param member メンバーを表す番号、0:箱推し、1～5：各メンバーに対応
	 * @param view 背景色を変える対象のTextView
	 */
	private void setBackgroudColor(int member, TextView view) {
		switch (member) {
		case 0:
			view.setBackgroundColor(Color.rgb(0xFF, 0xd5, 0xea));
			break;
		case 1:
			view.setBackgroundColor(Color.RED);
			break;
		case 2:
			view.setBackgroundColor(Color.YELLOW);
			break;
		case 3:
			view.setBackgroundColor(Color.rgb(0xFF, 0x69, 0xb4));
			break;
		case 4:
			view.setBackgroundColor(Color.GREEN);
			break;
		case 5:
			view.setBackgroundColor(Color.rgb(0xc0, 0x60, 0xf0));
			break;
		default:
			break;
		}
	}

}
