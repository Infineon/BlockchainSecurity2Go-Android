package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class SelectApplicationException extends NfcCardException {

    public SelectApplicationException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
