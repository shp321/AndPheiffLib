package com.pheiffware.lib.examples.andGraphics;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.pheiffware.lib.AssetLoader;
import com.pheiffware.lib.and.gui.graphics.openGL.BaseGameFragment;
import com.pheiffware.lib.and.gui.graphics.openGL.SurfaceMetrics;
import com.pheiffware.lib.and.gui.graphics.openGL.TouchTransformGameView;
import com.pheiffware.lib.geometry.collada.ColladaMaterial;
import com.pheiffware.lib.graphics.Color4F;
import com.pheiffware.lib.graphics.FilterQuality;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.Matrix4;
import com.pheiffware.lib.graphics.Mesh;
import com.pheiffware.lib.graphics.managed.GLCache;
import com.pheiffware.lib.graphics.managed.engine.ColladaLoader;
import com.pheiffware.lib.graphics.managed.engine.ObjectHandle;
import com.pheiffware.lib.graphics.managed.engine.ObjectManager;
import com.pheiffware.lib.graphics.managed.engine.renderers.SimpleRenderer;
import com.pheiffware.lib.graphics.managed.light.Lighting;
import com.pheiffware.lib.graphics.managed.program.RenderProperty;
import com.pheiffware.lib.graphics.managed.program.RenderPropertyValue;
import com.pheiffware.lib.graphics.managed.program.Technique;
import com.pheiffware.lib.graphics.managed.techniques.ColorMaterialTechnique;
import com.pheiffware.lib.graphics.managed.techniques.TextureMaterialTechnique;
import com.pheiffware.lib.graphics.managed.texture.Texture2D;
import com.pheiffware.lib.utils.dom.XMLParseException;

import java.io.IOException;

/**
 * Created by Steve on 6/19/2017.
 */

