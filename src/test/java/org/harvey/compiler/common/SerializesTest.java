package org.harvey.compiler.common;

import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.junit.Assert;
import org.junit.Test;

public class SerializesTest {

    @Test
    public void makeHeader() {
        SerializableData element = Serializes.makeHead(
                new HeadMap(0x1A, 5), // 11010
                new HeadMap(0x0D, 5),  // 01101
                new HeadMap(0x0A_12, 13),  // 0_1010_0001_0010
                new HeadMap(0x14, 5), // 10100
                new HeadMap(0x15, 5) // 10101
        );
        // 1101_0011_0101_0100_0010_0101_0100_1010_1000_0000
        // D3_54_25_4A_80
        System.out.println(element);
    }

    @Test
    public void getBits() {
        // 0110_1100
        long value = 0xFE_DC_BA_98_76_53_32_10L;
        for (int i = 0; i < 64; i++) {
            if (i % 4 == 0) {
                System.out.print("_");
            }
            System.out.print(Long.toString(Serializes.getBits(value, -i - 1, -i), 2));
        }
        System.out.println();
        for (int i = 0; i < 64; i++) {
            if (i % 4 == 0) {
                System.out.print("_");
            }
            System.out.print(Long.toString(Serializes.getBits(value, i, i + 1), 2));
        }
        System.out.println();
        Assert.assertEquals(
                Serializes.getBits(value, 0, 64),
                Serializes.getBits(value, -64, 0)
        );
    }

    @Test
    public void getBit() {
    }

    @Test
    public void getLow() {
    }

    @Test
    public void lowBitFullOneMark() {
    }

    @Test
    public void bitCountToByteCount() {
    }
}