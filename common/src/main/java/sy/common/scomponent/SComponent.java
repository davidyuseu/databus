package sy.common.scomponent;

import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class SComponent extends Pane implements Framing{
    public static Map<String,SComponent> sComponentMap = new HashMap<>();
    //在SB中显示的示意Label
    public Label nameLabel = new Label();

    //当前通用风格，可以通过SB来设置继承自Pane的风格（可以让子类拿到后再转化为自己所需风格）
    public String generalCrntStyle;

    public SComponent() {
        this.getStyleClass().add("SComponent");
    }

    //取第一子代中StyleClass为styleClassStr的List
    public ArrayList<Node> getStyleClassChildren(String styleClassStr){
        ObservableList<Node> Children = this.getChildren();//new ArrayList<>();
        ArrayList<Node> infoChildren = new ArrayList<>();
        for(Node node:Children)
        {
            ObservableList<String> strs = node.getStyleClass();
            if (strs.contains(styleClassStr))
                infoChildren.add(node);
        }
        return  infoChildren;
    }

    //从fxml中读出的主节点，解析并创建其子组件
    public static Pane reLoadNodeFromFxmlObject(Pane parent){
        Set<Node> nodes = parent.lookupAll(".SComponent");
        if(nodes!=null)
        {
            for(Node node:nodes){
                SComponent sComponent = (SComponent)node;
                if(sComponent.getId()!=null){
                    if(!sComponent.getId().trim().equals(""))
                    {
                        SComponent.sComponentMap.put(sComponent.getId().trim(),sComponent);
                    }
                }
                sComponent.initSComponent();
            }
        }
        return parent;
    }

    //初始化组件
    public boolean initSComponent(){
        return false;
    }

    public void doFraming() {

    }


    public boolean create(){
        return false;
    }

    public static Map<String, SComponent> getsComponentMap() {
        return sComponentMap;
    }

    public static void setsComponentMap(Map<String, SComponent> sComponentMap) {
        SComponent.sComponentMap = sComponentMap;
    }

    public Label getNameLabel() {
        return nameLabel;
    }

    public void setNameLabel(Label nameLabel) {
        this.nameLabel = nameLabel;
    }

    public String getGeneralCrntStyle() {
        return generalCrntStyle;
    }

    public void setGeneralCrntStyle(String generalCrntStyle) {
        this.generalCrntStyle = generalCrntStyle;
    }
}
