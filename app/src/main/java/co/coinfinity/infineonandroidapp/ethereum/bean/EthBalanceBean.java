package co.coinfinity.infineonandroidapp.ethereum.bean;

import java.math.BigDecimal;
import java.math.BigInteger;

public class EthBalanceBean {
    private BigInteger wei;
    private BigDecimal ether;
    private BigInteger unconfirmedWei;
    private BigDecimal unconfirmedEther;

    public EthBalanceBean(BigInteger wei, BigDecimal ether, BigInteger unconfirmedWei, BigDecimal unconfirmedEther) {
        this.wei = wei;
        this.ether = ether;
        this.unconfirmedWei = unconfirmedWei;
        this.unconfirmedEther = unconfirmedEther;
    }

    public BigInteger getWei() {
        return wei;
    }

    public void setWei(BigInteger wei) {
        this.wei = wei;
    }

    public BigDecimal getEther() {
        return ether;
    }

    public void setEther(BigDecimal ether) {
        this.ether = ether;
    }

    public BigInteger getUnconfirmedWei() {
        return unconfirmedWei;
    }

    public void setUnconfirmedWei(BigInteger unconfirmedWei) {
        this.unconfirmedWei = unconfirmedWei;
    }

    public BigDecimal getUnconfirmedEther() {
        return unconfirmedEther;
    }

    public void setUnconfirmedEther(BigDecimal unconfirmedEther) {
        this.unconfirmedEther = unconfirmedEther;
    }

    @Override
    public String toString() {
        if(ether != null && unconfirmedEther != null && ether.toString().equals(unconfirmedEther.toString())) {
            return ether + " ETH\n" + wei + " WEI";
        }

        return ether + " ETH ("+unconfirmedEther+")\n" + wei + " WEI ("+unconfirmedWei+")";
    }
}
