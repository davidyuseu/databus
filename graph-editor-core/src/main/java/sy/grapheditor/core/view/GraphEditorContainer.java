/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core.view;

import sy.grapheditor.api.GraphEditor;
import sy.grapheditor.model.GModel;
import sy.grapheditor.api.window.AutoScrollingWindow;
import sy.grapheditor.api.window.GraphEditorMinimap;
import javafx.beans.value.ChangeListener;
import javafx.scene.layout.Region;


/**
 * 图形编辑器的容器。
 *
 * <p>
 * 适用于可能大于场景中可用空间的图形。
 * 用户可以通过右键单击和拖动来平移。
 * 可以显示小地图以帮助导航。
 * </p>
 *
 * <p>
 * Example:
 *
 * <pre>
 * <code>
 * GraphEditorContainer graphEditorContainer = new GraphEditorContainer();
 * GraphEditor graphEditor = new DefaultGraphEditor();
 *
 * graphEditorContainer.setGraphEditor(graphEditor);
 * graphEditorContainer.getMinimap().setVisible(true);
 * </code>
 * </pre>
 *
 * 图编辑器容器是一个 {@link Region}，可以通过通常的方式添加到 JavaFX 场景图中。
 * </p>
 *
 * <p>
 * 当一个 {@link GraphEditor} 被设置在这个容器中时，它的视图变为<b>非托管的</b>，
 * 它的宽度和高度值被设置为 {@link GModel} 实例中的值。
 * </p>
 */
public class GraphEditorContainer extends AutoScrollingWindow
{

    /**
     * default view stylesheet
     */
    private static final String STYLESHEET_VIEW = GraphEditorContainer.class
            .getResource("defaults.css").toExternalForm(); //$NON-NLS-1$

    private static final double MINIMAP_INDENT = 10;

    private final GraphEditorMinimap minimap = new GraphEditorMinimap();

    private GraphEditor graphEditor;
    private final ChangeListener<GModel> modelChangeListener = (observable, oldValue, newValue) -> modelChanged(newValue);

    /**
     * Creates a new {@link GraphEditorContainer}.
     */
    public GraphEditorContainer()
    {
        getChildren().add(minimap);

        minimap.setWindow(this);
        minimap.setVisible(false);
    }

    @Override
    public String getUserAgentStylesheet()
    {
        return STYLESHEET_VIEW;
    }

    private void modelChanged(final GModel newValue)
    {
        if (newValue != null)
        {
            graphEditor.getView().resize(newValue.getContentWidth(), newValue.getContentHeight());
        }
        checkWindowBounds();
        minimap.setModel(newValue);
    }

    /**
     * Sets the graph editor to be displayed in this container.
     *
     * @param pGraphEditor
     *            a {@link GraphEditor} instance
     */
    public void setGraphEditor(final GraphEditor pGraphEditor)
    {
        final GraphEditor previous = graphEditor;
        if (previous != null)
        {
            previous.modelProperty().removeListener(modelChangeListener);
            setEditorProperties(null);
        }

        graphEditor = pGraphEditor;

        if (pGraphEditor != null)
        {
            pGraphEditor.modelProperty().addListener(modelChangeListener);

            final Region view = pGraphEditor.getView();
            final GModel model = pGraphEditor.getModel();

            if (model != null)
            {
                view.resize(model.getContentWidth(), model.getContentHeight());
            }

            setContent(view);
            minimap.setContent(view);
            minimap.setModel(model);
            minimap.setSelectionManager(pGraphEditor.getSelectionManager());

            view.toBack();

            setEditorProperties(pGraphEditor.getProperties());
        }
        else
        {
            setEditorProperties(null);
            minimap.setContent(null);
            minimap.setModel(null);
        }
    }

    /**
     * Returns the {@link GraphEditorMinimap}
     *
     * @return the graph editor minimap
     */
    public GraphEditorMinimap getMinimap()
    {
        return minimap;
    }

    @Override
    protected void layoutChildren()
    {
        super.layoutChildren();

        if (getChildren().contains(minimap))
        {
            minimap.relocate(getWidth() - (minimap.getWidth() + MINIMAP_INDENT), MINIMAP_INDENT);
        }
    }
}
