/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core;

import java.util.Collection;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;

import sy.grapheditor.api.GConnectionSkin;
import sy.grapheditor.api.GConnectorSkin;
import sy.grapheditor.api.GConnectorValidator;
import sy.grapheditor.api.GJointSkin;
import sy.grapheditor.api.GNodeSkin;
import sy.grapheditor.api.GTailSkin;
import sy.grapheditor.api.GraphEditor;
import sy.grapheditor.api.SelectionManager;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.core.connections.ConnectionEventManager;
import sy.grapheditor.core.skins.GraphEditorSkinManager;
import sy.grapheditor.core.view.ConnectionLayouter;
import sy.grapheditor.core.view.GraphEditorView;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import sy.grapheditor.api.utils.GraphEditorProperties;
import sy.grapheditor.api.utils.RemoveContext;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ObjectPropertyBase;
import javafx.scene.layout.Region;
import javafx.util.Callback;


/**
 * Default implementation of the {@link GraphEditor}.
 */
public class DefaultGraphEditor implements GraphEditor {

    private final GraphEditorProperties mProperties;
    private final GraphEditorView mView;
    private final ConnectionEventManager mConnectionEventManager = new ConnectionEventManager();
    private final GraphEditorSkinManager mSkinManager;
//    @Getter
    private final GraphEditorController<DefaultGraphEditor> mController;
    public GraphEditorController<DefaultGraphEditor> getMController() {
        return mController;
    }

    private final ObjectProperty<GModel> mModelProperty = new ObjectPropertyBase<GModel>()
    {

        @Override
        public Object getBean()
        {
            return DefaultGraphEditor.this;
        }

        @Override
        public String getName()
        {
            return "model";
        }

    };

    /**
     * Creates a new default implementation of the {@link GraphEditor}.
     */
    public DefaultGraphEditor()
    {
        this(null);
    }

    /**
     * Creates a new default implementation of the {@link GraphEditor}.
     */
    public DefaultGraphEditor(final GraphEditorProperties pProperties)
    {
        mProperties = pProperties == null ? new GraphEditorProperties() : pProperties;
        mView = new GraphEditorView(mProperties);
        mSkinManager = new GraphEditorSkinManager(this, mView);
        mController = new GraphEditorController<>(this, mSkinManager, mView, mConnectionEventManager, mProperties);

        final ConnectionLayouter connectionLayouter = mController.getConnectionLayouter();
        mView.setConnectionLayouter(connectionLayouter);
        mSkinManager.setConnectionLayouter(connectionLayouter);
    }

    @Override
    public void setNodeSkinFactory(final Callback<GNode, GNodeSkin> pSkinFactory)
    {
        mSkinManager.setNodeSkinFactory(pSkinFactory);
    }

    @Override
    public void setConnectorSkinFactory(final Callback<GConnector, GConnectorSkin> pConnectorSkinFactory)
    {
        mSkinManager.setConnectorSkinFactory(pConnectorSkinFactory);
    }

    @Override
    public void setConnectionSkinFactory(final Callback<GConnection, GConnectionSkin> pConnectionSkinFactory)
    {
        mSkinManager.setConnectionSkinFactory(pConnectionSkinFactory);
    }

    @Override
    public void setJointSkinFactory(final Callback<GJoint, GJointSkin> pJointSkinFactory)
    {
        mSkinManager.setJointSkinFactory(pJointSkinFactory);
    }

    @Override
    public void setTailSkinFactory(final Callback<GConnector, GTailSkin> pTailSkinFactory)
    {
        mSkinManager.setTailSkinFactory(pTailSkinFactory);
    }

    @Override
    public void setConnectorValidator(final GConnectorValidator pValidator)
    {
        mController.setConnectorValidator(pValidator);
    }

    @Override
    public void setModel(final GModel pModel)
    {
        mModelProperty.set(pModel);
    }

    @Override
    public GModel getModel()
    {
        return mModelProperty.get();
    }

    @Override
    public void reload()
    {
        mController.process();
    }

    @Override
    public ObjectProperty<GModel> modelProperty()
    {
        return mModelProperty;
    }

    @Override
    public Region getView()
    {
        return mView;
    }

    @Override
    public GraphEditorProperties getProperties()
    {
        return mProperties;
    }

    @Override
    public SkinLookup getSkinLookup()
    {
        return mSkinManager;
    }

    @Override
    public SelectionManager getSelectionManager()
    {
        return mController.getSelectionManager();
    }

    @Override
    public void setOnConnectionCreated(final Function<GConnection, Command> pConsumer)
    {
        mConnectionEventManager.setOnConnectionCreated(pConsumer);
    }

    @Override
    public void setOnConnectionRemoved(final BiFunction<RemoveContext, GConnection, Command> pOnConnectionRemoved)
    {
        mConnectionEventManager.setOnConnectionRemoved(pOnConnectionRemoved);
        getModelEditingManager().setOnConnectionRemoved(pOnConnectionRemoved);
    }

    @Override
    public void setOnNodeRemoved(final BiFunction<RemoveContext, GNode, Command> pOnNodeRemoved)
    {
        getModelEditingManager().setOnNodeRemoved(pOnNodeRemoved);
    }

    @Override
    public void delete(Collection<EObject> pItems)
    {
        getModelEditingManager().remove(pItems);
    }

    private ModelEditingManager getModelEditingManager()
    {
        return mController.getModelEditingManager();
    }
}
