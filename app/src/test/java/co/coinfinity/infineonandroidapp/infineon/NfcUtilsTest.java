package co.coinfinity.infineonandroidapp.infineon;

import co.coinfinity.infineonandroidapp.infineon.apdu.response.GenerateSignatureResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.apdu.response.GetKeyInfoResponseApdu;
import co.coinfinity.infineonandroidapp.infineon.exceptions.NfcCardException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static co.coinfinity.infineonandroidapp.utils.ByteUtils.bytesToHex;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore("javax.net.ssl.*")
public class NfcUtilsTest {

    @Mock
    private NfcTranceiver nfcTranceiver;

    @Test
    public void testGenerateSignature() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, (byte) 0x90, 0x00});
        byte[] bytes = new byte[]{};
        final GenerateSignatureResponseApdu responseApdu = NfcUtils.generateSignature(nfcTranceiver, 0, bytes, null);
        assertEquals(1, responseApdu.getSigCounterAsInteger());
        assertEquals(2, responseApdu.getGlobalSigCounterAsInteger());
    }

    @Test
    public void testGenerateSignatureError() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(new byte[]{0x00, 0x18, 0x00, 0x00})).thenReturn(new byte[]{0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, (byte) 0x40, 0x00});
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, (byte) 0x90, 0x00});
        byte[] bytes = new byte[]{};
        final GenerateSignatureResponseApdu responseApdu = NfcUtils.generateSignature(nfcTranceiver, 0, bytes, null);
        assertEquals(1, responseApdu.getSigCounterAsInteger());
        assertEquals(2, responseApdu.getGlobalSigCounterAsInteger());
    }

    @Test
    public void testReadPublicKeyOrCreateIfNotExists() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{0x00, 0x00, 0x00, 0x02, 0x00, 0x00, 0x00, 0x01, 0x04, (byte) 0x91, 0x17, (byte) 0x95, (byte) 0xEF, 0x45, (byte) 0xAC, (byte) 0xF7, (byte) 0x8B, 0x02, (byte) 0xE3, 0x08, 0x5F, 0x37, (byte) 0xF7, (byte) 0x87, 0x0F, 0x25, 0x63, (byte) 0xDB, 0x28, (byte) 0xC9, 0x77, (byte) 0xBE, 0x5D, 0x40, 0x15, 0x3C, (byte) 0xC0, 0x7F, 0x66, (byte) 0x9A, 0x5B, (byte) 0xBD, 0x33, 0x51, (byte) 0xC9, 0x00, (byte) 0xBC, (byte) 0xB9, (byte) 0xC2, 0x6E, (byte) 0x96, (byte) 0xF1, 0x49, 0x39, (byte) 0xD2, 0x5C, 0x5F, 0x23, 0x0B, 0x3C, 0x2A, 0x1A, (byte) 0xCD, 0x4D, 0x2B, (byte) 0xC3, 0x00, 0x15, (byte) 0x8D, (byte) 0xA9, 0x66, 0x06, 0x43, (byte) 0x90, 0x00});
        final GetKeyInfoResponseApdu responseApdu = NfcUtils.readPublicKeyOrCreateIfNotExists(nfcTranceiver, 0);
        assertEquals(1, responseApdu.getSigCounterAsInteger());
        assertEquals(2, responseApdu.getGlobalSigCounterAsInteger());
        assertEquals("911795EF45ACF78B02E3085F37F7870F2563DB28C977BE5D40153CC07F669A5BBD3351C900BCB9C26E96F14939D25C5F230B3C2A1ACD4D2BC300158DA9660643", responseApdu.getPublicKeyInHexWithoutPrefix());
    }

    @Test
    public void testGenerateKeyFromSeed() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{(byte) 0x90, 0x00});
        byte[] seed = new byte[]{};
        final boolean responseApdu = NfcUtils.generateKeyFromSeed(nfcTranceiver, seed, null);
        assertTrue(responseApdu);
    }

    @Test
    public void testInitializePinAndReturnPuk() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{0x04, 0x03, 0x02, 0x01, (byte) 0x90, 0x00});
        final String responseApdu = bytesToHex(NfcUtils.initializePinAndReturnPuk(nfcTranceiver,
                "1234".getBytes(StandardCharsets.UTF_8)));
        assertEquals("04030201", responseApdu);
    }

    @Test
    public void testChangePin() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{0x04, 0x03, 0x02, 0x01, (byte) 0x90, 0x00});
        final String responseApdu = bytesToHex(NfcUtils.changePin(nfcTranceiver,
                "1234".getBytes(StandardCharsets.UTF_8), "1337".getBytes(StandardCharsets.UTF_8)));
        assertEquals("04030201", responseApdu);
    }

    @Test
    public void testUnlockPin() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{(byte) 0x90, 0x00});
        final boolean responseApdu = NfcUtils.unlockPin(nfcTranceiver,
                "1234".getBytes(StandardCharsets.UTF_8));
        assertTrue(responseApdu);
    }

    @Test
    public void testVerifyPin() throws IOException, NfcCardException {
        when(nfcTranceiver.transceive(any())).thenReturn(new byte[]{(byte) 0x90, 0x00});
        final boolean responseApdu = NfcUtils.verifyPin(nfcTranceiver,
                "1234".getBytes(StandardCharsets.UTF_8));
        assertTrue(responseApdu);
    }
}