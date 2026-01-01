/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core.selections;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;

import sy.grapheditor.api.GConnectionSkin;
import sy.grapheditor.api.GConnectorSkin;
import sy.grapheditor.api.GJointSkin;
import sy.grapheditor.api.GNodeSkin;
import sy.grapheditor.api.GSkin;
import sy.grapheditor.api.SelectionManager;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.core.DefaultGraphEditor;
import sy.grapheditor.core.connections.Connectable;
import sy.grapheditor.core.connectors.DefaultConnectorTypes;
import sy.grapheditor.core.utils.EventUtils;
import sy.grapheditor.core.view.GraphEditorView;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import sy.grapheditor.api.utils.GraphEventManager;
import sy.grapheditor.api.utils.GraphInputGesture;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;


/**
 * 负责在图形编辑器中创建节点、连接和关节的选定。
 *
 * <p>
 * 当前可以通过单击来选择节点。 此外，可以通过拖动一个框来框选一个或多个节点、连接和关节。
 * </p>
 */
public class SelectionCreator {

    private final SkinLookup skinLookup;
    private final GraphEditorView view;
    private final SelectionDragManager selectionDragManager;
    private final SelectionManager selectionManager;

    private GModel model;

    private final Map<Node, EventHandler<MouseEvent>> mousePressedHandlers = new HashMap<>();
    private final Map<Node, EventHandler<MouseEvent>> mouseClickedHandlers = new HashMap<>();

    private final EventHandler<MouseEvent> viewPressedHandler = this::handleViewPressed;
    private final EventHandler<MouseEvent> viewDraggedHandler = this::handleViewDragged;
    private final EventHandler<MouseEvent> viewReleasedHandler = this::handleViewReleased;

    private final Set<EObject> selectedElementsBackup = new HashSet<>();

    private Rectangle2D selection;

    private Point2D selectionBoxStart;
    private Point2D selectionBoxEnd;

    /**
     * 创建一个新的选定创建者{@link SelectionCreator}实例。 每个 {@link DefaultGraphEditor} 实例应该只存在一个该实例。
     *
     * @param pSkinLookup           the {@link SkinLookup} used to look up skins
     * @param pView                 the {@link GraphEditorView} instance
     * @param pSelectionDragManager the {@link SelectionDragManager} instance for this graph
     *                              editor
     */
    public SelectionCreator(final SkinLookup pSkinLookup, final GraphEditorView pView, final SelectionManager pSelectionManager,
                            final SelectionDragManager pSelectionDragManager) {
        selectionManager = pSelectionManager;
        skinLookup = pSkinLookup;
        view = pView;
        selectionDragManager = pSelectionDragManager;

        pView.addEventHandler(MouseEvent.MOUSE_PRESSED, new WeakEventHandler<>(viewPressedHandler));
        pView.addEventHandler(MouseEvent.MOUSE_DRAGGED, new WeakEventHandler<>(viewDraggedHandler));
        pView.addEventHandler(MouseEvent.MOUSE_RELEASED, new WeakEventHandler<>(viewReleasedHandler));
    }

    /**
     * Initializes the selection creator for the current model.
     *
     * @param model the {@link GModel} currently being edited
     */
    public void initialize(final GModel model) {
        this.model = model;
        addClickSelectionMechanism();
    }

    /**
     * Adds a mechanism to select nodes by clicking on them.
     *
     * <p>
     * Holding the <b>shortcut</b> key while clicking will add to the existing
     * selection.
     * </p>
     */
    private void addClickSelectionMechanism() {
        // remove all listeners:
        EventUtils.removeEventHandlers(mousePressedHandlers, MouseEvent.MOUSE_PRESSED);
        EventUtils.removeEventHandlers(mouseClickedHandlers, MouseEvent.MOUSE_CLICKED);

        if (model != null) {
            addClickSelectionForNodes();
            addClickSelectionForJoints();
        }
    }

    private void handleSelectionClick(final MouseEvent event, final GSkin<?> skin) {
        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        if (!skin.isSelected()) {
            if (!event.isShortcutDown()) {
                selectionManager.clearSelection();
            } else {
                backupSelections();
            }
            selectionManager.select(skin.getItem());
        } else {
            if (event.isShortcutDown()) {
                selectionManager.clearSelection(skin.getItem());
            }
        }

        // 消费此事件，使其不会传递给父级（即视图）。
        event.consume();
    }

