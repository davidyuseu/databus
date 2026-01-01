/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.databus.view.customskins.titled;

import sy.grapheditor.core.skins.defaults.DefaultTailSkin;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.api.utils.GeometryUtils;
import javafx.geometry.Point2D;

public class TitledTailSkin extends DefaultTailSkin {

    private static final String STYLE_CLASS = "titled-tail"; //$NON-NLS-1$
    private static final String STYLE_CLASS_ENDPOINT = "titled-tail-endpoint"; //$NON-NLS-1$

    private static final double SIZE = 15;

    public TitledTailSkin(final GConnector connector) {

        super(connector);

        line.getStyleClass().setAll(STYLE_CLASS);
        endpoint.getStyleClass().setAll(STYLE_CLASS_ENDPOINT);
        endpoint.getPoints().setAll(0D, 0D, 0D, SIZE, SIZE, SIZE, SIZE, 0D);

        group.setManaged(false);
    }

    @Override
    protected void layoutEndpoint(final Point2D position) {
        endpoint.setLayoutX(GeometryUtils.moveOnPixel(position.getX() - SIZE / 2));
        endpoint.setLayoutY(GeometryUtils.moveOnPixel(position.getY() - SIZE / 2));
    }
}
