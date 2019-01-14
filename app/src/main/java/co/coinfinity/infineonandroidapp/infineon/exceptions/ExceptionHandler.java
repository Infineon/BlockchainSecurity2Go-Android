package co.coinfinity.infineonandroidapp.infineon.exceptions;

import co.coinfinity.infineonandroidapp.infineon.apdu.*;

public class ExceptionHandler {
    public static void handleErrorCodes(BaseCommandApdu commandApdu, int SW1SW2) throws NfcCardException {
        if (commandApdu instanceof GenerateKeyPairApdu) {
            if (SW1SW2 == 0x6A84) {
                throw new GenerateKeypairException(SW1SW2, "Not enough memory – key storage full");
            }
        } else if (commandApdu instanceof GetKeyInfoApdu) {
            if (SW1SW2 == 0x6A88) {
                throw new GetKeyInfoException(SW1SW2, "Referenced data not found – key with the given index is not available");
            }
        } else if (commandApdu instanceof GenerateSignatureApdu) {
            switch (SW1SW2) {
                case 0x6982:
                    throw new GenerateSignatureException(SW1SW2, "Security Status not satisfied - one of the signature counters exceeded ");
                case 0x6985:
                    throw new GenerateSignatureException(SW1SW2, "Condition of use not satisfied – not authenticated with PIN");
                case 0x6A88:
                    throw new GenerateSignatureException(SW1SW2, "Referenced Data not found – key with given index not available");
            }
        } else if (commandApdu instanceof GenerateKeyFromSeedApdu) {
            if (SW1SW2 == 0x6985) {
                throw new GenerateKeyFromSeedException(SW1SW2, "Condition of use not satisfied – not authenticated with PIN");
            }
        } else if (commandApdu instanceof SetPinApdu) {
            switch (SW1SW2) {
                case 0x6700:
                    throw new SetPinException(SW1SW2, "PIN format is not valid (invalid length)");
                case 0x6985:
                    throw new SetPinException(SW1SW2, "Condition of use not satisfied - No PIN has been set – in “PIN inactive” state ");
            }
        } else if (commandApdu instanceof ChangePinApdu) {
            if (Integer.toHexString(SW1SW2).toUpperCase().startsWith("63C")) {
                throw new ChangePinException(SW1SW2, "Authentication failed, PIN is not valid, " + Integer.toHexString(SW1SW2).toUpperCase().substring(3, 4) + " retries remaining");
            }
            switch (SW1SW2) {
                case 0x6983:
                    throw new ChangePinException(SW1SW2, "Authentication failed, PIN locked");
                case 0x6985:
                    throw new ChangePinException(SW1SW2, "Not in “PIN active state” e.g. PIN has not already been set or maximal number of wrong PUK entries reached (Condition of use not satisfied)");
                case 0x6A80:
                    throw new ChangePinException(SW1SW2, "Format of new PIN is not valid (min / max length) \n" +
                            "Format of data field not valid (i.e lengths do not match) \n" +
                            "(Incorrect parameter in the command data field)");
            }
        } else if (commandApdu instanceof VerifyPinApdu) {
            if (Integer.toHexString(SW1SW2).toUpperCase().startsWith("63C")) {
                throw new VerifyPinException(SW1SW2, "Authentication failed, PIN is not valid (" + Integer.toHexString(SW1SW2).toUpperCase().substring(3, 4) + " retries allowed)");
            }
            switch (SW1SW2) {
                case 0x6983:
                    throw new VerifyPinException(SW1SW2, "Authentication failed, PIN blocked");
                case 0x6985:
                    throw new VerifyPinException(SW1SW2, "Condition of use not satisfied  - PIN has not been set");
            }
        } else if (commandApdu instanceof UnlockPinApdu) {
            if (Integer.toHexString(SW1SW2).toUpperCase().startsWith("63C")) {
                throw new UnlockPinException(SW1SW2, "Authentication failed, PUK is not valid, " + Integer.toHexString(SW1SW2).toUpperCase().substring(3, 4) + " retries remaining");
            }
            switch (SW1SW2) {
                case 0x6983:
                    throw new UnlockPinException(SW1SW2, "Authentication failed, max number of wrong PUK entries reached ");
                case 0x6985:
                    throw new UnlockPinException(SW1SW2, "PIN has not been set before  - Card is in “PIN inactive” state (Condition of use not satisfied)");
            }
        } else if (commandApdu instanceof SelectApplicationApdu) {
            if (SW1SW2 == 0x6A82) {
                throw new SelectApplicationException(SW1SW2, "Selected Application not found – wrong AID");
            }
        }

        if (Integer.toHexString(SW1SW2).toUpperCase().startsWith("64")) {
            throw new UnlockPinException(SW1SW2, "Operation failed (further Information in SW2: " + Integer.toHexString(SW1SW2).toUpperCase().substring(2, 4) + ")");
        }

        switch (SW1SW2) {
            case 0x6700:
                throw new NfcCardException(SW1SW2, "Wrong length");
            case 0x6A86:
                throw new NfcCardException(SW1SW2, "Incorrect parameters P1/P2");
            case 0x6A87:
                throw new NfcCardException(SW1SW2, "Lc inconsistent");
            case 0x6D00:
                throw new NfcCardException(SW1SW2, "Instruction code is not supported or invalid or SELECT AID command not sent before");
            case 0x6E00:
                throw new NfcCardException(SW1SW2, "Class not supported");
            case 0x6F00:
                throw new NfcCardException(SW1SW2, "Unknown Error, SW1 SW2: 6F00");
            default:
                throw new NfcCardException(SW1SW2, "Unknown Error, SW1 SW2: " + Integer.toHexString(SW1SW2).toUpperCase());
        }

    }
}
