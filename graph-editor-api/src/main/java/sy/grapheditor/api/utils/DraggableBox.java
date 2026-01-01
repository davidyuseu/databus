/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.api.utils;

import sy.grapheditor.api.EditorElement;
import javafx.event.Event;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import lombok.Getter;
import lombok.Setter;

/**
 * 可以显示子项的可拖动框。
 *
 * <p>
 * 这是 {@link StackPane} 的子类，将相应地布置其子类。
 * box的大小需要通过{@code resize(width, height)}设置，不受父布局的影响。
 * </p>
 */
public class DraggableBox extends StackPane
{

    private static final double DEFAULT_ALIGNMENT_THRESHOLD = 5;

    private final EditorElement mType;

    /**
     * stored value of {@link #getLayoutX()}, see
     * {@link #storeClickValuesForDrag(double, double)}
     */
    double lastLayoutX;
    /**
     * stored value of {@link #getLayoutY()}, see
     * {@link #storeClickValuesForDrag(double, double)}
     */
    double lastLayoutY;

    /**
     * stored mouse position, see
     * {@link #storeClickValuesForDrag(double, double)}
     */
    double lastMouseX;
    /**
     * stored mouse position, see
     * {@link #storeClickValuesForDrag(double, double)}
     */
    double lastMouseY;

    private GraphEditorProperties editorProperties;

    // Note that ResizableBox subclass currently pays no attention to alignment targets!
    private double[] alignmentTargetsX;
    private double[] alignmentTargetsY;

    private double alignmentThreshold = DEFAULT_ALIGNMENT_THRESHOLD;

    private Point2D snapToGridOffset = Point2D.ZERO;

    private DraggableBox dependencyX;
    private DraggableBox dependencyY;

    protected boolean draggedResizable = false;

    public void setDraggedResizable(boolean draggedResizable) {
        this.draggedResizable = draggedResizable;
    }

    public boolean isDraggedResizable(){
        return draggedResizable;
    }

    /**
     * Creates an empty draggable box.
     *
     * @param pType
     *         {@link EditorElement}
     */
    public DraggableBox(final EditorElement pType)
    {
        mType = pType;
        setPickOnBounds(false);

        addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
    }

    /**
     * Called after the skin (using this box as root node) is removed. Can be
     * overridden for cleanup.
     */
    public void dispose()
    {
        finishGesture(GraphInputGesture.MOVE);
        dependencyX = null;
        dependencyY = null;
    }

    /**
     * Sets the editor properties object that the drag logic should respect.
     *
     * <p>
     * This method is called by the framework. Custom skins should <b>not</b>
     * call it. Editor properties should instead be set via the graph editor
     * instance.
     * </p>
     *
     * @param pEditorProperties
     *            the {@link GraphEditorProperties} instance for the graph
     *            editor
     */
    public void setEditorProperties(final GraphEditorProperties pEditorProperties)
    {
        editorProperties = pEditorProperties;
    }

    /**
     * Gets the set of x values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @return a list of x values that the box will align to when dragged, or {@code null}
     */
    public double[] getAlignmentTargetsX()
    {
        return alignmentTargetsX;
    }

    /**
     * Set a dependent {@link DraggableBox} that will be moved (on the X axis)
     * when this box is moved on the X-Axis.
     *
     * @param pDependencyX
     *            dependent {@link DraggableBox}
     */
    public void bindLayoutX(DraggableBox pDependencyX)
    {
        dependencyX = pDependencyX;
    }

    /**
     * Set a dependent {@link DraggableBox} that will be moved (on the Y axis)
     * when this box is moved on the Y-Axis.
     *
     * @param pDependencyY
     *            dependent {@link DraggableBox}
     */
    public void bindLayoutY(DraggableBox pDependencyY)
    {
        dependencyY = pDependencyY;
    }

    /**
     * Sets the set of x values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @param pAlignmentTargetsX a list of x values that the box will align to when dragged, or {@code null}
     */
    public void setAlignmentTargetsX(final double[] pAlignmentTargetsX)
    {
        alignmentTargetsX = pAlignmentTargetsX;
    }

    /**
     * Gets the set of y values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @return a list of y values that the box will align to when dragged, or {@code null}
     */
    public double[] getAlignmentTargetsY()
    {
        return alignmentTargetsY;
    }

    /**
     * Sets the set of y values that the box will align to when dragged close enough.
     *
     * <p>
     * This mechanism will be active if the list is not {@code null} and not empty. If both this mechanism and
     * snap-to-grid are active, snap-to-grid will take priority.
     * </p>
     *
     * @param pAlignmentTargetsY a list of y values that the box will align to when dragged, or {@code null}
     */
    public void setAlignmentTargetsY(final double[] pAlignmentTargetsY)
    {
        alignmentTargetsY = pAlignmentTargetsY;
    }

    /**
     * Gets the alignment threshold value.
     *
     * <p>
     * If the distance between the box and an alignment value is less than or equal to this threshold, the box will move
     * to the alignment value.
     * </p>
     *
     * @return the alignment threshold value
     */
    public double getAlignmentThreshold() {
        return alignmentThreshold;
    }