public class Example3ManagedRenderingFragment extends BaseGameFragment
{
    @Override
    public TouchTransformGameView onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return new TouchTransformGameView(getContext(), new Example3ManagedRenderingFragment.Renderer(), FilterQuality.MEDIUM, false, true);
    }

    static class ExampleColladaLoader extends ColladaLoader
    {
        private final Technique colorTechnique;
        private final Technique textureTechnique;

        public ExampleColladaLoader(ObjectManager objectManager,
                                    GLCache glCache,
                                    AssetLoader al,
                                    String imageDirectory,
                                    boolean homogenizePositions,
                                    ColladaMaterial defaultColladaMaterial,
                                    Technique colorTechnique,
                                    Technique textureTechnique) throws GraphicsException
        {
            super(objectManager, glCache, al, imageDirectory, homogenizePositions, defaultColladaMaterial);
            this.colorTechnique = colorTechnique;
            this.textureTechnique = textureTechnique;
        }

        @Override
        protected void addMesh(Mesh mesh, ColladaMaterial material, Matrix4 initialMatrix, String name)
        {
            Technique technique;
            RenderPropertyValue[] renderProperties;

            if (material.imageFileName == null)
            {
                technique = colorTechnique;
                renderProperties = new RenderPropertyValue[]
                        {
                                new RenderPropertyValue(RenderProperty.MODEL_MATRIX, initialMatrix),
                                new RenderPropertyValue(RenderProperty.MAT_COLOR, material.diffuseColor.comps),
                                new RenderPropertyValue(RenderProperty.SPEC_MAT_COLOR, material.specularColor.comps),
                                new RenderPropertyValue(RenderProperty.SHININESS, material.shininess)
                        };
            }
            else
            {
                technique = textureTechnique;
                renderProperties = new RenderPropertyValue[]
                        {
                                new RenderPropertyValue(RenderProperty.MODEL_MATRIX, initialMatrix),
                                new RenderPropertyValue(RenderProperty.MAT_COLOR_TEXTURE, glCache.getTexture(getTexturePath(material.imageFileName))),
                                new RenderPropertyValue(RenderProperty.SPEC_MAT_COLOR, material.specularColor.comps),
                                new RenderPropertyValue(RenderProperty.SHININESS, material.shininess)
                        };
            }

            objectManager.addStaticMesh(mesh, technique, renderProperties);
        }


        @Override
        protected Texture2D loadTexture2D(String imagePath) throws GraphicsException
        {
            return glCache.buildImageTex(imagePath).build();
        }
    }

    private static class Renderer extends Example3DRenderer
    {
        private Lighting lighting;
        private ObjectManager manager;
        private ObjectHandle monkeyHandle;
        private ObjectHandle cubeHandle;
        private float rotationRate = 1f;
        private SimpleRenderer simpleRenderer;
        private ColorMaterialTechnique colorTechnique;
        private TextureMaterialTechnique textureTechnique;
        private Matrix4 cubeTransform = Matrix4.newTranslation(-2, 2, -4);
        private Matrix4 monkeyTransform = Matrix4.newTranslation(0, 0, -4);

        public Renderer()
        {
            super(90f, 1.0f, 100.0f, 0.01f);
        }

        @Override
        public int maxMajorGLVersion()
        {
            return 3;
        }

        @Override
        public void onSurfaceCreated(AssetLoader al, GLCache glCache, SurfaceMetrics surfaceMetrics) throws GraphicsException
        {
            super.onSurfaceCreated(al, glCache, surfaceMetrics);

            colorTechnique = new ColorMaterialTechnique(al);
            textureTechnique = new TextureMaterialTechnique(al);
            lighting = new Lighting(new float[]{-3, 3, 0, 1}, new float[]{1.0f, 1.0f, 1.0f, 1.0f});
            simpleRenderer = new SimpleRenderer();
            manager = new ObjectManager();
            ColladaMaterial defaultMaterial = new ColladaMaterial(
                    "default",
                    null,
                    new Color4F(1.0f, 1.0f, 1.0f, 1.0f),
                    new Color4F(1.0f, 1.0f, 1.0f, 1.0f),
                    new Color4F(1.0f, 1.0f, 1.0f, 1.0f), 1.0f);
            ExampleColladaLoader loader = new ExampleColladaLoader(
                    manager,
                    glCache,
                    al,
                    "images", //Where images are located
                    true, //Homogenize coordinates
                    defaultMaterial,
                    colorTechnique,
                    textureTechnique);
            try
            {
                loader.loadCollada("meshes/test_render.dae");
                loader.loadCollada("meshes/cubes.dae");
                manager.packAndTransfer();

            }
            catch (XMLParseException | IOException e)
            {
                throw new RuntimeException("Failure", e);
            }
            monkeyHandle = loader.getHandle("Monkey");
            cubeHandle = loader.getHandle("multi");
        }

        @Override
        protected void onDrawFrame(Matrix4 projectionMatrix, Matrix4 viewMatrix) throws GraphicsException
        {
            lighting.calcOnLightPositionsInEyeSpace(viewMatrix);

            monkeyTransform.rotateBy(rotationRate, 1, 1, 0);
            cubeTransform.rotateBy(rotationRate, 1, 1, 0);

            monkeyHandle.setProperty(RenderProperty.MODEL_MATRIX, monkeyTransform);
            cubeHandle.setProperty(RenderProperty.MODEL_MATRIX, cubeTransform);

            colorTechnique.setProperty(RenderProperty.PROJECTION_MATRIX, projectionMatrix);
            colorTechnique.setProperty(RenderProperty.VIEW_MATRIX, viewMatrix);
            colorTechnique.setProperty(RenderProperty.AMBIENT_LIGHT_COLOR, new float[]{0.2f, 0.2f, 0.2f, 1.0f});
            colorTechnique.setProperty(RenderProperty.LIGHTING, lighting);
            textureTechnique.setProperty(RenderProperty.PROJECTION_MATRIX, projectionMatrix);
            textureTechnique.setProperty(RenderProperty.VIEW_MATRIX, viewMatrix);
            textureTechnique.setProperty(RenderProperty.AMBIENT_LIGHT_COLOR, new float[]{0.2f, 0.2f, 0.2f, 1.0f});
            textureTechnique.setProperty(RenderProperty.LIGHTING, lighting);

            simpleRenderer.add(monkeyHandle);
            simpleRenderer.add(cubeHandle);
            simpleRenderer.render();
        }
    }
}
