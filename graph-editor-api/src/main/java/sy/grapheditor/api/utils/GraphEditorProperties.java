/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.api.utils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

import sy.grapheditor.api.EditorElement;
import sy.grapheditor.api.impl.GraphEventManagerImpl;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.event.Event;


/**
 * 图形编辑器的一般属性。
 *
 * <p>
 * 例如，编辑器是否应该有“边界”，或者对象是否应该可以拖动到编辑器区域之外？
 * </p>
 *
 * <p>
 * 如果边界处于活动状态，则在编辑器中拖动或调整大小的对象应在到达边缘时停止，并且编辑器区域不会尝试增大大小。
 * 否则它将增长到其最大大小。
 * </p>
 *
 * <p>
 * 还存储有关网格是否可见和/或对齐网格是否打开的属性。
 * </p>
 */
public class GraphEditorProperties implements GraphEventManager
{

    /**
     * 编辑器区域的默认最大宽度，在启动时设置。
     */
    public static final double DEFAULT_MAX_WIDTH = Double.MAX_VALUE;
    /**
     * 编辑器区域的默认最大高度，在启动时设置。
     */
    public static final double DEFAULT_MAX_HEIGHT = Double.MAX_VALUE;

    public static final double DEFAULT_BOUND_VALUE = 15;
    public static final double DEFAULT_GRID_SPACING = 12;

    // 拖动/调整大小时对象应停止的距编辑器边缘的距离。
    private double northBoundValue = DEFAULT_BOUND_VALUE;
    private double southBoundValue = DEFAULT_BOUND_VALUE;
    private double eastBoundValue = DEFAULT_BOUND_VALUE;
    private double westBoundValue = DEFAULT_BOUND_VALUE;

    // 默认关闭。
    private final BooleanProperty gridVisible = new SimpleBooleanProperty(this, "gridVisible"); //$NON-NLS-1$
    private final BooleanProperty snapToGrid = new SimpleBooleanProperty(this, "snapToGrid"); //$NON-NLS-1$
    private final DoubleProperty gridSpacing = new SimpleDoubleProperty(this, "gridSpacing", DEFAULT_GRID_SPACING); //$NON-NLS-1$

    private final Map<EditorElement, BooleanProperty> readOnly = new EnumMap<>(EditorElement.class);

    private final ObservableMap<String, String> customProperties = FXCollections.observableHashMap();

    private final GraphEventManager eventManager = new GraphEventManagerImpl();

    /**
     * 创建一个包含一组默认属性的新编辑器属性的实例。
     */
    public GraphEditorProperties()
    {
    }

    /**
     * 复制构造函数。
     *
     * <p>
     * 创建一个新的编辑器属性实例，所有值都从现有实例复制过来。
     * </p>
     *
     * @param editorProperties
     *         一个已存在的 {@link GraphEditorProperties} 实例
     */
    public GraphEditorProperties(final GraphEditorProperties editorProperties)
    {
        northBoundValue = editorProperties.getNorthBoundValue();
        southBoundValue = editorProperties.getSouthBoundValue();
        eastBoundValue = editorProperties.getEastBoundValue();
        westBoundValue = editorProperties.getWestBoundValue();

        gridVisible.set(editorProperties.isGridVisible());
        snapToGrid.set(editorProperties.isSnapToGridOn());
        gridSpacing.set(editorProperties.getGridSpacing());

        for (final Map.Entry<EditorElement, BooleanProperty> entry : editorProperties.readOnly.entrySet())
        {
            readOnly.computeIfAbsent(entry.getKey(), k -> new SimpleBooleanProperty()).set(entry.getValue().get());
        }

        customProperties.putAll(editorProperties.getCustomProperties());
    }

    /**
     * Gets the value of the north bound.
     *
     * @return the value of the north bound
     */
    public double getNorthBoundValue()
    {
        return northBoundValue;
    }

    /**
     * Sets the value of the north bound.
     *
     * @param pNorthBoundValue
     *         the value of the north bound
     */
    public void setNorthBoundValue(final double pNorthBoundValue)
    {
        northBoundValue = pNorthBoundValue;
    }

    /**
     * Gets the value of the south bound.
     *
     * @return the value of the south bound
     */
    public double getSouthBoundValue()
    {
        return southBoundValue;
    }

    /**
     * Sets the value of the south bound.
     *
     * @param pSouthBoundValue
     *         the value of the south bound
     */
    public void setSouthBoundValue(final double pSouthBoundValue)
    {
        southBoundValue = pSouthBoundValue;
    }

    /**
     * Gets the value of the east bound.
     *
     * @return the value of the east bound
     */
    public double getEastBoundValue()
    {
        return eastBoundValue;
    }

