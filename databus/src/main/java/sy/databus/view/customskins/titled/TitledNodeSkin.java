package sy.databus.view.customskins.titled;

import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import sy.common.cache.CacheFactory;
import sy.common.cache.ICache;
import sy.databus.global.GlobalState;
import sy.databus.TitledGNodeAttachmentUtil;
import sy.databus.global.ProcessorSJsonUtil;
import sy.databus.organize.ProcessorManager;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.ProcessorInitException;
import sy.databus.process.Processor;
import sy.databus.view.watch.FoldingWatchPane;
import sy.databus.view.watch.WatchPane;
import sy.databus.view.watch.IProcessorPane;
import sy.grapheditor.api.GConnectorSkin;
import sy.grapheditor.api.GNodeSkin;
import javafx.css.PseudoClass;
import sy.grapheditor.api.GraphEditor;
import sy.grapheditor.api.utils.GeometryUtils;
import sy.grapheditor.core.DefaultGraphEditor;
import sy.grapheditor.core.connections.Connectable;
import sy.grapheditor.core.connectors.DefaultConnectorTypes;
import sy.grapheditor.core.view.ConnectionLayouter;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GNode;

import java.util.ArrayList;
import java.util.List;

import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;

public class TitledNodeSkin extends GNodeSkin {

    private static final String STYLE_CLASS_BORDER = "titled-node-border";
    private static final String STYLE_CLASS_BACKGROUND = "titled-node-background";
    private static final String STYLE_CLASS_SELECTION_HALO = "titled-node-selection-halo";

    private static final PseudoClass PSEUDO_CLASS_SELECTED = PseudoClass.getPseudoClass("selected"); //$NON-NLS-1$

    private static final double HALO_OFFSET = 5d;
    private static final double HALO_CORNER_SIZE = 8d;

    public static final double BORDER_WIDTH = 1d;
    public static final double TITLED_FOLD_HEIGHT = 29d;// TitledNodeSkin标题区域的高度，即折叠时高度

    private static final double MIN_WIDTH = 81d;
    private static final double MIN_HEIGHT = TITLED_FOLD_HEIGHT;

    public static final double CONTENTROOT_FIXED_INSETS = 2d;

    /** {@link sy.grapheditor.model.impl.GNodeImpl#getAttachment(Integer)}
     *  {@link sy.grapheditor.model.impl.GNodeImpl#PROCESSOR}
     * */
    @Getter
    AbstractIntegratedProcessor processor;

    @Getter @Setter
    private double initialHeight = TITLED_FOLD_HEIGHT;

    @Getter
    private final Rectangle selectionHalo = new Rectangle();

    /** embedded customized pane*/
    IProcessorPane processorPane;
    @Getter
    private WatchPane contentRoot ;

    private final List<GConnectorSkin> inputConnectorSkins = new ArrayList<>();
    private final List<GConnectorSkin> outputConnectorSkins = new ArrayList<>();

    @Getter
    private final Rectangle border = new Rectangle();

    /**
     * Creates a new {@link TitledNodeSkin} instance.
     *
     * @param node the {link GNode} this skin is representing
     */
    public TitledNodeSkin(final GNode node) {

        super(node);

        border.getStyleClass().setAll(STYLE_CLASS_BORDER);
        border.widthProperty().bind(getRoot().widthProperty());
        border.heightProperty().bind(getRoot().heightProperty());

        getRoot().getChildren().add(border);
        getRoot().setMinSize(MIN_WIDTH, MIN_HEIGHT);

        addSelectionHalo();

        //#- 反序列化综合处理器（考虑异步线程中进行？）=> processor

        createContent();

        //#- 如果不包含该综合处理器的配置器模板界面，则创建之
    }


    @Override
    protected void initSize() {
        getRoot().resize(getItem().getWidth(),
                FoldingWatchPane.class.isAssignableFrom(contentRoot.getClass()) ? initialHeight
                        : getItem().getHeight());
    }

    @Override
    public void initialize() {
        //#- 考虑如何使节点在paste操作时仅执行一次
        super.initialize();

    }

    public boolean appendCmdWhenWidthOrHeightChange(){
        return getRoot().isMouseInPositionForResize() && getRoot().isDraggedResizable();
    }

    @Override
    public void initProcessor() {
        processor = TitledGNodeAttachmentUtil.getProcessor(getItem());
        // if(processor != null) {
        //  新增node的情况一：此节点为新增节点，processor实例已在addNode中创建
        //  新增node的情况二：此节点为paste节点，processor实例已在reallocateIds中创建
        // }
        if(processor == null) {
            // 新增node的情况三：此节点load或"撤销/重做"加载的节点，未创建processor实例
            processor = ProcessorSJsonUtil.strToBean(getItem().getProcessorJson(), AbstractIntegratedProcessor.class);
            if (processor == null)
                throw new ProcessorInitException("fail to create a processor for the copied node!");
            else {
                processor.ensureProcessorId();
                TitledGNodeAttachmentUtil.setProcessor(getItem(), processor);
            }
        }
        processorPane.associateWith(processor);
    }

