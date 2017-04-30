package com.encens.khipus.util;

/**
 * Encrypt and decrypt URLs based in the Base64 algorithm.
 * In addition / , = and + symbols are processed to avoid URLEncode in the urls.
 *
 * @author
 * @version 1.0
 */

public class UrlBase64Encoder {


    /**
     * used to disable test driver
     */
    public static final boolean DEBUGGING = false;

    /**
     * Marker value for chars we just ignore, e.g. \n \r high ascii
     */
    static final int IGNORE = -1;

    /**
     * Marker for = trailing pad
     */
    static final int PAD = -2;

    /**
     * binary value encoded by a given letter of the alphabet 0..63
     */
    private static int[] cv;

    /**
     * letter of the alphabet used to encode binary values 0..63
     */
    private static char[] vc;

    /**
     * binary value encoded by a given letter of the alphabet 0..63
     */
    int[] charToValue;

    /**
     * special character 1, will be -
     */
    char spec1 = '+';

    /**
     * special character 2, will be _
     */
    char spec2 = '/';

    /**
     * special character 3, will be *
     */
    char spec3 = '=';

    /**
     * letter of the alphabet used to encode binary values 0..63.
     */
    char[] valueToChar;

    /**
     * how we separate lines, e.g. \n, \r\n, \r etc.
     */
    private String lineSeparator = System.getProperty("line.separator");

    /**
     * max chars per line, excluding lineSeparator. A multiple of 4.
     */
    private int lineLength = 72;


    /**
     * constructor
     */
    public UrlBase64Encoder() {
        /** replace the url special characters */
        spec1 = '-';
        spec2 = '_';
        spec3 = '*';
        lineSeparator = ""; //we don't need \n or another one in the urls, we need all in one line
        initTables();
    }


    /**
     * How lines are separated. Ignored by decode.
     *
     * @param lineSeparator may be "" but not null. Usually contains only a combination of
     *                      chars \n and \r. Could be any chars not in set A-Z a-z 0-9 + /.
     */
    public void setLineSeparator(String lineSeparator) {
        this.lineSeparator = lineSeparator;
    }