    /**
     * Sets the value of the east bound.
     *
     * @param pEastBoundValue
     *         the value of the east bound
     */
    public void setEastBoundValue(final double pEastBoundValue)
    {
        eastBoundValue = pEastBoundValue;
    }

    /**
     * Gets the value of the west bound.
     *
     * @return the value of the west bound
     */
    public double getWestBoundValue()
    {
        return westBoundValue;
    }

    /**
     * Sets the value of the west bound.
     *
     * @param pWestBoundValue
     *         the value of the west bound
     */
    public void setWestBoundValue(final double pWestBoundValue)
    {
        westBoundValue = pWestBoundValue;
    }

    /**
     * Checks if the background grid is visible.
     *
     * @return {@code true} if the background grid is visible, {@code false} if
     *         not
     */
    public boolean isGridVisible()
    {
        return gridVisible.get();
    }

    /**
     * Sets whether the background grid should be visible or not.
     *
     * @param pGridVisible
     *            {@code true} if the background grid should be visible,
     *            {@code false} if not
     */
    public void setGridVisible(final boolean pGridVisible)
    {
        gridVisible.set(pGridVisible);
    }

    /**
     * Gets the grid-visible property.
     *
     * @return a {@link BooleanProperty} tracking whether the grid is visible or
     *         not
     */
    public BooleanProperty gridVisibleProperty()
    {
        return gridVisible;
    }

    /**
     * Checks if snap-to-grid is on.
     *
     * @return {@code true} if snap-to-grid is on, {@code false} if not
     */
    public boolean isSnapToGridOn()
    {
        return snapToGrid.get();
    }

    /**
     * Sets whether snap-to-grid should be on.
     *
     * @param pSnapToGrid
     *            {@code true} if snap-to-grid should be on, {@code false} if
     *            not
     */
    public void setSnapToGrid(final boolean pSnapToGrid)
    {
        snapToGrid.set(pSnapToGrid);
    }

    /**
     * Gets the snap-to-grid property.
     *
     * @return a {@link BooleanProperty} tracking whether snap-to-grid is on or
     *         off
     */
    public BooleanProperty snapToGridProperty()
    {
        return snapToGrid;
    }

    /**
     * Gets the current grid spacing in pixels.
     *
     * @return the current grid spacing
     */
    public double getGridSpacing()
    {
        return gridSpacing.get();
    }

    /**
     * Sets the grid spacing to be used if the grid is visible and/or
     * snap-to-grid is enabled.
     *
     * <p>
     * Integer values are recommended to avoid sub-pixel positioning effects.
     * </p>
     *
     * @param pGridSpacing
     *         the grid spacing to be used
     */
    public void setGridSpacing(final double pGridSpacing)
    {
        gridSpacing.set(pGridSpacing);
    }

    /**
     * Gets the grid spacing property.
     *
     * @return the grid spacing {@link DoubleProperty}.
     */
    public DoubleProperty gridSpacingProperty()
    {
        return gridSpacing;
    }

    /**
     * Gets the read only property
     *
     * @param pType
     *         {@link EditorElement}
     * @return read only {@link BooleanProperty}
     */
    public BooleanProperty readOnlyProperty(final EditorElement pType)
    {
        Objects.requireNonNull(pType, "ElementType may not be null!");
        return readOnly.computeIfAbsent(pType, k -> new SimpleBooleanProperty());
    }

    /**
     * Returns whether or not the graph is in read only state.
     *
     * @param pType
     *         {@link EditorElement}
     * @return whether or not the graph is in read only state.
     */
    public boolean isReadOnly(final EditorElement pType)
    {
        return pType != null && readOnly.computeIfAbsent(pType, k -> new SimpleBooleanProperty()).get();
    }

    /**
     * @param pType
     *         {@link EditorElement}
     * @param pReadOnly
     *         {@code true} to set the graph editor in read only state or {@code false} (default) for edit state.
     */
    public void setReadOnly(final EditorElement pType, final boolean pReadOnly)
    {
        if (pType != null)
        {
            readOnly.computeIfAbsent(pType, k -> new SimpleBooleanProperty()).set(pReadOnly);
        }
    }

    /**
     * Additional properties that may be added and referred to in custom skin
     * implementations.
     *
     * @return a map of custom properties
     */
    public ObservableMap<String, String> getCustomProperties()
    {
        return customProperties;
    }

    @Override
    public boolean activateGesture(GraphInputGesture pGesture, Event pEvent, Object pOwner)
    {
        return eventManager.activateGesture(pGesture, pEvent, pOwner);
    }

    @Override
    public boolean finishGesture(GraphInputGesture pExpected, Object pOwner)
    {
        return eventManager.finishGesture(pExpected, pOwner);
    }
}
