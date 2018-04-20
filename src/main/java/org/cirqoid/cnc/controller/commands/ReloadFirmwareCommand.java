package org.cirqoid.cnc.controller.commands;

import java.nio.ByteBuffer;

/**
 * Created by simon on 29.06.17.
 */
public class ReloadFirmwareCommand extends Command
{
    private boolean bootMode;

    public ReloadFirmwareCommand(boolean bootMode)
    {
        this.bootMode = bootMode;
    }

    @Override
    public Type getType()
    {
        return Type.RELOAD_BOOTLOADER;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(bootMode ? 1 : 0);
        return b.array();
    }

    @Override
    public String toString()
    {
        return "ReloadFirmwareCommand{id = " + getId() + "}";
    }
}
