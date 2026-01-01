package sy.common.util;

import io.netty.buffer.ByteBuf;
import io.netty.util.internal.MathUtil;
import io.netty.util.internal.StringUtil;

import static io.netty.util.internal.StringUtil.NEWLINE;

public class SByteBufLogger {

    private static final String[] HEXDUMP_ROWPREFIXES = new String[4096];
    private static final char[] HEXDUMP_TABLE = new char[1024];
    private static final String[] BYTE2HEX = new String[256];
    private static final char[] BYTE2CHAR = new char[256];
    private static final String[] HEXPADDING = new String[16];
    private static final String[] BYTEPADDING = new String[16];

    static {
        char[] DIGITS = "0123456789abcdef".toCharArray();

        int i;
        for(i = 0; i < 256; ++i) {
            HEXDUMP_TABLE[i << 1] = DIGITS[i >>> 4 & 15];
            HEXDUMP_TABLE[(i << 1) + 1] = DIGITS[i & 15];
        }

        int padding;
        StringBuilder buf;
        int j;
        for(i = 0; i < HEXPADDING.length; ++i) {
            padding = HEXPADDING.length - i;
            buf = new StringBuilder(padding * 3);

            for(j = 0; j < padding; ++j) {
                buf.append("   ");
            }

            HEXPADDING[i] = buf.toString();
        }

        for(i = 0; i < HEXDUMP_ROWPREFIXES.length; ++i) {
            StringBuilder buf1 = new StringBuilder(12);
            buf1.append(StringUtil.NEWLINE);
            buf1.append(Long.toHexString((long)(i << 4) & 4294967295L | 4294967296L));
            buf1.setCharAt(buf1.length() - 9, '|');
            buf1.append('|');
            HEXDUMP_ROWPREFIXES[i] = buf1.toString();
        }

        for(i = 0; i < BYTE2HEX.length; ++i) {
            BYTE2HEX[i] = ' ' + StringUtil.byteToHexStringPadded(i);
        }

        for(i = 0; i < BYTEPADDING.length; ++i) {
            padding = BYTEPADDING.length - i;
            buf = new StringBuilder(padding);

            for(j = 0; j < padding; ++j) {
                buf.append(' ');
            }

            BYTEPADDING[i] = buf.toString();
        }

        for(i = 0; i < BYTE2CHAR.length; ++i) {
            if (i > 31 && i < 127) {
                BYTE2CHAR[i] = (char)i;
            } else {
                BYTE2CHAR[i] = '.';
            }
        }

    }

    private static void appendHexDumpRowPrefix(StringBuilder dump, int row, int rowStartIndex) {
        if (row < HEXDUMP_ROWPREFIXES.length) {
            dump.append(HEXDUMP_ROWPREFIXES[row]);
        } else {
            dump.append(StringUtil.NEWLINE);
            dump.append(Long.toHexString((long)rowStartIndex & 4294967295L | 4294967296L));
            dump.setCharAt(dump.length() - 9, '|');
            dump.append('|');
        }

    }

    public static void printByteBufWithChars(ByteBuf buffer) {
        int length = buffer.readableBytes();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 80 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        printHexDumpWithChars(buf, buffer);
        System.out.println(buf);
    }

    public static void printOriginalBuf(ByteBuf buffer) {
        int length = buffer.capacity();
        int rows = length / 16 + (length % 15 == 0 ? 0 : 1) + 4;
        StringBuilder buf = new StringBuilder(rows * 63 * 2)
                .append("read index:").append(buffer.readerIndex())
                .append(" write index:").append(buffer.writerIndex())
                .append(" capacity:").append(buffer.capacity())
                .append(NEWLINE);
        dumpOriBytes(buf, buffer, 0, buffer.capacity());
        System.out.println(buf);
    }

