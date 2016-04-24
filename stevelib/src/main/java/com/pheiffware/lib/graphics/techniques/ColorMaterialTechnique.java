package com.pheiffware.lib.graphics.techniques;

import com.pheiffware.lib.AssetLoader;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix3;
import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.ShadConst;
import com.pheiffware.lib.graphics.managed.program.Technique;
import com.pheiffware.lib.graphics.managed.program.Uniform;
import com.pheiffware.lib.graphics.utils.GraphicsUtils;

/**
 * Shades mesh with a constant surface color and one light.  Handles, ambient, diffuse and specular lighting.
 * <p/>
 * Required Properties:
 * <p/>
 * ShadConst.EYE_PROJECTION_MATRIX_PROPERTY - Matrix4
 * <p/>
 * ShadConst.EYE_VIEW_MATRIX_PROPERTY - Matrix4
 * <p/>
 * ShadConst.EYE_MODEL_MATRIX_PROPERTY) - Matrix4
 * <p/>
 * ShadConst.LIGHT_POS_PROPERTY - float[4]
 * <p/>
 * ShadConst.AMBIENT_LIGHT_COLOR_PROPERTY - float[4]
 * <p/>
 * ShadConst.LIGHT_COLOR_PROPERTY - float[4]
 * <p/>
 * ShadConst.AMBIENT_MAT_COLOR_PROPERTY - float[4]
 * <p/>
 * ShadConst.DIFF_MAT_COLOR_PROPERTY - float[4]
 * <p/>
 * ShadConst.SPEC_MAT_COLOR_PROPERTY - float[4]
 * <p/>
 * ShadConst.SHININESS_PROPERTY - float
 * <p/>
 * Created by Steve on 4/23/2016.
 */
public class ColorMaterialTechnique extends Technique
{
    private final Uniform shininessUniform;
    private final Uniform eyeProjUniform;
    private final Uniform eyeTransUniform;
    private final Uniform eyeNormUniform;
    private final Uniform ambLMUniform;
    private final Uniform diffLMUniform;
    private final Uniform specLMUniform;
    private final Uniform lightEyePosUniform;

    //Used internally to compute values to apply to uniforms
    private final Matrix4 viewModelMatrix = Matrix4.newIdentity();
    private final Matrix3 normalTransform = Matrix3.newIdentity();
    private final float[] lightMatColor = new float[4];
    private final float[] lightEyeSpace = new float[4];

    public ColorMaterialTechnique(AssetLoader al) throws GraphicsException
    {
        super(al, "shaders/vert_mncl.glsl", "shaders/frag_mncl.glsl");
        eyeProjUniform = getUniform(ShadConst.EYE_PROJECTION_MATRIX_UNIFORM);
        eyeTransUniform = getUniform(ShadConst.EYE_TRANSFORM_MATRIX_UNIFORM);
        eyeNormUniform = getUniform(ShadConst.EYE_NORMAL_MATRIX_UNIFORM);
        ambLMUniform = getUniform(ShadConst.AMBIENT_LIGHTMAT_COLOR_UNIFORM);
        diffLMUniform = getUniform(ShadConst.DIFF_LIGHTMAT_COLOR_UNIFORM);
        specLMUniform = getUniform(ShadConst.SPEC_LIGHTMAT_COLOR_UNIFORM);
        lightEyePosUniform = getUniform(ShadConst.LIGHT_POS_EYE_UNIFORM);
        shininessUniform = getUniform(ShadConst.SHININESS_UNIFORM);
    }


    @Override
    public void applyProperties()
    {
        Matrix4 projMatrix = (Matrix4) getPropertyValue(ShadConst.EYE_PROJECTION_MATRIX_PROPERTY);
        eyeProjUniform.setValue(projMatrix.m);

        Matrix4 viewMatrix = (Matrix4) getPropertyValue(ShadConst.EYE_VIEW_MATRIX_PROPERTY);
        Matrix4 modelMatrix = (Matrix4) getPropertyValue(ShadConst.EYE_MODEL_MATRIX_PROPERTY);
        viewModelMatrix.set(viewMatrix);
        viewModelMatrix.multiplyBy(modelMatrix);

        eyeTransUniform.setValue(viewModelMatrix.m);
        normalTransform.setNormalTransformFromMatrix4Fast(viewModelMatrix);
        eyeNormUniform.setValue(normalTransform.m);

        float[] lightColor = (float[]) getPropertyValue(ShadConst.AMBIENT_LIGHT_COLOR_PROPERTY);
        float[] matColor = (float[]) getPropertyValue(ShadConst.AMBIENT_MAT_COLOR_PROPERTY);
        GraphicsUtils.vecMultiply(4, lightMatColor, lightColor, matColor);
        ambLMUniform.setValue(lightMatColor);

        lightColor = (float[]) getPropertyValue(ShadConst.LIGHT_COLOR_PROPERTY);
        matColor = (float[]) getPropertyValue(ShadConst.DIFF_MAT_COLOR_PROPERTY);
        GraphicsUtils.vecMultiply(4, lightMatColor, lightColor, matColor);
        diffLMUniform.setValue(lightMatColor);

        matColor = (float[]) getPropertyValue(ShadConst.SPEC_MAT_COLOR_PROPERTY);
        GraphicsUtils.vecMultiply(4, lightMatColor, lightColor, matColor);
        specLMUniform.setValue(lightMatColor);

        float[] lightPosition = (float[]) getPropertyValue(ShadConst.LIGHT_POS_PROPERTY);
        viewMatrix.transformFloatVector(lightEyeSpace, 0, lightPosition, 0);
        lightEyePosUniform.setValue(lightEyeSpace);

        shininessUniform.setValue(getPropertyValue(ShadConst.SHININESS_PROPERTY));
    }

}
