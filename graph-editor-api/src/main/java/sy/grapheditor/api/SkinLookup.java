/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.api;

import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GNode;

/**
 * 提供各种通过model实体参数查询其对应的skin实体的方法。
 */
public interface SkinLookup {

    /**
     * Gets the skin for the given node.
     *
     * @param node a {@link GNode} instance
     *
     * @return the associated {@link GNodeSkin} instance
     */
    GNodeSkin lookupNode(final GNode node);

    GNodeSkin lookupNode(long id);

    /**
     * Gets the skin for the given connector.
     *
     * @param connector a {@link GConnector} instance
     *
     * @return the associated {@link GConnectorSkin} instance
     */
    GConnectorSkin lookupConnector(final GConnector connector);

    /**
     * Gets the skin for the given connection.
     *
     * @param connection a {@link GConnection} instance
     *
     * @return the associated {@link GConnectionSkin} instance
     */
    GConnectionSkin lookupConnection(final GConnection connection);

    /**
     * Gets the skin for the given joint.
     *
     * @param joint a {@link GJoint} instance
     *
     * @return the associated {@link GJointSkin} instance
     */
    GJointSkin lookupJoint(final GJoint joint);

    /**
     * Gets the tail skin for the given connector.
     *
     * @param connector a {@link GConnector} instance
     *
     * @return the associated {@link GTailSkin} instance
     */
    GTailSkin lookupTail(final GConnector connector);
}
