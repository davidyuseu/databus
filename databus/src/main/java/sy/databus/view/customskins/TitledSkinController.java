package sy.databus.view.customskins;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import sy.databus.TitledGNodeAttachmentUtil;
import sy.databus.entity.ProcessorId;
import sy.databus.global.ProcessorSJsonUtil;
import sy.databus.organize.ComponentsItem;
import sy.databus.organize.ProcessorManager;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.Processor;
import sy.databus.process.ProcessorInitException;
import sy.databus.selections.SelectionCopier;
import sy.databus.view.customskins.titled.TitledConnectorSkin;
import sy.databus.view.customskins.titled.TitledNodeSkin;
import sy.databus.view.customskins.titled.TitledSkinConstants;
import sy.databus.view.customskins.titled.TitledTailSkin;
import sy.databus.view.watch.WatchPaneConfig;
import sy.grapheditor.api.*;
import sy.grapheditor.core.skins.defaults.DefaultConnectorSkin;
import sy.grapheditor.core.skins.defaults.DefaultTailSkin;
import sy.grapheditor.core.view.GraphEditorContainer;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GNode;
import sy.grapheditor.model.GraphFactory;
import sy.grapheditor.model.GraphPackage;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

@Log4j2
public class TitledSkinController extends DefaultSkinController {

    public static final double DEFAULT_TITLED_NODE_HEIGHT = 114d;

    public TitledSkinController(final GraphEditor graphEditor, final GraphEditorContainer graphEditorContainer) {
        super(graphEditor, graphEditorContainer);
    }

    @Override
    public void activate() {
        super.activate();
        graphEditor.setNodeSkinFactory(this::createSkin);
        graphEditor.setConnectorSkinFactory(this::createSkin);
        graphEditor.setTailSkinFactory(this::createTailSkin);
    }

    private GNodeSkin createSkin(final GNode node) {
//        return TitledSkinConstants.TITLED_NODE.equals(node.getType()) ? new TitledNodeSkin(node) : null; //new DefaultNodeSkin(node);
        if (TitledSkinConstants.TITLED_NODE.equals(node.getType())) {
            WeakReference<TitledNodeSkin> titledNodeSkinWeakRef
                    = new WeakReference<>(new TitledNodeSkin(node));
            return titledNodeSkinWeakRef.get();
        } else {
            return null;
        }
    }

    private GConnectorSkin createSkin(final GConnector connector) {
        return TitledSkinConstants.TITLED_INPUT_CONNECTOR.equals(connector.getType()) || TitledSkinConstants.TITLED_OUTPUT_CONNECTOR.equals(connector.getType()) ?
                new TitledConnectorSkin(connector) : new DefaultConnectorSkin(connector);
    }

    private GTailSkin createTailSkin(final GConnector connector) {
        return TitledSkinConstants.TITLED_INPUT_CONNECTOR.equals(connector.getType()) || TitledSkinConstants.TITLED_OUTPUT_CONNECTOR.equals(connector.getType()) ?
                new TitledTailSkin(connector) : new DefaultTailSkin(connector);
    }

    @SneakyThrows
    @Override
    public void addNode(final double currentZoomFactor) {

        final double windowXOffset = graphEditorContainer.getContentX() / currentZoomFactor;
        final double windowYOffset = graphEditorContainer.getContentY() / currentZoomFactor;

        final GNode node = GraphFactory.eINSTANCE.createGNode();
        node.setY(NODE_INITIAL_Y + windowYOffset);

        node.setType(TitledSkinConstants.TITLED_NODE);
        node.setX(NODE_INITIAL_X + windowXOffset);

        // 新增node的情况一
        // 1. 获取当前选中的综合处理器类
        Class<? extends AbstractIntegratedProcessor> processorClass
                = ComponentsItem.selectedComponent.get().getOpClazz();
        /** 根据WatchPaneConfig注解初始化GNode，如高和宽的初值*/
        Processor processorConfig = processorClass.getAnnotation(Processor.class);
        if (processorConfig != null) {
            WatchPaneConfig watchPaneConfig = processorConfig.pane().getAnnotation(WatchPaneConfig.class);
            if (watchPaneConfig != null) { // 配置了WatchPaneConfig注解
                if (watchPaneConfig.initialHeight() > 0.0) { // 设置初始高
                    node.setHeight(watchPaneConfig.initialHeight());
                }
                if (watchPaneConfig.initialWidth() > 0.0) { // 设置初始宽
                    node.setWidth(watchPaneConfig.initialWidth());
                }
            } else {
                node.setHeight(DEFAULT_TITLED_NODE_HEIGHT);
            }
        } else {
            log.warn("There is no annotation of 'Processor' for the 'AbstractIntegratedProcessor', ensure you don't need it!");
        }
        // 2. 根据该类类型分配PId
        ProcessorId pId = ProcessorManager.allocateProcessorId(processorClass);
        // 3. 创建综合处理器实例
        AbstractIntegratedProcessor processor = null;
        try {
            processor = processorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            log.error(e);
        }
        processor.bindProcessorId(pId);
        processor.setNameValue(processor.getClass().getSimpleName());
        processor.initialize();
        TitledGNodeAttachmentUtil.setProcessor(node, processor);
        // 4. 序列化配置命令
        long pIdCode = pId.getProcessorCode();
        node.setId(pIdCode);
        node.setProcessorClassName(processorClass.getName());
        // #- 序列化到json （考虑是否需要在创建阶段序列化：需要，否则“创建” -> “回退” -> “重做” 会无法得到processor对象）
        node.setProcessorJson(ProcessorSJsonUtil.objToStr(processor));

        final GConnector input = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(input);
        input.setType(TitledSkinConstants.TITLED_INPUT_CONNECTOR);

        final GConnector output = GraphFactory.eINSTANCE.createGConnector();
        node.getConnectors().add(output);
        output.setType(TitledSkinConstants.TITLED_OUTPUT_CONNECTOR);

        // 允许一个输出创建多个连接.
        output.setConnectionDetachedOnDrag(false);
        // 允许一个输入创建多个连接
        input.setConnectionDetachedOnDrag(false);

        Commands.addNode(graphEditor.getModel(), node);
    }

