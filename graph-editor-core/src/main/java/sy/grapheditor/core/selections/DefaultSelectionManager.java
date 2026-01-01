/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core.selections;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import sy.grapheditor.api.SelectionManager;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.core.DefaultGraphEditor;
import sy.grapheditor.core.connections.Connectable;
import sy.grapheditor.core.connectors.DefaultConnectorTypes;
import sy.grapheditor.core.view.GraphEditorView;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import javafx.collections.ObservableSet;

import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;


/**
 * 管理与一个或多个节点和/或关节的选定相关的所有图形编辑器逻辑。
 *
 * <p>
 * 将某些工作委派给以下类。
 *
 * <ol>
 * <li>SelectionCreator - 通过单击或拖动创建对象选择
 * <li>SelectionDragManager - 确保选定对象在拖动时一起移动
 * <li>SelectionTracker - 跟踪当前选择
 * </ol>
 *
 * </p>
 */
public class DefaultSelectionManager implements SelectionManager
{

    private final SelectionCreator selectionCreator;
    private final SelectionDragManager selectionDragManager;
    private final SelectionTracker selectionTracker;

    private GModel model;

    /**
     * Creates a new default selection manager. Only one instance should exist
     * per {@link DefaultGraphEditor} instance.
     *
     * @param skinLookup
     *            the {@link SkinLookup} instance in use
     * @param view
     *            the {@link GraphEditorView} instance in use
     */
    public DefaultSelectionManager(final SkinLookup skinLookup, final GraphEditorView view)
    {
        selectionDragManager = new SelectionDragManager(skinLookup, view, this);
        selectionCreator = new SelectionCreator(skinLookup, view, this, selectionDragManager);
        selectionTracker = new SelectionTracker(skinLookup);
    }

    /**
     * Initializes the selection manager for the given model.
     *
     * @param model
     *            the {@link GModel} currently being edited
     */
    public void initialize(final GModel model)
    {
        this.model = model;

        selectionCreator.initialize(model);
        selectionTracker.initialize(model);
    }

    public void addNode(final GNode node)
    {
        selectionCreator.addNode(node);
    }

    public void removeNode(final GNode node)
    {
        selectionCreator.removeNode(node);
    }

    public void addConnector(final GConnector connector)
    {
        selectionCreator.addConnector(connector);
    }

    public void removeConnector(final GConnector connector)
    {
        selectionCreator.removeConnector(connector);
    }

    public void addConnection(final GConnection connection)
    {
        selectionCreator.addConnection(connection);
        GConnector inputConnector = DefaultConnectorTypes.isInput(connection.getSource().getType())
                ? connection.getSource() : connection.getTarget();
        GConnector outputConnector = inputConnector == connection.getSource()
                ? connection.getTarget() : connection.getSource();
        GNode nodeWithInput = inputConnector.getParent();
        GNode nodeWithOutput = outputConnector.getParent();
        // 若Node尚且为null
        if (nodeWithInput != null && nodeWithInput.getAttachments() != null
                && nodeWithOutput != null && nodeWithOutput.getAttachments() != null) {
            ((Connectable) nodeWithInput.getAttachment(PROCESSOR))
                    .connectedAsInput(outputConnector.getParent());
            ((Connectable) nodeWithOutput.getAttachment(PROCESSOR))
                    .connectedAsOutput(inputConnector.getParent());
        }
    }

    public void removeConnection(final GConnection connection)
    {
        selectionCreator.removeConnection(connection);
    }

    public void addJoint(final GJoint joint)
    {
        selectionCreator.addJoint(joint);
    }

    public void removeJoint(final GJoint joint)
    {
        selectionCreator.removeJoint(joint);
    }

    @Override
    public ObservableSet<EObject> getSelectedItems()
    {
        return selectionTracker.getSelectedItems();
    }

    @Override
    public void select(final EObject object)
    {
        getSelectedItems().add(object);
    }

    @Override
    public void clearSelection(final EObject object)
    {
        getSelectedItems().remove(object);
    }

    @Override
    public boolean isSelected(EObject object)
    {
        return getSelectedItems().contains(object);
    }

    @Override
    public List<GNode> getSelectedNodes()
    {
        return selectionTracker.getSelectedNodes();
    }

    @Override
    public List<GConnection> getSelectedConnections()
    {
        return selectionTracker.getSelectedConnections();
    }

    @Override
    public List<GJoint> getSelectedJoints()
    {
        return selectionTracker.getSelectedJoints();
    }

    @Override
    public void clearSelection()
    {
        if (!getSelectedItems().isEmpty())
        {
            // copy to prevent ConcurrentModificationException
            // (removal triggers update notification which in turn could modify the selection)
            final EObject[] selectedItems = getSelectedItems().toArray(new EObject[0]);
            for (final EObject remove : selectedItems)
            {
                getSelectedItems().remove(remove);
            }
        }
    }

    @Override
    public void selectAll()
    {
        if (model != null)
        {
            getSelectedItems().addAll(model.getNodes());
            for (final GConnection connection : model.getConnections())
            {
                getSelectedItems().add(connection);

                for (final GJoint joint : connection.getJoints())
                {
                    getSelectedItems().add(joint);
                }
            }
        }
    }
}
