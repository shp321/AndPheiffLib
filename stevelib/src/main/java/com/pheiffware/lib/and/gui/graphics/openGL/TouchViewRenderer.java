package com.pheiffware.lib.and.gui.graphics.openGL;

import android.opengl.GLES20;

import com.pheiffware.lib.geometry.Transform2D;
import com.pheiffware.lib.graphics.Camera;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix4;

/**
 * A basic implementation of the SimpleGLRenderer which provides support for a camera which can be moved by touching the screen. Extending classes can override onDrawFrame(Matrix4
 * projectionMatrix, Matrix4 viewMatrix) to perform drawing actions using matrices calculated from the camera.
 * <p/>
 * Created by Steve on 4/13/2016.
 */
public abstract class TouchViewRenderer implements SimpleGLRenderer
{
    //How far a move of a pointer on the screen scales to a translation of the camera
    private final double screenDragToCameraTranslation;
    private final Camera camera;

    public TouchViewRenderer(float initialFOV, float nearPlane, float farPlane, double screenDragToCameraTranslation)
    {
        this.screenDragToCameraTranslation = screenDragToCameraTranslation;
        camera = new Camera(initialFOV, 1, nearPlane, farPlane, false);
    }

    @Override
    public void onDrawFrame() throws GraphicsException
    {
        onDrawFrame(camera.getProjectionMatrix(), camera.getViewMatrix());
    }

    protected abstract void onDrawFrame(Matrix4 projectionMatrix, Matrix4 viewMatrix) throws GraphicsException;

    @Override
    public void onSurfaceResize(int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);
        camera.setAspect(width / (float) height);
    }

    /**
     * Must be called in renderer thread
     *
     * @param numPointers
     * @param transform   The transform generated by the last pointer motion event.
     */
    @Override
    public void touchTransformEvent(int numPointers, Transform2D transform)
    {
        if (numPointers > 2)
        {
            camera.zoom((float) transform.scale.x);
        }
        else if (numPointers > 1)
        {
            camera.roll((float) (180 * transform.rotation / Math.PI));
            camera.rotateScreenInputVector((float) transform.translation.x, (float) -transform.translation.y);
        }
        else
        {
            float cameraX = (float) (transform.translation.x * screenDragToCameraTranslation);
            float cameraZ = (float) (transform.translation.y * screenDragToCameraTranslation);
            camera.translateScreen(cameraX, 0, cameraZ);
        }
    }
}
