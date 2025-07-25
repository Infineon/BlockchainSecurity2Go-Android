package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class VerifyPinException extends NfcCardException {

    public VerifyPinException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
