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
    /* WARNING: please make sure how much private/pub key pairs are already generated on the card and increase max +1
                otherwise this index is wrong because the card will generate another lower index.
                e.g.: card has index 0x00 not more, you set this value to 0x02, but the card will generate next free 0x01.
                      so generate pub key is actually 0x01 but you will use 0x02 as const for signing keys and so on.*/
    public static final int KEY_ID_ON_THE_CARD = 0x00;
    private static final String HTTPS = "https://";
    private static final String BASEURL = ".infura.io/v3/7b40d72779e541a498cb0da69aa418a2";
    private static final String MAINNET = "mainnet";
    private static final String ROPSTEN = "ropsten";
    public static final String CHAIN_URL = HTTPS + ROPSTEN + BASEURL;
    public static final byte CHAIN_ID = ChainId.ROPSTEN;
    private static final String ETHEUR = "ETHEUR";
    public static final String HTTPS_COINFINITY_CO_PRICE_XBTEUR = "https://coinfinity.co/price/" + ETHEUR;
    public static final int SLEEP_BETWEEN_LOOPS_MILLIS = 10000;

    // preferences
    public static final String PREFERENCE_FILENAME = "CoinfineonPrefs";

    // used for all 3 activities:
    public static final String PREF_KEY_GASPRICE_WEI = "gasPriceInWei";

    // send eth
    public static final String PREF_KEY_RECIPIENT_ADDRESS = "recipientAddressTxt";
    public static final String PREF_KEY_GASLIMIT_SEND_ETH = "gasLimitSendEth";

    // send erc 20
    public static final String PREF_KEY_ERC20_CONTRACT_ADDRESS = "erc20ContractAddress";
    public static final String PREF_KEY_ERC20_RECIPIENT_ADDRESS = "erc20RecipientAddress";
    public static final String PREF_KEY_ERC20_AMOUNT = "erc20TokenAmount";
    public static final String PREF_KEY_ERC20_GASLIMIT = "erc20GasLimit";

    // voting
    public static final String PREF_KEY_VOTING_GASLIMIT = "erc20GasLimit";
    public static final String PREF_KEY_VOTING_CONTRACT_ADDRESS = "votingContractAddress";
}
