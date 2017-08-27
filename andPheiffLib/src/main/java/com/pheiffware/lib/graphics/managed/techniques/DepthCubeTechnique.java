package com.pheiffware.lib.graphics.managed.techniques;

import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.managed.program.RenderProperty;
import com.pheiffware.lib.graphics.managed.program.UniformName;
import com.pheiffware.lib.graphics.managed.program.shader.ShaderBuilder;

import java.util.Map;

/**
 * Renders the depth of geometry and nothing else.
 * <p>
 * Created by Steve on 6/21/2017.
 */

public class DepthCubeTechnique extends Technique3D
{
    private final Matrix4 projectionView = Matrix4.newZeroMatrix();
    private final Matrix4 projectionViewModel = Matrix4.newZeroMatrix();

    public DepthCubeTechnique(ShaderBuilder shaderBuilder, Map<String, Object> localConfig) throws GraphicsException
    {
        super(shaderBuilder, localConfig, "vert_depth.glsl", "frag_depth.glsl");
    }

    public void applyConstantPropertiesImplement()
    {
        projectionView.set((Matrix4) getPropertyValue(RenderProperty.PROJECTION_MATRIX));
        projectionView.multiplyBy((Matrix4) getPropertyValue(RenderProperty.VIEW_MATRIX));
    }

    @Override
    public void applyInstanceProperties()
    {
        projectionViewModel.set(projectionView);
        projectionViewModel.multiplyBy((Matrix4) getPropertyValue(RenderProperty.MODEL_MATRIX));
        setUniformValue(UniformName.PROJECTION_VIEW_MODEL_MATRIX, projectionViewModel.m);
    }
}
