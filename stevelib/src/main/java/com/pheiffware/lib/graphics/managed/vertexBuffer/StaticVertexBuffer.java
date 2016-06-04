/*
 * Created by Stephen Pheiffer.
 * Do not edit, distribute, modify or use without his permission.
*/
package com.pheiffware.lib.graphics.managed.vertexBuffer;

import android.opengl.GLES20;

import com.pheiffware.lib.graphics.Mesh;
import com.pheiffware.lib.graphics.managed.program.Program;
import com.pheiffware.lib.graphics.managed.program.Technique;
import com.pheiffware.lib.graphics.managed.program.VertexAttribute;

import java.util.EnumMap;

/**
 * Sets up a packed vertex buffer designed to be filled ONCE and then displayed over and over.
 * <p/>
 * This does not have to include all vertexAttributes of a program will use as some vertexAttributes may dynamically change and be handled in dynamic buffers.
 * <p/>
 * Usage should look like:
 * <p/>
 * One time setup:
 * <p/>
 * buffer.put*
 * <p/>
 * ...
 * <p/>
 * buffer.transfer(gl);
 * <p/>
 * Per frame (or update period)
 * <p/>
 * buffer.bind(gl);
 * <p/>
 * YOU CANNOT put more data in once transfer is called!
 */
public class StaticVertexBuffer extends BaseBuffer
{
    //TODO: Must be an even multiple of machine word size.  Check OpenGL ES spec.
    //Total size of each vertex in this buffer
    private int vertexByteSize;

    //Maps standard vertexAttributes to their corresponding byte offsets within each vertex data block
    private EnumMap<VertexAttribute, Integer> attributeVertexByteOffset = new EnumMap<>(VertexAttribute.class);

    //The vertexAttributes being managed by this buffer.  This is the order they will appear within each vertex data block
    private final VertexAttribute[] vertexAttributes;

    //Has the buffer been transferred?  Its illegal to transfer multiple times.
    private boolean isTransferred = false;

    /**
     * Create buffer which holds a specific set of standard vertex attributes
     */
    public StaticVertexBuffer(VertexAttribute[] vertexAttributes)
    {
        this.vertexAttributes = vertexAttributes;

        int attributeByteOffset = 0;
        for (VertexAttribute vertexAttribute : vertexAttributes)
        {
            setAttributeByteOffset(vertexAttribute, attributeByteOffset);
            attributeByteOffset += vertexAttribute.getByteSize();
        }
        vertexByteSize = attributeByteOffset;
    }

    public void allocate(int numVertices)
    {
        allocateBuffer(numVertices * vertexByteSize);
    }

    /**
     * Put all vertexAttributes from a given mesh, which this buffer supports, into this buffer a the given vertex offset.
     *
     * @param mesh
     * @param vertexOffset
     */
    public void putVertexAttributes(Mesh mesh, int vertexOffset)
    {
        for (VertexAttribute vertexAttribute : vertexAttributes)
        {
            if (mesh.hasAttributeData(vertexAttribute))
            {
                putAttributeFloats(vertexAttribute, mesh.getAttributeData(vertexAttribute), vertexOffset);
            }
        }
    }
    /**
     * For a given vertexAttribute put an array of floats in the appropriate buffer location, starting at the given vertex offset. Note, this is very inefficient, but is fine for one
     * time setup.
     *
     * @param vertexAttribute
     * @param values
     * @param vertexOffset
     */
    public final void putAttributeFloats(VertexAttribute vertexAttribute, float[] values, int vertexOffset)
    {
        putAttributeFloats(getAttributeByteOffset(vertexAttribute), vertexAttribute.getNumBaseTypeElements(), values, vertexOffset);
    }

    public final void putAttributeFloats(int attributeByteOffset, int numBaseTypeElements, float[] values, int vertexOffset)
    {
        int putPosition = attributeByteOffset + vertexByteSize * vertexOffset;
        for (int i = 0; i < values.length; i += numBaseTypeElements)
        {
            byteBuffer.position(putPosition);
            for (int j = 0; j < numBaseTypeElements; j++)
            {
                byteBuffer.putFloat(values[i + j]);
            }
            putPosition += vertexByteSize;
        }
    }

    public final void bind(Technique technique)
    {
        technique.bindBuffer(this);
    }

    /**
     * Binds this buffer with all vertexAttributes, such that it will work with the given program.
     */
    public final void bind(Program program)
    {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandle);
        for (VertexAttribute vertexAttribute : vertexAttributes)
        {
            int location = program.getAttributeLocation(vertexAttribute);
            GLES20.glEnableVertexAttribArray(location);
            GLES20.glVertexAttribPointer(location, vertexAttribute.getNumBaseTypeElements(), vertexAttribute.getBaseType(), false, vertexByteSize, getAttributeByteOffset(vertexAttribute));
        }
    }

    /**
     * Transfer contents loaded by putAttribute* calls into graphics library. CAN ONLY BE CALLED ONCE!  After this method is called, no more put/transfer operations should occur.
     */
    public void transfer()
    {
        if (isTransferred)
        {
            throw new RuntimeException("Static buffer already transferred");
        }
        isTransferred = true;
        // Bind to the buffer. Future commands will affect this buffer specifically.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandle);

        // Transfer data from client memory to the buffer.
        int transferSize = byteBuffer.position();

        // MUST RESET POSITION TO 0!
        byteBuffer.position(0);

        // Transfer data
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, transferSize, byteBuffer, GLES20.GL_STATIC_DRAW);

        // IMPORTANT: Unbind from the buffer when we're done with it.
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Destroy bytebuffer (immediately)
        deallocateByteBuffer();
    }

    /**
     * Has this buffer been transferred to the GL already?
     *
     * @return
     */
    public boolean isTransferred()
    {
        return isTransferred;
    }

    private void setAttributeByteOffset(VertexAttribute vertexAttribute, int byteOffset)
    {
        attributeVertexByteOffset.put(vertexAttribute, byteOffset);
    }

    private int getAttributeByteOffset(VertexAttribute vertexAttribute)
    {
        return attributeVertexByteOffset.get(vertexAttribute);
    }
}