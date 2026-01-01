/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.api.utils;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import sy.grapheditor.api.GConnectionSkin;
import sy.grapheditor.api.GConnectorSkin;
import sy.grapheditor.api.GConnectorStyle;
import sy.grapheditor.api.GJointSkin;
import sy.grapheditor.api.GNodeSkin;
import sy.grapheditor.api.GTailSkin;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import sy.grapheditor.model.GraphFactory;
import javafx.geometry.Point2D;
import javafx.scene.Node;

public class GeometryUtilsTest {

    private static final double NODE_X = 55;
    private static final double NODE_Y = 32;
    private static final double CONNECTOR_CENTER_X = 13;
    private static final double CONNECTOR_CENTER_Y = 29;
    private static final double CONNECTOR_WIDTH = 18;
    private static final double CONNECTOR_HEIGHT = 12;

    private final GModel model = GraphFactory.eINSTANCE.createGModel();
    private final GNode node = GraphFactory.eINSTANCE.createGNode();
    private final GConnector connector = GraphFactory.eINSTANCE.createGConnector();

    private SkinLookup skinLookup;

    @Before
    public void setUp() {

        // Assign some arbitrary position to the node.
        node.setX(NODE_X);
        node.setY(NODE_Y);

        model.getNodes().add(node);
        node.getConnectors().add(connector);

        skinLookup = new MockSkinLoop();
    }

    @Test
    public void testGetConnectorPosition() {
        // Should return the absolute position of the center of the connector.
        final Point2D target = new Point2D(NODE_X + CONNECTOR_CENTER_X, NODE_Y + CONNECTOR_CENTER_Y);
        assertEquals(GeometryUtils.getConnectorPosition(connector, skinLookup), target);
    }

    private static class MockSkinLoop implements SkinLookup
    {

        @Override
        public GNodeSkin lookupNode(GNode pNode)
        {
            return new GNodeSkin(pNode)
            {

                {
                    getRoot().relocate(NODE_X, NODE_Y);
                }

                @Override
                protected void selectionChanged(boolean pIsSelected)
                {
                    //  Auto-generated method stub

                }

                @Override
                public void setConnectorSkins(List<GConnectorSkin> pConnectorSkins)
                {
                    //  Auto-generated method stub

                }

                @Override
                public void layoutConnectors()
                {
                    //  Auto-generated method stub

                }

                @Override
                public Point2D getConnectorPosition(GConnectorSkin pConnectorSkin)
                {
                    return new Point2D(CONNECTOR_CENTER_X, CONNECTOR_CENTER_Y);
                }

                @Override
                public void releaseResource() {

                }
            };
        }

        @Override
        public GNodeSkin lookupNode(long id) {
            return null;
        }

        @Override
        public GConnectorSkin lookupConnector(GConnector pConnector)
        {
            return new GConnectorSkin(pConnector)
            {

                @Override
                protected void selectionChanged(boolean pIsSelected)
                {
                    //  Auto-generated method stub

                }

                @Override
                public Node getRoot()
                {
                    return null;
                }

                @Override
                public double getWidth()
                {
                    return CONNECTOR_WIDTH;
                }

                @Override
                public double getHeight()
                {
                    return CONNECTOR_HEIGHT;
                }

                @Override
                public void applyStyle(GConnectorStyle pStyle)
                {
                    //  Auto-generated method stub
                }
            };
        }

        @Override
        public GConnectionSkin lookupConnection(GConnection pConnection)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public GJointSkin lookupJoint(GJoint pJoint)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public GTailSkin lookupTail(GConnector pConnector)
        {
            throw new UnsupportedOperationException();
        }

    }
}
