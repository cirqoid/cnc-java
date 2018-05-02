package org.cirqoid.cnc.controller.interpreter;

import org.cirqoid.cnc.controller.commands.*;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by simon on 19.06.17.
 */
public class InterpreterTest
{

    @Test
    public void testRapidMotion() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> packets = interpreter.interpretBlock("G0 X10 Y20.4 Z-5.2 A22");
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof RapidMotionCommand);
        RapidMotionCommand packet = (RapidMotionCommand) packets.get(0);

        assertEquals(10000, packet.getPositions()[0]);
        assertEquals(20400, packet.getPositions()[1]);
        assertEquals(-5200, packet.getPositions()[2]);
        assertEquals(22000, packet.getPositions()[3]);
    }

    @Test
    public void testLinearMotion() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> packets = interpreter.interpretBlock("G1 X10 Y20.4 Z-5.2 A22 F1000");
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof LinearInterpolationCommand);
        LinearInterpolationCommand packet = (LinearInterpolationCommand) packets.get(0);

        assertEquals(10000, packet.getTarget()[0]);
        assertEquals(20400, packet.getTarget()[1]);
        assertEquals(-5200, packet.getTarget()[2]);
        assertEquals(22000, packet.getTarget()[3]);
        assertEquals(1000000, packet.getFeed());
    }

    @Test
    public void testCWCircularInterpolation() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> packets = interpreter.interpretBlock("G2 X10 Y10 I10 J0 F1000");
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof CircularInterpolationCommand);
        CircularInterpolationCommand packet = (CircularInterpolationCommand) packets.get(0);
        assertEquals(10000, packet.getPositions()[0]);
        assertEquals(10000, packet.getPositions()[1]);
        assertEquals(0, packet.getPositions()[2]);
        assertEquals(0, packet.getPositions()[3]);
        assertEquals(10000, packet.getRadius());
        assertEquals(10000, packet.getCenterCoordinates()[0]);
        assertEquals(0, packet.getCenterCoordinates()[1]);
        assertEquals(Context.Plane.XY, packet.getPlane());
        assertEquals(true, packet.isClockwise());
        assertEquals(1000000, packet.getFeed());
    }

    @Test
    public void testCCWCircularInterpolation() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> packets = interpreter.interpretBlock("G3 X10 Y10 I10 J0 F1000");
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof CircularInterpolationCommand);
        CircularInterpolationCommand packet = (CircularInterpolationCommand) packets.get(0);
        assertEquals(10000, packet.getPositions()[0]);
        assertEquals(10000, packet.getPositions()[1]);
        assertEquals(0, packet.getPositions()[2]);
        assertEquals(0, packet.getPositions()[3]);
        assertEquals(10000, packet.getRadius());
        assertEquals(10000, packet.getCenterCoordinates()[0]);
        assertEquals(0, packet.getCenterCoordinates()[1]);
        assertEquals(Context.Plane.XY, packet.getPlane());
        assertEquals(false, packet.isClockwise());
        assertEquals(1000000, packet.getFeed());
    }

    @Test
    public void testSelectWcs() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> packets = interpreter.interpretBlock("G54");
        assertTrue(packets.isEmpty());
        assertEquals(1, interpreter.getContext().getCurrentWcs());
        packets = interpreter.interpretBlock("G53");
        assertTrue(packets.isEmpty());
        assertEquals(0, interpreter.getContext().getCurrentWcs());
    }

    @Test
    public void testSetOffsets() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> packets = interpreter.interpretBlock("G92 X1 Y2 Z-5");
        assertTrue(packets.isEmpty());
        assertEquals(1000, interpreter.getContext().getOffset(1, 0));
        assertEquals(2000, interpreter.getContext().getOffset(1, 1));
        assertEquals(-5000, interpreter.getContext().getOffset(1, 2));
    }

    @Test
    public void testWcs() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        interpreter.interpretBlock("G92 X10 Y20 Z-10");
        interpreter.interpretBlock("G54");
        List<Command> packets = interpreter.interpretBlock("G0 X10 Y10 Z5");
        assertEquals(1, packets.size());
        assertTrue(packets.get(0) instanceof RapidMotionCommand);
        RapidMotionCommand packet = (RapidMotionCommand) packets.get(0);

        assertEquals(20000, packet.getPositions()[0]);
        assertEquals(30000, packet.getPositions()[1]);
        assertEquals(-5000, packet.getPositions()[2]);
        assertEquals(0, packet.getPositions()[3]);
    }

    @Test
    public void testSpindleOn() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlock("S1300 M3");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof SpindleControlCommand);
        SpindleControlCommand command = (SpindleControlCommand) commands.get(0);
        assertEquals(1300, command.getSpeed());
    }

    @Test
    public void testSpindleOff() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlock("M5");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof SpindleControlCommand);
        SpindleControlCommand command = (SpindleControlCommand) commands.get(0);
        assertEquals(0, command.getSpeed());
    }

    @Test
    public void testSpindleNoSpeed()
    {
        Interpreter interpreter = new Interpreter();
        try
        {
            interpreter.interpretBlock("M3");
            fail("Interpreter should fail with no speed defined");
        }
        catch (ParsingException e)
        {
        }
    }

    @Test
    public void testSpindleOnOff() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        interpreter.interpretBlock("S1300 M3");
        interpreter.interpretBlock("M5");
        List<Command> commands = interpreter.interpretBlock("M3");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof SpindleControlCommand);
        SpindleControlCommand command = (SpindleControlCommand) commands.get(0);
        assertEquals(1300, command.getSpeed());
    }

    @Test
    public void testRelays() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlock("M7");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof RelayControlCommand);
        RelayControlCommand command = (RelayControlCommand) commands.get(0);
        assertEquals(0x01, command.getStatus());

        commands = interpreter.interpretBlock("M8");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof RelayControlCommand);
        command = (RelayControlCommand) commands.get(0);
        assertEquals(0x03, command.getStatus());

        commands = interpreter.interpretBlock("M9");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof RelayControlCommand);
        command = (RelayControlCommand) commands.get(0);
        assertEquals(0x00, command.getStatus());
    }

    @Test
    public void testPause() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlock("G4 P1.5");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof SleepCommand);
        SleepCommand command = (SleepCommand) commands.get(0);
        assertEquals(1500, command.getDuration());
    }

    @Test
    public void testHoming() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlock("G28 Y-1.5");
        assertEquals(1, commands.size());
        assertTrue(commands.get(0) instanceof HomeCommand);
        HomeCommand command = (HomeCommand) commands.get(0);
        assertEquals(-1500, command.getParameters()[1]);
    }

    @Test
    public void testExitSpeedsYStraight() throws ParsingException {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G1 Y01 X0 F1500\n" +
                "G1 Y02 X0 F1500\n" +
                "G1 Y03 X0 F1500\n" +
                "G1 Y04 X0 F1500\n" +
                "G1 Y05 X0 F1500\n" +
                "G1 Y05.15 X0 F1500\n" +
                "G1 Y05.151 X0 F1500\n");

        assertEquals(910, ((LinearInterpolationCommand)commands.get(0)).getMaxExitSpeed());
        assertEquals(173, ((LinearInterpolationCommand)commands.get(4)).getMaxExitSpeed());
    }

    @Test
    public void testExitSpeedsStraightLineByAngle() throws ParsingException {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G1 Y01 X1 F1500\n" +
                "G1 Y02 X2 F1500\n" +
                "G1 Y03 X3 F1500\n" +
                "G1 Y04 X4 F1500\n" +
                "G1 Y05 X5 F1500\n" +
                "G1 Y05.15 X05.15 F1500\n" +
                "G1 Y05.16 X05.16 F1500\n");

        assertEquals(1083, ((LinearInterpolationCommand)commands.get(0)).getMaxExitSpeed());
        assertEquals(212, ((LinearInterpolationCommand)commands.get(4)).getMaxExitSpeed());
    }

    @Test
    public void testExitSpeedsStraightLineTurns90() throws ParsingException {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G1 Y01 X0 F1500\n" +
                "G1 Y02 X0 F1500\n" +
                "G1 Y03 X0 F1500\n" +
                "G1 Y04 X0 F1500\n" +
                "G1 Y05 X0 F1500\n" +
                "G1 Y05 X1 F1500\n" +
                "G1 Y05 X2 F1500\n" +
                "G1 Y05 X3 F1500\n" +
                "G1 Y05 X4 F1500\n" +
                "G1 Y05 X5 F1500\n");

        assertEquals(1337, ((LinearInterpolationCommand)commands.get(0)).getMaxExitSpeed());
        assertEquals(998, ((LinearInterpolationCommand)commands.get(4)).getMaxExitSpeed());
    }

    @Test
    public void testExitSpeedsXStraight() throws ParsingException {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G1 Y00 X01 F1500\n" +
                "G1 Y00 X02 F1500\n" +
                "G1 Y00 X03 F1500\n" +
                "G1 Y00 X04 F1500\n" +
                "G1 Y00 X05 F1500\n" +
                "G1 Y00 X06 F1500\n" +
                "G1 Y00 X07 F1500\n");

        assertEquals(1093, ((LinearInterpolationCommand)commands.get(0)).getMaxExitSpeed());
        assertEquals(632, ((LinearInterpolationCommand)commands.get(4)).getMaxExitSpeed());
    }

    @Test
    public void testExitSpeedsOneYStops() throws ParsingException {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G1 Y01 X01 F1500\n" +
                "G1 Y02 X02 F1500\n" +
                "G1 Y03 X03 F1500\n" +
                "G1 Y03 X04 F1500\n" +
                "G1 Y03 X05 F1500\n" +
                "G1 Y03 X06 F1500\n" +
                "G1 Y03 X07 F1500\n");

        assertEquals(1167, ((LinearInterpolationCommand)commands.get(0)).getMaxExitSpeed());
        assertEquals(893, ((LinearInterpolationCommand)commands.get(2)).getMaxExitSpeed());
    }

    @Test
    public void testExitSpeedsReverseY() throws ParsingException {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G1 Y07 X00 F1500\n" +
                "G1 Y06 X00 F1500\n" +
                "G1 Y05 X00 F1500\n" +
                "G1 Y04 X00 F1500\n" +
                "G1 Y03 X00 F1500\n" +
                "G1 Y02 X00 F1500\n" +
                "G1 Y01 X00 F1500\n");

        assertEquals(0, ((LinearInterpolationCommand)commands.get(0)).getMaxExitSpeed());
        assertEquals(998, ((LinearInterpolationCommand)commands.get(1)).getMaxExitSpeed());
    }

    @Test
    public void testFastSpeedAndyTurnBack() throws ParsingException
    {
        Interpreter interpreter = new Interpreter();
        List<Command> commands = interpreter.interpretBlocks("G0 X1 Y1\n" +
                "G1 X20 F1000\n" +
                "G1 X40 Y3\n" +
                "G1 X60\n" +
                "G1 X80  Y1\n" +
                "G0 X0 Y0");

        assertEquals(15074, ((LinearInterpolationCommand)commands.get(1)).getMaxExitSpeed());
        assertEquals(0, ((LinearInterpolationCommand)commands.get(3)).getMaxExitSpeed());
    }
}
