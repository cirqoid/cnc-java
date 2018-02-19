package org.cirqoid.cnc.controller.commands;

public class GetDiagnosticDataCommand extends Command
{
    @Override
    public Type getType()
    {
        return Type.GET_DIAGNOSTIC_DATA;
    }

    @Override
    public byte[] getPayload()
    {
        return new byte[0];
    }
}
