package co.coinfinity;

/**
 * @author Johannes Zweng on 14.08.18.
 */
public class AppConstants {

    /**
     * Log tag, used in logcat messages from this app
     */
    public static final String TAG = "Coinfineon";

    public static final String HTTPS = "https://";
    public static final String BASEURL = ".infura.io/v3/7b40d72779e541a498cb0da69aa418a2";
    public static final String ROPSTEN_TESTNET = HTTPS + "ropsten" + BASEURL;
    public static final String MAINNET = HTTPS + "mainnet" + BASEURL;

    public static final byte ROPSTEN_CHAIN_ID = 3;

}
