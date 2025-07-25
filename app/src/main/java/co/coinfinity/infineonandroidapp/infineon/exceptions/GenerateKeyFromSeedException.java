package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class GenerateKeyFromSeedException extends NfcCardException {

    public GenerateKeyFromSeedException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
