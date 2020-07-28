package co.coinfinity.infineonandroidapp.ethereum.utils;

import android.app.Activity;
import android.nfc.tech.IsoDep;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple5;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;

import java.math.BigInteger;

import co.coinfinity.infineonandroidapp.ethereum.NfcTransactionManager;
import co.coinfinity.infineonandroidapp.ethereum.contract.ProductDetail;
import co.coinfinity.infineonandroidapp.utils.UiUtils;

public class ProductDetailUtils {

    public static TransactionReceipt productDetail(String contractAddress, String ethAddress,IsoDep tag, String publicKey, String from, String productid, String productname,String time, String date, String manufacturer, BigInteger gasPrice, BigInteger gasLimit, String pubKey, Activity activity) throws Exception {
        ProductDetail contract = prepareWriteProductDetailContract(contractAddress, tag, publicKey, from,gasPrice, gasLimit, activity);
        return contract.setProductDetail(new Address(ethAddress).toString(), new Utf8String(productid).toString(), new Utf8String(productname).toString(), new Utf8String(time).toString(), new Utf8String(date).toString(), new Utf8String(manufacturer).toString(), new Utf8String(pubKey).toString()).send();
    }
    public static Tuple5<String, String, String, String, String> getProductDetail(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) throws Exception {
        ProductDetail contract = prepareReadOnlyProductContract(contractAddress, from, gasPrice, gasLimit, activity);
        return contract.getProductDetails(new Address(from).toString()).send();
        // return Product_detail;
    }
    public static boolean getAddress(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) throws Exception {
        ProductDetail contract = prepareReadOnlyProductContract(contractAddress, from, gasPrice, gasLimit, activity);
        return contract.checkProductAddress(new Address(from).toString()).send();
    }



    private static ProductDetail prepareWriteProductDetailContract(String contractAddress, IsoDep tag, String publicKey, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) {
        Web3j web3j = Web3jFactory.build(new HttpService(UiUtils.getFullNodeUrl(activity)));
        TransactionManager transactionManager = new NfcTransactionManager(web3j, from, tag, publicKey, activity);

        return ProductDetail.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    private static ProductDetail prepareReadOnlyProductContract(String contractAddress, String from, BigInteger gasPrice, BigInteger gasLimit, Activity activity) {
        Web3j web3j = Web3jFactory.build(new HttpService(UiUtils.getFullNodeUrl(activity)));
        TransactionManager transactionManager = new ReadonlyTransactionManager(web3j, from);

        return ProductDetail.load(
                contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

}
