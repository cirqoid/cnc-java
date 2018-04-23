package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

public class MeasureDifferentialOffsetResponse extends Response
{
    private static final int INVALID_OFFSET = 9999999;
    private int offset;

    public MeasureDifferentialOffsetResponse(int packetId, byte[] payload)
    {
        super(packetId, Code.DIFFERENTIAL_OFFSET);
        ByteBuffer b = ByteBuffer.wrap(payload);
        offset = b.getInt();
    }

    public int getOffset()
    {
        return offset;
    }

    public boolean isOffsetValid()
    {
        return offset != INVALID_OFFSET;
    }
}
