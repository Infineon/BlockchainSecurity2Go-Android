package co.coinfinity.infineonandroidapp.common;

import android.text.TextUtils;
import android.widget.TextView;

public class InputErrorUtils {

    private TextView recipientAddressTxt;
    private TextView amountTxt;
    private TextView gasPriceTxt;
    private TextView gasLimitTxt;
    private TextView contractAddress;

    public InputErrorUtils(TextView recipientAddressTxt, TextView amountTxt, TextView gasPriceTxt, TextView gasLimitTxt) {
        this.recipientAddressTxt = recipientAddressTxt;
        this.amountTxt = amountTxt;
        this.gasPriceTxt = gasPriceTxt;
        this.gasLimitTxt = gasLimitTxt;
    }

    public InputErrorUtils(TextView recipientAddressTxt, TextView amountTxt, TextView gasPriceTxt, TextView gasLimitTxt, TextView contractAddress) {
        this.recipientAddressTxt = recipientAddressTxt;
        this.amountTxt = amountTxt;
        this.gasPriceTxt = gasPriceTxt;
        this.gasLimitTxt = gasLimitTxt;
        this.contractAddress = contractAddress;
    }

    public InputErrorUtils(TextView gasPriceTxt, TextView gasLimitTxt, TextView contractAddress) {
        this.gasPriceTxt = gasPriceTxt;
        this.gasLimitTxt = gasLimitTxt;
        this.contractAddress = contractAddress;
    }

    public boolean isNoInputError() {
        boolean isNoError = true;

        if (recipientAddressTxt != null && TextUtils.isEmpty(recipientAddressTxt.getText())) {
            recipientAddressTxt.setError("Recipient Address is required!");
            isNoError = false;
        }

        if (amountTxt != null && TextUtils.isEmpty(amountTxt.getText())) {
            amountTxt.setError("Amount is required!");
            isNoError = false;
        }

        if (TextUtils.isEmpty(gasPriceTxt.getText()) || Double.valueOf(gasPriceTxt.getText().toString()) == 0) {
            gasPriceTxt.setError("Gas Price is required!");
            isNoError = false;
        }

        if (TextUtils.isEmpty(gasLimitTxt.getText()) || Double.valueOf(gasLimitTxt.getText().toString()) == 0) {
            gasLimitTxt.setError("Gas Limit is required!");
            isNoError = false;
        }

        if (contractAddress != null && TextUtils.isEmpty(contractAddress.getText())) {
            contractAddress.setError("Contract Address is required!");
            isNoError = false;
        }

        return isNoError;
    }
}
