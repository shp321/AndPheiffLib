package com.pheiffware.lib.graphics.managed.light;

import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.Vec4F;

/**
 * A version of the Lighting class which is customized for holographic lighting.  This allows lights to either track with the eye or with the screen.
 * <p/>
 * Created by Steve on 7/25/2016.
 */
public class HoloLighting extends Lighting
{
    //Does this light exist in eye space (fixed in the viewer's frame of reference) or in screen space (fixed in the screen's frame of reference).
    private final boolean[] eyeSpace;

    /**
     * Creates a Lighting object representing the set of lights to use for rendering.  Each light's position and color is encoded as a 4 element block in the corresponding array.
     * Any additional lights supported by the implementation will be turned off.  Each light can be in eye space OR tethered to screen space.
     *
     * @param ambientLightColor the general ambient light
     * @param positions         the positions of the lights
     * @param colors            the colors of the lights
     * @param eyeSpace          is the light fixed in eye space or part of the screen space itself
     */
    public HoloLighting(float[] ambientLightColor, float[] positions, float[] colors, boolean eyeSpace[])
    {
        super(ambientLightColor, positions, colors);
        this.eyeSpace = new boolean[eyeSpace.length];
        System.arraycopy(eyeSpace, 0, this.eyeSpace, 0, eyeSpace.length);
    }

    @Override
    public void transformLightPositions(Vec4F transformedPositions, Matrix4 matrix)
    {
        Vec4F positions = getPositions();

        positions.setIndex(0);
        transformedPositions.setIndex(0);
        for (int i = 0; i < Lighting.numLightsSupported; i++)
        {
            if (isLightOn(i))
            {
                if (eyeSpace[i])
                {
                    transformedPositions.copy(positions);
                }
                else
                {
                    transformedPositions.copy(positions);
                    transformedPositions.transformBy(matrix);
                }
            }
            positions.next();
            transformedPositions.next();
        }
    }


//    @Override
//    protected void transformLight(float[] transformedLightPositions, int lightIndex, Matrix4 lightTransform)
//    {
//        if (eyeSpace[lightIndex])
//        {
//            //If light exists in eye-space, perform standard transformation
//            super.transformLight(transformedLightPositions, lightIndex, lightTransform);
//        }
//        else
//        {
//            //Otherwise, use the raw light position (no transform)
//            System.arraycopy(getPositions(), lightIndex * 4, transformedLightPositions, lightIndex * 4, 4);
//        }
//    }
}
