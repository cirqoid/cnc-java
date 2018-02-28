package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

public class MagnetScanCommand extends Command
{
    private int axis;

    public MagnetScanCommand(int axis)
    {
        this.axis = axis;
    }

    @Override
    public Type getType()
    {
        return Type.SCAN_AXIS;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(axis);
        return b.array();
    }
}
