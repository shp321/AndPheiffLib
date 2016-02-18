package com.pheiffware.lib.graphics.managed;

import android.content.res.AssetManager;
import android.opengl.GLES20;

import com.pheiffware.lib.graphics.FatalGraphicsException;
import com.pheiffware.lib.graphics.FilterQuality;
import com.pheiffware.lib.graphics.utils.ProgramUtils;
import com.pheiffware.lib.graphics.utils.TextureUtils;
import com.pheiffware.lib.meshLegacy.MeshLegacy;

import java.util.HashMap;
import java.util.Map;

/**
 * A core object which manages references to and between graphics objects.
 * Created by Steve on 2/13/2016.
 */
public class ManGL
{
    private final AssetManager assetManager;
    private final Map<String, Integer> vertexShaders = new HashMap<>();
    private final Map<String, Integer> fragmentShaders = new HashMap<>();
    private final Map<String, Program> programs = new HashMap<>();
    private final Map<String, Texture> textures = new HashMap<>();
    private final Map<String, MeshLegacy> meshMap = new HashMap<String, MeshLegacy>();
    private final FilterQuality defaultFilterQuality;
    public ManGL(AssetManager assetManager, FilterQuality defaultFilterQuality)
    {
        this.assetManager = assetManager;
        this.defaultFilterQuality = defaultFilterQuality;
    }


    /**
     * Creates a vertex shader from the given asset path if not already loaded.
     *
     * @param vertexShaderAssetPath
     * @return
     * @throws FatalGraphicsException
     */
    public int getVertexShader(String vertexShaderAssetPath) throws FatalGraphicsException
    {
        Integer vertexShaderHandle = vertexShaders.get(vertexShaderAssetPath);
        if (vertexShaderHandle == null)
        {
            vertexShaderHandle = ProgramUtils.createShader(assetManager, GLES20.GL_VERTEX_SHADER, vertexShaderAssetPath);
            vertexShaders.put(vertexShaderAssetPath, vertexShaderHandle);
        }
        return vertexShaderHandle;
    }

    /**
     * Creates a fragment shader from the given asset path if not already loaded.
     *
     * @param fragmentShaderAssetPath
     * @return
     * @throws FatalGraphicsException
     */
    public int getFragmentShader(String fragmentShaderAssetPath) throws FatalGraphicsException
    {
        Integer fragmentShaderHandle = fragmentShaders.get(fragmentShaderAssetPath);
        if (fragmentShaderHandle == null)
        {
            fragmentShaderHandle = ProgramUtils.createShader(assetManager, GLES20.GL_FRAGMENT_SHADER, fragmentShaderAssetPath);
            fragmentShaders.put(fragmentShaderAssetPath, fragmentShaderHandle);
        }
        return fragmentShaderHandle;
    }

    /**
     * Creates a program from given shaders if not already loaded.
     *
     * @param name
     * @param vertexShaderAssetPath
     * @param fragmentShaderAssetPath
     * @return
     * @throws FatalGraphicsException
     */
    public Program getProgram(String name, String vertexShaderAssetPath, String fragmentShaderAssetPath) throws FatalGraphicsException
    {
        Program program = programs.get(name);
        if (program == null)
        {
            int vertexShaderHandle = getVertexShader(vertexShaderAssetPath);
            int fragmentShaderHandle = getFragmentShader(fragmentShaderAssetPath);
            program = new Program(vertexShaderHandle, fragmentShaderHandle);
            programs.put(name, program);
        }
        return program;
    }


    /**
     * Loads an image into a newly created texture or gets previously loaded texture.
     *
     * @param imageAssetPath  image path
     * @param generateMipMaps Set to true if it makes sense to try to use mip-maps for this texture. This may be ignored based on given filter quality.
     * @param filterQuality   HIGH/MEDIUM/LOW (look up my definition)
     * @param sWrapMode       typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @param tWrapMode       typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @return GL handle to texture
     * @throws FatalGraphicsException
     */
    public Texture getImageTexture(String imageAssetPath, boolean generateMipMaps, FilterQuality filterQuality, int sWrapMode, int tWrapMode) throws FatalGraphicsException
    {
        Texture texture = textures.get(imageAssetPath);
        if (texture == null)
        {
            texture = new Texture(TextureUtils.genTextureFromImage(assetManager, imageAssetPath, generateMipMaps, filterQuality, sWrapMode, tWrapMode));
            textures.put(imageAssetPath, texture);
        }
        return texture;
    }

