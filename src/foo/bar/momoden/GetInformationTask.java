package foo.bar.momoden;

import java.util.HashMap;

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

import android.os.AsyncTask;
import android.util.Log;

/**
 * �񓯊��ł��m�点��Web�T�[�o����擾���邽�߂̃^�X�N
 */
public class GetInformationTask extends AsyncTask<Object, Void, Void> {

	// ���O�o�͗p�^�O��
	private final String TAG = "GetInformationTask";

	// �Ăяo�����ւ̖߂�l�A�L�[info�Ɏ擾�������m�点������A�������I���ƃL�[endFlag��1������
	private HashMap<String, String> resultMap;

    // �R���X�g���N�^
	public GetInformationTask(HashMap<String, String> resultMap) {
        super();
		this.resultMap = resultMap;
	}

	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground start");

		// GET�ʐM�p�I�u�W�F�N�g����
		HttpUriRequest request = new HttpGet((String) params[0]);

		// http�̃^�C���A�E�g�p�����[�^�̐ݒ�
		HttpParams httpParams = new BasicHttpParams();
		// �R�l�N�V�����m���̃^�C���A�E�g
		httpParams.setIntParameter(AllClientPNames.CONNECTION_TIMEOUT, AppConstants.CONNECTION_TIMEOUT);
		// �f�[�^�҂��̃^�C���A�E�g
		httpParams.setIntParameter(AllClientPNames.SO_TIMEOUT, AppConstants.SO_TIMEOUT);

		// �N���C�A���g�C���X�^���X�쐬
		DefaultHttpClient client = new DefaultHttpClient(httpParams);

        // HTTP�ʐM�����X�|���X���擾
		try {
			HttpResponse response = client.execute(request);
	        // HTTP���X�|���X������ȏꍇ
	        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            // HTTP���X�|���X�̃R���e���c���擾
	            HttpEntity entity = response.getEntity();
	            JSONObject json = new JSONObject(EntityUtils.toString(entity));
	            resultMap.put("info", json.getString("info"));
	        }
		} catch (Exception e) {
			Log.e(TAG, "http error", e);
		} finally {
			client.getConnectionManager().shutdown();
		}

		resultMap.put("endFlag", "1");
		return null;
	}

}
