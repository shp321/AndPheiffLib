package com.pheiffware.lib.graphics.managed.collada;

import android.content.res.AssetManager;

import com.pheiffware.lib.graphics.GColor;
import com.pheiffware.lib.graphics.managed.mesh.Material;
import com.pheiffware.lib.graphics.managed.mesh.MeshGroup;
import com.pheiffware.lib.graphics.managed.mesh.Object3D;
import com.pheiffware.lib.utils.dom.DomUtils;
import com.pheiffware.lib.utils.dom.XMLParseException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.validation.Validator;

/**
 * Main class which parses a Collada file to produce a Collada object.  This holds lots of additional intermediate information which can be used during testing or for other purposes.
 * Created by Steve on 2/19/2016.
 */
public class ColladaFactory
{
    //Any material without a texture will have this as its default diffuse color
    static final GColor DEFAULT_AMBIENT = new GColor(0.1f, 0.1f, 0.1f, 1f);
    static final GColor DEFAULT_DIFFUSE = new GColor(0.6f, 0.6f, 0.6f, 1f);
    static final GColor DEFAULT_SPECULAR = new GColor(1f, 1f, 1f, 1f);

    //Any material which does not define shininess gets this value
    static final float DEFAULT_SHININESS = 2f;

    //If no default material is defined, this this is assigned automatically
    private static final Material defaultDefaultMaterial = new Material("", null, DEFAULT_AMBIENT, DEFAULT_DIFFUSE, DEFAULT_SPECULAR, DEFAULT_SHININESS);

    //Used to validate collada files against known schema
    private static final Validator validator = DomUtils.createValidator("meshes\\collada_schema_1_4_1.xsd");

    //Map from image ids to file names
    private final Map<String, String> imageFileNames = new HashMap<>();

    //Map from effect ids to effect data (effect data is identical to materials)
    private final Map<String, ColladaEffect> colladaEffects = new HashMap<>();

    //Map from material ids to material data
    private final Map<String, Material> materialsByID = new HashMap<>();

    //Map from ids to ColladaGeometry
    private final Map<String, ColladaGeometry> geometries = new HashMap<>();

    //Map from library node ids to MeshGroups (completely parsed/usable geometry objects).
    private final Map<String, MeshGroup> libraryMeshGroups = new HashMap<>();

    //Map from node names defined in the visual scene to collapsed collections of meshes, keyed by which material is used to render them
    private final Map<String, Object3D> objects = new HashMap<>();

    //List of all unnamed objects, either: node didn't have a name attribute, multiple nodes with the same name attribute (all but 1st put in this bin) or top-level geometry instance in a SketchUp node.
    private final List<Object3D> anonymousObjects = new LinkedList<>();

    //When position/normals are loaded, a 1/0 is appended to the end of the loaded data to create a homogeneous coordinate/vector
    private final boolean homogenizeCoordinates;


    public ColladaFactory(boolean homogenizeCoordinates)
    {
        this(homogenizeCoordinates, defaultDefaultMaterial);
    }

    public ColladaFactory(boolean homogenizeCoordinates, Material defaultMaterial)
    {
        this.homogenizeCoordinates = homogenizeCoordinates;
        //Store default material
        materialsByID.put("", defaultMaterial);
    }

    public Collada loadCollada(AssetManager assetManager, String assetFileName) throws XMLParseException
    {
        try
        {
            return loadCollada(assetManager.open(assetFileName));
        }
        catch (IOException e)
        {
            throw new XMLParseException(e);
        }
    }


    public Collada loadCollada(InputStream input) throws XMLParseException
    {
        Document doc = DomUtils.loadDocumentFromStream(input, validator);
        Element rootElement = doc.getDocumentElement();
        ColladaAuthoringSoftware colladaAuthoringSoftware = ColladaAuthoringSoftware.parse(rootElement);
        Element libraryImagesElement = DomUtils.assertGetSubElement(rootElement, "library_images");
        DomUtils.putSubElementsInMap(imageFileNames, libraryImagesElement, "image", "id", new ColladaLibraryImageFactory());
        Element libraryEffectsElement = DomUtils.assertGetSubElement(rootElement, "library_effects");
        DomUtils.putSubElementsInMap(colladaEffects, libraryEffectsElement, "effect", "id", new ColladaEffectFactory());
        Element libraryMaterialsElement = DomUtils.assertGetSubElement(rootElement, "library_materials");
        DomUtils.putSubElementsInMap(materialsByID, libraryMaterialsElement, "material", "id", new ColladaMaterialFactory(imageFileNames, colladaEffects));
        Element libraryGeometriesElement = DomUtils.assertGetSubElement(rootElement, "library_geometries");
        DomUtils.putSubElementsInMap(geometries, libraryGeometriesElement, "geometry", "id", new ColladaGeometryFactory(homogenizeCoordinates));

        Element libraryNodesElement = DomUtils.getSubElement(rootElement, "library_nodes");
        if (libraryNodesElement != null)
        {
            LibraryColladaNodeProcessor colladaNodeProcessor = new LibraryColladaNodeProcessor(libraryNodesElement, materialsByID, geometries, colladaAuthoringSoftware == ColladaAuthoringSoftware.BLENDER);
            libraryMeshGroups.putAll(colladaNodeProcessor.getLibraryMeshGroups());
        }
        Element sceneElement = DomUtils.assertGetSubElementChain(rootElement, "library_visual_scenes", "visual_scene");
        if (colladaAuthoringSoftware == ColladaAuthoringSoftware.SKETCHUP)
        {
            //SketchUp wraps everything in a "SketchUp" node.
            sceneElement = DomUtils.assertGetSubElement(sceneElement, "node");
            if (!sceneElement.getAttribute("name").equals("SketchUp"))
            {
                throw new XMLParseException("SketchUp Collada file missing root \"SketchUp\" node in scene");
            }
        }
        SceneColladaNodeProcessor colladaNodeProcessor = new SceneColladaNodeProcessor(sceneElement, materialsByID, geometries, colladaAuthoringSoftware == ColladaAuthoringSoftware.BLENDER, libraryMeshGroups);
        objects.putAll(colladaNodeProcessor.getObjects());
        anonymousObjects.addAll(colladaNodeProcessor.getAnonymousObjects());
        return new Collada(imageFileNames.values(), materialsByID, objects, anonymousObjects);
    }


    Map<String, String> getImageFileNames()
    {
        return imageFileNames;
    }

    Map<String, ColladaEffect> getColladaEffects()
    {
        return colladaEffects;
    }

    Map<String, ColladaGeometry> getGeometries()
    {
        return geometries;
    }

    Map<String, MeshGroup> getLibraryMeshGroups()
    {
        return libraryMeshGroups;
    }

    Map<String, Object3D> getObjects()
    {
        return objects;
    }

    List<Object3D> getAnonymousObjects()
    {
        return anonymousObjects;
    }
}
