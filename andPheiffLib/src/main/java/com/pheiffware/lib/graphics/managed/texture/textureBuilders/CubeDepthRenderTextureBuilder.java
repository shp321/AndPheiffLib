package com.pheiffware.lib.graphics.managed.texture.textureBuilders;

import android.opengl.GLES20;
import android.opengl.GLES30;

import com.pheiffware.lib.graphics.FilterQuality;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.managed.texture.TextureBinder;
import com.pheiffware.lib.graphics.managed.texture.TextureCubeMap;

/**
 * Created by Steve on 6/22/2017.
 */

public class CubeDepthRenderTextureBuilder extends TextureBuilder<TextureCubeMap>
{
    private final int width;
    private final int height;

    public CubeDepthRenderTextureBuilder(TextureBinder textureBinder, FilterQuality defaultFilterQuality, int width, int height)
    {
        super(textureBinder, defaultFilterQuality, false);
        this.width = width;
        this.height = height;
    }


    @Override
    public TextureCubeMap build() throws GraphicsException
    {
        TextureCubeMap texture = new TextureCubeMap(textureBinder, width, height);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT, null);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GLES20.GL_DEPTH_COMPONENT, width, height, 0, GLES20.GL_DEPTH_COMPONENT,
                GLES20.GL_UNSIGNED_SHORT, null);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_COMPARE_MODE, GLES30.GL_COMPARE_REF_TO_TEXTURE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_COMPARE_FUNC, GLES30.GL_LEQUAL);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES20.GL_CLAMP_TO_EDGE);
        return texture;

    }
}
