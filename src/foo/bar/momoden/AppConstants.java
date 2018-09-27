package foo.bar.momoden;

/**
 * �A�v���S�̗p�萔�̐ݒ�
 */
public final class AppConstants {

    private AppConstants() {
    }
    // SIP�T�[�o�[��IP�A�h���X�i�����{�ԁ����j
    public static final String SIPSERVER_IP = "ec2-xxx-xxx-xxx-xxx.ap-northeast-1.compute.amazonaws.com";
    // SIP�T�[�o�[��IP�A�h���X�i�e�X�g�p�j
//    public static final String SIPSERVER_IP = "ec2-xxx-xxx-xxx-xxx.ap-northeast-1.compute.amazonaws.com";

    // ���m�点�擾�pURL
    public static final String GET_INFORMATION_URL = "http://" + SIPSERVER_IP + "/info_momoden_1.1.php";

    // Caller�i���k�������l�j��DN�擾�pURL
    public static final String GET_CALLER_DN_URL = "http://" + SIPSERVER_IP + "/getdn_pool.php";

    // Caller�i���k�������l�j�Ɋ��蓖�Ă�ꂽDN�̉���pURL
    public static final String RELEASE_CALLER_DN_URL = "http://" + SIPSERVER_IP + "/releasedn_pool.php";

    // ����̐����������擾����URL
    public static final String GET_PROFILE_URL = "http://" + SIPSERVER_IP + "/getprofile_pool.php";

    // �����̐���������5000�Ԃɋ����I�ɏ�������URL
    public static final String PUT_PROFILE_URL = "http://" + SIPSERVER_IP + "/putprofile_pool.php";

    // �c�C�[�g�{�^����\�����邽�߂�URL
    public static final String TWEET_BUTTON_URL = "http://" + SIPSERVER_IP + "/momoden_tw.htm";

    // �ʘb���n�܂����^�C�~���O�Ń��O����������URL
    public static final String INSERT_LOG_URL = "http://" + SIPSERVER_IP + "/insert_log.php";

    // �������������L���O���擾����URL
    public static final String GET_POINT_URL = "http://" + SIPSERVER_IP + "/getpoint_log.php";

    // ���k�������l���������̓d�b�ԍ�
    public static final String CALLER_TARGET_DN = "5000";

    // HTTP�ʐM�̃R�l�N�V�����^�C���A�E�g�i�~���b�j
    public static final int CONNECTION_TIMEOUT = 10000;

    // HTTP�ʐM�̃f�[�^�҂��^�C���A�E�g�i�~���b�j
    public static final int SO_TIMEOUT = 10000;

    // �d�b�������āA�ʘb���n�܂�܂ő҂�������b���i�b�j
    public static final int CALL_WAIT_TIMER = 120;

    // �ʘb���I�����ACallActivity����MainActivity�ɖ߂�܂ł̎��ԁi�~���b�j
    public static final int AFTER_CALL_TIMER = 3000;

    // �ʘb�𑱂�����ő厞�ԁi�b�j
    public static final int CALL_END_TIMER = 180;

    // �ʘb�c�莞�Ԃ����̎��ԂɂȂ�����1��ڂ̃A���[����炷�i�b�j
	public static final int FIRST_ALARM = 60;

    // �ʘb�c�莞�Ԃ����̎��ԂɂȂ�����2��ڂ̃A���[����炷�i�b�j
	public static final int SECOND_ALARM = 10;

    // �����{�^�������������ɉ�������鎞�ԁi�b�j
	public static final int CALL_END_EXTEND_TIME = 180;

    // ����������ۑ�����v���t�@�����X�̃t�@�C����
    public static final String PREFERENCE_FILE_NAME = "momoden_preference";
}
