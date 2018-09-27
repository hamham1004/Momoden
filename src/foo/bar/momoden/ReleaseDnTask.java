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
 * ���蓖�Ă�ꂽDN���������http���N�G�X�g�𑗐M����^�X�N
 */
public class ReleaseDnTask extends AsyncTask<Object, Void, Void> {

	// ���O�o�͗p�^�O��
	private final String TAG = "ReleaseDnTask";

	// �Ăяo�����ւ̖߂�l�A�������I���ƃL�[endFlag��1������
	private HashMap<String, String> resultMap;

	/**
	 * ���蓖�Ă�ꂽDN���������http���N�G�X�g�𑗐M����^�X�N�̃R���X�g���N�^
	 * @param resultMap �������ʂ�Ԃ�HashMap�A�L�[result��ok��ng�A�L�[endFlag�ɏ����I����1������
	 */
	public ReleaseDnTask(HashMap<String, String> resultMap) {
        super();
        this.resultMap = resultMap;
	}

	// ���蓖�Ă�ꂽDN���������http���N�G�X�g���M����
	// execute�̈���1�ڂ͑��M��URL�A����2�ڂ͉������DN
	// http�ւ̑��M��POST�ŁA"dn"�ɉ������DN���Z�b�g���đ���
	// http����̉�����JSON�`���ŁA{"result":"ok"}�A�܂���{"result":"ng"}
	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground start");

		// POST���N�G�X�g�p�̃C���X�^���X����
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST�p�̃p�����[�^
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("dn", (String) params[1]));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(paramList,HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UrlEncodedFormEntity error (UnsupportedEncodingException", e);
		}

		// http�̃^�C���A�E�g�p�����[�^�̐ݒ�
		HttpParams httpParams = new BasicHttpParams();
		// �R�l�N�V�����m���̃^�C���A�E�g
		httpParams.setIntParameter(AllClientPNames.CONNECTION_TIMEOUT, AppConstants.CONNECTION_TIMEOUT);
		// �f�[�^�҂��̃^�C���A�E�g
		httpParams.setIntParameter(AllClientPNames.SO_TIMEOUT, AppConstants.SO_TIMEOUT);

		// �N���C�A���g�I�u�W�F�N�g����
		DefaultHttpClient defaultHttpClient = new DefaultHttpClient(httpParams);

		// http�ʐM�����X�|���X���擾
		try {
			HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
	        // HTTP���X�|���X������ȏꍇ�i��M�d���͎Q�Ƃ��Ȃ��j
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
