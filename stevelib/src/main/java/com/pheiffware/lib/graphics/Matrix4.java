package com.pheiffware.lib.graphics;

import android.opengl.Matrix;

import com.pheiffware.lib.graphics.utils.MathUtils;

import java.util.Arrays;

/**
 * Stores and manipulates a 4x4 matrix.  Stored as a 16 element float array in column major order.
 * This is an enhanced version of the built in Matrix4f.
 * 1. Provides more convenience methods
 * 2. Doesn't have a bugged rotation method (Matrix4f's method does not normalize axis properly).
 * 3. Backed by the native Matrix library, so it is slightly faster
 * <p/>
 * Created by Steve on 3/9/2016.
 */
public class Matrix4
{
    public static Matrix4 multiply(Matrix4 lhs, Matrix4 rhs)
    {
        float[] product = new float[16];
        Matrix.multiplyMM(product, 0, lhs.m, 0, rhs.m, 0);
        return new Matrix4(product);
    }

    public static Matrix4 multiply(Matrix4 lhs, Matrix4... matrices)
    {
        Matrix4 product = new Matrix4(lhs);
        for (Matrix4 matrix : matrices)
        {
            product = multiply(product, matrix);
        }
        return product;
    }

    public static Matrix4 newTranslation(float x, float y, float z)
    {
        Matrix4 matrix = new Matrix4(new float[16]);
        matrix.setTranslate(x, y, z);
        return matrix;
    }

    public static Matrix4 newScale(float x, float y, float z)
    {
        Matrix4 matrix = new Matrix4(new float[16]);
        matrix.setScale(x, y, z);
        return matrix;
    }

    public static Matrix4 newRotate(float angle, float x, float y, float z)
    {
        Matrix4 matrix = new Matrix4(new float[16]);
        matrix.setRotate(angle, x, y, z);
        return matrix;
    }

    public static Matrix4 newInverse(Matrix4 matrix)
    {
        float[] inverse = new float[16];
        Matrix.invertM(inverse, 0, matrix.m, 0);
        return new Matrix4(inverse);
    }

    public static Matrix4 newTranspose(Matrix4 transformMatrix)
    {
        //TODO: replace with Matrix.transpose
        float[] transpose = new float[16];
        float[] mData = transformMatrix.m;

        int destIndex = 0;
        for (int srcRowIndex = 0; srcRowIndex < 4; srcRowIndex++)
        {
            transpose[destIndex++] = mData[srcRowIndex + 0];
            transpose[destIndex++] = mData[srcRowIndex + 4];
            transpose[destIndex++] = mData[srcRowIndex + 8];
            transpose[destIndex++] = mData[srcRowIndex + 12];
        }
        return new Matrix4(transpose);
    }
    public static Matrix3 newNormalTransform(Matrix4 transformMatrix)
    {
        float[] floats = Arrays.copyOf(transformMatrix.m, 16);
        floats[12] = 0;
        floats[13] = 0;
        floats[14] = 0;
        float[] inverse = new float[16];
        Matrix.invertM(inverse, 0, floats, 0);
        Matrix.transposeM(floats, 0, inverse, 0);
        Matrix3 matrix3 = Matrix3.newZeroMatrix();
        matrix3.setFloatMatrix4UpperLeft(floats);
        return matrix3;
    }
    /**
     * Creates a projection matrix. You generally want to set flipVertical to true when using this to render to a texture as texture coordinates are
     * backward.
     *
     * @param fieldOfViewY The field of view in the y direction (in degrees)
     * @param aspect
     * @param zNear
     * @param zFar
     * @param flipVertical
     * @return
     */
    public static Matrix4 newProjection(float fieldOfViewY, float aspect, float zNear, float zFar, boolean flipVertical)
    {
        float[] matrix = new float[16];

        float top = (float) (zNear * Math.tan(Math.PI / 180.0 * fieldOfViewY / 2));
        float right = top * aspect;
        if (flipVertical)
        {
            top *= -1;
        }
        float bottom = -top;
        float left = -right;
        Matrix.frustumM(matrix, 0, left, right, bottom, top, zNear, zFar);
        return new Matrix4(matrix);
    }