    /**
     * Sets the alignment threshold value.
     *
     * <p>
     * If the distance between the box and an alignment target is lower than this threshold, the box will move to the
     * alignment target.
     * </p>
     *
     * @param pAlignmentThreshold the alignment threshold value
     */
    public void setAlignmentThreshold(final double pAlignmentThreshold) {
        alignmentThreshold = pAlignmentThreshold;
    }

    /**
     * Gets the offset that the box will snap to when a drag finishes and snap-to-grid is active.
     *
     * @return the snap offset for snap-to-grid calculations
     */
    public Point2D getSnapToGridOffset() {
        return snapToGridOffset;
    }

    /**
     * Sets the offset that the box will snap to when a drag finishes and snap-to-grid is active.
     *
     * <p>
     * The offset is taken from the top-left corner and is <b>(0, 0)</b> by default. If the default value is used, the
     * top-left corner of the box will snap exactly onto a grid line.
     * </p>
     *
     * @param pSnapToGridOffset the snap offset for snap-to-grid calculations
     */
    public void setSnapToGridOffset(final Point2D pSnapToGridOffset) {
        snapToGridOffset = pSnapToGridOffset;
    }

    @Override
    public boolean isResizable() {
        return false;
    }

    /**
     * Gets whether or not the current mouse position would lead to a resize
     * operation.
     *
     * @return {@code true} if the mouse is near the edge of the rectangle so
     *         that a resize would occur
     */
    public boolean isMouseInPositionForResize()
    {
        return false;
    }

    /**
     * @return negated value of {@link GraphEditorProperties#isReadOnly(EditorElement)}
     */
    protected boolean isEditable()
    {
        return editorProperties != null && !editorProperties.isReadOnly(mType);
    }

    /**
     * activate the given {@link GraphInputGesture}
     */
    boolean activateGesture(final GraphInputGesture pGesture, final Event pEvent)
    {
        if (editorProperties != null)
        {
            return editorProperties.activateGesture(pGesture, pEvent, this);
        }
        return true;
    }

    /**
     * deactivate the given {@link GraphInputGesture}
     */
    boolean finishGesture(final GraphInputGesture pGesture)
    {
        return editorProperties == null || editorProperties.finishGesture(pGesture, this);
    }

    /**
     * Handles mouse-pressed events.
     *
     * @param pEvent
     *            a {@link MouseEvent}
     */
    protected void handleMousePressed(final MouseEvent pEvent)
    {
        if (pEvent.getButton() != MouseButton.PRIMARY || !isEditable())
        {
            return;
        }

        final Point2D cursorPosition = GeometryUtils.getCursorPosition(pEvent, getContainer(this));
        storeClickValuesForDrag(cursorPosition.getX(), cursorPosition.getY());
        pEvent.consume();
    }

    /**
     * Handles mouse-dragged events.
     *
     * @param pEvent {@link MouseEvent}
     */
    protected void handleMouseDragged(final MouseEvent pEvent)
    {
        if (pEvent.getButton() != MouseButton.PRIMARY || !isEditable() || !activateGesture(GraphInputGesture.MOVE, pEvent))
        {
            return;
        }

        final Point2D cursorPosition = GeometryUtils.getCursorPosition(pEvent, getContainer(this));
        handleDrag(cursorPosition.getX(), cursorPosition.getY());
        pEvent.consume();
    }

    /**
     * Handles mouse-released events.
     *
     * @param pEvent {@link MouseEvent}
     */
    protected void handleMouseReleased(final MouseEvent pEvent)
    {
        if (finishGesture(GraphInputGesture.MOVE))
        {
            pEvent.consume();
        }
    }

    /**
     * 存储最后一次鼠标点击时的相关布局值
     * (mouse-pressed event).
     *
     * @param pX
     *            单击事件的 container-x 位置
     * @param pY
     *            单击事件的容器 container-y 位置
     */
    protected void storeClickValuesForDrag(final double pX, final double pY)
    {
        lastLayoutX = getLayoutX();
        lastLayoutY = getLayoutY();

        lastMouseX = pX;
        lastMouseY = pY;
    }

    /**
     * 将某个值四舍五入到最接近的网格间距倍数。
     *
     * @param value
     *            a double value
     *
     * @return 输入值四舍五入到最接近的网格间距倍数
     */
    protected double roundToGridSpacing(final double value)
    {
        return GeometryUtils.roundToGridSpacing(editorProperties, value);
    }

    /**
     * 当这个可拖动框的位置被用户移动时将被调用
     *
     */
    public void positionMoved()
    {
        // 空，被自定义skin逻辑覆盖
    }

    /**
     * Handles a drag event to the given cursor position.
     *
     * @param pX
     *            the cursor x position relative to the container
     * @param pY
     *            the cursor y position relative to the container
     */
    private void handleDrag(final double pX, final double pY)
    {
        handleDragX(pX);
        handleDragY(pY);
        // notify
        positionMoved();
    }

