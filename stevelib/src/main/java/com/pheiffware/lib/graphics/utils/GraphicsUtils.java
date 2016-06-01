package com.pheiffware.lib.graphics.utils;

/**
 * Created by Steve on 2/13/2016.
 */
public class GraphicsUtils
{
    /**
     * Multiply 2 vectors component by component.  Store in out vector.
     *
     * @param length how many components to go through
     * @param out    where result is written
     * @param vec1   input vector
     * @param vec2   input vector
     */
    public static void vecMultiply(int length, float[] out, float[] vec1, float[] vec2)
    {
        for (int i = 0; i < length; i++)
        {
            out[i] = vec1[i] * vec2[i];
        }
    }

    /**
     * Multiply 2 vectors component by component.  Store at given offset in out vector.
     *
     * @param length    how many components to go through
     * @param outOffset output vector write offset
     * @param out       where result is written
     * @param vec1      input vector
     * @param vec2      input vector
     */
    public static void vecMultiply(int length, int outOffset, float[] out, float[] vec1, float[] vec2)
    {
        for (int i = 0; i < length; i++)
        {
            out[outOffset] = vec1[i] * vec2[i];
            outOffset++;
        }
    }

    /**
     * Multiply 2 vectors component by component.  Store at given offset in out vector.  Read from given offsets of input vectors
     *
     * @param length    how many components to go through
     * @param outOffset output vector write offset
     * @param out       where result is written
     * @param v1Offset  input vector read offset
     * @param vec1      input vector
     * @param v2Offset  input vector read offset
     * @param vec2      input vector
     */
    public static void vecMultiply(int length, int outOffset, float[] out, int v1Offset, float[] vec1, int v2Offset, float[] vec2)
    {
        for (int i = 0; i < length; i++)
        {
            out[outOffset] = vec1[v1Offset] * vec2[v2Offset];
            outOffset++;
            v1Offset++;
            v2Offset++;
        }
    }

    /**
     * Given an array of non-homogeneous 3d vectors, create a new array homogeneous vectors, append the given element4 to all vectors and create a new array.
     *
     * @param nonHomogeneousVectors float data of the form [x1 y1 z1 x2 y2 z2 ...]
     * @param element4              the value for "w" coordinate to add
     * @return float data of the form [x1 y1 z1 w x2 y2 z2 w ...]
     */
    public static float[] homogenizeVec3Array(float[] nonHomogeneousVectors, float element4)
    {
        float[] homogeneousVectors = new float[(nonHomogeneousVectors.length / 3) * 4];
        int homogeneousIndex = 0, nonHomogeneousIndex = 0;
        while (homogeneousIndex < homogeneousVectors.length)
        {
            homogeneousVectors[homogeneousIndex++] = nonHomogeneousVectors[nonHomogeneousIndex++];
            homogeneousVectors[homogeneousIndex++] = nonHomogeneousVectors[nonHomogeneousIndex++];
            homogeneousVectors[homogeneousIndex++] = nonHomogeneousVectors[nonHomogeneousIndex++];
            homogeneousVectors[homogeneousIndex++] = element4;
        }
        return homogeneousVectors;
    }
}
