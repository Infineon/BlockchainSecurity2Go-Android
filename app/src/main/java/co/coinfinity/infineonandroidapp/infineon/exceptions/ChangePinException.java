package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class ChangePinException extends NfcCardException {

    public ChangePinException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
