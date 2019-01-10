package co.coinfinity.infineonandroidapp.utils;

import android.nfc.tech.IsoDep;
import co.coinfinity.infineonandroidapp.infineon.NfcTranceiver;

import java.io.IOException;

/**
 * Wraps IsoDep tag into NfcTranceiver interface.
 * (this wrapper is used so that the co.coinfinity.infineonandroidapp.infineon package
 * doesn't have any Android dependencies)
 */
public class IsoTagWrapper implements NfcTranceiver {

    private final IsoDep isoDep;

    private IsoTagWrapper(IsoDep isoDep) {
        this.isoDep = isoDep;
    }

    /**
     * Create wrapper of given IsoDep tag.
     *
     * @param isoDep
     * @return wrapper instance
     */
    public static IsoTagWrapper of(IsoDep isoDep) {
        return new IsoTagWrapper(isoDep);
    }

    /**
     * Sends a command APDU to the NFC card and returns the received response APDU.
     *
     * @param commandApdu command APDU to send
     * @return bytes received as response
     * @throws IOException in case of communication errors
     */
    @Override
    public byte[] transceive(byte[] commandApdu) throws IOException {

        if (!isoDep.isConnected()) {
            isoDep.connect();
        }
        return isoDep.transceive(commandApdu);
    }
}
