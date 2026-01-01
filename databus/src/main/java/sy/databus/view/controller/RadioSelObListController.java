package sy.databus.view.controller;

import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.CacheFactory;
import sy.databus.entity.property.RadioSelectObList;
import sy.databus.entity.property.RadioSelectObList_Ins;
import sy.databus.organize.BaseCustomisedChangeListener;
import sy.databus.process.Console;
import sy.databus.view.SLabel;

import java.lang.reflect.Field;
import java.util.Vector;

/**
 * entity: {@link RadioSelectObList}
 * */
@Log4j2
public class RadioSelObListController<C> extends ConsoleController {

    class RadioSelObListStringConverter<T> extends StringConverter<T> {
        private RadioSelectObList<T> refObList;
        public void setRefObList(RadioSelectObList<T> refObList) {
            this.refObList = refObList;
        }

        @Override
        public String toString(T t) {
            if (t != null && refObList != null)
                return refObList.getItemString(t);
            else
                return null;
        }

        @Override
        public T fromString(String s) {
            return null;
        }
    }

    protected VBox vBox = new VBox();

    protected SLabel label = new SLabel();

    protected ComboBox<C> comboBox = new ComboBox<>();

    protected RadioSelectObList<C> radioSelectObList;

    protected RadioSelObListStringConverter converter = new RadioSelObListStringConverter();

    private final String styleOfSelCell = "-fx-text-fill: #578dd8;";

    private final String styleOfUnSelCell = "-fx-text-fill: black;";

    private BaseCustomisedChangeListener<Number> selectedIndexListener = null;

    public RadioSelObListController(Console console, Field field, Object parentObj,@NonNull RadioSelectObList radioSelObList) {
        super(console, field, parentObj);
        this.radioSelectObList = radioSelObList;

        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());

        comboBox.setPrefWidth(238d);

        // 绑定候选列表
        comboBox.setItems(this.radioSelectObList.getCandidateList());
        // 选中当前索引
        selectCrntItem();

        selectedIndexListener = createSelectedIndexListener();
        var weakListener = new WeakChangeListener<>(selectedIndexListener);
        comboBox.getSelectionModel().selectedIndexProperty().addListener(weakListener);

        converter.setRefObList(this.radioSelectObList);
        comboBox.setConverter(converter);

        comboBox.setCellFactory(new Callback<ListView<C>, ListCell<C>>() {
            @Override
            public ListCell<C> call(ListView<C> cListView) {
                ListCell<C> listCell = new ListCell<>() {
                    @Override
                    protected void updateItem(C item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null && radioSelectObList != null && radioSelectObList.getCandidateList() != null) {
                            Vector<Integer> occupiedIndexes = radioSelectObList.getOccupiedIndexGroup();
                            int index = radioSelectObList.getCandidateList().indexOf(item);
                            if (occupiedIndexes != null
                                    && occupiedIndexes.contains(index)
                                    /*&& index != radioSelectObList.getSelIndex()*/) {
                                setText("√ " + radioSelectObList.getItemString(item));
                                setStyle(styleOfSelCell);
                                if (index != radioSelectObList.getSelIndex())
                                    setDisable(true);
                            } else {
                                setText(radioSelectObList.getItemString(item));
                                setStyle(styleOfUnSelCell);
                                setDisable(false);
                            }
                        } else {
                            setText("");
                            setStyle("");
                        }
                    }
                };
                return listCell;
            }
        });

        comboBox.showingProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                comboBox.setItems(this.radioSelectObList.getCandidateList());
            }
        });


        vBox.getChildren().addAll(label, comboBox);
        vBox.setSpacing(2d);
        this.getChildren().add(vBox);
    }

    private BaseCustomisedChangeListener<Number> createSelectedIndexListener() {
        return new BaseCustomisedChangeListener<>(RadioSelObListController.this) {
            @Override
            public void syncChanged(ObservableValue<? extends Number> observableValue,
                                    Number oldValue,
                                    Number newValue) {
                selIndexChanged(newValue);
            }
        };
    }

    private void selIndexChanged(Number newValue) {
        /** 如果是clean方法触发的select(-1)，则不能往下处理！*/
        if (radioSelectObList == null)
            return;
        /** 由于该ComboBox组件是复用的，监听到的oldValue未必真的属于当前radioSelectObList*/
        int realOldValue = radioSelectObList.getSelIndex();
        int realNewValue = newValue.intValue();
        radioSelectObList.setSelIndex(realNewValue);
        if (realNewValue >= 0 && realNewValue != realOldValue) {
            try {
                var actions = radioSelectObList.getSelIndexChangedActions();
                if (actions != null) {
                    for (var action : actions) {
                        action.changed(realOldValue, realNewValue);
                    }
                }
            } catch (Exception exception) {
                // #- log to view
                abnormalInput();
                log.warn("Failed to change the selection!", exception);
                return;
            }
            normalDisplay();
            if (radioSelectObList.getOccupiedIndexGroup() != null) {
                radioSelectObList.getOccupiedIndexGroup().add(realNewValue);
            }
            if (realOldValue >= 0
                    && radioSelectObList.getOccupiedIndexGroup() != null) {
                radioSelectObList.getOccupiedIndexGroup()
                        .removeElement(realOldValue);
            }
        }
    }

    public static RadioSelObListController buildController(Console console,
                                                           Field field,
                                                           Object obj,
                                                           RadioSelectObList radioSelObList) {
        RadioSelObListController cachedController
                = (RadioSelObListController) CacheFactory.getWeakCache(RadioSelObListController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, radioSelObList);
            return cachedController;
        } else {
            if (radioSelObList.getClass() == RadioSelectObList_Ins.class)
                return new RadioSelObList_InsController(console, field, obj, radioSelObList);

            return new RadioSelObListController(console, field, obj, radioSelObList);
        }
    }

    private void rebuild(Console console, Field field, Object parentObj, @NonNull RadioSelectObList radioSelObList) {
        super.rebuild(console, field, parentObj);
        this.radioSelectObList = radioSelObList;

        converter.setRefObList(this.radioSelectObList);
        comboBox.setItems(this.radioSelectObList.getCandidateList());

        // 选中当前索引
        selectCrntItem();

        selectedIndexListener = createSelectedIndexListener();
        var weakListener = new WeakChangeListener<>(selectedIndexListener);
        comboBox.getSelectionModel().selectedIndexProperty().addListener(weakListener);

        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());

    }

    protected void selectCrntItem() {
        int tIndex = this.radioSelectObList.getSelIndex();
        comboBox.getSelectionModel().select(tIndex);
    }

    @Override
    protected void normalDisplay() {
        super.normalDisplay();
        label.setNormalDisplay();
    }

    @Override
    protected void abnormalInput(){
        super.abnormalInput();
        label.setErrorDisplay();
    }

    @Override
    protected void abnormalReport() {
        super.abnormalReport();
        label.setWarnDisplay();
    }

    @Override
    public void clean() {
        super.clean();
        selectedIndexListener.setCustomised(false);
        selectedIndexListener = null;
    }
}
