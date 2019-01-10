package co.coinfinity.infineonandroidapp.infineon.exceptions;

public class GenerateSignatureException extends NfcCardException {

    public GenerateSignatureException(int SW1SW2, String message) {
        super(SW1SW2, message);
    }

}
