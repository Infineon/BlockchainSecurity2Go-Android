package co.coinfinity.infineonandroidapp.ethereum.utils;

import co.coinfinity.infineonandroidapp.ethereum.exceptions.InvalidEthereumAddressException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UriUtilsTest {

    @Test
    public void extractEtherAddressWithChecksum() throws InvalidEthereumAddressException {
        final String result = UriUtils.extractEtherAddressFromUri("6e670c473A2AD5894aE354b832aD4BADF1d919bf");

        assertEquals("0x6e670c473A2AD5894aE354b832aD4BADF1d919bf", result);
    }

    @Test
    public void extractEtherAddressWithoutChecksum() throws InvalidEthereumAddressException {
        final String result = UriUtils.extractEtherAddressFromUri("0x6e670c473a2ad5894ae354b832ad4badf1d919bf");

        assertEquals("0x6e670c473a2ad5894ae354b832ad4badf1d919bf", result);
    }

    @Test(expected = InvalidEthereumAddressException.class)
    public void extractEtherAddressWithoutInvalidLength() throws InvalidEthereumAddressException {
        UriUtils.extractEtherAddressFromUri("0x6e670c473a2ad5894ae354b832ad4badf1d919bfttttttttttttttttt");
    }

    @Test
    public void extractEtherAddressWithSchema() throws InvalidEthereumAddressException {
        final String result = UriUtils.extractEtherAddressFromUri("ethereum:6e670c473A2AD5894aE354b832aD4BADF1d919bf");

        assertEquals("0x6e670c473A2AD5894aE354b832aD4BADF1d919bf", result);
    }

    @Test(expected = InvalidEthereumAddressException.class)
    public void extractEtherAddressWithInvalidChecksum() throws InvalidEthereumAddressException {
        UriUtils.extractEtherAddressFromUri("0x6e670c473A2AD5894aE354b832aD4BADF1d919bF");
    }

    @Test
    public void extractEtherAddressWithUppercaseLetters() throws InvalidEthereumAddressException {
        final String result = UriUtils.extractEtherAddressFromUri("0x6E670C473A2AD5894AE354B832AD4BADF1D919BF");

        assertEquals("0x6E670C473A2AD5894AE354B832AD4BADF1D919BF", result);
    }
}