package sy.grapheditor.core.skins;

import java.util.List;

import sy.grapheditor.api.GConnectionSkin;
import sy.grapheditor.api.GConnectorSkin;
import sy.grapheditor.api.GJointSkin;
import sy.grapheditor.api.GNodeSkin;
import sy.grapheditor.api.GraphEditorSkins;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.core.view.ConnectionLayouter;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GNode;

/**
 * Graph编辑器skin管理器
 *
 */
public interface SkinManager extends SkinLookup, GraphEditorSkins
{

    /**
     * @param pConnectionLayouter
     *            {@link ConnectionLayouter}
     */
    void setConnectionLayouter(final ConnectionLayouter pConnectionLayouter);

    /**
     * remove all cached skins and clear the graph editor view
     */
    void clear();

    /**
     * Removes the given {@link GNode} skin from the view
     *
     * @param pNodeToRemove
     *            node to remove
     */
    void removeNode(final GNode pNodeToRemove);

    /**
     * Removes the given {@link GConnector} skin from the view
     *
     * @param pConnectorToRemove
     *            connector to remove
     */
    void removeConnector(final GConnector pConnectorToRemove);

    /**
     * Removes the given {@link GConnection} skin from the view
     *
     * @param pConnectionToRemove
     *            connection to remove
     */
    void removeConnection(final GConnection pConnectionToRemove);

    /**
     * Removes the given {@link GJoint} skin from the view
     *
     * @param pJointToRemove
     *            joint to remove
     */
    void removeJoint(final GJoint pJointToRemove);
    /**
     * Calls {@link GNodeSkin#setConnectorSkins(List)} to update a nodes list of
     * connectors.
     *
     * @param pNode
     *            node to update
     */
    void updateConnectors(final GNode pNode);

    /**
     * Calls {@link GConnectionSkin#setJointSkins(List)} to update a connections
     * list of joints.
     *
     * @param pConnection
     *            connection to update
     */
    void updateJoints(final GConnection pConnection);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pNode
     * @return skin
     */
    GNodeSkin lookupOrCreateNode(final GNode pNode);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pConnector
     * @return skin
     */
    GConnectorSkin lookupOrCreateConnector(final GConnector pConnector);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pConnection
     * @return skin
     */
    GConnectionSkin lookupOrCreateConnection(final GConnection pConnection);

    /**
     * Creates (if not yet existing) and returns the skin for the given item
     *
     * @param pJoint
     * @return skin
     */
    GJointSkin lookupOrCreateJoint(final GJoint pJoint);


}
