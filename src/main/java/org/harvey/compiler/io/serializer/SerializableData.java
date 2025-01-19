package org.harvey.compiler.io.serializer;

import org.harvey.compiler.common.Serializes;
import org.harvey.compiler.exception.CompilerException;
import org.harvey.compiler.exception.io.CompilerFileReaderException;
import org.harvey.compiler.exception.io.CompilerFileWriterException;

/**
 * 写进二进制文件的元素的元素
 *
 * @author <a href="mailto:harvey.blocks@outlook.com">Harvey Blocks</a>
 * @version 1.0
 * @date 2024-11-28 16:12
 */
public class SerializableData {
    public static final HeadMap[] EMPTY_HEAD_MAP_ARRAY = new HeadMap[0];
    public static final SerializableData EMPTY = new SerializableData(0);

    private final byte[] data;


    public SerializableData(int initLength) {
        this.data = new byte[initLength];
    }

    public SerializableData(byte[] data) {
        this(data, 0, data.length);
    }

    public SerializableData(byte[] data, int off, int len) {
        this(len);
        System.arraycopy(data, off, this.data, 0, data.length);
    }

    public SerializableData(SerializableData head, SerializableData body) {
        this(head.data, body.data);
    }

    public SerializableData(byte[] head, SerializableData body) {
        this(head, body.data);
    }

    public SerializableData(SerializableData head, byte[] body) {
        this(head.data, body);
    }

    public SerializableData(byte[] head, byte[] body) {
        this.data = new byte[head.length + body.length];
        this.write(head, 0);
        this.write(body, head.length);
    }

    public static SerializableData create(byte... data) {
        return data == null ? null : new SerializableData(data);
    }

    public static SerializableData concat(SerializableData[] elements) {
        int sizeSum = 0;
        for (SerializableData element : elements) {
            sizeSum += element.data.length;
        }
        SerializableData result = new SerializableData(sizeSum);
        int p = 0;
        for (SerializableData element : elements) {
            byte[] src = element.data;
            System.arraycopy(src, 0, result.data, p, src.length);
            p += src.length;
        }
        return result;
    }

    public static SerializableData concat(SerializableData head, SerializableData[] elements) {
        SerializableData[] newElements = new SerializableData[elements.length + 1];
        newElements[0] = head;
        System.arraycopy(elements, 0, newElements, 1, elements.length);
        return concat(newElements);
    }

    public void write(byte[] src, int fromDataIndex) {
        System.arraycopy(src, 0, data, fromDataIndex, src.length);
    }

    public void write(SerializableData src, int fromDataIndex) {
        write(src.data, fromDataIndex);
    }

    public byte[] data() {
        return data;
    }

    public int length() {
        return data.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("0x");
        for (byte datum : this.data) {
            int value = (int) Serializes.unsignedByteToLong(datum);
            sb.append("_").append(Integer.toString(value, 16));
        }
        return sb.toString();
    }


    public HeadMap[] phaseHeader(int... bitCounts) {
        if (bitCounts == null) {
            return null;
        }
        if (bitCounts.length == 0) {
            return EMPTY_HEAD_MAP_ARRAY;
        }
        HeadMap[] headMaps = new HeadMap[bitCounts.length];
        for (int i = 0, bitIndex = 0; i < bitCounts.length; i++) {
            int mapBitCount = bitCounts[i];
            HeadMap.assertSupport(mapBitCount);
            int byteStartIndex = bitIndex >>> 3;
            int bitStartIndex = bitIndex & 7;
            bitIndex += mapBitCount;
            int byteEndIndex = bitIndex >>> 3;
            int bitEndIndex = bitIndex & 7;
            if (byteStartIndex == byteEndIndex) {
                // 同一个byte里的读取操作
                // 将mapBitCount长度的mapValue 从 bitStartIndex到bitEndIndex读出
                if (bitStartIndex > bitEndIndex) {
                    throw new CompilerFileWriterException("I don't known how it happens, it's imposible",
                            new IllegalStateException());
                }
                long mapValue = (byte) Serializes.getBits(data[byteStartIndex], -8 + bitStartIndex, -8 + bitEndIndex);
                headMaps[i] = new HeadMap(mapValue, mapBitCount);
                continue;
            }
            // 从data中取出Start后不足一个Byte的部分填充的mapValue的前端
            byte startBit = (byte) Serializes.getBits(data[byteStartIndex], -8 + bitStartIndex, 0);
            byteStartIndex++;
            long mapValue = Serializes.unsignedByteToLong(startBit) << (mapBitCount - 8 + bitStartIndex);
            // 从data中取出End前不足一个Byte的部分填充的mapValue的末尾
            int moveToLeft = 0;
            if (bitEndIndex != 0) {
                byte endByte = (byte) Serializes.getBits(data[byteEndIndex], -8, -8 + bitEndIndex);
                mapValue |= endByte;
                moveToLeft += bitEndIndex;
            }
            byteEndIndex--;
            // byte的全部赋值
            for (int j = byteStartIndex; j <= byteEndIndex; j++) {
                mapValue |= Serializes.unsignedByteToLong(data[j]) << moveToLeft;
                moveToLeft += 8;
            }
            headMaps[i] = new HeadMap(mapValue, mapBitCount);
        }
        return headMaps;
    }

    public SerializableData read(int formIndex, int length) {
        if (data.length <= formIndex) {
            throw new CompilerException("form index mast smaller than data length", new CompilerFileReaderException());
        }
        if (data.length < formIndex + length) {
            throw new CompilerException("no more data to read", new CompilerFileReaderException());
        }
        SerializableData element = new SerializableData(length);
        System.arraycopy(data, formIndex, element.data, 0, length);
        return element;
    }

    public SerializableData[] readAll(int formIndex, int eachSize, int size) {
        if (data.length <= formIndex) {
            throw new CompilerException("form index mast smaller than data length", new CompilerFileReaderException());
        }
        if (data.length < formIndex + eachSize * size) {
            throw new CompilerException("no more data to read", new CompilerFileReaderException());
        }
        SerializableData[] elements = new SerializableData[size];
        for (int i = formIndex, j = 0; i < data.length && j < size; i += eachSize, j++) {
            elements[j] = read(i, eachSize);
        }
        return elements;
    }
}
