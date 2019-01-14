package co.coinfinity.infineonandroidapp.infineon.apdu;

import org.apache.commons.lang3.ArrayUtils;

/**
 * @author Coinfinity, 2018
 */
public class ChangePinApdu extends BaseCommandApdu {

    /**
     * Instruction byte for CHANGE PIN operation
     */
    private static final int INS_CHANGE_PIN = 0x42;

    /**
     * Constructs a CHANGE PIN command apdu.
     *
     * @param currentPin current pin to change
     * @param newPin     new pin which should be used
     */
    public ChangePinApdu(byte[] currentPin, byte[] newPin) {
        this.ins = INS_CHANGE_PIN;

        byte currentPinInByte = (byte) currentPin.length;
        byte newPinInByte = (byte) newPin.length;

        byte[] firstBytes = ArrayUtils.addAll(new byte[]{currentPinInByte}, currentPin);
        byte[] secondBytes = ArrayUtils.addAll(new byte[]{newPinInByte}, newPin);

        this.setData(ArrayUtils.addAll(firstBytes, secondBytes));
    }

}