    @Override
    public void setConnectorSkins(final List<GConnectorSkin> connectorSkins) {

        removeAllConnectors();

        inputConnectorSkins.clear();
        outputConnectorSkins.clear();

        if (connectorSkins != null) {
            for (final GConnectorSkin connectorSkin : connectorSkins) {

                final boolean isInput = connectorSkin.getItem().getType().contains("input");
                final boolean isOutput = connectorSkin.getItem().getType().contains("output");

                if (isInput) {
                    inputConnectorSkins.add(connectorSkin);
                } else if (isOutput) {
                    outputConnectorSkins.add(connectorSkin);
                }

                if (isInput || isOutput) {
                    getRoot().getChildren().add(connectorSkin.getRoot());
                }
            }
        }

        setConnectorsSelected();
    }

    @Override
    public void layoutConnectors() {
        layoutLeftAndRightConnectors();
        layoutSelectionHalo();
    }

    @Override
    public Point2D getConnectorPosition(final GConnectorSkin connectorSkin) {

        final Node connectorRoot = connectorSkin.getRoot();

        final double x = connectorRoot.getLayoutX() + connectorSkin.getWidth() / 2;
        final double y = connectorRoot.getLayoutY() + connectorSkin.getHeight() / 2;

        if (inputConnectorSkins.contains(connectorSkin)) {
            return new Point2D(x, y);
        }
        // ELSE:
        // Subtract 1 to align start-of-connection correctly. Compensation for rounding errors?
        return new Point2D(x - 1, y);
    }

    @Override
    public void releaseResource() {
//        ProcessorManager.removeProcessorId(getItem().getId());
//        processorPane.release();

        // 解除作为input时的connection
        processor.disconnectAsInput();

        // 解除作为output时的connection
        processor.disconnectAsOutput();

        /** release the embedded pane*/
        unbindContent();
        if (processorPane instanceof ICache cache) {
            CacheFactory.recycle(cache);
        } else {
            processorPane.release();
        }
        getRoot().getChildren().removeAll(getRoot().getChildren());
        processorPane = null;
        contentRoot = null;
        /** release the processor*/
        processor.clear();
        processor = null;

        getItem().clearAttachments();
        if (GlobalState.currentNodeSkin.get() == this)
            GlobalState.setCurrentNodeSkin(null);
    }

    private void redrawConnection() {
        ConnectionLayouter connectionLayouter
                = ((DefaultGraphEditor)getGraphEditor()).getMController().getConnectionLayouter();
        for (GConnector connector : getItem().getConnectors()) {
            for (GConnection connection : connector.getConnections()) {
                connectionLayouter.redraw(connection);
            }
        }
        connectionLayouter.draw();
    }

