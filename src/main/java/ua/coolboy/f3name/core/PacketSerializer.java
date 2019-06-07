package ua.coolboy.f3name.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;

public class PacketSerializer {

    private ByteBuf buf;
    private byte[] result;

    public PacketSerializer(String string) {
        buf = Unpooled.buffer();
        /*byte[] str = string.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(str.length);
        buf.writeBytes(str);*/
        writeString(string, buf);
        result = buf.array();
        buf.release();
    }

    //wiki.vg methods
    private void writeString(String s, ByteBuf buf) {
        if (s.length() > Short.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("Cannot send string longer than Short.MAX_VALUE (got %s characters)", s.length()));
        }

        byte[] b = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(b.length, buf);
        buf.writeBytes(b);

    }

    private void writeVarInt(int value, ByteBuf output) {
        int part;
        while (true) {
            part = value & 0x7F;

            value >>>= 7;
            if (value != 0) {
                part |= 0x80;
            }

            output.writeByte(part);

            if (value == 0) {
                break;
            }
        }
    }

    public byte[] toArray() {
        return result;
    }

}
