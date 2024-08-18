package org.mobiarch;

import java.nio.ByteBuffer;

/**
 * <p>A class that gives String like interface to ByteBuffer.
 * Common operations like trim() and toUpperCase() are done without 
 * allocating any memory or copying data.
 * </p>
 * 
 * <p><b>Caveat:</b> The ByteBuffer must contain bytes encoded in UTF-8
 * or a subset like ASCII.
 * </p>
 * 
 * <p>
 * The mental model of this class is very similar to  
 * std::string_view of C++. This will be useful to port std::string_view 
 * based code to Java.
 * </p>
 * 
 */
public class ByteStr {
    /*
     * Dumps the bytes in a ByteBuffer as-is to console. This is pretty
     * much how std::string or std::string_view is printed in C++.
     * 
     * Most modern terminals will be able to interpret the byte sequence as UTF-8.
     */
    public static void print(ByteBuffer buff) {
        if (buff == null) {
            System.out.println("null");

            return;
        }

        //Dump bytes
        for (int i = 0; i < buff.limit(); ++i) {
            System.out.write((int) buff.get(i));
        }
    }

    /**
     * 
     * Returns a slice of the supplied buffer by
     * excluding the leading and trailing white spaces.
     * 
     * @param buff The ByteBuffer to trim.
     * 
     * @return A new ByteBuffer that is a slice of the supplied
     * ByteBuffer. The original ByteBuffer is left unaltered.
     */
    public static ByteBuffer trim(ByteBuffer buff) {
        if (buff.limit() == 0) {
            return buff;
        }

        int start = 0;
        int end = 0;
        int i;
        final int MAX_INDEX = buff.limit() - 1;

        for (i = 0; i < buff.limit(); ++i) {
            var ch = buff.get(i);

            if (ch != 32 && ch != 9) {
                break;
            }
        }

        //Clip start to max index.
        start = Math.min(i, MAX_INDEX);

        for (i = buff.limit() - 1; i >= 0; --i) {
            var ch = buff.get(i);

            if (ch != 32 && ch != 9) {
                break;
            }

            //Early breaking
            if (i < start) {
                break;
            }
        }

        end = i;

        if (end < start) {
            //This happens for a completely white space string.
            end = start - 1;
        }

        return buff.slice(start, end - start + 1);
    }

    /**
     * <p>Parses the ByteBuffer into a double. Aside from digits 0-9 only 
     * a leading "-" and a decimal "." character are permitted.</p>
     * 
     * <p>Valid values: -0.001, 100, -100, -.001, -12.001</p>
     * 
     * @param buff The ByteBuffer to parse.
     * 
     * @return The parsed double value.
     */
    public static double parseDouble(ByteBuffer buff) {
        double result = 0.0;
        int decimalBase = 1;
        int base = 1;
        boolean hasDecimal = false;
     
        for (int i = buff.limit() - 1; i >= 0; --i) {
            int ch = buff.get(i);
     
            //Deal with minus sign
            if (i == 0 && ch == 45) {
                result *= -1;
     
                continue;
            }

            if (ch == 46) {
                hasDecimal = true;

                continue;
            }
     
            if (ch < 48 || ch > 57) {
                throw new NumberFormatException("Invalid input.");
            }
     
            int digit = buff.get(i) - 48;
     
            result = result + digit * base;
     
            base = base * 10;

            if (!hasDecimal) {
                decimalBase *= 10;
            }
        }
     
        if (hasDecimal) {
            return result / decimalBase;
        } else {
            return result;
        }
    }

    /**
     * <p>Parses the ByteBuffer into an integer. Aside from digits 0-9 only 
     * a leading "-" character is permitted.</p>
     * 
     * <p>Valid values: 100, -100, -0012</p>
     * 
     * @param buff The ByteBuffer to parse.
     * 
     * @return The parsed integer value.
     */
    public static int parseInt(ByteBuffer buff) {
        int result = 0;
        int base = 1;
     
        for (int i = buff.limit() - 1; i >= 0; --i) {
            int ch = buff.get(i);
     
            //Deal with minus sign
            if (i == 0 && ch == 45) {
                result *= -1;
     
                continue;
            }
     
            if (ch < 48 || ch > 57) {
                throw new NumberFormatException("Invalid input.");
            }
     
            int digit = buff.get(i) - 48;
     
            result = result + digit * base;
     
            base = base * 10;
        }
     
        return result;
    }

    /**
     * Converts the lowercase Latin characters to upper case.
     * Unlike Java's String class the change is made in place and no new
     * memory is allocated.
     * 
     * @param buff The ByteBuffer to convert to upper case.
     */
    public static void toUpperCase(ByteBuffer buff) {
        for (int i = 0; i < buff.limit(); ++i) {
            var ch = buff.get(i);

            if(ch >= 97 && ch <= 122) {
                buff.put(i, (byte) (ch - 32));
            }
        }
    }

    /**
     * Converts the upper case Latin characters to lower case.
     * Unlike Java's String class the change is made in place and no new
     * memory is allocated.
     * 
     * @param buff The ByteBuffer to convert to lower case.
     */
    public static void toLowerCase(ByteBuffer buff) {
        for (int i = 0; i < buff.limit(); ++i) {
            var ch = buff.get(i);

            if(ch >= 65 && ch <= 90) {
                buff.put(i, (byte) (ch + 32));
            }
        }
    }

    /**
     * Returns the first index of a byte in the ByteBuffer
     * starting from the specified index.
     * 
     * @param buff The buffer to search.
     * @param ch The byte to look for.
     * @param fromIndex Start searching from this index.
     * @return The index where the given byte appears. If not found -1
     * is returned.
     */
    public static int indexOf(ByteBuffer buff, byte ch, int fromIndex) {
        for (int i = fromIndex; i < buff.limit(); ++i) {
            if (buff.get(i) == ch) {
                return i;
            }
        }

        return -1;
    }
}
