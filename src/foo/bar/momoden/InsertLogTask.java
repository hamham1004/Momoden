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
 * �ʘb���n�܂����^�C�~���O�Ń��O���������ރ^�X�N<br>
 * ����1�ځFString ���M��URL<br>
 * ����2�߁FString �����̓d�b�ԍ�<br>
 * ����3�ځFint �����̐���������\���ԍ��A1�F�������A2�`6�F�e�����o�[
 */
public class InsertLogTask extends AsyncTask<Object, Void, Void> {

	// ���O�o�͗p�^�O��
	private final String TAG = "InsertLogTask";

	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground start");

		// POST���N�G�X�g�p�̃C���X�^���X����
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST�p�̃p�����[�^
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("dn", (String) params[1]));
		paramList.add(new BasicNameValuePair("pass", String.valueOf((Integer)params[2])));
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

        // HTTP�ʐM�����X�|���X���擾
		try {
			HttpResponse httpResponse = defaultHttpClient.execute(httpPost);
	        // HTTP���X�|���X������ȏꍇ
	        if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	            // ���X�|���X�͕Ԃ��Ă��Ȃ��̂ŉ������Ȃ�
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
