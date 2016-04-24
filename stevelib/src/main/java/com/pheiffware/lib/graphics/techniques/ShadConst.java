package com.pheiffware.lib.graphics.techniques;

/**
 * Naming conventions used in shaders for standard attributes/uniforms.
 * <p/>
 * Created by Steve on 4/15/2016.
 */
public class ShadConst
{
    public static final String VERTEX_POSITION_ATTRIBUTE = "vertexPosition";
    public static final String VERTEX_NORMAL_ATTRIBUTE = "vertexNormal";
    public static final String VERTEX_TEXCOORD_ATTRIBUTE = "vertexTexCoord";

    public static final String PROJECTION_MATRIX_UNIFORM = "projectionMatrix";
    public static final String VIEW_MODEL_MATRIX_UNIFORM = "viewModelMatrix";
    public static final String NORMAL_MATRIX_UNIFORM = "normalMatrix";

    public static final String AMBIENT_LIGHT_COLOR_UNIFORM = "ambientLightColor";

    public static final String LIGHT_COLOR_UNIFORM = "lightColor";

    public static final String AMBIENT_LIGHTMAT_COLOR_UNIFORM = "ambientLightMaterialColor";
    public static final String DIFF_LIGHTMAT_COLOR_UNIFORM = "diffuseLightMaterialColor";
    public static final String SPEC_LIGHTMAT_COLOR_UNIFORM = "specLightMaterialColor";

    public static final String LIGHT_POS_EYE_UNIFORM = "lightPositionEyeSpace";
    public static final String MATERIAL_SAMPLER_UNIFORM = "materialColorSampler";
    public static final String SHININESS_UNIFORM = "shininess";

}