    /**
     * @return {@link GraphEditorProperties#isSnapToGridOn()}
     */
    protected boolean isSnapToGrid()
    {
        return editorProperties != null && editorProperties.isSnapToGridOn();
    }

    /**
     * @return {@link GraphEditorProperties#getWestBoundValue()}
     */
    protected double getWestBoundValue()
    {
        return editorProperties != null ? editorProperties.getWestBoundValue() : GraphEditorProperties.DEFAULT_BOUND_VALUE;
    }

    /**
     * @return {@link GraphEditorProperties#getNorthBoundValue()}
     */
    protected double getNorthBoundValue()
    {
        return editorProperties != null ? editorProperties.getNorthBoundValue() : GraphEditorProperties.DEFAULT_BOUND_VALUE;
    }

    /**
     * @return {@link GraphEditorProperties#getSouthBoundValue()}
     */
    protected double getSouthBoundValue()
    {
        return editorProperties != null ? editorProperties.getSouthBoundValue() : GraphEditorProperties.DEFAULT_BOUND_VALUE;
    }

    /**
     * @return {@link GraphEditorProperties#getEastBoundValue()}
     */
    protected double getEastBoundValue()
    {
        return editorProperties != null ? editorProperties.getEastBoundValue() : GraphEditorProperties.DEFAULT_BOUND_VALUE;
    }

    /**
     * Handles the x component of a drag event to the given cursor x position.
     *
     * @param pX
     *            the cursor x position
     */
    private void handleDragX(final double pX)
    {
        final double maxParentWidth = getParent().getLayoutBounds().getWidth();

        final double minLayoutX = getWestBoundValue();
        final double maxLayoutX = maxParentWidth - getWidth() - getEastBoundValue();

        final double scaleFactor = getLocalToSceneTransform().getMxx();

        double newLayoutX = lastLayoutX + (pX - lastMouseX) / scaleFactor;

        if (isSnapToGrid())
        {
            newLayoutX = roundToGridSpacing(newLayoutX - snapToGridOffset.getX()) + snapToGridOffset.getX();
        }
        else {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newLayoutX = Math.round(newLayoutX);

            if (alignmentTargetsX != null) {
                newLayoutX = align(newLayoutX, alignmentTargetsX);
            }
        }

        if (editorProperties != null && newLayoutX < minLayoutX)
        {
            newLayoutX = minLayoutX;
        }
        else if (newLayoutX > maxLayoutX)
        {
            newLayoutX = maxLayoutX;
        }

        setLayoutX(newLayoutX);
        if (dependencyX != null)
        {
            dependencyX.setLayoutX(newLayoutX);
        }
    }

    /**
     * Handles the y component of a drag event to the given cursor y position.
     *
     * @param pY
     *            the cursor y position
     */
    private void handleDragY(final double pY)
    {
        final double maxParentHeight = getParent().getLayoutBounds().getHeight();

        final double minLayoutY = getNorthBoundValue();
        final double maxLayoutY = maxParentHeight - getHeight() - getSouthBoundValue();

        final double scaleFactor = getLocalToSceneTransform().getMxx();

        double newLayoutY = lastLayoutY + (pY - lastMouseY) / scaleFactor;

        if (isSnapToGrid())
        {
            newLayoutY = roundToGridSpacing(newLayoutY - snapToGridOffset.getY()) + snapToGridOffset.getY();
        }
        else
        {
            // Even if snap-to-grid is off, we use Math.round to ensure drawing 'on-pixel' when zoomed in past 100%.
            newLayoutY = Math.round(newLayoutY);

            if (alignmentTargetsY != null)
            {
                newLayoutY = align(newLayoutY, alignmentTargetsY);
            }
        }

        if (editorProperties != null && newLayoutY < minLayoutY)
        {
            newLayoutY = minLayoutY;
        }
        else if (newLayoutY > maxLayoutY)
        {
            newLayoutY = maxLayoutY;
        }

        setLayoutY(newLayoutY);
        if (dependencyY != null)
        {
            dependencyY.setLayoutY(newLayoutY);
        }
    }

    /**
     * Gets the closest ancestor (e.g. parent, grandparent) to a node that is a
     * subclass of {@link Region}.
     *
     * @param node
     *            a JavaFX {@link Node}
     * @return the node's closest ancestor that is a subclass of {@link Region},
     *         or {@code null} if none exists
     */
    Region getContainer(final Node node)
    {
        final Parent parent = node.getParent();
        if (parent == null)
        {
            return null;
        }
        else if (parent instanceof Region)
        {
            return (Region) parent;
        }
        else
        {
            return getContainer(parent);
        }
    }

    /**
     * Aligns the given position to the first alignment value that is closer than the alignment threshold.
     *
     * <p>
     * Returns the original position if no alignment values are nearby.
     * </p>
     *
     * @param position the position to be aligned
     * @param alignmentValues the list of the alignment values
     * @return the new position after alignment
     */
    private double align(final double position, final double[] alignmentValues)
    {
        for (final double alignmentValue : alignmentValues)
        {
            if (Math.abs(alignmentValue - position) <= alignmentThreshold)
            {
                return alignmentValue;
            }
        }
        return position;
    }
}
