/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core.model;

import sy.grapheditor.api.EditorElement;
import sy.grapheditor.api.GJointSkin;
import sy.grapheditor.api.GNodeSkin;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.core.ModelEditingManager;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import sy.grapheditor.api.utils.GraphEditorProperties;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;


/**
 * Responsible for updating the {@link GModel}'s layout values at the end of
 * each mouse gesture.
 */
public class ModelLayoutUpdater {

    private final SkinLookup skinLookup;
    private final ModelEditingManager modelEditingManager;
    private final GraphEditorProperties properties;
    private final EventHandler<MouseEvent> mouseReleasedHandlerNode = event -> elementMouseReleased(EditorElement.NODE);
    private final EventHandler<MouseEvent> mouseReleasedHandlerJoint = event -> elementMouseReleased(EditorElement.JOINT);

    /**
     * Creates a new model layout updater. Only one instance should exist per
     * graph editor instance.
     *
     * @param pSkinLookup          the {@link SkinLookup} used to lookup skins
     * @param pModelEditingManager the {@link ModelEditingManager} used to update the model
     *                             values
     */
    public ModelLayoutUpdater(final SkinLookup pSkinLookup, final ModelEditingManager pModelEditingManager,
                              final GraphEditorProperties pProperties) {
        skinLookup = pSkinLookup;
        modelEditingManager = pModelEditingManager;
        properties = pProperties;
    }

    /**
     * Adds a handler to update the model when a node's layout properties
     * change.
     *
     * @param node the {@link GNode} whose values should be updated
     */
    public void addNode(final GNode node) {
        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null) {
            final Node root = nodeSkin.getRoot();
            if (root != null) {
                root.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandlerNode);
            }
        }
    }

    public void removeNode(final GNode node) {
        final GNodeSkin nodeSkin = skinLookup.lookupNode(node);
        if (nodeSkin != null) {
            final Node root = nodeSkin.getRoot();
            if (root != null) {
                root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandlerNode);
            }
            // 释放节点资源
            nodeSkin.releaseResource();
        }
    }

    /**
     * Adds a handler to update the model when a joint's layout properties
     * change.
     *
     * @param joint the {@link GJoint} whose values should be updated
     */
    public void addJoint(final GJoint joint) {
        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {
            final Node root = jointSkin.getRoot();
            if (root != null) {
                root.addEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandlerJoint);
            }
        }
    }

    public void removeJoint(final GJoint joint) {
        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {
            final Node root = jointSkin.getRoot();
            if (root != null) {
                root.removeEventHandler(MouseEvent.MOUSE_RELEASED, mouseReleasedHandlerJoint);
            }
        }
    }

    private void elementMouseReleased(final EditorElement pType) {
        if (canEdit(pType)) {
            modelEditingManager.updateLayoutValues(skinLookup);
        }
    }

    private boolean canEdit(final EditorElement pType) {
        final GraphEditorProperties props = properties;
        return props != null && !props.isReadOnly(pType);
    }
}
