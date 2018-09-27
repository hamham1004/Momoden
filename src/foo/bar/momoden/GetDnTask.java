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
 * �񓯊��ő��k�������l��DN���擾���邽�߂̃^�X�N
 */
public class GetDnTask extends AsyncTask<Object, Void, Void> {

	// ���O�o�͗p�^�O��
	private final String TAG = "GetCallerDnTask";

	// �Ăяo�����̃A�N�e�B�r�e�B�擾�p�iMainActivity�j
//	private Activity activity;

	// �Ăяo�����ւ̖߂�l�A�L�[dn�Ɏ擾�����d�b�ԍ�������A�������I���ƃL�[endFlag��1������
	private HashMap<String, String> resultMap;

	// �v���O���X�_�C�A���O
//	private ProgressDialog dialog;

    // �R���X�g���N�^
	public GetDnTask(Activity activity, HashMap<String, String> resultMap) {
        super();
//        this.activity = activity;
        this.resultMap = resultMap;
	}

    // onPreExecute���\�b�h(�o�b�N�O���E���h�����O����)
//	@Override
//	protected void onPreExecute() {
//		Log.i(TAG, "onPreExecute started");
//		super.onPreExecute();
//		// �v���O���X�_�C�A���O�\��
//		dialog = new ProgressDialog(activity);
//		dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
//		dialog.setMessage("���΂炭���҂���������");
//		dialog.setCancelable(true);
//		dialog.show();
//	}

	@Override
	protected Void doInBackground(Object... params) {
		Log.i(TAG, "doInBackground stard");

		// POST���N�G�X�g�p�̃C���X�^���X����
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST�p�̃p�����[�^
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("pass", String.valueOf((Integer)params[1])));
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
	            // HTTP���X�|���X�̃R���e���c���擾
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

    // onPostExecute���\�b�h(�o�b�N�O���E���h�I���㏈��)
//    @Override
//    protected void onPostExecute(Void result) {
//		Log.i(TAG, "onPostExecute started");
//    	super.onPostExecute(result);
//
//    	// �v���O���X�_�C�A���O�I��
//    	dialog.dismiss();
//    }

}
