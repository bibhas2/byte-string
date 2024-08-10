package org.mobiarch;

import java.nio.ByteBuffer;

/**
 * <p>A class that wraps a ByteBuffer and creates a String like
 * interface. The idea is to treat bytes like characters.</p>
 * 
 * <p><b>Caveat:</b> Most methods like parseInt() and trim() will only work if the data 
 * in ByteBuffer is in a multi-byte encoding, such as 
 * ASCII or UTF-8.</p>
 * <p>
 * The mental model of this class is very similar to  
 * std::string_view of C++.
 * </p>
 * <p>
 * Unlike Java's String, this class is fully mutable. Also, a substring
 * returns a slice and does not allocate or copy.
 * </p>
 */
public class ByteBufferString implements Comparable<ByteBufferString> {
    private ByteBuffer buff;

    /**
     * Creates a new ByteBufferString by wrapping over
     * the supplied ByteBuffer. No copy of data is made.
     * 
     * @param buff The ByteBuffer to wrap over. The underlying data should use a multi-byte 
     * encoding such as ASCII and UTF-8.
     */
    public ByteBufferString(ByteBuffer buff) {
        this.buff = buff;
    }

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
     * Prints this string.
     */
    public void print() {
        ByteBufferString.print(buff);
    }

    /**
     * 
     * Excludes the leading and trailing white spaces.
     * Unlike Java's String, the change is made in place.
     * 
     */
    public void trim() {
        if (buff.limit() == 0) {
            return;
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

        buff = buff.slice(start, end - start + 1);
    }

    /**
     * <p>Parses the ByteBuffer into a double. Aside from digits 0-9 only 
     * a leading "-" and a decimal "." character are permitted.</p>
     * 
     * <p>Valid values: -0.001, 100, -100, -.001, -12.001</p>
     * 
     * @return The parsed double value.
     */
    public double parseDouble() {
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
     * @return The parsed integer value.
     */
    public int parseInt() {
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

    @Override
    public int compareTo(ByteBufferString other) {
        return buff.compareTo(other.buff);
    }

    public ByteBuffer buffer() {
        return buff;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ByteBufferString) { 
            return ((ByteBufferString)obj).buffer().equals(this.buff);
        }

        return false;
    }

    /**
     * Converts the lowercase Latin characters to upper case.
     * Unlike Java's String class the change is made in place and no new
     * memory is allocated.
     */
    public void toUpperCase() {
        for (int i = 0; i < buff.limit(); ++i) {
            var ch = buff.get(i);

            if(ch >= 97 && ch <= 122) {
                buff.put(i, (byte) (ch - 32));
            }
        }
    }

    /*
     * TODO:
     * substring
     * length
     * toString
     */
}
