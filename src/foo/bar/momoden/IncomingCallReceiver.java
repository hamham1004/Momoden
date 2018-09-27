package foo.bar.momoden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.SystemClock;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver {

	// ���O�o�͗p�^�O��
	private final String TAG = "IncomingCallReceiver";

	// context�ɓ����ēn����Ă���e��ʂւ̎Q�Ƃ�����
    CallActivity callActivity;

    /**
     * �ʘb�����M������Ăяo����A�e��ʂ�SipManager�̒ʘb���J�n���鏈�����Ăяo���A�ʘb���n�߂�
     * �e��ʂ�CallActivity
     * @param context The context under which the receiver is running.
     * @param intent The intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "onReceive start");

    	SipAudioCall incomingCall = null;
        try {

            SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                	Log.i(TAG, "onRinging");
                	try {
                		call.answerCall(30);
                    } catch (Exception e) {
                    	Log.e(TAG, "call.answerCall error", e);
                    }
                }

                // �ʐM�m�����̏���
                @Override
                public void onCallEstablished(SipAudioCall call) {
                	Log.i(TAG, "onCallEstablished");
                }

                // �ʘb�I�����̏���
                @Override
                public void onCallEnded(SipAudioCall call) {
                	Log.i(TAG, "onCallEnded");

                	if (callActivity != null) {

                		// �ʘb�I�����̉���炷
                		callActivity.soundPool.play(3, 1.0F, 1.0F, 0, 0, 1.0F);

                		callActivity.showCallEndMessage();

        				// �d�b��؂��ă��C����ʂɖ߂�
        				callActivity.endCallAndBackToMainActivity();
                	}
                }

            };

            // �e��ʂւ̎Q�Ƃ��擾
            callActivity = (CallActivity) context;

            // ���̌Ă����M���Ȃ��悤�Ƀ��V�[�o�[���A�����W�X�g����
            callActivity.unregisterReceiver(callActivity.callReceiver);
            callActivity.callReceiver = null;

            // �����Ɏ��Ƃ����������т����肷��̂ŁA0.5�b�҂�
            SystemClock.sleep(500);

            // ���M�������Ƃ�`���鉹��炷
            callActivity.soundPool.play(2, 1.0F, 1.0F, 0, 0, 1.0F);

            // ���M�҂��̃_�C�A���O������
            callActivity.dismissProgressDialog();

            // ���M�����ʘb�����
            incomingCall = callActivity.manager.takeAudioCall(intent, listener);
            callActivity.call = incomingCall;
            incomingCall.answerCall(30);

            // �����ʘb���J�n
            incomingCall.startAudio();

            // �X�s�[�J�[���[�h��OFF�ɂ���
			incomingCall.setSpeakerMode(false);

            // �~���[�Ƃ���Ă������������
            if(incomingCall.isMuted()) {
                incomingCall.toggleMute();
            }

            // textView�̃��b�Z�[�W��ʘb���ɕς���
        	callActivity.showCallStartMessage();

        	// ����̐����������擾���A��ʂɕ\������
        	new GetProfileTask(callActivity).execute(AppConstants.GET_PROFILE_URL, callActivity.call.getPeerProfile().getUserName());

        	// �ʘb���O����������
        	new InsertLogTask().execute(AppConstants.INSERT_LOG_URL, callActivity.thisDN, callActivity.getMyFavoriteMember());

        	// �ʘb���ԃ^�C�}�[���J�n����
        	callActivity.callEndTimer();

        } catch (Exception e) {
        	Log.e(TAG, "takeAudioCall or answerCall error", e);
            if (incomingCall != null) {
                incomingCall.close();
            }
        }
    }

}
