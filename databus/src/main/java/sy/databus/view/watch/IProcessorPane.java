package sy.databus.view.watch;

import sy.databus.process.AbstractIntegratedProcessor;
import sy.grapheditor.api.GNodeSkin;

public interface IProcessorPane {
    // 刷新监测节点(TitledNode)界面
    void refresh();

    // 将当前监测界面嵌入到GNodeSkin中（TitledNodeSkin）
    void embeddedInRootNodeSkin(GNodeSkin rootNodeSkin);

    // 关联监测节点对应的综合处理器
    void associateWith(AbstractIntegratedProcessor processor);

    // 释放节点资源
    void release();
}
