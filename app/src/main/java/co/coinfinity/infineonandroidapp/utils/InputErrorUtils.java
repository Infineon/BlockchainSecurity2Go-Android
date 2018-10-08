package co.coinfinity.infineonandroidapp.utils;

import android.content.Context;
import android.text.TextUtils;
import android.widget.TextView;
import co.coinfinity.infineonandroidapp.R;

/**
 * this class is used to display input errors of TextViews
 * e.g.: if some input is missing but it is mandatory
 */
public class InputErrorUtils {

    private Context ctx;
    private TextView recipientAddressTxt;
    private TextView amountTxt;
    private TextView gasPriceTxt;
    private TextView gasLimitTxt;
    private TextView contractAddress;

    public InputErrorUtils(Context ctx, TextView recipientAddressTxt, TextView amountTxt, TextView gasPriceTxt, TextView gasLimitTxt) {
        this.ctx = ctx;
        this.recipientAddressTxt = recipientAddressTxt;
        this.amountTxt = amountTxt;
        this.gasPriceTxt = gasPriceTxt;
        this.gasLimitTxt = gasLimitTxt;
    }

    public InputErrorUtils(Context ctx, TextView recipientAddressTxt, TextView amountTxt, TextView gasPriceTxt, TextView gasLimitTxt, TextView contractAddress) {
        this.ctx = ctx;
        this.recipientAddressTxt = recipientAddressTxt;
        this.amountTxt = amountTxt;
        this.gasPriceTxt = gasPriceTxt;
        this.gasLimitTxt = gasLimitTxt;
        this.contractAddress = contractAddress;
    }

    public InputErrorUtils(Context ctx, TextView gasPriceTxt, TextView gasLimitTxt, TextView contractAddress) {
        this.ctx = ctx;
        this.gasPriceTxt = gasPriceTxt;
        this.gasLimitTxt = gasLimitTxt;
        this.contractAddress = contractAddress;
    }

    public boolean isNoInputError() {
        boolean isNoError = true;

        if (recipientAddressTxt != null && TextUtils.isEmpty(recipientAddressTxt.getText())) {
            recipientAddressTxt.setError(ctx.getString(R.string.err_receipient_required));
            isNoError = false;
        }

        if (amountTxt != null && TextUtils.isEmpty(amountTxt.getText())) {
            amountTxt.setError(ctx.getString(R.string.err_amount_required));
            isNoError = false;
        }

        if (TextUtils.isEmpty(gasPriceTxt.getText()) || Double.valueOf(gasPriceTxt.getText().toString()) == 0) {
            gasPriceTxt.setError(ctx.getString(R.string.err_gasprice_required));
            isNoError = false;
        }

        if (TextUtils.isEmpty(gasLimitTxt.getText()) || Double.valueOf(gasLimitTxt.getText().toString()) == 0) {
            gasLimitTxt.setError(ctx.getString(R.string.err_gaslimit_required));
            isNoError = false;
        }

        if (contractAddress != null && TextUtils.isEmpty(contractAddress.getText())) {
            contractAddress.setError(ctx.getString(R.string.err_contract_address_missing));
            isNoError = false;
        }

        return isNoError;
    }
}