    /** 打印整个Buf的原码*/
    public static void dumpOriBytes(StringBuilder dump, ByteBuf buf, int offset, int maxLen) {
        if (offset >= buf.capacity()) {
            return;
        }
        int length = Math.min(maxLen, buf.writerIndex() - offset);
        if (length <= 0)
            return;
        dump.append("+--------+-------------------------------------------------+");
        int fullRows = length >>> 4;
        int remainder = length & 15;

        int rowStartIndex;
        int rowEndIndex;
        int j;
        for(rowStartIndex = 0; rowStartIndex < fullRows; ++rowStartIndex) {
            rowEndIndex = (rowStartIndex << 4) + offset;
            appendHexDumpRowPrefix(dump, rowStartIndex, rowEndIndex);
            j = rowEndIndex + 16;

            int k;
            for(k = rowEndIndex; k < j; ++k) {
                dump.append(BYTE2HEX[buf.getUnsignedByte(k)]);
            }

            dump.append(" |");
        }

        if (remainder != 0) {
            rowStartIndex = (fullRows << 4) + offset;
            appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);
            rowEndIndex = rowStartIndex + remainder;

            for(j = rowStartIndex; j < rowEndIndex; ++j) {
                dump.append(BYTE2HEX[buf.getUnsignedByte(j)]);
            }

            dump.append(HEXPADDING[remainder]);
            dump.append(" |");
        }

        dump.append(StringUtil.NEWLINE + "+--------+-------------------------------------------------+" + StringUtil.NEWLINE);
    }

    public static void printHexDumpWithChars(StringBuilder dump, ByteBuf buf) {
        printHexDumpWithChars(dump, buf, buf.readerIndex(), buf.readableBytes());
    }

    private static void printHexDumpWithChars(StringBuilder dump, ByteBuf buf, int offset, int length) {
        if (MathUtil.isOutOfBounds(offset, length, buf.capacity())) {
            throw new IndexOutOfBoundsException("expected: 0 <= offset(" + offset + ") <= offset + length(" + length + ") <= buf.capacity(" + buf.capacity() + ')');
        } else if (length != 0) {
            dump.append("+--------+-------------------------------------------------+----------------+");
//            dump.append("         +-------------------------------------------------+" + StringUtil.NEWLINE + "         |  0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f |" + StringUtil.NEWLINE + "+--------+-------------------------------------------------+----------------+");
            int fullRows = length >>> 4;
            int remainder = length & 15;

            int rowStartIndex;
            int rowEndIndex;
            int j;
            for(rowStartIndex = 0; rowStartIndex < fullRows; ++rowStartIndex) {
                rowEndIndex = (rowStartIndex << 4) + offset;
                appendHexDumpRowPrefix(dump, rowStartIndex, rowEndIndex);
                j = rowEndIndex + 16;

                int k;
                for(k = rowEndIndex; k < j; ++k) {
                    dump.append(BYTE2HEX[buf.getUnsignedByte(k)]);
                }

                dump.append(" |");

                for(k = rowEndIndex; k < j; ++k) {
                    dump.append(BYTE2CHAR[buf.getUnsignedByte(k)]);
                }

                dump.append('|');
            }

            if (remainder != 0) {
                rowStartIndex = (fullRows << 4) + offset;
                appendHexDumpRowPrefix(dump, fullRows, rowStartIndex);
                rowEndIndex = rowStartIndex + remainder;

                for(j = rowStartIndex; j < rowEndIndex; ++j) {
                    dump.append(BYTE2HEX[buf.getUnsignedByte(j)]);
                }

                dump.append(HEXPADDING[remainder]);
                dump.append(" |");

                for(j = rowStartIndex; j < rowEndIndex; ++j) {
                    dump.append(BYTE2CHAR[buf.getUnsignedByte(j)]);
                }

                dump.append(BYTEPADDING[remainder]);
                dump.append('|');
            }

            dump.append(StringUtil.NEWLINE + "+--------+-------------------------------------------------+----------------+" + StringUtil.NEWLINE);
        }
    }
}
