package sy.grapheditor.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

import org.eclipse.emf.common.command.CommandStackListener;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sy.grapheditor.api.Commands;
import sy.grapheditor.api.GConnectorValidator;
import sy.grapheditor.api.GJointSkin;
import sy.grapheditor.api.GNodeSkin;
import sy.grapheditor.api.GraphEditor;
import sy.grapheditor.api.SelectionManager;
import sy.grapheditor.api.SkinLookup;

import sy.grapheditor.core.connections.ConnectionEventManager;
import sy.grapheditor.core.connections.ConnectorDragManager;

import sy.grapheditor.core.model.DefaultModelEditingManager;
import sy.grapheditor.core.model.ModelLayoutUpdater;
import sy.grapheditor.core.model.ModelSanityChecker;
import sy.grapheditor.core.selections.DefaultSelectionManager;
import sy.grapheditor.core.skins.SkinManager;
import sy.grapheditor.core.view.ConnectionLayouter;
import sy.grapheditor.core.view.GraphEditorView;
import sy.grapheditor.core.view.impl.DefaultConnectionLayouter;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import sy.grapheditor.model.GraphPackage;
import sy.grapheditor.api.utils.GraphEditorProperties;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;

import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;


/**
 * 默认图形编辑器实现的中央控制器类。
 *
 * <p>
 * 负责使用{@link SkinManager}为当前{@link GModel}创建所有皮肤实例，
 * 并将它们添加到{@link GraphEditorView}视图中。
 * </p>
 *
 * <p>
 * 还负责创建所有二级管理器，如 {@link ConnectorDragManager} 并在模型更改时重新初始化它们。
 * </p>
 *
 * <p>
 * 如果多个模型是资源集的一部分，则同步过程会相当复杂：
 * <ol>
 * <li>在资源集中的每个模型上注册侦听器（使用 {@link EContentAdapter}，本类的内部类</li>
 * <li>接收通知</li>
 * <li>将通知放入队列</li>
 * <li>{@link #process() process queue} 在每次重新加载和/或command堆栈更改时</li>
 * </ol>
 * 此过程处理有关command堆栈更改
 * 或 {@link GraphEditor#reload()} 的大量通知是确定有效更改包的一种非常安全的方法。
 * </p>
 *
 * <p>
 * 此实现是线程安全的：能够并行处理通知，且在 FX 线程上以块的形式处理这些通知。
 * </p>
 */