    /**
     * Generates a texture which can have colors rendered onto it.
     *
     * @param pixelWidth    width
     * @param pixelHeight   height
     * @param alpha         should there be an alpha channel?
     * @param filterQuality HIGH/MEDIUM/LOW (look up my definition)
     * @param sWrapMode     typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @param tWrapMode     typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @return GL handle to texture
     */
    public Texture getColorRenderTexture(String name, int pixelWidth, int pixelHeight, boolean alpha, FilterQuality filterQuality, int sWrapMode, int tWrapMode)
    {
        Texture texture = textures.get(name);
        if (texture == null)
        {
            texture = new Texture(TextureUtils.genTextureForColorRendering(pixelWidth, pixelHeight, alpha, filterQuality, sWrapMode, tWrapMode));
            textures.put(name, texture);
        }
        return texture;
    }

    /**
     * Generates a texture which can have depth rendered onto it.
     *
     * @param pixelWidth    width
     * @param pixelHeight   height
     * @param filterQuality HIGH/MEDIUM/LOW (look up my definition)
     * @param sWrapMode     typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @param tWrapMode     typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @return GL handle to texture
     */
    public Texture getDepthRenderTexture(String name, int pixelWidth, int pixelHeight, FilterQuality filterQuality, int sWrapMode, int tWrapMode)
    {
        Texture texture = textures.get(name);
        if (texture == null)
        {
            texture = new Texture(TextureUtils.genTextureForDepthRendering(pixelWidth, pixelHeight, filterQuality, sWrapMode, tWrapMode));
            textures.put(name, texture);
        }
        return texture;

    }

    public Program getProgram(String name)
    {
        return programs.get(name);
    }

    /**
     * Loads an image into a newly created texture or gets previously loaded texture.
     * Filter quality defaulted.
     *
     * @param imageAssetPath  image path
     * @param generateMipMaps Set to true if it makes sense to try to use mip-maps for this texture. This may be ignored based on given filter quality.
     * @param sWrapMode       typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @param tWrapMode       typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @return GL handle to texture
     * @throws FatalGraphicsException
     */
    public Texture getImageTexture(String imageAssetPath, boolean generateMipMaps, int sWrapMode, int tWrapMode) throws FatalGraphicsException
    {
        return getImageTexture(imageAssetPath, generateMipMaps, defaultFilterQuality, sWrapMode, tWrapMode);
    }

    /**
     * Generates a texture which can have colors rendered onto it.
     * Filter quality defaulted.
     *
     * @param pixelWidth  width
     * @param pixelHeight height
     * @param alpha       should there be an alpha channel?
     * @param sWrapMode   typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @param tWrapMode   typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @return GL handle to texture
     */
    public Texture getColorRenderTexture(String name, int pixelWidth, int pixelHeight, boolean alpha, int sWrapMode, int tWrapMode)
    {
        return getColorRenderTexture(name, pixelWidth, pixelHeight, alpha, defaultFilterQuality, sWrapMode, tWrapMode);
    }

    /**
     * Generates a texture which can have depth rendered onto it.
     * Filter quality defaulted.
     *
     * @param pixelWidth  width
     * @param pixelHeight height
     * @param sWrapMode   typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @param tWrapMode   typically: GL_CLAMP_TO_EDGE, GL_CLAMP_TO_BORDER, GL_MIRRORED_REPEAT, GL_REPEAT
     * @return GL handle to texture
     */
    public Texture getDepthRenderTexture(String name, int pixelWidth, int pixelHeight, int sWrapMode, int tWrapMode)
    {
        return getDepthRenderTexture(name, pixelWidth, pixelHeight, defaultFilterQuality, sWrapMode, tWrapMode);
    }


    @Deprecated
    public AssetManager getAssetManager()
    {
        return assetManager;
    }

}