    /**
     * Creates an empty matrix
     *
     * @return new zero matrix
     */
    public static Matrix4 newZeroMatrix()
    {
        return new Matrix4();
    }

    /**
     * Creates new identity matrix
     *
     * @return new identity matrix
     */
    public static Matrix4 newIdentity()
    {
        Matrix4 matrix = new Matrix4();
        matrix.setIdentity();
        return matrix;
    }

    /**
     * Creates new 4x4 matrix from given floats.
     *
     * @param floats 16 floats in column major order
     * @return new 4x4 matrix from given floats
     */
    public static Matrix4 newMatrixFromFloats(float[] floats)
    {
        return new Matrix4(Arrays.copyOf(floats, 16));
    }

    //Stored matrix data in column major order
    public final float[] m;

    /**
     * Constructs a new blank matrix
     */
    private Matrix4()
    {
        m = new float[9];
    }

    /**
     * Internal constructor which does NOT copy data
     *
     * @param m reference to array which should back this matrix.
     */
    private Matrix4(float[] m)
    {
        this.m = m;
    }

    /**
     * Creates a new copy of the given matrix.
     *
     * @param matrix matrix to copy
     */
    public Matrix4(Matrix4 matrix)
    {
        m = Arrays.copyOf(matrix.m,16);
    }

    public final void setIdentity()
    {
        //@formatter:off
        m[0] = 1;m[4] = 0;m[8] =  0;m[12] = 0;
        m[1] = 0;m[5] = 1;m[9] =  0;m[13] = 0;
        m[2] = 0;m[6] = 0;m[10] = 1;m[14] = 0;
        m[3] = 0;m[7] = 0;m[11] = 0;m[15] = 1;
        //@formatter:on
    }

    public final void setTranslate(float x, float y, float z)
    {
        //@formatter:off
        m[0] = 1;m[4] = 0;m[8] =  0;m[12] = x;
        m[1] = 0;m[5] = 1;m[9] =  0;m[13] = y;
        m[2] = 0;m[6] = 0;m[10] = 1;m[14] = z;
        m[3] = 0;m[7] = 0;m[11] = 0;m[15] = 1;
        //@formatter:on
    }

    public final void setRotate(float angle, float x, float y, float z)
    {
        Matrix.setRotateM(m, 0, angle, x, y, z);
    }

    public final void setScale(float x, float y, float z)
    {
        //@formatter:off
        m[0] = x;m[4] = 0;m[8] =  0;m[12] = 0;
        m[1] = 0;m[5] = y;m[9] =  0;m[13] = 0;
        m[2] = 0;m[6] = 0;m[10] = z;m[14] = 0;
        m[3] = 0;m[7] = 0;m[11] = 0;m[15] = 1;
        //@formatter:on
    }

    public final void setOrthographic(float left, float right, float bottom, float top,
                                      float near, float far)
    {
        Matrix.orthoM(m, 0, left, right, bottom, top, near, far);
    }

    public final void setProjection(float fieldOfViewY, float aspect, float near, float far, boolean flipVertical)
    {
        float top = (float) (near * Math.tan(Math.PI / 180.0 * fieldOfViewY / 2));
        float right = top * aspect;
        if (flipVertical)
        {
            top *= -1;
        }
        float bottom = -top;
        float left = -right;
        setFrustum(left, right, bottom, top, near, far);
    }

    public final void setFrustum(float left, float right, float bottom, float top,
                                 float near, float far)
    {
        Matrix.frustumM(m, 0, left, right, bottom, top, near, far);
    }

    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 4; i++)
        {
            for (int j = 0; j < 16; j += 4)
            {
                builder.append(m[i + j]);
                builder.append(",");
            }
            builder.append("\n");
        }
        return builder.toString();

    }


}