public class GraphEditorController<E extends GraphEditor> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphEditorController.class);

    private final GraphEditorEContentAdapter mContentAdapter = new GraphEditorEContentAdapter();

    private final Map<EStructuralFeature, Consumer<Notification>> mHandlersByFeature = new HashMap<>();
    private final Map<Integer, Consumer<Notification>> mHandlersByType = new HashMap<>();

    private final Collection<GNode> mNodeConnectorsDirty = new HashSet<>();
    private final Collection<GConnection> mConnectionsDirty = new HashSet<>();

    private final Collection<GConnection> mConnectionsToAdd = new HashSet<>();
    private final Collection<GNode> mNodesToAdd = new HashSet<>();
    private final Collection<GJoint> mJointsToAdd = new HashSet<>();
    private final Collection<GConnector> mConnectorsToAdd = new HashSet<>();

    private final CommandStackListener mCommandStackListener = event -> process();

    private final ModelEditingManager mModelEditingManager = new DefaultModelEditingManager(mCommandStackListener);
    private final ModelLayoutUpdater mModelLayoutUpdater;
    private final ConnectionLayouter mConnectionLayouter;
    private final ConnectorDragManager mConnectorDragManager;
    private final DefaultSelectionManager mSelectionManager;
    private final SkinManager mSkinManager;

    private final E mEditor;
    private final ChangeListener<GModel> mModelChangeListener = (w, o, n) -> modelChanged(o, n);

    /**
     * 在处理来自 EMF 的 {@link Notification} 时，它可能会触发其他更改，最终导致无限更新周期。
     * <br> 当此标志为 {@code true} 时，表示当前正在处理一批更新。
     * 所有新通知都将放入队列中，但稍后会进行处理。
     */
    private boolean mProcessing = false;

    /**
     * Creates a new controller instance. Only one instance should exist per
     * {@link GraphEditor} instance.
     *
     * @param pEditor                 {@link GraphEditor} instance
     * @param pSkinManager            the {@link SkinManager} instance
     * @param pView                   {@link GraphEditorView}
     * @param pConnectionEventManager the {@link ConnectionEventManager} instance
     */
    public GraphEditorController(final E pEditor, final SkinManager pSkinManager,
                                 final GraphEditorView pView, final ConnectionEventManager pConnectionEventManager, final GraphEditorProperties pProperties) {
        mEditor = Objects.requireNonNull(pEditor, "GraphEditor instance may not be null!");
        mConnectionLayouter = new DefaultConnectionLayouter(pSkinManager);

        mSkinManager = Objects.requireNonNull(pSkinManager, "SkinManager may not be null!");

        mModelLayoutUpdater = new ModelLayoutUpdater(pSkinManager, mModelEditingManager, pProperties);
        mConnectorDragManager = new ConnectorDragManager(pSkinManager, pConnectionEventManager, pView);
        mSelectionManager = new DefaultSelectionManager(pSkinManager, pView);

        initDefaultListeners();

        pEditor.modelProperty().addListener(new WeakChangeListener<>(mModelChangeListener));
        modelChanged(null, pEditor.getModel());
    }

    private void initDefaultListeners() {
        registerChangeListener(GraphPackage.Literals.GMODEL__NODES, e -> processNotification(e, this::addNode, this::removeNode));

        registerChangeListener(GraphPackage.Literals.GNODE__CONNECTORS,
                e -> processNotification(e, this::addConnector, this::removeConnector));

        registerChangeListener(GraphPackage.Literals.GNODE__CONNECTORS, e ->
        {
            if (e.getNotifier() instanceof GNode) {
                // 如果连接器被移除，父元素会为null...
                // 不过 getNotifier() 仍然返回连接器被移除的 GNode：
                markConnectorsDirty((GNode) e.getNotifier());
            }
        });
        registerChangeListener(GraphPackage.Literals.GMODEL__CONNECTIONS,
                e -> processNotification(e, this::addConnection, this::removeConnection));

        registerChangeListener(GraphPackage.Literals.GCONNECTION__JOINTS,
                e -> processNotification(e, (GJoint j) -> addJoint(j, e.getNotifier()),
                        (GJoint j) -> removeJoint(j, e.getNotifier())));

        registerChangeListener(GraphPackage.Literals.GJOINT__X, this::jointPositionChanged);
        registerChangeListener(GraphPackage.Literals.GJOINT__Y, this::jointPositionChanged);

        registerChangeListener(GraphPackage.Literals.GNODE__Y, this::nodePositionChanged);
        registerChangeListener(GraphPackage.Literals.GNODE__X, this::nodePositionChanged);

        registerChangeListener(GraphPackage.Literals.GNODE__HEIGHT, this::nodeSizeChanged);
        registerChangeListener(GraphPackage.Literals.GNODE__WIDTH, this::nodeSizeChanged);

        registerChangeListener(GraphPackage.Literals.GNODE__TYPE, e -> {
            final GNode node = (GNode) e.getNotifier();
            removeNode(node);
            addNode(node);
        });
    }

    /**
     * Registers a change listener
     *
     * @param pFeature {@link EStructuralFeature feature to watch}
     * @param pHandler {@link Consumer} handling the {@link Notification}
     */
    public final void registerChangeListener(final EStructuralFeature pFeature, final Consumer<Notification> pHandler) {
        Objects.requireNonNull(pFeature, "EStructuralFeature may not be null!");
        Objects.requireNonNull(pHandler, "Notification Consumer may not be null!");
        mHandlersByFeature.merge(pFeature, pHandler, Consumer::andThen);
    }

    /**
     * Registers a change listener
     *
     * @param pNotificationType {@link Notification notification type}
     * @param pHandler          {@link Consumer} handling the {@link Notification}
     */
    public final void registerChangeListener(final int pNotificationType, final Consumer<Notification> pHandler) {
        Objects.requireNonNull(pHandler, "Notification Consumer may not be null!");
        mHandlersByType.merge(pNotificationType, pHandler, Consumer::andThen);
    }

    private void modelChanged(final GModel pOldModel, final GModel pNewModel) {
        if (pOldModel != null) {
            final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pOldModel);
            editingDomain.getResourceSet().eAdapters().remove(mContentAdapter);

            for (int i = 0; i < pOldModel.getNodes().size(); i++) {
                removeNode(pOldModel.getNodes().get(i));
            }
            for (int i = 0; i < pOldModel.getConnections().size(); i++) {
                removeConnection(pOldModel.getConnections().get(i));
            }
        }

        // 删除所有可能残余的skin:
        mSkinManager.clear();

        if (pNewModel != null) {
            ModelSanityChecker.validate(pNewModel);

            mModelEditingManager.initialize(pNewModel);

            final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pNewModel);
            editingDomain.getResourceSet().eAdapters().add(mContentAdapter);

            if (pNewModel instanceof InternalEObject) {
                // add existing nodes through the registered change handlers:
                processFeatureChanged(new ENotificationImpl((InternalEObject) pNewModel, Notification.ADD_MANY,
                        GraphPackage.Literals.GMODEL__NODES, List.of(), List.copyOf(pNewModel.getNodes())));

                // add existing connections through the registered change handlers:
                processFeatureChanged(new ENotificationImpl((InternalEObject) pNewModel, Notification.ADD_MANY,
                        GraphPackage.Literals.GMODEL__CONNECTIONS, List.of(), List.copyOf(pNewModel.getConnections())));
            } else {
                for (final GNode node : pNewModel.getNodes()) {
                    addNode(node);
                }

                for (final GConnection connection : pNewModel.getConnections()) {
                    addConnection(connection);
                }
            }

            process();

            mSelectionManager.initialize(pNewModel);
            mConnectionLayouter.initialize(pNewModel);
            mConnectorDragManager.initialize(pNewModel);

            // 1) 等待至图形编辑器已在可见视图中注册（scene != null）
            // 2) 使用 Platform.runLater() 稍等片刻，以便 UI 有机会“安定下来”
            // 3) 更新布局值
            executeOnceWhenPropertyIsNonNull(mEditor.getView().sceneProperty(),
                    scene -> Platform.runLater(() -> updateLayoutValues(pNewModel)));
        }
    }

    public void updateLayoutValues(final GModel pModel) {
        // 因为使用 Platform.runLater() 推迟执行，所以必须检查给定的模型是否仍然有效：
        if (mEditor.getModel() != pModel) {
            return;
        }

        // 当模型从配置文件（或数据库）加载并绘制到 UI 时，
        // 有时渲染过程计算的尺寸与模型中存储的尺寸不同，
        // 这会触发更改..
        // 对于这种情况，等待渲染完成并手动更新布局值
        final CompoundCommand cmd = new CompoundCommand();
        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(pModel);
        if (editingDomain != null) {
            Commands.updateLayoutValues(cmd, pModel, getSkinLookup());
            if (!cmd.getCommandList().isEmpty() && cmd.canExecute()) {
                cmd.execute();
                process();
            }
        }
    }

    /**
     * 刷新当前排队的命令并立即处理它们。<br>
     * 仅当当前线程是 {@link Platform#isFxApplicationThread() FX Application Thread} 时才有效
     */
    public final void process() {
        if (mProcessing || !Platform.isFxApplicationThread()) {// 防止GUI在FX Application Thread之外的线程更新
            return;
        }

        mProcessing = true;
        try {
            Notification n;
            while ((n = mContentAdapter.getQueue().poll()) != null) {
                try {
                    processFeatureChanged(n);
                } catch (Exception e) {
                    LOGGER.error("Could not process update notification '{}': ", n, e); //$NON-NLS-1$
                }
            }

            if (!mNodesToAdd.isEmpty()) {
                /** key fix! 取消所有节点的选中*/
                getSelectionManager().clearSelection();
                for (final Iterator<GNode> iter = mNodesToAdd.iterator(); iter.hasNext(); ) {
                    final GNode next = iter.next();
                    mSkinManager.lookupOrCreateNode(next); // 隐式创建GNode界面
                    mModelLayoutUpdater.addNode(next);
                    mSelectionManager.addNode(next);
                    markConnectorsDirty(next);
                    // 新增节点后选中之
                    getSelectionManager().select(next);
                    iter.remove();
                }
            }

            if (!mConnectorsToAdd.isEmpty()) {
                for (final Iterator<GConnector> iter = mConnectorsToAdd.iterator(); iter.hasNext(); ) {
                    final GConnector next = iter.next();
                    mSkinManager.lookupOrCreateConnector(next); // 隐式创建
                    mConnectorDragManager.addConnector(next);
                    mSelectionManager.addConnector(next);
                    markConnectorsDirty(next.getParent());
                    iter.remove();
                }
            }

            if (!mConnectionsToAdd.isEmpty()) {
                for (final Iterator<GConnection> iter = mConnectionsToAdd.iterator(); iter.hasNext(); ) {
                    final GConnection next = iter.next();
                    mSkinManager.lookupOrCreateConnection(next); // 隐式创建
                    mSelectionManager.addConnection(next);
                    mConnectionsDirty.add(next);
                    iter.remove();
                }
            }

            if (!mJointsToAdd.isEmpty()) {
                for (final Iterator<GJoint> iter = mJointsToAdd.iterator(); iter.hasNext(); ) {
                    final GJoint next = iter.next();
                    mSkinManager.lookupOrCreateJoint(next); // 隐式创建
                    mModelLayoutUpdater.addJoint(next);
                    mSelectionManager.addJoint(next);
                    mConnectionsDirty.add(next.getConnection());
                    iter.remove();
                }
            }

            if (!mNodeConnectorsDirty.isEmpty()) {
                for (final Iterator<GNode> iter = mNodeConnectorsDirty.iterator(); iter.hasNext(); ) {
                    mSkinManager.updateConnectors(iter.next());
                    iter.remove();
                }
            }

            if (!mConnectionsDirty.isEmpty()) {
                for (final Iterator<GConnection> iter = mConnectionsDirty.iterator(); iter.hasNext(); ) {
                    final GConnection conn = iter.next();
                    mSkinManager.updateJoints(conn);
                    iter.remove();
                }
            }

            processingDone();
        } finally {
            mProcessing = false;
        }
    }

    private void processFeatureChanged(final Notification pNotification) {
        // 调用所有注册的consumer(registered for the feature)
        final Consumer<Notification> consumerForFeature;
        if ((consumerForFeature = mHandlersByFeature.get(pNotification.getFeature())) != null) {
            consumerForFeature.accept(pNotification);
        }

        // 调用所有注册的consumer(registered for the feature)
        final Consumer<Notification> consumerForType;
        if ((consumerForType = mHandlersByType.get(pNotification.getEventType())) != null) {
            consumerForType.accept(pNotification);
        }
    }

    /**
     * Called when all queued commands have been processed
     */
    protected void processingDone() {
        mConnectionLayouter.redrawAll();
    }

    private void nodePositionChanged(final Notification pChange) {
        final GNode node = (GNode) pChange.getNotifier();
        if (node != null) {
            final GNodeSkin skin = mSkinManager.lookupNode(node);
            if (skin != null) {
                skin.getRoot().relocate(node.getX(), node.getY());
            }
        }
    }

    private void nodeSizeChanged(final Notification pChange) {
        final GNode node = (GNode) pChange.getNotifier();
        if (node != null) {
            final GNodeSkin skin = mSkinManager.lookupNode(node);
            if (skin != null) {
                skin.getRoot().resize(node.getWidth(), node.getHeight());
            }
        }
    }

    private void jointPositionChanged(final Notification pChange) {
        final GJoint joint = (GJoint) pChange.getNotifier();
        if (joint != null) {
            final GJointSkin skin = mSkinManager.lookupJoint(joint);
            if (skin != null) {
                skin.initialize();
            }
        }
    }

    private void addJoint(final GJoint pJoint) {
        mJointsToAdd.add(pJoint);
    }

    private void addJoint(final GJoint pJoint, final Object pNotifier) {
        addJoint(pJoint);
        updateConnectionAfterJointChange(pJoint, pNotifier);
    }

    private void removeJoint(final GJoint pJoint) {
        mJointsToAdd.remove(pJoint);

        mSelectionManager.removeJoint(pJoint);
        mSelectionManager.getSelectedJoints().remove(pJoint);
        mModelLayoutUpdater.removeJoint(pJoint);
        mSkinManager.removeJoint(pJoint);
    }

    private void removeJoint(final GJoint pJoint, final Object pNotifier) {
        removeJoint(pJoint);
        updateConnectionAfterJointChange(pJoint, pNotifier);
    }

    private void updateConnectionAfterJointChange(final GJoint pJoint, final Object pNotifier) {
        if (pJoint.getConnection() != null) {
            mConnectionsDirty.add(pJoint.getConnection());
        } else if (pNotifier instanceof GConnection) {
            mConnectionsDirty.add((GConnection) pNotifier);
        }
    }

    /**
     * @param pNode {@link GNode} of which the {@link GConnector connectors} have
     *              been changed
     */
    protected final void markConnectorsDirty(final GNode pNode) {
        mNodeConnectorsDirty.add(pNode);
    }

    private void addConnection(final GConnection pConnection) {
        mConnectionsToAdd.add(pConnection);
        for (final GJoint joint : pConnection.getJoints()) {
            addJoint(joint);
        }
    }

    private void removeConnection(final GConnection pConnection) {
        mConnectionsToAdd.remove(pConnection);

        mSelectionManager.removeConnection(pConnection);
        mSelectionManager.getSelectedConnections().remove(pConnection);
        mSkinManager.removeConnection(pConnection);

        for (final GJoint joint : pConnection.getJoints()) {
            removeJoint(joint);
        }

    }

    private void addNode(final GNode pNode) {
        mNodesToAdd.add(pNode);

        for (int i = 0; i < pNode.getConnectors().size(); i++) {
            addConnector(pNode.getConnectors().get(i));
        }
    }

    private void removeNode(final GNode pNode) {
        mNodesToAdd.remove(pNode);

        for (int i = 0; i < pNode.getConnectors().size(); i++) {
            removeConnector(pNode.getConnectors().get(i));
        }

        mSelectionManager.removeNode(pNode);
        mSelectionManager.clearSelection(pNode);
        mModelLayoutUpdater.removeNode(pNode);
        mSkinManager.removeNode(pNode);

        // release the processor
//        ProcessorManager.removeProcessorId(pNode.getId());
//        pNode.getProcessorPane().release();
//        pNode.setProcessorPane(null);
//        pNode.setProcessor(null);
    }

    private void addConnector(final GConnector pConnector) {
        mConnectorsToAdd.add(pConnector);
    }

    private void removeConnector(final GConnector pConnector) {
        mConnectorsToAdd.remove(pConnector);

        mSelectionManager.removeConnector(pConnector);
        mConnectorDragManager.removeConnector(pConnector);
        mSkinManager.removeConnector(pConnector);
    }

    /**
     * @return {@link ConnectionLayouter}
     */
    public final ConnectionLayouter getConnectionLayouter() {
        return mConnectionLayouter;
    }

    /**
     * @return {@link GraphEditor} instance
     */
    public final E getEditor() {
        return mEditor;
    }

    /**
     * @return {@link SkinLookup} instance
     */
    public final SkinLookup getSkinLookup() {
        return mSkinManager;
    }

    /**
     * Gets the selection manager currently being used.
     *
     * @return selection manager currently being used.
     */
    public final SelectionManager getSelectionManager() {
        return mSelectionManager;
    }

    /**
     * Sets the validator that determines what connections can be created.
     *
     * @param validator a {@link GConnectorValidator} implementation, or null to use the
     *                  default
     */
    public final void setConnectorValidator(final GConnectorValidator validator) {
        mConnectorDragManager.setValidator(validator);
    }

    /**
     * @return {@link ModelEditingManager}
     */
    public final ModelEditingManager getModelEditingManager() {
        return mModelEditingManager;
    }

    /**
     * Delegates the contents of the given {@link Notification} to the given
     * {@link Consumer consumers}:
     * <ul>
     * <li>In case of {@link Notification#ADD} or {@link Notification#ADD_MANY},
     * the add consumer will be called with each added element</li>
     * <li>In case of {@link Notification#REMOVE} or
     * {@link Notification#REMOVE_MANY}, the remove consumer will be called with
     * each added element</li>
     * </ul>
     * IMPORTANT: The added/removed values are casted without any checks, any
     * one calling this method should take extra care to not mix wrong types!
     *
     * @param pNotification the Notification to examine
     * @param pAdd          Consumer to invoke with the new element(s) (if any)
     * @param pRemove       Consumer to invoke with the deleted element(s) (if any)
     */
    protected static <T> void processNotification(final Notification pNotification, final Consumer<T> pAdd, final Consumer<T> pRemove) {
        Objects.requireNonNull(pNotification);
        Objects.requireNonNull(pAdd);
        Objects.requireNonNull(pRemove);
        switch (pNotification.getEventType()) {
            case Notification.ADD:
                @SuppressWarnings("unchecked") final T newValue = (T) pNotification.getNewValue();
                pAdd.accept(newValue);
                break;

            case Notification.ADD_MANY:
                @SuppressWarnings("unchecked") final List<T> newValues = (List<T>) pNotification.getNewValue();
                newValues.forEach(pAdd);
                break;

            case Notification.REMOVE:
                @SuppressWarnings("unchecked") final T oldValue = (T) pNotification.getOldValue();
                pRemove.accept(oldValue);
                break;

            case Notification.REMOVE_MANY:
                @SuppressWarnings("unchecked") final List<T> oldValues = (List<T>) pNotification.getOldValue();
                oldValues.forEach(pRemove);
                break;
        }
    }

    /**
     * <p>
     * Attaches a value listener to the given {@link ObservableValue} and when
     * the value changes to a non-{@code null} value the given {@link Consumer}
     * will be invoked with the new value and the listener will be removed.
     * </p>
     * <p>
     * NOTE: If the {@link ObservableValue} already has a non-{@code null} value
     * the {@link Consumer} will be invoked directly.
     * </p>
     * <p>
     * This proves useful for linking things together before a property is
     * necessarily set.
     * </p>
     *
     * @param pProperty {@link ObservableValue} to observe
     * @param pConsumer {@link Consumer} to call with the first non-{@code null} value
     */
    private static <T> void executeOnceWhenPropertyIsNonNull(final ObservableValue<T> pProperty, final Consumer<T> pConsumer) {
        if (pProperty == null) {
            return;
        }

        final T value = pProperty.getValue();
        if (value != null) {
            pConsumer.accept(value);
        } else {
            final InvalidationListener listener = new InvalidationListener() {

                @Override
                public void invalidated(final Observable observable) {
                    final T newValue = pProperty.getValue();
                    if (newValue != null) {
                        pProperty.removeListener(this);
                        pConsumer.accept(newValue);
                    }
                }
            };
            pProperty.addListener(listener);
        }
    }

    private static class GraphEditorEContentAdapter extends EContentAdapter {

        private final Queue<Notification> imQueue = new ConcurrentLinkedQueue<>();

        @Override
        public final void notifyChanged(final Notification pNotification) {
            super.notifyChanged(pNotification);
            if (pNotification.getEventType() != Notification.REMOVING_ADAPTER) {
                imQueue.add(pNotification);
            }
        }

        Queue<Notification> getQueue() {
            return imQueue;
        }
    }
}
