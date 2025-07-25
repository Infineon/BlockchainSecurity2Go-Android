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
    private static final String BASEURL = ".infura.io/v3/" + "insert your infura project ID here";
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
    public static final String PREF_KEY_MAIN_NETWORK = "mainNetwork";
    public static final String PREF_KEY_PIN = "pin";
    public static final String KEY_INDEX_OF_CARD = "keyIndexOfCard";

    public static final String DEFAULT_GASPRICE_IN_GIGAWEI = "30";
    public static final String DEFAULT_GASLIMIT = "554321";
    
    //used for brandprotection activity
    public static final String GASPRICE="100000";
    public static final String GASLIMIT="800000";
    public static final String PREF_KEY_BRANDPROTECTION_GASLIMIT="brandProtectionGasLimit";
    public static final String PREF_KEY_BRANDPROTECTION_GASPRICE="brandprotectionGasPrice";

    // send eth
    public static final String PREF_KEY_RECIPIENT_ADDRESS = "recipientAddressTxt";
    public static final String PREF_KEY_GASPRICE_WEI = "gasPriceInWei";
    public static final String PREF_KEY_GASLIMIT_SEND_ETH = "gasLimitSendEth";

    // send erc 20
    // Dummy test ERC-20 contract: Coinfinity CFTEST Tokens: https://etherscan.io/token/0xfb09d466e7fe677439d695241d6327ddd8153848
    public static final String DEFAULT_ERC20_CONTRACT_ADDRESS = "0xFB09d466E7fE677439D695241d6327ddD8153848";
    public static final String PREF_KEY_ERC20_CONTRACT_ADDRESS = "erc20ContractAddress";
    public static final String PREF_KEY_ERC20_RECIPIENT_ADDRESS = "erc20RecipientAddress";
    public static final String PREF_KEY_ERC20_AMOUNT = "erc20TokenAmount";
    public static final String PREF_KEY_ERC20_GASLIMIT = "erc20GasLimit";

    // voting
    public static final String PREF_KEY_VOTING_GASLIMIT = "votingGasLimit";
    public static final String PREF_KEY_VOTING_CONTRACT_ADDRESS = "votingContractAddress";
    // https://etherscan.io/address/0x2c680955cd340eae72703e6886957bf8465f9583#code
    public static final String DEFAULT_VOTING_CONTRACT_ADDRESS = "0x2c680955cd340eae72703e6886957bf8465f9583";
    public static final String PREF_KEY_VOTING_CONTRACT_ADDRESS_TESTNET = "votingContractAddressTestnet";
    // https://ropsten.etherscan.io/address/0x104d919b299dbbbea258a41e2e910c29c551bf17#code
    public static final String DEFAULT_VOTING_CONTRACT_ADDRESS_TESTNET = "0x104d919b299dbbbea258a41e2e910c29c551bf17";

    // Brand protection
    public static final String PREF_KEY_PRODUCT_DETAIL_CONTRACT_ADDRESS_TESTNET = "productDetailContractAddressTestnet";
    //https://ropsten.etherscan.io/tx/0xb1cb598a3b0e5231b646fe6ce45daa8b0cb8e242e0a97521e813df0723f681ff
    public static final String DEFAULT_PRODUCT_DETAIL_ADDRESS_TESTNET  =  "0x00aFCb0d565F5D8792F200fC003657892968e66c";
}

