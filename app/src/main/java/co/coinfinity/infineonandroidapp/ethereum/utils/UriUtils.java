package co.coinfinity.infineonandroidapp.ethereum.utils;

import co.coinfinity.infineonandroidapp.ethereum.exceptions.InvalidEthereumAddressException;
import org.web3j.crypto.Keys;
import org.web3j.utils.Numeric;

public class UriUtils {
    public static String extractEtherAddressFromUri(String uri) throws InvalidEthereumAddressException {
        String uriWithoutSchema = uri.replaceFirst("ethereum:", "");
        uriWithoutSchema = Numeric.prependHexPrefix(uriWithoutSchema);

        if (uriWithoutSchema.length() != 42) {
            throw new InvalidEthereumAddressException("Invalid address. The Ethereum address does not match the 42 char length!");
        }

        boolean hasChecksum = !uriWithoutSchema.equals(uriWithoutSchema.toLowerCase());
        if (hasChecksum) {
            if (!uriWithoutSchema.equals(Keys.toChecksumAddress(uriWithoutSchema))) {
                throw new InvalidEthereumAddressException("Wrong checksum. The Ethereum address is invalid!");
            }
        }

        return uriWithoutSchema;
    }
}
