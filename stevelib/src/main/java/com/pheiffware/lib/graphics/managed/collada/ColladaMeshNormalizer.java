package com.pheiffware.lib.graphics.managed.collada;

import com.pheiffware.lib.graphics.managed.mesh.Mesh;

import java.util.HashMap;
import java.util.Map;

//TODO: Put this code into the ColladaMesh (we don't need a separate class).
/**
 * Does the ugly job of untangling the ridiculous Collada input meshes vertex index data.  The test cases demonstrate this better though examples than I can explain here.
 * <p/>
 * The result is a list of indices, each of which, references data in the various arrays stored in the data map.
 * Created by Steve on 2/15/2016.
 */
class ColladaMeshNormalizer
{
    //Original mesh as loaded from Collada
    private final ColladaMesh colladaMesh;
    //The number of unique vertices.  Each array in vertex data is this length
    private short numUniqueVertices;
    //Data for each unique vertex.  A map from names like POSITION, NORMAL, TEXCOORD, etc to actual arrays holding vertex data.  The same vertex may be referenced multiple times in the vertexIndices array.
    private final Map<String, float[]> vertexData = new HashMap<>();
    //Indices to the data itself.  These are grouped together to form triangle primitives
    private short[] vertexDataIndices;

    /**
     * For each unique combination of indices within a stride (a unique vertex), create a new universal vertex index.
     * Write these universal vertex indices, in order, in vertexDataIndices.  Also stores the total number of unique vertices in numUniqueVertices.
     */
    private void generateUniversalVertexIndices()
    {
        vertexDataIndices = new short[colladaMesh.vertexCount];

        //Map from unique group of indices to a unified unique index
        Map<VertexIndexGroup, Short> uniqueIndexMap = new HashMap<>();
        int vertexIndex = 0;
        numUniqueVertices = 0;
        short[] interleavedIndices = colladaMesh.interleavedIndices;
        int collatedIndexStride = colladaMesh.vertexStride;
        for (int collatedIndex = 0; collatedIndex < interleavedIndices.length; collatedIndex += collatedIndexStride)
        {
            VertexIndexGroup vertexIndexGroup = new VertexIndexGroup(interleavedIndices, collatedIndex, collatedIndexStride);
            Short uniqueIndex = uniqueIndexMap.get(vertexIndexGroup);
            if (uniqueIndex == null)
            {
                uniqueIndex = numUniqueVertices;
                numUniqueVertices++;
                uniqueIndexMap.put(vertexIndexGroup, uniqueIndex);
            }
            vertexDataIndices[vertexIndex] = uniqueIndex;
            vertexIndex++;
        }
    }

    /**
     * Once unique, universal, indices have been generated, this takes the raw data and maps it to these new indices
     */
    private void generateUniqueVertexData()
    {
        short[] interleavedIndices = colladaMesh.interleavedIndices;
        int collatedIndexStride = colladaMesh.vertexStride;
        for (Map.Entry<String, ColladaInput> entry : colladaMesh.inputs.entrySet())
        {
            String key = entry.getKey();
            ColladaInput input = entry.getValue();
            float[] destFloats = new float[numUniqueVertices * input.source.stride];
            int collatedIndex = input.offset;
            for (int i = 0; i < vertexDataIndices.length; i++)
            {
                short sourceIndex = interleavedIndices[collatedIndex];
                collatedIndex += collatedIndexStride;
                short destIndex = vertexDataIndices[i];
                input.transfer(sourceIndex, destFloats, destIndex);
            }
            vertexData.put(key, destFloats);
        }
    }

    public ColladaMeshNormalizer(Map<String, ColladaInput> vertexDataInputs, short[] interleavedIndices, int vertexCount)
    {
        this(new ColladaMesh(vertexDataInputs, interleavedIndices, vertexCount));
    }

    public ColladaMeshNormalizer(ColladaMesh colladaMesh)
    {
        this.colladaMesh = colladaMesh;
    }

    public Mesh generateMesh()
    {
        generateUniversalVertexIndices();
        generateUniqueVertexData();
        return new Mesh(numUniqueVertices, vertexData, vertexDataIndices);
    }

    /**
     * Holds a group of indices representing different aspects of vertex.  It is setup for hashing these combinations.
     */
    private static class VertexIndexGroup
    {
        private final short[] indices;

        public VertexIndexGroup(short[] interleavedIndices, int start, int length)
        {
            indices = new short[length];
            for (int i = 0; i < indices.length; i++)
            {
                indices[i] = interleavedIndices[start + i];
            }
        }

        @Override
        public int hashCode()
        {
            int code = 0;
            int mult = 1;
            for (int i = 0; i < indices.length; i++)
            {
                code += indices[i] * mult;
                mult *= 97;
            }
            return code;
        }

        @Override
        public boolean equals(Object o)
        {
            VertexIndexGroup other = (VertexIndexGroup) o;
            for (int i = 0; i < indices.length; i++)
            {
                if (indices[i] != other.indices[i])
                {
                    return false;
                }
            }
            return true;
        }
    }

}