    public void addNode(final GNode node) {
        final GNodeSkin skin = skinLookup.lookupNode(node);
        if (skin != null) {
            final Region nodeRegion = skin.getRoot();

            if (!mousePressedHandlers.containsKey(nodeRegion)) {
                final EventHandler<MouseEvent> newNodePressedHandler = event -> handleNodePressed(event, skin);
                nodeRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, newNodePressedHandler);
                mousePressedHandlers.put(nodeRegion, newNodePressedHandler);
            }

            for (final GConnector connector : node.getConnectors()) {
                addConnector(connector);
            }
        }
    }

    public void removeNode(final GNode node) {
        final GNodeSkin skin = skinLookup.lookupNode(node);
        if (skin != null) {
            final Region nodeRegion = skin.getRoot();

            final EventHandler<MouseEvent> newNodePressedHandler = mousePressedHandlers.remove(nodeRegion);

            if (newNodePressedHandler != null) {
                nodeRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, newNodePressedHandler);
            }

            for (final GConnector connector : node.getConnectors()) {
                removeConnector(connector);
            }
        }
    }

    public void addConnector(final GConnector connector) {
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        if (connectorSkin != null) {
            final Node connectorRoot = connectorSkin.getRoot();

            if (!mouseClickedHandlers.containsKey(connectorRoot)) {
                final EventHandler<MouseEvent> connectorClickedHandler = event -> handleSelectionClick(event, connectorSkin);
                connectorRoot.addEventHandler(MouseEvent.MOUSE_CLICKED, connectorClickedHandler);
                mouseClickedHandlers.put(connectorRoot, connectorClickedHandler);
            }
        }
    }

    public void removeConnector(final GConnector connector) {
        final GConnectorSkin connectorSkin = skinLookup.lookupConnector(connector);
        if (connectorSkin != null) {
            final Node connectorRoot = connectorSkin.getRoot();
            final EventHandler<MouseEvent> connectorClickedHandler = mouseClickedHandlers.remove(connectorRoot);
            if (connectorClickedHandler != null) {
                connectorRoot.removeEventHandler(MouseEvent.MOUSE_CLICKED, connectorClickedHandler);
            }
        }
    }

    /**
     * @Description: 增加一个连接，在图形编辑器增加节点或连接一对Connector时调用
     * @Param:
     * @return:
     */
    public void addConnection(final GConnection connection) {
        final GConnectionSkin connSkin = skinLookup.lookupConnection(connection);
        if (connSkin != null) {

            final Node skinRoot = connSkin.getRoot();
            if (!mousePressedHandlers.containsKey(skinRoot)) {
                final EventHandler<MouseEvent> connectionPressedHandler = event -> handleConnectionPressed(event, connection);
                skinRoot.addEventHandler(MouseEvent.MOUSE_PRESSED, connectionPressedHandler);
                mousePressedHandlers.put(skinRoot, connectionPressedHandler);
            }
        }

        for (final GJoint joint : connection.getJoints()) {
            addJoint(joint);
        }
    }

    /**
     * @Description: 删除一个连接，在图形编辑器移除节点或通过拖动移除连接时调用
     * @Param:
     * @return:
     */
    public void removeConnection(final GConnection pConnection) {
        /**-Removing in Processor---------------------------------------------------------------------*/
        GConnector inputConnector = DefaultConnectorTypes.isInput(pConnection.getSource().getType())
                ? pConnection.getSource() : pConnection.getTarget();
        GConnector outputConnector = inputConnector == pConnection.getSource()
                ? pConnection.getTarget() : pConnection.getSource();
        GNode nodeWithInput = inputConnector.getParent();
        GNode nodeWithOutput = outputConnector.getParent();
        /** 若Node已经被删除则在{@link sy.databus.view.customskins.titled.TitledNodeSkin#releaseResource()}
         * 方法中已经完成了connection的解除方法。
         * */
        if (nodeWithInput != null && nodeWithInput.getAttachments() != null
                && nodeWithOutput != null && nodeWithOutput.getAttachments() != null) {
            ((Connectable) nodeWithInput.getAttachment(PROCESSOR))
                    .detachedAsInput(outputConnector.getParent());
            ((Connectable) nodeWithOutput.getAttachment(PROCESSOR))
                    .detachedAsOutput(inputConnector.getParent());
        }

        /**-Removing in UI-----------------------------------------------------------------------*/
        final GConnectionSkin connSkin = skinLookup.lookupConnection(pConnection);
        if (connSkin != null) {

            final EventHandler<MouseEvent> connectionPressedHandler = mousePressedHandlers.remove(connSkin.getRoot());
            if (connectionPressedHandler != null) {
                connSkin.getRoot().removeEventHandler(MouseEvent.MOUSE_PRESSED, connectionPressedHandler);
            }
        }

        for (final GJoint joint : pConnection.getJoints()) {
            removeJoint(joint);
        }
    }

    public void addJoint(final GJoint joint) {
        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {

            final Region jointRegion = jointSkin.getRoot();

            if (!mousePressedHandlers.containsKey(jointRegion)) {

                final EventHandler<MouseEvent> jointPressedHandler = event -> handleJointPressed(event, jointSkin);//, joint);
                jointRegion.addEventHandler(MouseEvent.MOUSE_PRESSED, jointPressedHandler);
                mousePressedHandlers.put(jointRegion, jointPressedHandler);
            }
        }
    }

    public void removeJoint(final GJoint joint) {
        final GJointSkin jointSkin = skinLookup.lookupJoint(joint);
        if (jointSkin != null) {

            final Region jointRegion = jointSkin.getRoot();

            final EventHandler<MouseEvent> jointPressedHandler = mousePressedHandlers.remove(jointRegion);

            if (jointPressedHandler != null) {
                jointRegion.removeEventHandler(MouseEvent.MOUSE_PRESSED, jointPressedHandler);
            }
        }
    }

    /**
     * Adds a click selection mechanism for nodes.
     */
    private void addClickSelectionForNodes() {
        for (final GNode node : model.getNodes()) {
            addNode(node);
        }
    }

    /**
     * Adds a click selection mechanism for joints.
     */
    private void addClickSelectionForJoints() {
        for (final GConnection connection : model.getConnections()) {
            addConnection(connection);
        }
    }

    /**
     * 处理选定节点上的鼠标按下事件。
     *
     * @param event    鼠标按下事件
     * @param nodeSkin 发生此事件的 {@link GNodeSkin}
     */
    private void handleNodePressed(final MouseEvent event, final GNodeSkin nodeSkin) {
        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        // 首先更新selection：
        handleSelectionClick(event, nodeSkin);

        // 如果此节点即将调整大小，不要绑定其他选定节点的位置。
        if (!nodeSkin.getRoot().isMouseInPositionForResize()) {
            selectionDragManager.bindPositions(nodeSkin.getRoot());
        }

        // 使用此事件，使其不会传递给父级(i.e. the view)。
        event.consume();
    }

    /**
     * 处理选定connection上的鼠标按下事件。
     *
     * @param event      鼠标按下事件
     * @param connection 发生此事件的 {@link GConnection}
     */
    private void handleConnectionPressed(final MouseEvent event, final GConnection connection) {
        if (!MouseButton.PRIMARY.equals(event.getButton())) {
            return;
        }

        final GConnectionSkin connSkin = skinLookup.lookupConnection(connection);
        if (connSkin != null) {
            handleSelectionClick(event, connSkin);
        }

        event.consume();
    }

    /**
     * 处理选定关节上的鼠标按下事件。
     *
     * @param event     鼠标按下事件
     * @param jointSkin 发生此事件的 {@link GJointSkin}
     */
    private void handleJointPressed(final MouseEvent event, final GJointSkin jointSkin, GJoint joint) {
        if (!MouseButton.PRIMARY.equals(event.getButton())) //|| event.isConsumed())
        {
            return;
        }

        if (event.isConsumed()) {
            handleConnectionPressed(event, joint.getConnection());
        } else {
            // 首先更新selection：
            handleSelectionClick(event, jointSkin);

            // 然后绑定其他从属项的位置：
            selectionDragManager.bindPositions(jointSkin.getRoot());

            event.consume();
        }
    }

    private void handleJointPressed(final MouseEvent event, final GJointSkin jointSkin) {
        if (!MouseButton.PRIMARY.equals(event.getButton()) || event.isConsumed()) {
            return;
        }

        // 首先更新selection：
        handleSelectionClick(event, jointSkin);

        // 然后绑定其他从属项的位置：
        selectionDragManager.bindPositions(jointSkin.getRoot());

        event.consume();

    }

    /**
     * 处理视图上的鼠标按下事件。
     *
     * @param pEvent 鼠标按下事件
     */
    private void handleViewPressed(final MouseEvent pEvent) {
        if (model == null || pEvent.isConsumed() || !activateGesture(pEvent)) {
            return;
        }

        if (!pEvent.isShortcutDown()) {
            selectionManager.clearSelection();
        } else {
            backupSelections();
        }

        selectionBoxStart = new Point2D(Math.max(0, pEvent.getX()), Math.max(0, pEvent.getY()));
    }

    /**
     * 处理视图上的鼠标拖动事件。
     *
     * @param pEvent 鼠标拖动事件
     */
    private void handleViewDragged(final MouseEvent pEvent) {
        if (model == null || pEvent.isConsumed() || selectionBoxStart == null || !activateGesture(pEvent)) {
            return;
        }

        selectionBoxEnd = new Point2D(Math.min(model.getContentWidth(), Math.max(0, pEvent.getX())),
                Math.min(model.getContentHeight(), Math.max(0, pEvent.getY())));

        evaluateSelectionBoxParameters();

        view.drawSelectionBox(selection.getMinX(), selection.getMinY(), selection.getWidth(), selection.getHeight());
        updateSelection(pEvent.isShortcutDown());
    }

    /**
     * 处理视图上的鼠标释放事件。
     *
     * @param event 鼠标释放事件
     */
    private void handleViewReleased(final MouseEvent event) {
        selectionBoxStart = null;
        if (finishGesture()) {
            event.consume();
        }
        view.hideSelectionBox();
    }

    private boolean isNodeSelected(final GNode node, final boolean isShortcutDown) {
        // 对于TitledNodeSkin类型节点，框选住标题头部即可被选中
        //        double selectedHeight = node.getSelectedHeight();
        double selectedHeight = 29d;// TitledNodeSkin标题区域的高度！
        return selection.contains(node.getX(), node.getY(), node.getWidth(), selectedHeight)
                || isShortcutDown && selectedElementsBackup.contains(node);
    }

    private boolean isJointSelected(final GJoint joint, final boolean isShortcutDown) {
        return selection.contains(joint.getX(), joint.getY()) || isShortcutDown && selectedElementsBackup.contains(joint);
    }

    private boolean isConnectionSelected(final GConnection connection, final boolean isShortcutDown) {
        return isShortcutDown && selectedElementsBackup.contains(connection);
    }

    /**
     * 根据选择框内部/外部的节点和关节更新选择。
     */
    private void updateSelection(final boolean isShortcutDown) {
        for (int i = 0; i < model.getNodes().size(); i++) {
            final GNode node = model.getNodes().get(i);

            if (isNodeSelected(node, isShortcutDown)) {
                selectionManager.select(node);
            } else {
                selectionManager.clearSelection(node);
            }
        }

        for (int i = 0; i < model.getConnections().size(); i++) {
            final GConnection connection = model.getConnections().get(i);

            if (isConnectionSelected(connection, isShortcutDown)) {
                selectionManager.select(connection);
            } else {
                selectionManager.clearSelection(connection);
            }
            int jointCount = connection.getJoints().size();
            int j = 0;
            boolean hasJointSelected = false;
            while (j < jointCount) {
                final GJoint joint = connection.getJoints().get(j);
                if (isJointSelected(joint, isShortcutDown)) {
                    hasJointSelected = true;
                    break;
                }
                j++;
            }
            j = 0;
            while (j < jointCount) {
                final GJoint joint = connection.getJoints().get(j);
                if (hasJointSelected)
                    selectionManager.select(joint);
                else
                    selectionManager.clearSelection(joint);
                j++;
            }
        }
    }

    /**
     * 将当前选定的对象存储在此类的备份列表{@link #selectedElementsBackup}中。
     *
     * <p>
     * 用于在按住快捷键时添加到现有选择（例如 Windows 中的 Ctrl）。
     * <p>
     */
    private void backupSelections() {
        selectedElementsBackup.clear();
        selectedElementsBackup.addAll(selectionManager.getSelectedItems());
    }

    /**
     * 根据光标起点和终点更新当前选择框的值。
     */
    private void evaluateSelectionBoxParameters() {
        final double x = Math.min(selectionBoxStart.getX(), selectionBoxEnd.getX());
        final double y = Math.min(selectionBoxStart.getY(), selectionBoxEnd.getY());

        final double width = Math.abs(selectionBoxStart.getX() - selectionBoxEnd.getX());
        final double height = Math.abs(selectionBoxStart.getY() - selectionBoxEnd.getY());

        selection = new Rectangle2D(x, y, width, height);
    }

    private boolean activateGesture(final Event pEvent) {
        final GraphEventManager eventManager = view.getEditorProperties();
        if (eventManager != null) {
            return eventManager.activateGesture(GraphInputGesture.SELECT, pEvent, this);
        }
        return true;
    }

    private boolean finishGesture() {
        final GraphEventManager eventManager = view.getEditorProperties();
        if (eventManager != null) {
            return eventManager.finishGesture(GraphInputGesture.SELECT, this);
        }
        return true;
    }
}
