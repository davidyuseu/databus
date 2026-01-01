package sy.databus.view.controller;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;
import sy.common.cache.CacheFactory;
import sy.databus.entity.property.AbstractMultiSelectObList;
import sy.databus.process.Console;
import sy.databus.view.SLabel;

import java.lang.reflect.Field;

/**
 * entity: {@link AbstractMultiSelectObList}
 * */
@Log4j2
public class MultiSelObListController<C, S> extends ConsoleController {
    protected VBox vBox = new VBox();

    protected SLabel label = new SLabel();

    protected Button selAll = new Button("全选");

    protected Button unselAll = new Button("重置");

    protected HBox selBtnPane = new HBox();

    protected ListView<C> listView = new ListView<>();

    protected AbstractMultiSelectObList<C, S> multiSelectObList;

    private long lastRefreshTimestamp = 0L;

    protected static String styleOfSelCell = "-fx-text-fill: aliceblue;"
            + "-fx-background-color: #578dd8;"
            + "-fx-background-insets: 0 1 1 1;"
            + "-fx-background-radius: 2;"
            + "-fx-text-alignment: center;"
            ;

    protected static String styleOfInvalidCell = "-fx-text-fill: #a9aeb3; -fx-background-color: transparent;";

    protected static String styleOfUnSelCell = "-fx-text-fill: black;";

    public static MultiSelObListController buildController(Console console,
                                                                 Field field,
                                                                 Object obj,
                                                                 AbstractMultiSelectObList mulSelObList) {
        MultiSelObListController cachedController
                = (MultiSelObListController) CacheFactory.getWeakCache(MultiSelObListController.class);
        if (cachedController != null) {
            cachedController.rebuild(console, field, obj, mulSelObList);
            return cachedController;
        } else {
            return new MultiSelObListController(console, field, obj, mulSelObList);
        }

    }

    public MultiSelObListController(Console console, Field field, Object parentObj, AbstractMultiSelectObList mulSelObList) {
        super(console, field, parentObj);
        this.multiSelectObList = mulSelObList;
        this.getStyleClass().add("multiSelObList");

        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());

        selAll.setPrefSize(44d, 18d);
        selAll.setOnAction(event -> {
            if (multiSelectObList.getSelectedList().size() == multiSelectObList.getCandidateList().size())
                return;
            multiSelectObList.clearSelectedAndUnions();
            try {
                multiSelectObList.selectAllAndUnions();
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            Platform.runLater(() -> listView.refresh());
        });
        unselAll.setPrefSize(44d, 18d);
        unselAll.setOnAction(e -> {
            if (multiSelectObList.getSelectedList().size() == 0)
                return;
            multiSelectObList.clearSelectedAndUnions();
            Platform.runLater(() -> listView.refresh());
        });

        listView.getStyleClass().add("listView0");
        listView.setPlaceholder(new Label("no params"));
        listView.setFixedCellSize(26d);
        listView.setPrefWidth(238d);

        listView.setItems(multiSelectObList.getCandidateList());
        listView.setCellFactory(new Callback<ListView<C>, ListCell<C>>() {
            @Override
            public ListCell<C> call(ListView<C> param) {
                ListCell<C> listCell = new ListCell<C>(){
                    protected void updateItem(C item, boolean empty){
                        super.updateItem(item, empty);
                        if (!empty) {
                            if (item != null) {
                                setText(multiSelectObList.getItemString(item));
                                if (multiSelectObList.isIgnoredItem(item)) {
                                    setStyle(styleOfInvalidCell);
                                    setDisable(true);
                                } else {
                                    setDisable(false);
                                    if (multiSelectObList.isSelected(item) != null) {
                                        setStyle(styleOfSelCell);
                                    } else {
                                        setStyle(styleOfUnSelCell);
                                    }
                                }
                            } else {
                                setText("");
                                setStyle("");
                            }
                        }
                    }
                };

                listCell.setOnMousePressed(event -> {
                    if(!editable)
                        return;
                    if(listView.getSelectionModel().getSelectedIndex() < 0)
                        return;
                    C val = listCell.getItem();

                    if (multiSelectObList.isIgnoredItem(val)) {
                        return;
                    }

                    S selected = multiSelectObList.isSelected(val);
                    if(selected != null) {
                        int index = multiSelectObList.removeSelItem(selected, val);
                        var unionSelections = multiSelectObList.getUnionSelections();
                        if (unionSelections != null) {
                            unionSelections.forEach(sel -> {
                                if (multiSelectObList != sel)
                                    sel.removeSelItem(index);
                            });
                        }
                    }else {
                        try {
                            int index = multiSelectObList.select(val);
                            if (index >= 0) {
                                var unionSelections = multiSelectObList.getUnionSelections();
                                if (unionSelections != null) {
                                    unionSelections.forEach(sel -> {
                                        if (multiSelectObList != sel)
                                            sel.select(index);
                                    });
                                }
                            }
                        } catch (Exception e) {
                            log.error(e);
                            return;
                        }
                    }
//                    currentIndex.set(listView.getSelectionModel().getSelectedIndex());
                    listView.getSelectionModel().clearSelection();
                    listView.refresh();
                });
                return listCell;
            }
        });

        selBtnPane.getChildren().addAll(selAll, unselAll);
        selBtnPane.setSpacing(4);
        vBox.getChildren().addAll(label, selBtnPane, listView);
        vBox.setSpacing(2d);
        this.getChildren().add(vBox);
    }

    public void rebuild(Console console, Field field, Object parentObj, AbstractMultiSelectObList mulSelObList) {
        super.rebuild(console, field, parentObj);
        this.multiSelectObList = mulSelObList;
        listView.setItems(this.multiSelectObList.getCandidateList());
        listView.refresh();

        if (!console.display().isEmpty())
            label.setText(console.display());
        else
            label.setText(field.getName());
    }

}
