package sy.databus.entity.property;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.databus.process.AbstractHandler;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.view.controller.RadioSelObListController;

import java.util.*;

/**
 * controller: {@link RadioSelObListController}
 * */
@Log4j2
public class RadioSelectObList<C> implements IDeletionResponder {
    /** 候选列表，其中的待选项将呈现在{@link RadioSelObListController}中的下拉框控件ComboBox中*/
    @Getter @Setter
    protected SSyncObservableList<C> candidateList;
    public void setCandidateItems(Collection<C> candidateItems) {
        if (candidateList == null) {
            log.warn("'candidateList' was null, new a SSyncObservableList without specific mutex to it!");
            candidateList = new SSyncObservableList<>();
        }
        candidateList.setAll(candidateItems);
    }
    /** 序列化的时候是否保存候选列表*/
    @Getter @Setter
    private boolean saveCandidateList = false;

    /** 当前选中项的索引*/
    @Getter @Setter
    private int selIndex = -1; // 当前选中项的索引

    @FunctionalInterface
    public interface Action<C> {
        void changed(C oldValue, C newValue) throws Exception;
    }

    /**
     * 可设置当前选中的索引改变时，发生的动作
     * */
    @Getter
    private List<Action<Integer>> selIndexChangedActions;
    public void addIndexChangedAction(Action<Integer> action) {
        if (selIndexChangedActions == null)
            selIndexChangedActions = new ArrayList<>();
        selIndexChangedActions.add(action);
    }

    /**
     *  “已占有索引组”，表示已被选的索引，不可再被选，
     * 即加入该组的各个{@link RadioSelectObList}不能持有相同的{@link #selIndex}。
     * 设置已占有索引组时，若该组已经包含当前{@link #selIndex}，则将其置为-1，否则将该{@link #selIndex}加入组
     * ps: 一般在{@link AbstractIntegratedProcessor} 或 {@link AbstractHandler} 中的initialize()方法中设置。
     * */
    @Getter
    private Vector<Integer> occupiedIndexGroup;
    public void setOccupiedIndexGroup(Vector<Integer> vector) {
        if (vector == null) {
            selIndex = -1;
            return;
        }
        if (selIndex >= 0) {
            if (vector.contains(selIndex))
                selIndex = -1;
            else
                vector.add(selIndex);
        }
        this.occupiedIndexGroup = vector;
    }

    /**
     * 当所属的processor被执行删除动作时，须删除它在{@link #occupiedIndexGroup}中的“已占有索引”，
     * 即{@link #selIndex}
     * */
    @Override
    public void beforeDeletion() {
        if (occupiedIndexGroup != null)
            occupiedIndexGroup.removeElement(selIndex);
    }

    public String getItemString(C item) {
        return item.toString();
    }

    public RadioSelectObList() {
        candidateList = new SSyncObservableList<>();
    }

    public RadioSelectObList(boolean saveList, int selIndex) {
        this.saveCandidateList = saveList;
        this.selIndex = selIndex;
        this.candidateList = new SSyncObservableList<>();
    }

    public RadioSelectObList(boolean saveList, int selIndex, SSyncObservableList candidateList) {
        this.saveCandidateList = saveList;
        this.selIndex = selIndex;
        this.candidateList = candidateList;
    }

    public RadioSelectObList(boolean saveList, int selIndex, C... candidateItems) {
        this.saveCandidateList = saveList;
        this.selIndex = selIndex;
        this.candidateList = new SSyncObservableList<>();
        Collections.addAll(candidateList, candidateItems);
    }

    public RadioSelectObList(Object mutex) {
        candidateList = new SSyncObservableList<>(mutex);
    }

    /**
     * ps: 一般在{@link AbstractIntegratedProcessor} 或 {@link AbstractHandler} 中的initialize()方法中设置。
     * */
    public void setMutex(Object mutex) {
        candidateList.setMutex(mutex);
    }

    public C getSelectedItem() {
        if (selIndex >= 0)
            return candidateList.get(selIndex);
        else
            return null;
    }

    public void clearCandidateItems() {
        candidateList.clear();
    }

    public void resetSelectedIndex() {
        selIndex = -1;
    }
}
