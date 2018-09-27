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
 * �ʘb�������������Ƀ��O�ɋL�^���ꂽ�����������W�v�����|�C���g���擾����
 * execute�̈���1�ڂ�URL
 */
public class GetPointTask extends AsyncTask<String, Void, HashMap<Integer, Integer>> {

	// ���O�o�͗p�^�O��
	private final String TAG = "GetPointTask";

	// �Ăяo�����̃A�N�e�B�r�e�B�擾�p�iMainActivity�j
	private MainActivity activity;

    // �R���X�g���N�^
	public GetPointTask(MainActivity activity) {
        super();
        this.activity = activity;
	}

	@SuppressLint("UseSparseArrays")
	@Override
	protected HashMap<Integer, Integer> doInBackground(String... params) {
		Log.i(TAG, "doInBackground stard");

		// �擾�����e�����o�[���Ƃ̃|�C���g������Akey�����������Avalue���|�C���g�A�����onPostExecute�ɓn��
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

		// GET�ʐM�p�I�u�W�F�N�g����
		HttpUriRequest request = new HttpGet(params[0]);

		// http�̃^�C���A�E�g�p�����[�^�̐ݒ�
		HttpParams httpParams = new BasicHttpParams();
		// �R�l�N�V�����m���̃^�C���A�E�g
		httpParams.setIntParameter(AllClientPNames.CONNECTION_TIMEOUT, AppConstants.CONNECTION_TIMEOUT);
		// �f�[�^�҂��̃^�C���A�E�g
		httpParams.setIntParameter(AllClientPNames.SO_TIMEOUT, AppConstants.SO_TIMEOUT);

		// �N���C�A���g�I�u�W�F�N�g����
		DefaultHttpClient client = new DefaultHttpClient(httpParams);

        // HTTP�ʐM�����X�|���X���擾
		try {
			HttpResponse response = client.execute(request);
	        // HTTP���X�|���X������ȏꍇ
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            // HTTP���X�|���X�̃R���e���c���擾
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

		// http���N�G�X�g���^�C���A�E�g���Ŏ��s���Ă���ꍇ��map����Ȃ̂ŁA�`�揈���͍s�킸���^�[��
		if (map.isEmpty()) {
			return;
		}

		// map���̗v�f��key�ɓ����Ă���|�C���g���ɍ~���Ń\�[�g����
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

		// string.xml���烁���o�[�����擾�A���̒l��map��key�ɓ���
		String[] memberArray = activity.getResources().getStringArray(R.array.member_name);

		// ��ʂɃ����o�[���ƃ|�C���g��\������
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
	 * �����o�[�ɂ������w�i�F���Z�b�g����
	 * @param member �����o�[��\���ԍ��A0:�������A1�`5�F�e�����o�[�ɑΉ�
	 * @param view �w�i�F��ς���Ώۂ�TextView
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
