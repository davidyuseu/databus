package sy.common.fx.ui;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Font;
import javafx.util.Callback;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

@Data
public abstract class ParaListView<T> {
    private static final Logger LOGGER_MAIN = LoggerFactory.getLogger(ParaListView.class);
    String listName = "";
    volatile boolean editable = true;
    IntegerProperty currentIndex = new SimpleIntegerProperty(0);
    ListView<T> listView = new ListView<>();
    ObservableList<T> remoteList = null;//FXCollections.observableArrayList();
    IntegerProperty remoteListIndex = new SimpleIntegerProperty(0);
    boolean appendToRListTail = true;
    String styleOfSelCell = "-fx-text-fill: aliceblue;"
            + "-fx-background-color: #578dd8;"
            + "-fx-background-insets: 0 1 1 1;"
            + "-fx-background-radius: 2;"
            + "-fx-text-alignment: center;"
            ;
    String styleOfUnSelCell = "-fx-text-fill: black;";
    //设置字体
    URL url = null;
    Font font = null;

    public abstract String getItemString(T item);

    public ParaListView() {
        listView.setPlaceholder(new Label("no params"));
        listView.setFixedCellSize(26);

        listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                ListCell<T> listCell = new ListCell<T>(){
                    protected void updateItem(T item, boolean empty){
                        super.updateItem(item, empty);
                        if(item != null){
                            if(remoteList.contains(item)) {
                                setStyle(styleOfSelCell);
                            }else{
                                setStyle(styleOfUnSelCell);
                            }
                            setText(getItemString(item));
                        }else{
                            setText("");
                            setStyle("");
                        }
                    }
                };

                listCell.setOnMousePressed(event -> {
                    if(!editable)
                        return;
                    if(listView.getSelectionModel().getSelectedIndex() < 0)
                        return;
                    T val = listCell.getItem();
                    if(remoteList.contains(val)) {
                        remoteList.remove(val);
                    }else {
                        if(appendToRListTail) {
                            remoteList.add(val);
                        }else{
                            remoteList.add(remoteListIndex.get(), val);
                        }
                    }
                    currentIndex.set(listView.getSelectionModel().getSelectedIndex());
                    listView.getSelectionModel().clearSelection();
                    listView.refresh();
                });
                return listCell;
            }
        });
    }
}
