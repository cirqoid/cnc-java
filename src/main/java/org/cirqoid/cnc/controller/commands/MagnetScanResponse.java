package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

public class MagnetScanResponse extends Response
{
    private double target;
    private int position;
    private int lastPosition;
    private int fieldStrength;
    private boolean isValidReading;

    private int position2;
    private int lastPosition2;
    private int fieldStrength2;
    private boolean isValidReading2;

    public MagnetScanResponse(int packetId, byte[] payload)
    {
        super(packetId, Code.MAGNET_SCAN);
        ByteBuffer b = ByteBuffer.wrap(payload);
        target = (double)b.getInt() / 1000;
        position = b.getInt();
        lastPosition = b.getInt();
        fieldStrength = b.getInt();
        isValidReading = b.getInt() == 1;

        if (payload.length > 20)
        {
            position2 = b.getInt();
            lastPosition2 = b.getInt();
            fieldStrength2 = b.getInt();
            isValidReading2 = b.getInt() == 1;
        }
    }

    public double getTarget()
    {
        return target;
    }

    public int getPosition()
    {
        return position;
    }

    public int getLastPosition()
    {
        return lastPosition;
    }

    public int getFieldStrength()
    {
        return fieldStrength;
    }

    public boolean isValidReading()
    {
        return isValidReading;
    }

    public int getPosition2()
    {
        return position2;
    }

    public int getLastPosition2()
    {
        return lastPosition2;
    }

    public int getFieldStrength2()
    {
        return fieldStrength2;
    }

    public boolean isValidReading2()
    {
        return isValidReading2;
    }
}
