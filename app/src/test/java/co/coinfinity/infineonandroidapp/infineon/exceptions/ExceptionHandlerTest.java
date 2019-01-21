package co.coinfinity.infineonandroidapp.infineon.exceptions;

import co.coinfinity.infineonandroidapp.infineon.apdu.*;
import org.junit.Test;

public class ExceptionHandlerTest {

    @Test(expected = GenerateKeypairException.class)
    public void testGenerateKeyPairError() throws NfcCardException {
        GenerateKeyPairApdu apdu = new GenerateKeyPairApdu(0);
        ExceptionHandler.handleErrorCodes(apdu, 0x6A84);
    }

    @Test(expected = GetKeyInfoException.class)
    public void testGetKeyInfoError() throws NfcCardException {
        GetKeyInfoApdu apdu = new GetKeyInfoApdu(0);
        ExceptionHandler.handleErrorCodes(apdu, 0x6A88);
    }

    @Test(expected = GenerateSignatureException.class)
    public void testGenerateSignatureError() throws NfcCardException {
        GenerateSignatureApdu apdu = new GenerateSignatureApdu(0, new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6985);
    }

    @Test(expected = GenerateKeyFromSeedException.class)
    public void testGenerateKeyFromSeedError() throws NfcCardException {
        GenerateKeyFromSeedApdu apdu = new GenerateKeyFromSeedApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6985);
    }

    @Test(expected = SetPinException.class)
    public void testSetPinError() throws NfcCardException {
        SetPinApdu apdu = new SetPinApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6700);
    }

    @Test(expected = ChangePinException.class)
    public void testChangePinError() throws NfcCardException {
        ChangePinApdu apdu = new ChangePinApdu(new byte[]{}, new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6983);
    }

    @Test(expected = VerifyPinException.class)
    public void testVerifyPinError() throws NfcCardException {
        VerifyPinApdu apdu = new VerifyPinApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6983);
    }

    @Test(expected = UnlockPinException.class)
    public void testUnlockPinError() throws NfcCardException {
        UnlockPinApdu apdu = new UnlockPinApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6983);
    }

    @Test(expected = SelectApplicationException.class)
    public void testSelectApplicationError() throws NfcCardException {
        SelectApplicationApdu apdu = new SelectApplicationApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6A82);
    }

    @Test(expected = NfcCardException.class)
    public void testNfcCardError() throws NfcCardException {
        SelectApplicationApdu apdu = new SelectApplicationApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6700);
    }

    @Test(expected = NfcCardException.class)
    public void testNfcCardErrorWithSW2Info() throws NfcCardException {
        SelectApplicationApdu apdu = new SelectApplicationApdu(new byte[]{});
        ExceptionHandler.handleErrorCodes(apdu, 0x6401);
    }
}