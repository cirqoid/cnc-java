package org.cirqoid.cnc.controller.commands;

import org.cirqoid.cnc.controller.settings.ApplicationConstants;

import java.nio.ByteBuffer;

public class GetDiagnosticDataResponse extends Response
{
    private int[] positions;

    public GetDiagnosticDataResponse(int packetId, byte[] payload)
    {
        super(packetId, Code.DIANGOSTIC_DATA_INFO);
        ByteBuffer b = ByteBuffer.wrap(payload);

        positions = new int[ApplicationConstants.MAX_AXES_COUNT];
        for (int i = 0; i < positions.length; i++)
            positions[i] = b.getInt();

    }

    public int[] getPositions()
    {
        return positions;
    }
}
