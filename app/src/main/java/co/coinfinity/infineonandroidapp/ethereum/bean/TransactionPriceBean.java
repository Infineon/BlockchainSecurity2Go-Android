package co.coinfinity.infineonandroidapp.ethereum.bean;

import java.util.Locale;

public class TransactionPriceBean {
    Double priceInEuro;
    Double txFeeInEuro;

    public TransactionPriceBean() {
    }

    public TransactionPriceBean(Double priceInEuro, Double txFeeInEuro) {
        this.priceInEuro = priceInEuro;
        this.txFeeInEuro = txFeeInEuro;
    }

    public Double getPriceInEuro() {
        return priceInEuro;
    }

    public void setPriceInEuro(Double priceInEuro) {
        this.priceInEuro = priceInEuro;
    }

    public Double getTxFeeInEuro() {
        return txFeeInEuro;
    }

    public void setTxFeeInEuro(Double txFeeInEuro) {
        this.txFeeInEuro = txFeeInEuro;
    }

    @Override
    public String toString() {
        return String.format(Locale.ENGLISH, "Price: %.2f€ \n Tx Fee: %.2f€", priceInEuro, txFeeInEuro);
    }
}
