package org.cirqoid.cnc.controller.interpreter;

import org.cirqoid.cnc.controller.commands.CircularInterpolationCommand;
import org.cirqoid.cnc.controller.commands.Command;
import org.cirqoid.cnc.controller.commands.LinearInterpolationCommand;
import org.cirqoid.cnc.controller.commands.RapidMotionCommand;
import org.cirqoid.cnc.controller.interpreter.commands.CommandFactory;
import org.cirqoid.cnc.controller.settings.ApplicationConstants;
import org.cirqoid.cnc.controller.settings.HardwareSettings;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simon on 16.06.17.
 */
public class Interpreter
{
    private Context context = new Context();

    public Context getContext()
    {
        return context;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public List<Command> interpretBlocks(String blocks) throws ParsingException
    {
        LineNumberReader reader = new LineNumberReader(new StringReader(blocks));
        List<Command> result = new ArrayList<>();
        String str;
        try
        {
            while ((str = reader.readLine()) != null)
            {
                result.addAll(interpretBlock(str));
            }
        }
        catch (IOException e) {}
        optimizeExitSpeed(result);
        return result;
    }

    public List<Command> interpretBlock(String block) throws ParsingException
    {
        int[] startPosition = new int[ApplicationConstants.MAX_AXES_COUNT];
        System.arraycopy(context.getCurrentPosition(), 0, startPosition, 0, startPosition.length);
        Context contextBackup = (Context) context.clone();
        List<Command> executionList = new ArrayList<>();
        List<Token> tokens = GCodeParser.parseBlock(block);
        boolean interpolationCommanded = false;

        try
        {
            int[] arcCenterPoint = new int[3];

            while (!tokens.isEmpty())
            {
                Token t = tokens.remove(0);
                if (ParsingUtil.isCommandLetter(t.getLetter()))
                {
                    org.cirqoid.cnc.controller.interpreter.commands.Command command = CommandFactory.findCommand(t);
                    if (command == null)
                        throw new ParsingException("Unknown command: " + t.getLetter() + t.getIntegerParameter());
                    List<Command> packets = command.act(context, tokens);
                    if (packets != null)
                        executionList.addAll(packets);
                }
                else if (t.getLetter() == 'F')
                    context.setFeed(ParsingUtil.toInteger(t.getDecimalParameter()));
                else if (t.getLetter() == 'S')
                    context.setSpeed(t.getIntegerParameter());
                else
                {
                    int axis = ParsingUtil.getAxisNumber(t.getLetter());
                    if (ParsingUtil.isCenterOffsetLetter(t.getLetter()))
                        arcCenterPoint[axis] = ParsingUtil.toInteger(t.getDecimalParameter());
                    else if (ParsingUtil.isAxisLetter(t.getLetter()))
                    {
                        interpolationCommanded = true;
                        context.setCurrentPosition(axis,
                                adjustCoordinateForOffset(axis, ParsingUtil.toInteger(t.getDecimalParameter())));
                    }
                }
            }
            if (interpolationCommanded)
            {
                if (!TravelRangeValidator.validate(context.getCurrentPosition(), HardwareSettings.getCirqoidSettings()))
                    throw new ParsingException("Axis overtravel");
                executionList.add(createInterpolationCommand(startPosition, arcCenterPoint));
            }
        }
        catch (ParsingException e)
        {
            context = contextBackup;
            throw new ParsingException(e.getMessage(), block);
        }

        return executionList;
    }

    private int adjustCoordinateForOffset(int axis, int coordinate)
    {
        return coordinate + context.getOffset(context.getCurrentWcs(), axis);
    }

    private Command createInterpolationCommand(int[] originalPosition, int[] arcCenterPoint) throws ParsingException
    {
        int[] target = new int[ApplicationConstants.MAX_AXES_COUNT];
        System.arraycopy(context.getCurrentPosition(), 0, target, 0, target.length);
        switch (context.getCurrentInterpolationMode())
        {
            case RAPID:
                return new RapidMotionCommand(target);
            case LINEAR:
                if (context.getFeed() == null)
                    throw new ParsingException("Feed is not selected");
                LinearInterpolationCommand lic = createLinearInterpolationCommand(originalPosition, target);
                return lic;
            case CIRCUAR_CW:
            case CIRCULAR_CCW:
                if (context.getFeed() == null)
                    throw new ParsingException("Feed is not selected");
                int[] axes = null;
                switch (context.getPlane())
                {
                    case XY: axes = new int[] {0, 1, 2}; break;
                    case YZ: axes = new int[] {1, 2, 0}; break;
                    case XZ: axes = new int[] {0, 2, 1}; break;
                }
                int[] centerCoordinates = new int[] {originalPosition[axes[0]] + arcCenterPoint[axes[0]],
                        originalPosition[axes[1]] + arcCenterPoint[axes[1]]};
                int radius = (int) Math.hypot(context.getCurrentPosition(axes[0]) - centerCoordinates[0],
                        context.getCurrentPosition(axes[1]) - centerCoordinates[1]);
                return new CircularInterpolationCommand(target, radius, centerCoordinates, context.getPlane(),
                        context.getCurrentInterpolationMode() == Context.InterpolationMode.CIRCUAR_CW, context.getFeed());
            case NOT_SELECTED:
                throw new ParsingException("Interpolation mode is not selected");
        }
        return null;
    }

    private LinearInterpolationCommand createLinearInterpolationCommand(int[] originalPosition, int[] target)
    {
        return new LinearInterpolationCommand(originalPosition, target, context.getFeed());
    }

    private void optimizeExitSpeed(List<Command> commands)
    {
        double minAccelerationSpeed = 100;

        for(int i = 0; commands.size() > 1 && i < commands.size(); ++i)
        {
            if (!(commands.get(i) instanceof LinearInterpolationCommand))
            {
                continue;
            }

            if (i + 1 < commands.size() && commands.get(i + 1) instanceof LinearInterpolationCommand)
            {
                LinearInterpolationCommand command1 = (LinearInterpolationCommand) commands.get(i);
                int[] command1Start = command1.getStart();
                int[] command1Target = command1.getTarget();

                double command1Distance = Math.sqrt(Math.pow(command1Target[0] - command1Start[0], 2) + Math.pow(command1Target[1] - command1Start[1], 2) +
                        Math.pow(command1Target[2] - command1Start[2], 2));

                double v1x = ((command1Target[0] - command1Start[0]) / command1Distance) * command1.getFeed() / 60;
                double v1y = ((command1Target[1] - command1Start[1]) / command1Distance) * command1.getFeed() / 60;
                double v1z = ((command1Target[2] - command1Start[2]) / command1Distance) * command1.getFeed() / 60;

                LinearInterpolationCommand command2 = (LinearInterpolationCommand) commands.get(i + 1);
                int[] command2Start = command2.getStart();
                int[] command2Target = command2.getTarget();

                double command2Distance = Math.sqrt(Math.pow(command2Target[0] - command2Start[0], 2) + Math.pow(command2Target[1] - command2Start[1], 2) +
                        Math.pow(command2Target[2] - command2Start[2], 2));

                double v2x = ((command2Target[0] - command2Start[0]) / command2Distance) * command2.getFeed() / 60;
                double v2y = ((command2Target[1] - command2Start[1]) / command2Distance) * command2.getFeed() / 60;
                double v2z = ((command2Target[2] - command2Start[2]) / command2Distance) * command2.getFeed() / 60;

                double vtx = getSegmentComponent(v1x, v2x);
                double vty = getSegmentComponent(v1y, v2y);
                double vtz = getSegmentComponent(v1z, v2z);

                double v2xs = v2x / (command2.getFeed() / 60);
                double v2ys = v2y / (command2.getFeed() / 60);
                double v2zs = v2z / (command2.getFeed() / 60);

                double v1xs = v1x / (command1.getFeed() / 60);
                double v1ys = v1y / (command1.getFeed() / 60);
                double v1zs = v1z / (command1.getFeed() / 60);

                double v1xsr = vtx / v1xs;
                double v1ysr = vty / v1ys;
                double v1zsr = vtz / v1zs;

                double v2xsr = vtx / v2xs;
                double v2ysr = vty / v2ys;
                double v2zsr = vty / v2zs;

                double[] array = {v1xsr, v1ysr, v2xsr, v2ysr, v1zsr, v2zsr};

                int result = Integer.MAX_VALUE;
                for (double r : array)
                {
                    if (!Double.isNaN(r) && !Double.isInfinite(r) && r < result)
                    {
                        result = (int)r;
                    }
                }

                command1.setMaxExitSpeed(result == Integer.MAX_VALUE ? 0 : result);
            }

            for (int j = i - 1; j >= 0 && commands.get(j) instanceof LinearInterpolationCommand; --j)
            {
                LinearInterpolationCommand command = (LinearInterpolationCommand) commands.get(j + 1);
                LinearInterpolationCommand previousCommand = (LinearInterpolationCommand) commands.get(j);

                double distance = Math.sqrt(Math.pow(command.getTarget()[0]-command.getStart()[0], 2) + Math.pow(command.getTarget()[1]-command.getStart()[1], 2)) / 1000;
                int targetSpeed = command.getMaxExitSpeed() / 1000;

                int maxEnterSpeed = (int)(Math.sqrt(targetSpeed * targetSpeed + 2 * minAccelerationSpeed * distance) * 1000);

                if (previousCommand.getMaxExitSpeed() > maxEnterSpeed)
                {
                    previousCommand.setMaxExitSpeed(maxEnterSpeed);
                }
                else
                {
                    break;
                }
            }
        }
    }

    private double getSegmentComponent(double v1, double v2)
    {
        double accelerationStartSpeed = 1500;

        double vt = 0;
        if (v1 > 0 && v2 > 0)
        {
            vt = Math.min(v1, v2);
        }
        else if (v1 < 0 && v2 < 0)
        {
            vt = Math.max(v1, v2);
        }
        else if (v1 == 0 && v2 != 0)
        {
            if (Math.abs(v2) <= accelerationStartSpeed)
            {
                vt = v2;
            }
            else
            {
                vt = accelerationStartSpeed;
            }
        }
        else if (v1 != 0 && v2 == 0)
        {
            if (Math.abs(v1) <= accelerationStartSpeed)
            {
                vt = v1;
            }
            else
            {
                vt = accelerationStartSpeed;
            }
        }

        return vt;
    }
}
