package org.cirqoid.cnc.controller.commands;

public class MeasureDifferentialOffsetCommand extends Command
{
    @Override
    public Type getType()
    {
        return Type.MEASURE_DIFFERENTIAL_OFFSET;
    }

    @Override
    public byte[] getPayload()
    {
        return new byte[0];
    }
}
