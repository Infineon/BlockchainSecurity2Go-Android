package co.coinfinity.infineonandroidapp.utils;


/**
 * Some helper utils to work with byte arrays.
 */
public class ByteUtils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();


    /**
     * returns hexadecimal string representation of a byte array.
     *
     * @param bytes byte array
     * @return hex string
     */
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * This method combines two byte arrays to one.
     *
     * @param one first byte array
     * @param two second byte, array to be appended after the first one
     * @return combined byte array
     */
    public static byte[] combineByteArrays(byte[] one, byte[] two) {
        byte[] combined = new byte[one.length + two.length];

        System.arraycopy(one, 0, combined, 0, one.length);
        System.arraycopy(two, 0, combined, one.length, two.length);

        return combined;
    }

    /**
     * Creates a byte array out of a Hex string.
     *
     * @param hexString
     * @return byte array
     */
    public static byte[] fromHexString(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
