package co.coinfinity.infineonandroidapp.utils;

public class InvalidContractException extends Exception {

    public InvalidContractException() {
    }

    public InvalidContractException(String message) {
        super(message);
    }

    public InvalidContractException(String message, Throwable cause) {
        super(message, cause);
    }

}
