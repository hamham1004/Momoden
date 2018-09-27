package foo.bar.momoden;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipProfile;
import android.os.SystemClock;
import android.util.Log;

public class IncomingCallReceiver extends BroadcastReceiver {

	// ログ出力用タグ名
	private final String TAG = "IncomingCallReceiver";

	// contextに入って渡されてくる親画面への参照を入れる
    CallActivity callActivity;

    /**
     * 通話が着信したら呼び出され、親画面のSipManagerの通話を開始する処理を呼び出し、通話を始める
     * 親画面はCallActivity
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

                // 通信確立時の処理
                @Override
                public void onCallEstablished(SipAudioCall call) {
                	Log.i(TAG, "onCallEstablished");
                }

                // 通話終了時の処理
                @Override
                public void onCallEnded(SipAudioCall call) {
                	Log.i(TAG, "onCallEnded");

                	if (callActivity != null) {

                		// 通話終了時の音を鳴らす
                		callActivity.soundPool.play(3, 1.0F, 1.0F, 0, 0, 1.0F);

                		callActivity.showCallEndMessage();

        				// 電話を切ってメイン画面に戻る
        				callActivity.endCallAndBackToMainActivity();
                	}
                }

            };

            // 親画面への参照を取得
            callActivity = (CallActivity) context;

            // 次の呼が着信しないようにレシーバーをアンレジストする
            callActivity.unregisterReceiver(callActivity.callReceiver);
            callActivity.callReceiver = null;

            // すぐに取るとかけた側がびっくりするので、0.5秒待つ
            SystemClock.sleep(500);

            // 着信したことを伝える音を鳴らす
            callActivity.soundPool.play(2, 1.0F, 1.0F, 0, 0, 1.0F);

            // 着信待ちのダイアログを消す
            callActivity.dismissProgressDialog();

            // 着信した通話を取る
            incomingCall = callActivity.manager.takeAudioCall(intent, listener);
            callActivity.call = incomingCall;
            incomingCall.answerCall(30);

            // 音声通話を開始
            incomingCall.startAudio();

            // スピーカーモードをOFFにする
			incomingCall.setSpeakerMode(false);

            // ミューとされていたら解除する
            if(incomingCall.isMuted()) {
                incomingCall.toggleMute();
            }

            // textViewのメッセージを通話中に変える
        	callActivity.showCallStartMessage();

        	// 相手の推しメンを取得し、画面に表示する
        	new GetProfileTask(callActivity).execute(AppConstants.GET_PROFILE_URL, callActivity.call.getPeerProfile().getUserName());

        	// 通話ログを書き込む
        	new InsertLogTask().execute(AppConstants.INSERT_LOG_URL, callActivity.thisDN, callActivity.getMyFavoriteMember());

        	// 通話時間タイマーを開始する
        	callActivity.callEndTimer();

        } catch (Exception e) {
        	Log.e(TAG, "takeAudioCall or answerCall error", e);
            if (incomingCall != null) {
                incomingCall.close();
            }
        }
    }

}
