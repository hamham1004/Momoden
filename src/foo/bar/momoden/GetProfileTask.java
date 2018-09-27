/**
 * 
 */
package foo.bar.momoden;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

/**
 * �񓯊��ő���̐����������擾����
 * execute�̈���1�ڂ�URL�A2�߂������������擾�����������DN
 */
public class GetProfileTask extends AsyncTask<Object, Void, Integer> {

	// ���O�o�͗p�^�O��
	private final String TAG = "GetProfileTask";

	// �Ăяo�����̃A�N�e�B�r�e�B�擾�p�iMainActivity�j
	private CallActivity activity;

    // �R���X�g���N�^
	public GetProfileTask(CallActivity activity) {
        super();
        this.activity = activity;
	}

	@Override
	protected Integer doInBackground(Object... params) {
		Log.i(TAG, "doInBackground stard");

		Integer selectedMember = -1;
		
		// POST���N�G�X�g�p�̃C���X�^���X����
		HttpPost httpPost = new HttpPost((String) params[0]);

		// POST�p�̃p�����[�^
		List<NameValuePair> paramList = new ArrayList<NameValuePair>();
		paramList.add(new BasicNameValuePair("dn", (String)params[1]));
		try {
			httpPost.setEntity(new UrlEncodedFormEntity(paramList,HTTP.UTF_8));
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "UrlEncodedFormEntity error (UnsupportedEncodingException)", e);
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
	            selectedMember = Integer.parseInt(json.getString("isUse"));
	        }
		} catch (Exception e) {
			Log.e(TAG, "http error", e);
		} finally {
			defaultHttpClient.getConnectionManager().shutdown();
		}

		// �ʏ��isUse��0�ł��邱�Ƃ͂��肦�Ȃ����A�����̃^�C�~���O��0�������ꍇ�Ɏ���onPostExectute��IndexOutOfBounds�ɂȂ�̂ŋ����I��1�ɕς��Ă���
		if (selectedMember == 0) {
			selectedMember = 1;
		}

		Log.i(TAG, "doInBackground end, dn=<" + params[1] + ">, result=<" + selectedMember + ">");
		return selectedMember;
	}

	protected void onPostExecute(Integer result) {
		Log.i(TAG, "onPostExecute start");
		
		// string.xml���烁���o�[�����擾
		String[] memberArray = activity.getResources().getStringArray(R.array.member_name);
		
		// ����̐��������ɂ������w�i�F���Z�b�g
		switch (result) {
		case 1:
			break;
		case 2:
			activity.memberText.setBackgroundColor(Color.RED);
			break;
		case 3:
			activity.memberText.setBackgroundColor(Color.YELLOW);
			break;
		case 4:
			activity.memberText.setBackgroundColor(Color.rgb(0xFF, 0x69, 0xb4));
			break;
		case 5:
			activity.memberText.setBackgroundColor(Color.GREEN);
			break;
		case 6:
			activity.memberText.setBackgroundColor(Color.rgb(0xc0, 0x60, 0xf0));
			break;
		default:
			break;
		}
		
		// �����������Z�b�g
		activity.memberText.setText(memberArray[result - 1]);

    }

}
