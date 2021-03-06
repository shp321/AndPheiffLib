package com.pheiffware.libDemo.andGraphics;

import android.opengl.GLES20;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pheiffware.lib.AssetLoader;
import com.pheiffware.lib.and.graphics.AndGraphicsUtils;
import com.pheiffware.lib.and.gui.graphics.openGL.BaseGameFragment;
import com.pheiffware.lib.and.gui.graphics.openGL.GameRenderer;
import com.pheiffware.lib.and.gui.graphics.openGL.GameView;
import com.pheiffware.lib.and.gui.graphics.openGL.SystemInfo;
import com.pheiffware.lib.graphics.FilterQuality;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.managed.GLCache;
import com.pheiffware.lib.graphics.managed.program.GraphicsConfig;
import com.pheiffware.lib.graphics.managed.program.Program;
import com.pheiffware.lib.graphics.managed.program.UniformName;
import com.pheiffware.lib.graphics.managed.program.VertexAttribute;
import com.pheiffware.lib.graphics.managed.texture.Texture;
import com.pheiffware.lib.graphics.managed.vertexBuffer.DynamicAttributeBuffer;
import com.pheiffware.lib.graphics.managed.vertexBuffer.IndexBuffer;
import com.pheiffware.lib.graphics.managed.vertexBuffer.StaticAttributeBuffer;
import com.pheiffware.lib.graphics.managed.vertexBuffer.VertexAttributeGroup;
import com.pheiffware.lib.graphics.utils.MeshGenUtils;

import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Example of using a CombinedBuffer for storing some vertex attributes statically and other dynamically.  In this case, vertices are static and colors are dynamically updated.
 * Created by Steve on 3/27/2016.
 */
public class Demo1RawVertexBufferFragment extends BaseGameFragment
{
    @Override
    public GameView onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return new GameView(getContext(), new Renderer(), FilterQuality.MEDIUM, false);
    }

    private static class Renderer extends GameRenderer
    {
        private static final int samplerToUse = 0;

        private Program programTextureColor;
        private IndexBuffer indexBuffer;
        private StaticAttributeBuffer staticBuffer;
        private DynamicAttributeBuffer dynamicBuffer;
        private float globalTestColor = 0.0f;
        private Matrix4 ortho2DMatrix;
        private Texture faceTexture;
        private VertexAttributeGroup staticVertexAttributeGroup;
        private VertexAttributeGroup dynamicVertexAttributeGroup;

        private Renderer()
        {
            super(AndGraphicsUtils.GL_VERSION_30, AndGraphicsUtils.GL_VERSION_30, "shaders");
        }

        @Override
        public void onSurfaceCreated(AssetLoader al, GLCache glCache, SystemInfo systemInfo) throws GraphicsException
        {
            // Wait for vertical retrace
            GLES20.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);

            Map<String, Object> settings = new HashMap<>();
            settings.put(GraphicsConfig.COLOR_VERTEX_2D, true);
            settings.put(GraphicsConfig.TEXTURED_2D, true);
            programTextureColor = glCache.buildProgram(settings, "2d/vert_2d.glsl", "2d/frag_2d.glsl");

            faceTexture = glCache.buildImageTex("images/face.png").build();

            indexBuffer = new IndexBuffer();
            staticBuffer = new StaticAttributeBuffer();
            dynamicBuffer = new DynamicAttributeBuffer();

            short[] indexData = MeshGenUtils.genSingleQuadIndexData();
            indexBuffer.allocateSoftwareBuffer(6 * 2);
            ByteBuffer byteBuffer = indexBuffer.editBuffer();
            for (int i = 0; i < indexData.length; i++)
            {
                byteBuffer.putShort(indexData[i]);
            }

            float[] posData = MeshGenUtils.genSingleQuadPositionData(0, 0, 1, 1, VertexAttribute.POSITION4);
            float[] textureData = MeshGenUtils.genSingleQuadTexData();
            staticBuffer.allocateSoftwareBuffer(6 * 4 * 4);
            byteBuffer = staticBuffer.editBuffer();
            for (int i = 0; i < 4; i++)
            {
                byteBuffer.putFloat(posData[i * 4 + 0]);
                byteBuffer.putFloat(posData[i * 4 + 1]);
                byteBuffer.putFloat(posData[i * 4 + 2]);
                byteBuffer.putFloat(posData[i * 4 + 3]);

                byteBuffer.putFloat(textureData[i * 2 + 0]);
                byteBuffer.putFloat(textureData[i * 2 + 1]);
            }
            staticVertexAttributeGroup = new VertexAttributeGroup(EnumSet.of(VertexAttribute.POSITION4, VertexAttribute.TEXCOORD));

            float[] colorData = MeshGenUtils.genSingleQuadColorData(new float[]{1, 0, 0, 1});
            dynamicBuffer.allocateSoftwareBuffer(4 * 4 * 4);
            byteBuffer = dynamicBuffer.editBuffer();
            for (int i = 0; i < 4; i++)
            {
                byteBuffer.putFloat(colorData[i * 4 + 0]);
                byteBuffer.putFloat(colorData[i * 4 + 1]);
                byteBuffer.putFloat(colorData[i * 4 + 2]);
                byteBuffer.putFloat(colorData[i * 4 + 3]);
            }
            dynamicVertexAttributeGroup = new VertexAttributeGroup(EnumSet.of(VertexAttribute.COLOR));
            indexBuffer.transfer();
            staticBuffer.transfer();
            dynamicBuffer.transfer();
        }

        @Override
        public void onDrawFrame()
        {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            ByteBuffer byteBuffer;
            byteBuffer = dynamicBuffer.editBuffer();
            for (int i = 0; i < 4; i++)
            {
                byteBuffer.putFloat(1f);
                byteBuffer.putFloat(0f);
                byteBuffer.putFloat(globalTestColor);
                byteBuffer.putFloat(1f);
            }
            dynamicBuffer.transfer();


            //Scale down everything drawn by a factor of 5.
            Matrix4 scale = Matrix4.newScale(0.2f, 0.2f, 1f);
            Matrix4 projectionViewModelMatrix = Matrix4.multiply(ortho2DMatrix, scale);

            programTextureColor.bind();
            programTextureColor.setUniformValue(UniformName.PROJECTION_VIEW_MODEL_MATRIX, projectionViewModelMatrix.m);
            faceTexture.manualBind(samplerToUse);

            programTextureColor.setUniformValue(UniformName.IMAGE_TEXTURE, samplerToUse);

            staticBuffer.bindToProgram(programTextureColor, staticVertexAttributeGroup, 0);
            dynamicBuffer.bindToProgram(programTextureColor, dynamicVertexAttributeGroup, 0);
            indexBuffer.draw(GLES20.GL_TRIANGLES, 6, 0);
            globalTestColor += 0.01;
        }


        @Override
        public void onSurfaceResize(int width, int height)
        {
            super.onSurfaceResize(width, height);
            GLES20.glViewport(0, 0, width, height);
            ortho2DMatrix = Matrix4.newOrtho2D(width / (float) height);
        }
    }
}
