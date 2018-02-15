package org.cirqoid.cnc.controller.commands;
import java.nio.ByteBuffer;

public class SetDifferentialMatrixCommand extends Command
{
    private int [][] differentialMatrix;

    public SetDifferentialMatrixCommand(int length)
    {
        this.differentialMatrix = new int[length][3];
    }

    public void setRectOffset(int position, int[] info)
    {
        differentialMatrix[position] = info;
    }

    @Override
    public Type getType()
    {
        return Type.SET_DIFFERENTIAL_MATRIX;
    }

    @Override
    public byte[] getPayload()
    {
        ByteBuffer b = ByteBuffer.allocate(differentialMatrix.length * 3 * 4);
        for (int[] info : differentialMatrix)
        {
            b.putInt(info[0]);
            b.putInt(info[1]);
            b.putInt(info[2]);
        }
        return b.array();
    }
}
