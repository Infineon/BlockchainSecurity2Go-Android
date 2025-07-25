package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class UnlockPinException extends NfcCardException {

    public UnlockPinException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
