package com.pheiffware.lib.geometry.collada;

import com.pheiffware.lib.graphics.managed.mesh.Material;
import com.pheiffware.lib.graphics.managed.mesh.MeshGroup;
import com.pheiffware.lib.graphics.managed.mesh.Object3D;
import com.pheiffware.lib.utils.dom.XMLParseException;

import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * When processing the main scene:
 * MeshGroups already loaded from the library_nodes scene are injected into the node map ahead of time.
 * Top-level objects are not flattened
 * Created by Steve on 2/19/2016.
 */
class SceneColladaNodeProcessor extends BaseColladaNodeProcessor
{
    private Map<String, Object3D> objects = new HashMap<>();
    private List<Object3D> anonymousObjects = new LinkedList<>();

    /**
     * On creation, this processes all nodes in the given element hierarchy, using provided libraryMeshGroups, to create Object3D instances.
     * It does not flatten top level objects and instead initializes objects geometry untransformed, but with initial transform specified.
     *
     * @param element                   what to parse
     * @param materialsByID             library of materials, mapped by id which may be looked up.
     * @param geometries                a map from ids to ColladaGeometries
     * @param ignoreMaterialAssignments if parsing blender, materials will already have been assigned inside ColladaGeometries and what is encountered in this node structure is ambiguous and should be ignored.
     * @param libraryMeshGroups         any previous parsed meshGroups mapped by id
     * @throws XMLParseException
     */
    public SceneColladaNodeProcessor(Element element, Map<String, Material> materialsByID, Map<String, ColladaGeometry> geometries, boolean ignoreMaterialAssignments, Map<String, MeshGroup> libraryMeshGroups) throws XMLParseException
    {
        super(materialsByID, geometries, ignoreMaterialAssignments);
        injectLibraryNodes(libraryMeshGroups);
        List<MeshGroupProxy> topLevelMeshGroupProxies = getMeshGroupProxies(element);
        for (MeshGroupProxy topLevelMeshGroupProxy : topLevelMeshGroupProxies)
        {
            String name = topLevelMeshGroupProxy.getName();
            float[] transform = topLevelMeshGroupProxy.getTransform();
            MeshGroup meshGroup = topLevelMeshGroupProxy.retrieveMeshGroup(false);
            Object3D object3D = new Object3D(transform, meshGroup);
            if (name == null)
            {
                anonymousObjects.add(object3D);
            }
            else
            {
                //We don't want to lose any objects which have the same name (author just didn't care about name) so dump them in anonymous bin.
                if (!objects.containsKey(name))
                {
                    objects.put(name, object3D);
                }
                else
                {
                    anonymousObjects.add(object3D);
                }
            }
        }
    }

    private void injectLibraryNodes(Map<String, MeshGroup> libraryMeshGroups)
    {
        for (Map.Entry<String, MeshGroup> entry : libraryMeshGroups.entrySet())
        {
            MeshGroup meshGroup = entry.getValue();
            meshGroupProxies.put(entry.getKey(), new DirectMeshGroupProxy(meshGroup));
        }
    }

    public Map<String, Object3D> getObjects()
    {
        return objects;
    }

    public List<Object3D> getAnonymousObjects()
    {
        return anonymousObjects;
    }
}