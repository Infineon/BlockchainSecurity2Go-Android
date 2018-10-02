package co.coinfinity;

import org.web3j.tx.ChainId;

/**
 * Common app constants
 */
public class AppConstants {

    /**
     * Log tag, used in logcat messages from this app
     */
    public static final String TAG = "Coinfineon";

    public static final String ASK = "ask";
    public static final int KEY_ID_ON_THE_CARD = 0x00;
    private static final String HTTPS = "https://";
    private static final String BASEURL = ".infura.io/v3/7b40d72779e541a498cb0da69aa418a2";
    private static final String MAINNET = "mainnet";
    private static final String ROPSTEN = "ropsten";
    public static final String CHAIN_URL = HTTPS + ROPSTEN + BASEURL;
    public static final byte CHAIN_ID = ChainId.ROPSTEN;
    private static final String ETHEUR = "ETHEUR";
    public static final String HTTPS_COINFINITY_CO_PRICE_XBTEUR = "https://coinfinity.co/price/" + ETHEUR;
    public static final String PREFERENCE_FILENAME = "CoinfineonPrefs";
    public static final int SLEEP_BETWEEN_LOOPS_MILLIS = 10000;
}
