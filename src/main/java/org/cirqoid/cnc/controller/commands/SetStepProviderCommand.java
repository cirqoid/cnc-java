package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

public class SetStepProviderCommand  extends Command
{
    private int axis;
    private boolean enable;

    public SetStepProviderCommand(int axis, boolean enable)
    {
        this.axis = axis;
        this.enable = enable;
    }

    @Override
    public Type getType()
    {
        return Type.SET_STEP_PROVIDER;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4 * 2);
        b.putInt(axis);
        b.putInt(enable ? 1 : 0);
        return b.array();
    }
}
