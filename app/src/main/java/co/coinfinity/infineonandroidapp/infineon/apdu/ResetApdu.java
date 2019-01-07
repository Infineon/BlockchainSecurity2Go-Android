package co.coinfinity.infineonandroidapp.infineon.apdu;

//TODO TESTT !!!!
public class ResetApdu extends BaseCommandApdu {

    public ResetApdu() {
        this.cla = 0xA2;
        this.ins = 0x20;
        this.p1 = 0x00;
        this.p2 = 0x00;
        this.leIncluded = false;
    }

}
