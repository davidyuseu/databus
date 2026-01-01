package sy.grapheditor.core.selections;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;

import sy.grapheditor.api.GSkin;
import sy.grapheditor.api.GraphEditor;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GConnector;
import sy.grapheditor.model.GJoint;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import javafx.collections.SetChangeListener;


/**
 * 为方便起见，提供选定节点、连接和关节的可观察列表{@value selectedElements}。
 */
public class SelectionTracker {

    //记录当前选中的元素，包括node, connection, joint等
    private final ObservableSet<EObject> selectedElements
            = FXCollections.observableSet(Collections.newSetFromMap(new ConcurrentHashMap<>()));
//    private final ObservableSet<EObject> selectedElements = FXCollections.synchronizedObservableSet(selectedElements0);
    private final SkinLookup skinLookup;


    /**
     * 创建 {@link SelectionTracker} 实例.
     *
     * //@param graphEditor the {@link GraphEditor}
     */
    public SelectionTracker(final SkinLookup skinLookup) {
        this.skinLookup = skinLookup;
        selectedElements.addListener(this::selectedElementsChanged);
    }

    /** 
    * @Description: 当前选中节点的列表{@value selectedElements}有删除或新增时，通过{@value update}方法将这些删除或新增的元素
     * 的
    * @Param:
    * @return:  
    */ 
    private void selectedElementsChanged(final SetChangeListener.Change<? extends EObject> change) {
        if (change.wasRemoved()) {
            update(change.getElementRemoved());
        }
        if (change.wasAdded()) {
            update(change.getElementAdded());
        }
    }

    private void update(final EObject obj) {

        GSkin<?> skin = null;
        if (obj instanceof GNode) {
            skin = skinLookup.lookupNode((GNode) obj);
        } else if (obj instanceof GJoint) {
            skin = skinLookup.lookupJoint((GJoint) obj);
        } else if (obj instanceof GConnection) {
            skin = skinLookup.lookupConnection((GConnection) obj);
        } else if (obj instanceof GConnector) {
            skin = skinLookup.lookupConnector((GConnector) obj);
        }

        if (skin != null) {
            skin.updateSelection();
        }

    }

    /**
     * Initializes the selection tracker for the given model.
     *
     * @param model the {@link GModel} instance being edited
     */
    public void initialize(final GModel model) {
        selectedElements.clear();
    }

    /**
     * @return the list of currently selected nodes
     */
    public List<GNode> getSelectedNodes() {
        return selectedElements.stream().filter(e -> e instanceof GNode).map(e -> (GNode) e)
                .collect(Collectors.toList());
    }

    /**
     * @return the list of currently selected connections
     */
    public List<GConnection> getSelectedConnections() {
        return selectedElements.stream().filter(e -> e instanceof GConnection).map(e -> (GConnection) e)
                .collect(Collectors.toList());
    }

    /**
     * @return the list of currently selected joints
     */
    public List<GJoint> getSelectedJoints() {
        return selectedElements.stream().filter(e -> e instanceof GJoint).map(e -> (GJoint) e)
                .collect(Collectors.toList());
    }

    public ObservableSet<EObject> getSelectedItems() {
        return selectedElements;
    }
}
