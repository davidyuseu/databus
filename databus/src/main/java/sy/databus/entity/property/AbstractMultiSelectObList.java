package sy.databus.entity.property;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import sy.common.concurrent.vector.SSyncObservableList;
import sy.databus.process.AbstractHandler;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.view.controller.MultiSelObListController;

import java.util.*;

/**
 * Controller: {@link MultiSelObListController}
 * 该多选列表抽象类没有指定(反)序列化器，
 * 故注意：勿增加无(反)序列化器的成员变量，且新增变量后关心是否需要使用@JsonIgnore*/
public abstract class AbstractMultiSelectObList<C, S> {
    @JsonIgnore @Getter @Setter
    protected SSyncObservableList<C> candidateList;

    @JsonIgnore @Getter @Setter
    protected Set<AbstractMultiSelectObList> unionSelections = null;
    @JsonIgnore @Getter @Setter
    protected LinkedHashSet<Integer> ignoredIndexes = null;

    public boolean isIgnoredItem(C item) {
        if (ignoredIndexes == null || ignoredIndexes.isEmpty()
                || candidateList == null || candidateList.isEmpty()) {
            return false;
        } else {
            return ignoredIndexes.contains(candidateList.indexOf(item));
        }
    }

    /**
     * 当selectedList中有数据而candidateList无数据时，认为此时为第一次初始化
     * */
    public void setAllCandidateItems(Collection<? extends C> candidateItems) throws Exception {
        boolean initFlag = candidateList.size() == 0 && selectedIndexes.size() > 0;
        candidateList.setAll(candidateItems);
        if (initFlag) {
            for (int index : selectedIndexes) {
                if (index > candidateList.size() - 1)
                    throw new Exception("The index to select is outbounded of the 'candidateList'!");
                S sel = transToSelected(candidateList.get(index));
                selectedList.add(sel);
                if (remoteList != null)
                    remoteList.add(sel);
            }
        }
    }

    @JsonIgnore @Getter @Setter
    protected SSyncObservableList<S> selectedList;

    @JsonIgnore @Getter @Setter
    protected SSyncObservableList<S> remoteList;

    @Getter @Setter //仅序列化/反序列化被选项索引集合
    protected LinkedHashSet<Integer> selectedIndexes = new LinkedHashSet<>();

    public int removeSelItem(@NonNull S sel, @NonNull C canItem) {
        int index = candidateList.indexOf(canItem);
        selectedIndexes.remove(index);
        if (remoteList != null)
            remoteList.remove(sel);
        selectedList.remove(sel);
        return index;
    }

    public void removeSelItem(int toRemove) {
        if (toRemove < 0)
            return;
        int selIndex = selectedIndexes.stream().toList().indexOf(toRemove);
        if (selIndex < 0)
            return;
        var sel = selectedList.get(selIndex);
        if (remoteList != null)
            remoteList.remove(sel);
        selectedList.remove(selIndex);
        selectedIndexes.remove(toRemove);
    }

    /**
     * 该方法将被选中的 {@link C} 转为 {@link S}。
     * 在{@link MultiSelObListController}组件中将选中的{@link C} 转为 {@link S}
     * 后放入 {@link AbstractMultiSelectObList#selectedList}。
     * */
    public int select(C selectingItem) throws Exception {
        int index = candidateList.indexOf(selectingItem);
        if (ignoredIndexes != null && ignoredIndexes.contains(index))
            return index;
        if (index < 0)
            throw new Exception("The item to select is not contained in the candidateList!");
        selectedIndexes.add(index);
        S sel = transToSelected(selectingItem);
        selectedList.add(sel);
        if (remoteList != null)
            remoteList.add(sel);
        return index;
    }

    public void select(int toSelIndex) {
        if (toSelIndex < 0)
            return;
        if (ignoredIndexes != null && ignoredIndexes.contains(toSelIndex))
            return;
        selectedIndexes.add(toSelIndex);
        S sel = transToSelected(candidateList.get(toSelIndex));
        selectedList.add(sel);
        if (remoteList != null)
            remoteList.add(sel);
    }

    /**
     * 该方法将被选中的 {@link C} 转为 {@link S}。
     * */
    public abstract S transToSelected(C selectingItem);

    /**
     * 该方法须将待选中的 {@link C} 代入 {@link #selectedList}中判断是否已被选。
     * 返回选中的C对应的 {@link S}
     * 见{@link MultiSelObListController} 中对点击事件的响应方式。
     * */
    public S isSelected(C candidateItem) {
        for (S res : selectedList) {
            if (isHomologous(res, candidateItem)) {
                return res;
            }
        }
        return null;
    }

    /**
     * 当前sel项是否选自于canItem
     * */
    public abstract boolean isHomologous(S sel, C canItem);

    /**
     * 该方法为{@link #candidateList}中各元素的toString方法，
     * {@link MultiSelObListController} 通过调用该方法将{@link #candidateList}
     * 中的各元素呈现在ListView中。
     * */
    public abstract String getItemString(C item);


    public AbstractMultiSelectObList() {
        candidateList = new SSyncObservableList<>();
        selectedList = new SSyncObservableList<>();
    }

    public AbstractMultiSelectObList(SSyncObservableList selectedList) {
        candidateList = new SSyncObservableList<>();
        this.selectedList = selectedList;
    }

    public AbstractMultiSelectObList(Object mutex) {
        candidateList = new SSyncObservableList<>(mutex);
        selectedList = new SSyncObservableList<>(mutex);
    }

    /** ps: 一般在{@link AbstractIntegratedProcessor} 或 {@link AbstractHandler} 中的initialize()方法中设置。*/
    public void setMutex(Object mutex) {
        candidateList.setMutex(mutex);
        selectedList.setMutex(mutex);
    }

    public void clearCandidateItems() {
        candidateList.clear();
    }

    public void clearSelectedItems() {
        selectedList.clear();
    }

    /**
     * 该清理方法须触发监听对象（即ListView等控件）
     * 当selectedList中有数据而candidateList无数据时，认为此时为第一次初始化
     * */
    public void clearItems() {
        if (unionSelections != null) {
            for (var sel : unionSelections) {
                if (sel.getCandidateList().size() > 0) {
                    sel.getSelectedList().clear();
                    sel.getSelectedIndexes().clear();
                    sel.getCandidateList().clear();
                }
            }
        } else {
            if (candidateList.size() > 0) {
                selectedList.clear();
                selectedIndexes.clear();
                candidateList.clear();
            }
        }
    }

    public void clearSelectedAndUnions() {
        if (unionSelections != null) {
            for (var sel : unionSelections) {
                sel.getSelectedList().clear();
                sel.getSelectedIndexes().clear();
            }
        } else {
            selectedList.clear();
            selectedIndexes.clear();
        }
    }

    public void clearSelected() {
        selectedList.clear();
        selectedIndexes.clear();
    }

    public void selectAllAndUnions() throws Exception {
        if (unionSelections != null) {
            for (var sel : unionSelections) {
                for (var item : sel.getCandidateList()) {
                    sel.select(item);
                }
            }
        } else {
            for (var item : candidateList) {
                select(item);
            }
        }
    }
}