    /**
     * Creates the content of the node skin - header, title, close button, etc.
     */
    @SneakyThrows
    private void createContent() {
        Class<? extends AbstractIntegratedProcessor> processorClass = null;
        try {
            processorClass =
                    (Class<? extends AbstractIntegratedProcessor>) Class.forName(getItem().getProcessorClassName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Processor processorAnno = processorClass.getAnnotation(Processor.class);
        var processorPaneClass = processorAnno.pane();
        if (ICache.class.isAssignableFrom(processorPaneClass)) {
            processorPane = (IProcessorPane) CacheFactory.getWeakCache((Class<? extends ICache>) processorPaneClass);
        }
        /* SneakyThrows*/
        if (processorPane == null)
            processorPane = processorPaneClass.getDeclaredConstructor().newInstance();
        // TODO 考虑WatchPane是否需要rebuild方法，还是仅release方法足够重建一个全新的WatchPane了
        contentRoot = (WatchPane) processorPane;
        contentRoot.embeddedInRootNodeSkin(this);
        TitledGNodeAttachmentUtil.setProcessorPane(getItem(), processorPane);
        contentRoot.setPadding(new Insets(CONTENTROOT_FIXED_INSETS));

        bindContent();
        contentRoot.getStyleClass().add(STYLE_CLASS_BACKGROUND);
        getRoot().getChildren().add(contentRoot);
    }

    public WatchPane detachContent() {
        getRoot().getChildren().remove(contentRoot);
        return contentRoot;
    }

    public void attachContent() {
        getRoot().getChildren().add(contentRoot);
    }

    // 绑定watchPane与root，ps：使其高和款能够随root拖拽变化
    private void bindContent() {
        contentRoot.minWidthProperty().bind(getRoot().widthProperty());
        contentRoot.prefWidthProperty().bind(getRoot().widthProperty());
        contentRoot.maxWidthProperty().bind(getRoot().widthProperty());
        contentRoot.minHeightProperty().bind(getRoot().heightProperty());
        contentRoot.prefHeightProperty().bind(getRoot().heightProperty());
        contentRoot.maxHeightProperty().bind(getRoot().heightProperty());
    }
    // 解除绑定，防止watchPane无法被gc回收
    private void unbindContent() {
        contentRoot.minWidthProperty().unbind();
        contentRoot.prefWidthProperty().unbind();
        contentRoot.maxWidthProperty().unbind();

        contentRoot.minHeightProperty().unbind();
        contentRoot.prefHeightProperty().unbind();
        contentRoot.maxHeightProperty().unbind();
    }

    /**
     * 布置所有连接器
     */
    private void layoutLeftAndRightConnectors() {

        final int inputCount = inputConnectorSkins.size();
        //        final double inputOffsetY = (getRoot().getHeight() - HEADER_HEIGHT) / (inputCount + 1);
        final double inputOffsetY = TITLED_FOLD_HEIGHT / 2 + inputCount - 1;

        for (int i = 0; i < inputCount; i++) {

            final GConnectorSkin inputSkin = inputConnectorSkins.get(i);
            final Node connectorRoot = inputSkin.getRoot();

            final double layoutX = GeometryUtils.moveOnPixel(0 - inputSkin.getWidth() / 2);
            final double layoutY = GeometryUtils.moveOnPixel((i + 1) * inputOffsetY - inputSkin.getHeight() / 2);

            connectorRoot.setLayoutX(layoutX);
            connectorRoot.setLayoutY(layoutY); //+ HEADER_HEIGHT);
        }

        final int outputCount = outputConnectorSkins.size();
        //        final double outputOffsetY = (getRoot().getHeight() - HEADER_HEIGHT) / (outputCount + 1);
        final double outputOffsetY = TITLED_FOLD_HEIGHT / 2 + outputCount - 1;

        for (int i = 0; i < outputCount; i++) {

            final GConnectorSkin outputSkin = outputConnectorSkins.get(i);
            final Node connectorRoot = outputSkin.getRoot();

            final double layoutX = GeometryUtils.moveOnPixel(getRoot().getWidth() - outputSkin.getWidth() / 2);
            final double layoutY = GeometryUtils.moveOnPixel((i + 1) * outputOffsetY - outputSkin.getHeight() / 2);

            connectorRoot.setLayoutX(layoutX);
            connectorRoot.setLayoutY(layoutY);// + HEADER_HEIGHT);
        }
    }

    private void addSelectionHalo() {

        getRoot().getChildren().add(selectionHalo);

        selectionHalo.setManaged(false);
        selectionHalo.setMouseTransparent(false);
        selectionHalo.setVisible(false);

        selectionHalo.setLayoutX(-HALO_OFFSET);
        selectionHalo.setLayoutY(-HALO_OFFSET);

        selectionHalo.getStyleClass().add(STYLE_CLASS_SELECTION_HALO);
    }

    public void layoutSelectionHalo() {

        if (selectionHalo.isVisible()) {

            selectionHalo.setWidth(getRoot().getWidth() + 2 * HALO_OFFSET);
            selectionHalo.setHeight(getRoot().getHeight() + 2 * HALO_OFFSET);

            final double cornerLength = 2 * HALO_CORNER_SIZE;
            final double xGap = getRoot().getWidth() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;
            final double yGap = getRoot().getHeight() - 2 * HALO_CORNER_SIZE + 2 * HALO_OFFSET;

            selectionHalo.setStrokeDashOffset(HALO_CORNER_SIZE);
            selectionHalo.getStrokeDashArray().setAll(cornerLength, yGap, cornerLength, xGap);
        }
    }

    @Override
    protected void selectionChanged(final boolean isSelected) {
        if (isSelected) {
            selectionHalo.setVisible(true);
            layoutSelectionHalo();
            contentRoot.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, true);
            getRoot().toFront();
        } else {
            selectionHalo.setVisible(false);
            contentRoot.pseudoClassStateChanged(PSEUDO_CLASS_SELECTED, false);
            if (getGraphEditor().getSelectionManager()
                    .getSelectedNodes().size() == 0) {
                GlobalState.setCurrentNodeSkin(null);
            }
        }
        setConnectorsSelected();
    }

    private void removeAllConnectors() {

        for (final GConnectorSkin connectorSkin : inputConnectorSkins) {
            getRoot().getChildren().remove(connectorSkin.getRoot());
        }

        for (final GConnectorSkin connectorSkin : outputConnectorSkins) {
            getRoot().getChildren().remove(connectorSkin.getRoot());
        }
    }

    private void setConnectorsSelected()
    {
        final GraphEditor editor = getGraphEditor();
        if(editor == null) {
            return;
        }

        for (final GConnectorSkin skin : inputConnectorSkins) {
            if (skin instanceof TitledConnectorSkin) {
                editor.getSelectionManager().select(skin.getItem());
            }
        }

        for (final GConnectorSkin skin : outputConnectorSkins) {
            if (skin instanceof TitledConnectorSkin) {
                editor.getSelectionManager().select(skin.getItem());
            }
        }
    }
}
