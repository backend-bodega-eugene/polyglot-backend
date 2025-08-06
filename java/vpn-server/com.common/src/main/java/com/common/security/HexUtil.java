package com.common.security;


import com.common.exception.BizException;
import com.common.result.ResultEnum;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HexUtil implements Serializable {
    public static final Charset DEFAULT_CHARSET;
    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    private static final char[] DIGITS_LOWER;
    private static final char[] DIGITS_UPPER;
    private final Charset charset;
    private static Integer ERROR_CODE = ResultEnum.SERVER_INTERNAL_ERROR.getCode();

    public static byte[] decodeHex(char[] data) throws BizException {
        byte[] out = new byte[data.length >> 1];
        decodeHex(data, out, 0);
        return out;
    }

    public static int decodeHex(char[] data, byte[] out, int outOffset) throws BizException {
        int len = data.length;
        if ((len & 1) != 0) {
            throw new BizException(ERROR_CODE, "Odd number of characters.");
        } else {
            int outLen = len >> 1;
            if (out.length - outOffset < outLen) {
                throw new BizException(ERROR_CODE, "Output array is not large enough to accommodate decoded data.");
            } else {
                int i = outOffset;

                for (int j = 0; j < len; ++i) {
                    int f = toDigit(data[j], j) << 4;
                    ++j;
                    f |= toDigit(data[j], j);
                    ++j;
                    out[i] = (byte) (f & 255);
                }

                return outLen;
            }
        }
    }

    public static byte[] decodeHex(String data) throws BizException {
        return decodeHex(data.toCharArray());
    }

    public static char[] encodeHex(byte[] data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(byte[] data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(byte[] data, char[] toDigits) {
        int l = data.length;
        char[] out = new char[l << 1];
        encodeHex(data, 0, data.length, toDigits, out, 0);
        return out;
    }

    public static char[] encodeHex(byte[] data, int dataOffset, int dataLen, boolean toLowerCase) {
        char[] out = new char[dataLen << 1];
        encodeHex(data, dataOffset, dataLen, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER, out, 0);
        return out;
    }

    public static void encodeHex(byte[] data, int dataOffset, int dataLen, boolean toLowerCase, char[] out, int outOffset) {
        encodeHex(data, dataOffset, dataLen, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER, out, outOffset);
    }

    private static void encodeHex(byte[] data, int dataOffset, int dataLen, char[] toDigits, char[] out, int outOffset) {
        int i = dataOffset;

        for (int var7 = outOffset; i < dataOffset + dataLen; ++i) {
            out[var7++] = toDigits[(240 & data[i]) >>> 4];
            out[var7++] = toDigits[15 & data[i]];
        }

    }

    public static char[] encodeHex(ByteBuffer data) {
        return encodeHex(data, true);
    }

    public static char[] encodeHex(ByteBuffer data, boolean toLowerCase) {
        return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
    }

    protected static char[] encodeHex(ByteBuffer byteBuffer, char[] toDigits) {
        return encodeHex(toByteArray(byteBuffer), toDigits);
    }

    public static String encodeHexString(byte[] data) {
        return new String(encodeHex(data));
    }

    public static String encodeHexString(byte[] data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase));
    }

    public static String encodeHexString(ByteBuffer data) {
        return new String(encodeHex(data));
    }

    public static String encodeHexString(ByteBuffer data, boolean toLowerCase) {
        return new String(encodeHex(data, toLowerCase));
    }

    private static byte[] toByteArray(ByteBuffer byteBuffer) {
        int remaining = byteBuffer.remaining();
        byte[] byteArray;
        if (byteBuffer.hasArray()) {
            byteArray = byteBuffer.array();
            if (remaining == byteArray.length) {
                byteBuffer.position(remaining);
                return byteArray;
            }
        }

        byteArray = new byte[remaining];
        byteBuffer.get(byteArray);
        return byteArray;
    }

    protected static int toDigit(char ch, int index) throws BizException {
        int digit = Character.digit(ch, 16);
        if (digit == -1) {
            throw new BizException(ERROR_CODE, "Illegal hexadecimal character " + ch + " at index " + index);
        } else {
            return digit;
        }
    }

    public HexUtil() {
        this.charset = DEFAULT_CHARSET;
    }

    public HexUtil(Charset charset) {
        this.charset = charset;
    }

    public HexUtil(String charsetName) {
        this(Charset.forName(charsetName));
    }

    public byte[] decode(byte[] array) throws BizException {
        return decodeHex((new String(array, this.getCharset())).toCharArray());
    }

    public byte[] decode(ByteBuffer buffer) throws BizException {
        return decodeHex((new String(toByteArray(buffer), this.getCharset())).toCharArray());
    }

    public Object decode(Object object) throws BizException {
        if (object instanceof String) {
            return this.decode((Object) ((String) object).toCharArray());
        } else if (object instanceof byte[]) {
            return this.decode((byte[]) ((byte[]) object));
        } else if (object instanceof ByteBuffer) {
            return this.decode((ByteBuffer) object);
        } else {
            try {
                return decodeHex((char[]) ((char[]) object));
            } catch (ClassCastException var3) {
                throw new BizException(ERROR_CODE, var3.getMessage());
            }
        }
    }

    public byte[] encode(byte[] array) {
        return encodeHexString(array).getBytes(this.getCharset());
    }

    public byte[] encode(ByteBuffer array) {
        return encodeHexString(array).getBytes(this.getCharset());
    }

    public Object encode(Object object) throws BizException {
        byte[] byteArray;
        if (object instanceof String) {
            byteArray = ((String) object).getBytes(this.getCharset());
        } else if (object instanceof ByteBuffer) {
            byteArray = toByteArray((ByteBuffer) object);
        } else {
            try {
                byteArray = (byte[]) ((byte[]) object);
            } catch (ClassCastException var4) {
                throw new BizException(ERROR_CODE, var4.getMessage());
            }
        }

        return encodeHex(byteArray);
    }

    public Charset getCharset() {
        return this.charset;
    }

    public String getCharsetName() {
        return this.charset.name();
    }

    public String toString() {
        return super.toString() + "[charsetName=" + this.charset + "]";
    }

    static {
        DEFAULT_CHARSET = StandardCharsets.UTF_8;
        DIGITS_LOWER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        DIGITS_UPPER = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    }
}
