/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.databus.view.customskins.titled;

import javafx.scene.layout.AnchorPane;
import sy.grapheditor.api.GConnectorSkin;
import sy.grapheditor.api.GConnectorStyle;
import sy.grapheditor.model.GConnector;
import javafx.css.PseudoClass;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;

/**
 * 正方形连接器.
 */
public class TitledConnectorSkin extends GConnectorSkin {

    private static final String STYLE_CLASS = "titled-connector"; //$NON-NLS-1$
    private static final String STYLE_CLASS_FORBIDDEN_GRAPHIC = "titled-connector-forbidden-graphic"; //$NON-NLS-1$

    private static final double SIZE = 15;

    private static final PseudoClass PSEUDO_CLASS_ALLOWED = PseudoClass.getPseudoClass("allowed"); //$NON-NLS-1$
    private static final PseudoClass PSEUDO_CLASS_FORBIDDEN = PseudoClass.getPseudoClass("forbidden"); //$NON-NLS-1$
    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected"); //$NON-NLS-1$

    private final Pane root = new Pane();

    private final Group forbiddenGraphic;

    public TitledConnectorSkin(final GConnector connector) {

        super(connector);

        root.setMinSize(SIZE, SIZE);
        root.setPrefSize(SIZE, SIZE);
        root.setMaxSize(SIZE, SIZE);
        root.getStyleClass().setAll(STYLE_CLASS);
        root.setPickOnBounds(false);

        forbiddenGraphic = createForbiddenGraphic();
        root.getChildren().addAll(forbiddenGraphic);
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public double getWidth() {
        return SIZE;
    }

    @Override
    public double getHeight() {
        return SIZE;
    }

    @Override
    public void applyStyle(final GConnectorStyle style) {

        switch (style) {

        case DEFAULT:
            root.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            root.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            forbiddenGraphic.setVisible(false);
            break;

        case DRAG_OVER_ALLOWED:
            root.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, false);
            root.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, true);
            forbiddenGraphic.setVisible(false);
            break;

        case DRAG_OVER_FORBIDDEN:
            root.pseudoClassStateChanged(PSEUDO_CLASS_FORBIDDEN, true);
            root.pseudoClassStateChanged(PSEUDO_CLASS_ALLOWED, false);
            forbiddenGraphic.setVisible(true);
            break;
        }
    }

    @Override
    protected void selectionChanged(boolean isSelected) {
        if (isSelected) {
            root.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
        } else {
            root.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
        }
    }

    private Group createForbiddenGraphic() {

        final Group group = new Group();
        final AnchorPane diamond = new AnchorPane();
        diamond.setStyle("-fx-background-color: red");
        final Line firstLine = new Line(1, 1, SIZE - 1, SIZE - 1);
        final Line secondLine = new Line(1, SIZE - 1, SIZE - 1, 1);

        firstLine.getStyleClass().add(STYLE_CLASS_FORBIDDEN_GRAPHIC);
        secondLine.getStyleClass().add(STYLE_CLASS_FORBIDDEN_GRAPHIC);
        diamond.getChildren().addAll(firstLine, secondLine);
        group.getChildren().addAll(diamond);
        group.setVisible(false);

        return group;
    }
}
