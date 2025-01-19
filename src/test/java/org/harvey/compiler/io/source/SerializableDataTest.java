package org.harvey.compiler.io.source;

import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.io.serializer.HeadMap;
import org.harvey.compiler.io.serializer.SerializableData;
import org.junit.Test;

import java.util.Arrays;

public class SerializableDataTest {

    @Test
    public void phaseHeader() {
        SerializableData element = new SerializableData(new byte[]{
                (byte) 0xa4,  // 1010_0100_1000_1000_1010_0101_0100_1011
                (byte) 0x88,
                (byte) 0xa5,
                (byte) 0x4b,
                (byte) 0x52, // 0101_0010_1001_0100_1010_0101_0101_0010
                (byte) 0x94,
                (byte) 0xa5,
                (byte) 0x52,
                (byte) 0x94,// 1001_0100_1000_0000
                (byte) 0x80,
        });
        // 1
        // 01
        // 001
        // 00100
        // 0_1000_1010_0101
        // 0100_1011
        // 01
        // 0_1001_0100_1010_0101_0010_1010
        int[] numbers = {1, 2, 3, 5, 13, 8, 2, 25};
        int sum = Arrays.stream(numbers).sum();
        HeadMap[] headMaps = element.phaseHeader(numbers);
        System.out.println(Arrays.toString(headMaps));
    }

    @Test
    public void read() {
        SerializableData element = new SerializableData(new byte[]{
                (byte) 0xa4,  // 1010_0100_1000_1000_1010_0101_0100_1011
                (byte) 0x88,
                (byte) 0xa5,
                (byte) 0x4b,
                (byte) 0x52, // 0101_0010_1001_0100_1010_0101_0101_0010
                (byte) 0x94,
                (byte) 0xa5,
                (byte) 0x52,
                (byte) 0x94,// 1001_0100_1000_0000
                (byte) 0x80,
        });
        int[] numbers = {1, 2, 3, 5, 13, 8, 2, 25};
        int sum = Serializes.bitCountToByteCount(Arrays.stream(numbers).sum());
        HeadMap[] headMaps = element.phaseHeader(numbers);
        System.out.println(element.read(sum, 2));
        System.out.println(Arrays.toString(element.readAll(sum, 1, 2)));
    }

    @Test
    public void readAll() {
    }
}