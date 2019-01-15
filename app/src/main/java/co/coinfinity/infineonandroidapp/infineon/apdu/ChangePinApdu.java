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

        byte currentPinLength = (byte) currentPin.length;
        byte newPinLength = (byte) newPin.length;

        byte[] firstPart = ArrayUtils.addAll(new byte[]{currentPinLength}, currentPin);
        byte[] secondPart = ArrayUtils.addAll(new byte[]{newPinLength}, newPin);

        this.setData(ArrayUtils.addAll(firstPart, secondPart));
    }

}
