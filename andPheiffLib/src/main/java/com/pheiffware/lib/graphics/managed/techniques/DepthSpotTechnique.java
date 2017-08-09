package com.pheiffware.lib.graphics.managed.techniques;

import com.pheiffware.lib.ParseException;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.managed.program.RenderProperty;
import com.pheiffware.lib.graphics.managed.program.shader.ShaderBuilder;

import java.io.IOException;
import java.util.Map;

/**
 * Renders the depth of geometry and nothing else.
 * Created by Steve on 6/21/2017.
 */

public class DepthSpotTechnique extends Technique3D
{
    private final Matrix4 projectionViewModelMatrix = Matrix4.newIdentity();

    public DepthSpotTechnique(ShaderBuilder shaderBuilder, Map<String, Object> localConfig) throws GraphicsException, IOException, ParseException
    {
        super(shaderBuilder, localConfig, new RenderProperty[]{
                RenderProperty.PROJECTION_LINEAR_DEPTH,
                RenderProperty.VIEW_MATRIX,
                RenderProperty.MODEL_MATRIX
        }, "vert_depth.glsl", "frag_depth.glsl");
    }

    public void applyConstantPropertiesImplement()
    {
        setProjectionLinearDepth();
    }

    @Override
    public void applyInstanceProperties()
    {
        setViewModel();
    }
}
