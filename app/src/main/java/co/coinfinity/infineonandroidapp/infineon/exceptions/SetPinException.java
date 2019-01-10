package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class SetPinException extends NfcCardException {

    public SetPinException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
