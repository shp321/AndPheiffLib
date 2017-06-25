package com.pheiffware.lib.graphics.managed.texture.textureBuilders;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.pheiffware.lib.AssetLoader;
import com.pheiffware.lib.graphics.FilterQuality;
import com.pheiffware.lib.graphics.GraphicsException;
import com.pheiffware.lib.graphics.managed.texture.Texture2D;
import com.pheiffware.lib.graphics.managed.texture.TextureBinder;

import java.io.IOException;

/**
 * Created by Steve on 6/22/2017.
 */

public class ImageTextureBuilder extends TextureBuilder<Texture2D>
{
    private final AssetLoader assetLoader;
    private final String imageAssetPath;

    public ImageTextureBuilder(TextureBinder textureBinder, FilterQuality defaultFilterQuality, boolean defaultGenerateMipMaps, AssetLoader assetLoader, String imageAssetPath)
    {
        super(textureBinder, defaultFilterQuality, defaultGenerateMipMaps);
        this.imageAssetPath = imageAssetPath;
        this.assetLoader = assetLoader;
    }

    @Override
    public Texture2D build() throws GraphicsException
    {
        try
        {
            Bitmap bitmap = assetLoader.loadBitmap(imageAssetPath);
            Texture2D texture = new Texture2D(textureBinder, bitmap.getWidth(), bitmap.getHeight());
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
            bitmap.recycle();

            filterQuality.applyToBoundTexture2D(generateMipMaps);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, sWrap);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, tWrap);

            return texture;
        }
        catch (IOException exception)
        {
            throw new GraphicsException(exception);
        }
    }
}
