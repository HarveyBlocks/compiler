package org.harvey.compiler.common.util;

import org.junit.Test;

import java.util.Random;

public class ByteUtilTest {

    @Test
    public void testPhaseUnsignedInt8() {
        char[] dir = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        Random r = new Random();
        long count = 0;
        for (long i = Long.MIN_VALUE; i < 0; i += r.nextLong() & 0xff_ff_ff_ffL) {
            byte[] rawBytes = ByteUtil.toRawBytes(i);
            StringBuilder sb = new StringBuilder();
            for (byte rawByte : rawBytes) {
                sb.append(dir[(rawByte & 0xf0) >>> 4]).append(dir[rawByte & 0xf]);
            }
            byte[] result = ByteUtil.phaseUnsignedInt8(sb.toString(), 16);
            for (int j = 0; j < 8; j++) {
                if (result[j] != rawBytes[j]) {
                    throw new RuntimeException(sb.toString());
                }
            }
            count++;
            if ((count & 0x7fffff) == 0) {
                System.out.println(count / 0x800000 + "\t" + i);
            }
        }
    }
}