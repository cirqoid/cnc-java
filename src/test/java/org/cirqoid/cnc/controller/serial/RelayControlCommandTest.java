package org.cirqoid.cnc.controller.serial;

import org.cirqoid.cnc.controller.commands.RelayControlCommand;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by simon on 27.06.17.
 */
public class RelayControlCommandTest
{

    @Test
    public void testSerialization()
    {
        RelayControlCommand command = new RelayControlCommand(1000);
        command.setId(12345);
        byte[] p = command.serializePacket();

        assertEquals(22, p.length);
        // Header
        assertEquals((byte)0xAA, p[0]);
        assertEquals((byte)0xAA, p[1]);

        // Id
        assertEquals(0x00, p[2]);
        assertEquals(0x00, p[3]);
        assertEquals(0x30, p[4]);
        assertEquals(0x39, p[5]);

        // Type
        assertEquals(0x00, p[6]);
        assertEquals(0x00, p[7]);
        assertEquals(0x00, p[8]);
        assertEquals(0x08, p[9]);

        // Length
        assertEquals(0x00, p[10]);
        assertEquals(0x00, p[11]);
        assertEquals(0x00, p[12]);
        assertEquals(0x04, p[13]);

        // speed
        assertEquals((byte) 0x00, p[14]);
        assertEquals((byte) 0x00, p[15]);
        assertEquals((byte) 0x03, p[16]);
        assertEquals((byte) 0xE8, p[17]);

        // CRC
        assertEquals((byte) 0x0C, p[18]);
        assertEquals((byte) 0x25, p[19]);
        assertEquals((byte) 0xD5, p[20]);
        assertEquals((byte) 0x77, p[21]);

    }

}
