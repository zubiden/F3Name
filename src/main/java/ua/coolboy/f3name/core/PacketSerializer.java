package ua.coolboy.f3name.core;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;

public class PacketSerializer {

    private ByteBuf buf;
    private byte[] result;

    public PacketSerializer(String string) {
        buf = Unpooled.buffer();
        byte[] str = string.getBytes(StandardCharsets.UTF_8);
        buf.writeByte(str.length);
        buf.writeBytes(str);
        result = buf.array();
        buf.release();
    }

    public byte[] toArray() {
        return result;
    }

}
