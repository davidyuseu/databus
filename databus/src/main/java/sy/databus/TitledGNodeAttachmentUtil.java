package sy.databus;

import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.view.customskins.titled.TitledSkinConstants;
import sy.databus.view.watch.IProcessorPane;
import sy.grapheditor.model.GNode;

import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR;
import static sy.grapheditor.model.impl.GNodeImpl.PROCESSOR_PANE;

/**
 * GNode的实例通过get/set方法将processor和processorPane缓存
 * code 1:
 *         AbstractIntegratedProcessor processor = processorClass.getDeclaredConstructor().newInstance();
 *         processor.bindProcessorId(pId);
 *         node.setAttachment(TitledGNodeAttachment.PROCESSOR, processor);
 *
 * code 2:
 *         // copy各GNode之前先缓存序列化的processor
 *         node.setProcessorJson(ProcessorSJsonUtil.objToStr(node.getAttachment(TitledGNodeAttachment.PROCESSOR)));
 * */
public class TitledGNodeAttachmentUtil {



    public static AbstractIntegratedProcessor getProcessor(GNode node) {
        if (TitledSkinConstants.TITLED_NODE.equals(node.getType())) {
            return (AbstractIntegratedProcessor) node.getAttachment(PROCESSOR);
        } else throw new RuntimeException("The type of GNode dos not match the attachments!");
    }
    public static AbstractIntegratedProcessor getTitledGNodeProcessor(GNode node) {
        return (AbstractIntegratedProcessor) node.getAttachment(PROCESSOR);
    }

    public static void setProcessor(GNode node, AbstractIntegratedProcessor processor) {
        if (TitledSkinConstants.TITLED_NODE.equals(node.getType())) {
            node.setAttachment(PROCESSOR, processor);
        } else throw new RuntimeException("The type of GNode dos not match the attachments!");
    }
    public static void setTitledGNodeProcessor(GNode node, AbstractIntegratedProcessor processor) {
        node.setAttachment(PROCESSOR, processor);
    }


    public static IProcessorPane getProcessorPane(GNode node) {
        if (TitledSkinConstants.TITLED_NODE.equals(node.getType())) {
            return (IProcessorPane) node.getAttachment(PROCESSOR_PANE);
        } else throw new RuntimeException("The type of GNode dos not match the attachments!");
    }
    public static IProcessorPane getTitledGNodeProcessorPane(GNode node) {
        return (IProcessorPane) node.getAttachment(PROCESSOR_PANE);
    }

    public static void setProcessorPane(GNode node, IProcessorPane processorPane) {
        if (TitledSkinConstants.TITLED_NODE.equals(node.getType())) {
            node.setAttachment(PROCESSOR_PANE, processorPane);
        } else throw new RuntimeException("The type of GNode dos not match the attachments!");
    }
    public static void setTitledGNodeProcessorPane(GNode node, IProcessorPane processorPane) {
        node.setAttachment(PROCESSOR_PANE, processorPane);
    }
}
