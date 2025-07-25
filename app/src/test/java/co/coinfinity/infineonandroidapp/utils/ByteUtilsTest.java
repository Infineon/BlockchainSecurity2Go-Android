package co.coinfinity.infineonandroidapp.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ByteUtilsTest {

    @Test
    public void testBytesToHex() {
        final String hex = ByteUtils.bytesToHex("coinfinity".getBytes());

        assertEquals("636F696E66696E697479", hex);
    }

    @Test
    public void testCombineByteArrays() {
        final byte[] combineByteArrays = ByteUtils.combineByteArrays("coinfinity".getBytes(), "infineon".getBytes());

        assertEquals("coinfinityinfineon", new String(combineByteArrays));
    }

    @Test
    public void testFromHexString() {
        final byte[] bytes = ByteUtils.fromHexString("636F696E66696E697479");

        assertEquals("coinfinity", new String(bytes));
    }
}