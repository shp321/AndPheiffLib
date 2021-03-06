package com.pheiffware.lib.graphics.managed.techniques;

import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.managed.GLCache;
import com.pheiffware.lib.graphics.managed.light.Lighting;
import com.pheiffware.lib.graphics.managed.program.GraphicsConfig;
import com.pheiffware.lib.graphics.managed.program.RenderProperty;
import com.pheiffware.lib.graphics.managed.program.UniformName;
import com.pheiffware.lib.graphics.managed.program.shader.ShaderBuilder;
import com.pheiffware.lib.graphics.managed.texture.NullTexture;
import com.pheiffware.lib.graphics.managed.texture.Texture;

import java.util.Map;

/**
 * Shades mesh with a constant surface color and given lights' settings.  Handles, ambient, diffuse and specular lighting.
 * <p>
 * Omni-directional shadows - diffuse/specular light, is blocked based on given cube depth map.
 * Created by Steve on 4/23/2016.
 */
public class Std3DTechnique extends Technique3D
{
    private boolean textured;
    private NullTexture nullCubeMapTexture;

    public Std3DTechnique() throws GraphicsException
    {
        super("vert_3d.glsl", "frag_3d.glsl");
    }

    @Override
    protected void init(GLCache glCache)
    {
        nullCubeMapTexture = glCache.nullTextureCubeMap;
    }

    public void applyConstantPropertiesImplement()
    {
        setProjection();
        setLightingConstants();
        Lighting lighting = (Lighting) getPropertyValue(RenderProperty.LIGHTING);
        setUniformValue(UniformName.LIGHT_POS_ABS, lighting.getPositions().getData());
        setUniformValue(UniformName.DEPTH_Z_CONST, getPropertyValue(RenderProperty.DEPTH_Z_CONST));
        setUniformValue(UniformName.DEPTH_Z_FACTOR, getPropertyValue(RenderProperty.DEPTH_Z_FACTOR));
    }

    @Override
    public void applyInstanceProperties()
    {
        setViewModelNormal();

        Lighting lighting = (Lighting) getPropertyValue(RenderProperty.LIGHTING);

        if (textured)
        {
            setSpecLightingColor();
            Texture texture = (Texture) getPropertyValue(RenderProperty.MAT_COLOR_TEXTURE);

            setUniformValue(UniformName.AMBIENT_LIGHT_COLOR, lighting.getAmbientLightColor().getData());
            setUniformValue(UniformName.LIGHT_COLOR, lighting.getColors().getData());
            setUniformValue(UniformName.DIFFUSE_MATERIAL_SAMPLER, texture.autoBind());
        }
        else
        {
            setLightingColors();
        }
        int[] castsCubeShadow = lighting.getCastsCubeShadow();
        setUniformValue(UniformName.CASTS_CUBE_SHADOW, castsCubeShadow);
        Texture[] depthTextures = (Texture[]) getPropertyValue(RenderProperty.CUBE_DEPTH_TEXTURES);
        for (int i = 0; i < Lighting.numLightsSupported; i++)
        {
            if (castsCubeShadow[i] == 1)
            {
                setUniformValue(UniformName.depthCubeSampler(i), depthTextures[i].autoBind());
            }
            else
            {
                //Even if light doesn't cast shadows, OpenGL may still query texture due to shader if branching.
                setUniformValue(UniformName.depthCubeSampler(i), nullCubeMapTexture.autoBind());
            }
        }
        Matrix4 modelMatrix = (Matrix4) getPropertyValue(RenderProperty.MODEL_MATRIX);
        setUniformValue(UniformName.MODEL_MATRIX, modelMatrix.m);
        setUniformValue(UniformName.SHININESS, getPropertyValue(RenderProperty.SHININESS));
    }

    @Override
    protected void onConfigChanged(ShaderBuilder shaderBuilder, Map<String, Object> config) throws GraphicsException
    {
        super.onConfigChanged(shaderBuilder, config);
        textured = (Boolean) config.get(GraphicsConfig.TEXTURED_MATERIAL);
    }
}
