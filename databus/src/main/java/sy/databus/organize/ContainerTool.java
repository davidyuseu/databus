package sy.databus.organize;

import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import lombok.SneakyThrows;
import sy.common.cache.CacheFactory;
import sy.common.cache.ICache;
import sy.common.util.SStringUtil;
import sy.databus.view.container.*;


import static sy.databus.global.Constants.TITLED_HEAD_COMMON;
import static sy.databus.global.Constants.TITLED_HEAD_GENERIC;
import static sy.databus.global.ProcessorType.Category.PROCESSOR_SINGLE_EXECUTOR;
import static sy.databus.process.Console.Category.PROPERTIES;
import static sy.databus.view.container.Constants.STYLE_CLASS_UNTITLED_CONTENT;
import static sy.databus.view.container.ScrolledContent.STYLE_CLASS_SCROLLED_CONTENT;

public class ContainerTool {

    // @Param root TitledPane
    public static Pane getInternalContainer(Pane root, String title) {
        if(title == null || title.isEmpty()) {
            Pane content = createContent(root); // content为无标题的VBox
            if (!root.getChildren().contains(content))
                root.getChildren().add(0, content);
            return content;
        } else {
            TitledPane tPane = createContainer(root, title, InternalContainer.class);
            if (!root.getChildren().contains(tPane)) {
                if (TITLED_HEAD_COMMON.equals(title) || TITLED_HEAD_GENERIC.equals(title)) {
                    root.getChildren().add(0, tPane); // 将“common”“generic”置顶
                } else {
                    root.getChildren().add(tPane);
                }
            }
            // internal-titled的content应该为VBox类型
            return (Pane) tPane.getContent();
        }
    }

    /** 用于得到右边栏中的“Properties”等面板或左边栏中的“File Reader”等面板*/
    public static Pane getScrolledOuterContainer(Accordion outerContainers, String title) {
        TitledPane tPane = createContainer(outerContainers, title, ScrolledContainer.class);
        // 调整标题风格
        if (title.equals(PROCESSOR_SINGLE_EXECUTOR.getName())) {
            tPane.getStyleClass().add("fileReaderPane");
        }
        // 调整标题位置
        if (!outerContainers.getPanes().contains(tPane)) {
            // 将左边栏中的“File Reader”置顶
            // 将右边栏中的“Properties”置顶
            if (title.equals(PROPERTIES.getName()) || title.equals(PROCESSOR_SINGLE_EXECUTOR.getName()))
                outerContainers.getPanes().add(0, tPane);
            else
                outerContainers.getPanes().add(tPane);
        }
        // 对于outer container，包含一个scrollPane
        ScrollPane scrollPane = (ScrollPane) tPane.getContent();
        return (Pane) scrollPane.getContent();
    }

    /**用于得到右边栏中的Pre Handlers或Service Handlers面板
     *  Pre Handlers或Service Handlers面板 = 非滚动handler列表面板 + 滚动属性面板*/
    public static Pane getNonScrolledOuterContainer(Accordion outerContainers, String title) {
        TitledPane tPane = createContainer(outerContainers, title, UnScrolledContainer.class);
        if (!outerContainers.getPanes().contains(tPane)) {
            if (title.equals(PROPERTIES.getName())) // 将“Properties”置顶
                outerContainers.getPanes().add(0, tPane);
            else
                outerContainers.getPanes().add(tPane);
        }
        return (Pane) tPane.getContent();
    }


    /**
     * ps:
     * id = SStringUtil.replaceBlank(title)
     * */
    @SneakyThrows
    public static TitledPane createContainer(Node node, String title, Class<? extends ICache> containerClazz) {
        TitledPane tPane = (TitledPane) node.lookup("#" + SStringUtil.removeBlank(title));
        if (tPane != null) {
            return tPane;
        } else {
            IContainer container = (IContainer) CacheFactory.getWeakCache(containerClazz);
            if (container != null) {
                container.rebuild(title);
                return (TitledPane) container;
            } else {
                /** ScrolledContainer / InternalContainer / UnScrolledContainer */
                return (TitledPane) containerClazz.getDeclaredConstructor(String.class).newInstance(title);
            }
        }
    }

    public static Content createContent(Node node) {
        Content content = (Content) node.lookup("." + STYLE_CLASS_UNTITLED_CONTENT);
        if (content == null) {
            content = (Content) CacheFactory.getWeakCache(Content.class);
            if (content != null)
                return content;
            else
                return new Content();
        } else {
            return content;
        }
    }

    public static ScrolledContent createScrolledContent(Node node) {
        ScrolledContent content = (ScrolledContent) node.lookup("." + STYLE_CLASS_SCROLLED_CONTENT);
        if (content == null) {
            content = (ScrolledContent) CacheFactory.getWeakCache(ScrolledContent.class);
            if (content != null)
                return content;
            else
                return new ScrolledContent();
        } else {
            return content;
        }
    }

}
