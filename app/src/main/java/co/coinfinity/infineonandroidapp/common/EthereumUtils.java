package co.coinfinity.infineonandroidapp.common;

import android.nfc.tech.IsoDep;
import android.util.Log;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class EthereumUtils {

    public static String getBalance(String ethAddress) {
        // connect to node
        Web3j web3 = Web3jFactory.build(new HttpService("https://mainnet.infura.io/v3/7b40d72779e541a498cb0da69aa418a2"));  // defaults to http://localhost:8545/

        // send asynchronous requests to get balance
        EthGetBalance ethGetBalance = null;
        try {
            ethGetBalance = web3
                    .ethGetBalance(ethAddress, DefaultBlockParameterName.LATEST)
                    .send();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if(ethGetBalance != null) {
            BigInteger wei = ethGetBalance.getBalance();
            BigDecimal ether = Convert.fromWei(ethGetBalance.getBalance().toString(), Convert.Unit.ETHER);
            Log.i("info",wei + " Wei");
            Log.i("info",ether + " Ether");
            return "Wei: " + wei + "\nEther: " + ether;
        }

        return null;
    }

    public static String getPublicKey(IsoDep isoDep, int parameter) throws IOException {
        //        get pub key
        final byte[] GET_PUB_KEY = {
                (byte) 0x00, // CLA Class
                (byte) 0x16, // INS Instruction
                (byte) parameter, // P1  Parameter 1
                (byte) 0x00, // P2  Parameter 2
                (byte) 0x00, // Length
        };

        byte[] response = isoDep.transceive(GET_PUB_KEY);
        String hex = bytesToHex(response);
        return hex.subSequence(0,hex.length()-4).toString();
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}
