package foo.bar.momoden;

/**
 * アプリ全体用定数の設定
 */
public final class AppConstants {

    private AppConstants() {
    }
    // SIPサーバーのIPアドレス（★★本番★★）
    public static final String SIPSERVER_IP = "ec2-xxx-xxx-xxx-xxx.ap-northeast-1.compute.amazonaws.com";
    // SIPサーバーのIPアドレス（テスト用）
//    public static final String SIPSERVER_IP = "ec2-xxx-xxx-xxx-xxx.ap-northeast-1.compute.amazonaws.com";

    // お知らせ取得用URL
    public static final String GET_INFORMATION_URL = "http://" + SIPSERVER_IP + "/info_momoden_1.1.php";

    // Caller（相談したい人）のDN取得用URL
    public static final String GET_CALLER_DN_URL = "http://" + SIPSERVER_IP + "/getdn_pool.php";

    // Caller（相談したい人）に割り当てられたDNの解放用URL
    public static final String RELEASE_CALLER_DN_URL = "http://" + SIPSERVER_IP + "/releasedn_pool.php";

    // 相手の推しメンを取得するURL
    public static final String GET_PROFILE_URL = "http://" + SIPSERVER_IP + "/getprofile_pool.php";

    // 自分の推しメンを5000番に強制的に書き込むURL
    public static final String PUT_PROFILE_URL = "http://" + SIPSERVER_IP + "/putprofile_pool.php";

    // ツイートボタンを表示するためのURL
    public static final String TWEET_BUTTON_URL = "http://" + SIPSERVER_IP + "/momoden_tw.htm";

    // 通話が始まったタイミングでログを書き込むURL
    public static final String INSERT_LOG_URL = "http://" + SIPSERVER_IP + "/insert_log.php";

    // 推しメンランキングを取得するURL
    public static final String GET_POINT_URL = "http://" + SIPSERVER_IP + "/getpoint_log.php";

    // 相談したい人がかける先の電話番号
    public static final String CALLER_TARGET_DN = "5000";

    // HTTP通信のコネクションタイムアウト（ミリ秒）
    public static final int CONNECTION_TIMEOUT = 10000;

    // HTTP通信のデータ待ちタイムアウト（ミリ秒）
    public static final int SO_TIMEOUT = 10000;

    // 電話をかけて、通話が始まるまで待ち続ける秒数（秒）
    public static final int CALL_WAIT_TIMER = 120;

    // 通話が終了し、CallActivityからMainActivityに戻るまでの時間（ミリ秒）
    public static final int AFTER_CALL_TIMER = 3000;

    // 通話を続けられる最大時間（秒）
    public static final int CALL_END_TIMER = 180;

    // 通話残り時間がこの時間になったら1回目のアラームを鳴らす（秒）
	public static final int FIRST_ALARM = 60;

    // 通話残り時間がこの時間になったら2回目のアラームを鳴らす（秒）
	public static final int SECOND_ALARM = 10;

    // 延長ボタンを押した時に延長される時間（秒）
	public static final int CALL_END_EXTEND_TIME = 180;

    // 推しメンを保存するプリファレンスのファイル名
    public static final String PREFERENCE_FILE_NAME = "momoden_preference";
}
