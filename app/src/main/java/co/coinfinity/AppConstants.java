package co.coinfinity;

/**
 * Class used for common app constants.
 */
public class AppConstants {

    /**
     * Log tag, used in logcat messages from this app.
     */
    public static final String TAG = "Coinfineon";

    public static final String ASK = "ask";
    private static final String HTTPS = "https://";
    private static final String BASEURL = ".infura.io/v3/7b40d72779e541a498cb0da69aa418a2";
    public static final String MAINNET_URI = HTTPS + "mainnet" + BASEURL;
    public static final String ROPSTEN_URI = HTTPS + "ropsten" + BASEURL;
    private static final String ETHEUR = "ETHEUR";
    private static final String COINFINITY_BASE_URL = "https://coinfinity.co";
    public static final String HTTPS_COINFINITY_CO_PRICE_XBTEUR = COINFINITY_BASE_URL + "/price/" + ETHEUR;
    public static final int TEN_SECONDS = 10;
    public static final int FIVE_SECONDS = 5;
    //if sig counter below this value warning is shown to user
    public static final int WARNING_SIG_COUNTER = 100;

    // preferences
    public static final String PREFERENCE_FILENAME = "CoinfineonPrefs";

    // used for all activities:
    public static final String PREF_KEY_GASPRICE_WEI = "gasPriceInWei";
    public static final String PREF_KEY_MAIN_NETWORK = "mainNetwork";
    public static final String PREF_KEY_PIN = "pin";

    public static final String DEFAULT_GASPRICE_IN_GIGAWEI = "15";

    public static final String KEY_INDEX_OF_CARD = "keyIndexOfCard";

    // send eth
    public static final String PREF_KEY_RECIPIENT_ADDRESS = "recipientAddressTxt";
    public static final String PREF_KEY_GASLIMIT_SEND_ETH = "gasLimitSendEth";

    // send erc 20
    // default contract: coinfinity CFTEST Tokens: https://etherscan.io/token/0xfb09d466e7fe677439d695241d6327ddd8153848
    public static final String DEFAULT_ERC20_CONTRACT_ADDRESS = "0xFB09d466E7fE677439D695241d6327ddD8153848";
    public static final String PREF_KEY_ERC20_CONTRACT_ADDRESS = "erc20ContractAddress";
    public static final String PREF_KEY_ERC20_RECIPIENT_ADDRESS = "erc20RecipientAddress";
    public static final String PREF_KEY_ERC20_AMOUNT = "erc20TokenAmount";
    public static final String PREF_KEY_ERC20_GASLIMIT = "erc20GasLimit";

    // voting
    public static final String PREF_KEY_VOTING_GASLIMIT = "votingGasLimit";
    public static final String PREF_KEY_VOTING_CONTRACT_ADDRESS = "votingContractAddress";
    public static final String DEFAULT_VOTING_CONTRACT_ADDRESS = "0x5545ccecd05ef6943bc397773c72252cd7560f41";
    public static final String PREF_KEY_VOTING_CONTRACT_ADDRESS_TESTNET = "votingContractAddressTestnet";
    public static final String DEFAULT_VOTING_CONTRACT_ADDRESS_TESTNET = "0x6e670c473a2ad5894ae354b832ad4badf1d919bf";
}
