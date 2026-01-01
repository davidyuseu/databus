package sy.common.fx.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.Font;
import javafx.util.Callback;
import lombok.Getter;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import sy.common.cache.ICache;
import sy.common.concurrent.vector.SSyncObservableList;

import java.lang.ref.WeakReference;



@Log4j2
public abstract class MovableListView<T> {

    private static final double cellSize = 30d;

    @Setter
    private boolean editable;

    @Getter
    private final ListView<T> listView = new ListView<>();

    @Getter
    private SSyncObservableList<T> syncObList;

    public void setSyncObList(SSyncObservableList<T> syncObList){
        this.syncObList = syncObList;
        this.listView.setItems(this.syncObList);
    }

    protected abstract String getItemString(T item);

    private class ItemView extends HBox implements ICache {
        Label label = new Label();
        Button up = new Button("\uF0D8");
        Button down = new Button("\uF0D7");
        Button delete = new Button("\uF00D");
        @Getter
        T item;
        public void setItem (T item) {
            this.item = item;
            label.setText(getItemString(this.item));
        }

        public void rebuild(T item) {
            setItem(item);
        }

        ItemView (T item) {
            setItem(item);
            label.setPadding(new Insets(4, 0, 0, 0));
            up.getStyleClass().add("handlerBtn");
            up.setFont(Font.font("FontAwesome"));
            down.getStyleClass().add("handlerBtn");
            down.setFont(Font.font("FontAwesome"));
            delete.getStyleClass().add("handlerBtn");
            delete.setFont(Font.font("FontAwesome"));

            this.getChildren().addAll(label, up, down, delete );
            this.setSpacing(2);
            setHgrow(label, Priority.ALWAYS);
            label.setAlignment(Pos.CENTER_LEFT);
            label.setCursor(Cursor.DISAPPEAR);
            label.setPrefWidth(168d);

            // 删除
            delete.setOnAction(event -> {
                int crntPos = listView.getSelectionModel().getSelectedIndex();
                if (crntPos >= 0)
                    listView.getSelectionModel().clearSelection(); // 删除时先取消所有焦点，触发删除当前handler控件面板
                syncObList.remove(this.item);
            });

            // 上移
            up.setOnAction(event -> {
                int targetPos = syncObList.moveUp(this.item);
                listView.getSelectionModel().select(targetPos);
                listView.requestFocus();
            });

            // 下移
            down.setOnAction(event -> {
                int targetPos = syncObList.moveDown(this.item);
                listView.getSelectionModel().select(targetPos);
                listView.requestFocus();
            });

            if (!MovableListView.this.editable) {
                delete.setVisible(false);
                delete.setManaged(false);
                up.setVisible(false);
                up.setManaged(false);
                down.setVisible(false);
                down.setManaged(false);
            }
        }

        /* 若对象没有重写hashCode()方法，则不可以用syncObList.indexOf(this.item);， 否则索引对象时总是返回-1*/
        private int getIndexOf(T item) {
            for (int i = 0; i < syncObList.size(); i++) {
                if (syncObList.get(i) == item)
                    return i;
            }
            return -1;
        }

        @Override
        public void clean() {
            this.item = null;
        }
    }

    public MovableListView(SSyncObservableList syncObList, boolean editable) {
        this.editable = editable;
        setSyncObList(syncObList);

        listView.getStyleClass().add("listView0");
        listView.setPlaceholder(new Label("Handler List"));
        listView.setFixedCellSize(cellSize);
        listView.setPrefWidth(278d);
        listView.setPrefHeight(130d);

        listView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<T> call(ListView<T> tListView) {
                ListCell<T> listCell = new ListCell<>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item != null) {
                            ItemView itemView = (ItemView) this.getGraphic();
                            if (itemView == null) {
                                itemView = new ItemView(item);
                                WeakReference<ItemView> itemViewWeakRef
                                        = new WeakReference<>(itemView);
                                this.setGraphic(itemViewWeakRef.get());
                            } else {
                                if (item != itemView.getItem()) {
                                    itemView.setItem(item);
                                }
                            }
                        } else {
                            setText(null);
                            setGraphic(null);
                        }
                    }
                };
                return listCell;
            }
        });
    }

    public void forbidEditing() {
        listView.lookupAll(".handlerBtn").forEach(node -> {
            node.setVisible(false);
            node.setManaged(false);
        });
    }

    public void allowEditing() {
        listView.lookupAll(".handlerBtn").forEach(node -> {
            node.setVisible(true);
            node.setManaged(true);
        });
    }

}