    /**
     * decode a well-formed complete Base64 string back into an array of bytes.
     * It must have an even multiple of 4 data characters (not counting \n),
     * padded out with = as needed.
     *
     * @param s base64-encoded string
     * @return plaintext as a byte array.
     */
    public byte[] decode(String s) {
        // estimate worst case size of output array, no embedded newlines.
        byte[] b = new byte[(s.length() / 4) * 3];

        // tracks where we are in a cycle of 4 input chars.
        int cycle = 0;

        // where we combine 4 groups of 6 bits and take apart as 3 groups of 8.
        int combined = 0;

        // how many bytes we have prepared.
        int j = 0;
        // will be an even multiple of 4 chars, plus some embedded \n
        int len = s.length();
        int dummies = 0;
        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);
            int value = (c <= 255) ? charToValue[c] : IGNORE;
            // there are two magic values PAD (=) and IGNORE.
            switch (value) {
                case IGNORE:
                    // e.g. \n, just ignore it.
                    break;

                case PAD:
                    value = 0;
                    dummies++;
                    // deliberate fallthrough
                default:
                    /* regular value character */
                    switch (cycle) {
                        case 0:
                            combined = value;
                            cycle = 1;
                            break;

                        case 1:
                            combined <<= 6;
                            combined |= value;
                            cycle = 2;
                            break;

                        case 2:
                            combined <<= 6;
                            combined |= value;
                            cycle = 3;
                            break;

                        case 3:
                            combined <<= 6;
                            combined |= value;
                            // we have just completed a cycle of 4 chars.
                            // the four 6-bit values are in combined in
                            // big-endian order
                            // peel them off 8 bits at a time working lsb to msb
                            // to get our original 3 8-bit bytes back

                            b[j + 2] = (byte) combined;
                            combined >>>= 8;
                            b[j + 1] = (byte) combined;
                            combined >>>= 8;
                            b[j] = (byte) combined;
                            j += 3;
                            cycle = 0;
                            break;
                    }
                    break;
            }
        } // end for
        if (cycle != 0) {
            throw new ArrayIndexOutOfBoundsException(
                    "Input to decode not an even multiple of 4 characters; pad with "
                            + spec3);
        }
        j -= dummies;
        if (b.length != j) {
            byte[] b2 = new byte[j];
            System.arraycopy(b, 0, b2, 0, j);
            b = b2;
        }
        return b;
    }// end decode

    /**
     * Encode an arbitrary array of bytes as Base64 printable ASCII. It will be
     * broken into lines of 72 chars each. The last line is not terminated with
     * a line separator. The output will always have an even multiple of data
     * characters, exclusive of \n. It is padded out with =.
     *
     * @param b byte array to encode, typically produced by a
     *          ByteArrayOutputStream
     * @return base-64 encoded String, not char[] or byte[].
     */
    public String encode(byte[] b) {
        // Each group or partial group of 3 bytes becomes four chars
        // covered quotient
        int outputLength = ((b.length + 2) / 3) * 4;

        // account for trailing newlines, on all but the very last line
        if (lineLength != 0) {
            int lines = (outputLength + lineLength - 1) / lineLength - 1;
            if (lines > 0) {
                outputLength += lines * lineSeparator.length();
            }
        }

        // must be local for recursion to work.
        StringBuffer sb = new StringBuffer(outputLength);

        // must be local for recursion to work.
        int linePos = 0;

        // first deal with even multiples of 3 bytes.
        int len = (b.length / 3) * 3;
        int leftover = b.length - len;
        for (int i = 0; i < len; i += 3) {
            // Start a new line if next 4 chars won't fit on the current line
            // We can't encapsulete the following code since the variable need
            // to
            // be local to this incarnation of encode.
            linePos += 4;
            if (linePos > lineLength) {
                if (lineLength != 0) {
                    sb.append(lineSeparator);
                }
                linePos = 4;
            }

            // get next three bytes in unsigned form lined up,
            // in big-endian order
            int combined = b[i + 0] & 0xff;
            combined <<= 8;
            combined |= b[i + 1] & 0xff;
            combined <<= 8;
            combined |= b[i + 2] & 0xff;

            // break those 24 bits into a 4 groups of 6 bits,
            // working LSB to MSB.
            int c3 = combined & 0x3f;
            combined >>>= 6;
            int c2 = combined & 0x3f;
            combined >>>= 6;
            int c1 = combined & 0x3f;
            combined >>>= 6;
            int c0 = combined & 0x3f;

            // Translate into the equivalent alpha character
            // emitting them in big-endian order.
            sb.append(valueToChar[c0]);
            sb.append(valueToChar[c1]);
            sb.append(valueToChar[c2]);
            sb.append(valueToChar[c3]);
        }

        // deal with leftover bytes
        switch (leftover) {
            case 0:
            default:
                // nothing to do
                break;

            case 1:
                // One leftover byte generates xx==
                // Start a new line if next 4 chars won't fit on the current
                // line
                linePos += 4;
                if (linePos > lineLength) {
                    if (lineLength != 0) {
                        sb.append(lineSeparator);
                    }
                    linePos = 4;
                }

                // Handle this recursively with a faked complete triple.
                // Throw away last two chars and replace with ==
                sb.append(encode(new byte[]{b[len], 0, 0}).substring(0,
                        2));
                sb.append(spec3);
                sb.append(spec3);
                break;

            case 2:
                // Two leftover bytes generates xxx=
                // Start a new line if next 4 chars won't fit on the current
                // line
                linePos += 4;
                if (linePos > lineLength) {
                    if (lineLength != 0) {
                        sb.append(lineSeparator);
                    }
                    linePos = 4;
                }
                // Handle this recursively with a faked complete triple.
                // Throw away last char and replace with =
                sb.append(encode(new byte[]{b[len],
                        b[len + 1],
                        0}).substring(0, 3));
                sb.append(spec3);
                break;
        } // end switch;

        return sb.toString();
    }// end encode

    /**
     * Initialise both static and instance table.
     */
    private void initTables() {
        /* initialise valueToChar and charToValue tables */

        if (vc == null) {
            // statics are not initialised yet

            vc = new char[64];

            cv = new int[256];

            // build translate valueToChar table only once.
            // 0..25 -> 'A'..'Z'
            for (int i = 0; i <= 25; i++) {
                vc[i] = (char) ('A' + i);
            }
            // 26..51 -> 'a'..'z'
            for (int i = 0; i <= 25; i++) {
                vc[i + 26] = (char) ('a' + i);
            }
            // 52..61 -> '0'..'9'
            for (int i = 0; i <= 9; i++) {
                vc[i + 52] = (char) ('0' + i);
            }
            vc[62] = spec1;
            vc[63] = spec2;

            // build translate charToValue table only once.
            for (int i = 0; i < 256; i++) {
                cv[i] = IGNORE; // default is to ignore
            }

            for (int i = 0; i < 64; i++) {
                cv[vc[i]] = i;
            }

            cv[spec3] = PAD;
        }
        valueToChar = vc;
        charToValue = cv;
    }


}