    @Override
    public void handlePaste(final SelectionCopier selectionCopier) {
//        selectionCopier.paste((nodes, command) -> reinitialize(nodes, command));
        selectionCopier.paste((nodes, command) -> adjustNodes(nodes, command));
    }

    private void adjustNodes(final List<GNode> nodes, final CompoundCommand command) {

        for (final GNode node : nodes) {

            if (isIdExist(node, nodes)) {
                // 新增node的情况二
                // 1. reallocatedPId
                final ProcessorId newPId = allocateNewId(node);
                node.setId(newPId.getProcessorCode());
                // 3. 由缓存的json反序列化出processor
                AbstractIntegratedProcessor processor
                        = ProcessorSJsonUtil.strToBean(node.getProcessorJson(), AbstractIntegratedProcessor.class);
                if (processor == null)
                    throw new ProcessorInitException("fail to create a processor for the copied node!");
                // 2. 将processorId改写为newPId
                processor.bindProcessorId(newPId);
                ObjectNode newJsonNode = ((ObjectNode) ProcessorSJsonUtil.readTree(node.getProcessorJson()))
                        .put("processorId", newPId.getProcessorCode());
                node.setProcessorJson(newJsonNode.toString());
/*                String newJson = ProcessorSJsonUtil.objToStr(processor);
                Objects.requireNonNull(newJson, "Failed to generate a new processor json!");
                node.setProcessorJson(newJson);*/
                if(node.getAttachments() == null) {
                    processor.ensureProcessorId(); // ? processor.bindProcessorId(newPId);
                    TitledGNodeAttachmentUtil.setProcessor(node, processor);
                } else {
                    log.warn("{}","why the GNode to adjust had a no-none attachments?");
                }
            } else {
                log.warn("{}","why copied node had a different PId?");
            }
        }
    }

    private void reinitialize(final List<GNode> nodes, final CompoundCommand command) {
        final EditingDomain domain = AdapterFactoryEditingDomain.getEditingDomainFor(graphEditor.getModel());
        final EAttribute feature = GraphPackage.Literals.GNODE__ID;

        for (final GNode node : nodes) {

            if (isIdExist(node, nodes)) {

                final ProcessorId id = allocateNewId(node);
                final Command setCommand = SetCommand.create(domain, node, feature, id.getProcessorCode());

                if (setCommand.canExecute()) {
                    command.appendAndExecute(setCommand);
                }

                graphEditor.getSkinLookup().lookupNode(node).initialize();
            }
        }
    }

    private boolean isIdExist(final GNode node, final List<GNode> pastedNodes) {
        final List<GNode> nodes = new ArrayList<>(graphEditor.getModel().getNodes());
        nodes.removeAll(pastedNodes);

        return nodes.stream().anyMatch(other -> other.getId() == node.getId());
    }

    private ProcessorId allocateNewId(GNode node) {
        String className = node.getProcessorClassName();
        Class processorClass = null;
        if(!className.equals("null")) {
            try {
                processorClass = Class.forName(className);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        if (processorClass == null)
            throw new ProcessorInitException("need a correct className to allocate a new id!");
        return ProcessorManager.allocateProcessorId(processorClass);
    }

}
