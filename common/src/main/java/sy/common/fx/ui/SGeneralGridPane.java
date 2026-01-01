package sy.common.fx.ui;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import lombok.Getter;

public class SGeneralGridPane extends GridPane {
    private static final String BORDER_COLOR = "-fx-border-color: rgba(128,128,128,0.64)";

    @Getter
    private SimpleDoubleProperty innerPaddingProp = new SimpleDoubleProperty(0d);

    @Override
    public void addRow(int var1, Node... var2){
        StackPane[] nodes = new StackPane[var2.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new StackPane();
            nodes[i].getChildren().add(var2[i]);
            nodes[i].setAlignment(Pos.CENTER);
        }
        super.addRow(var1, nodes);
    }

    @Override
    public void addColumn(int var1, Node... var2){
        StackPane[] nodes = new StackPane[var2.length];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = new StackPane();
            nodes[i].getChildren().add(var2[i]);
            nodes[i].setAlignment(Pos.CENTER);
        }
        super.addColumn(var1, nodes);
    }

    public void setInternalGridLineVisible(boolean flag) {
        int rowCount = this.getRowCount();
        int columnCount = this.getColumnCount();
        if(flag) {
            for (int i = 0; i < rowCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    if(i == rowCount - 1 && j == columnCount - 1)
                        this.getChildren().get(i * columnCount + j).setStyle("-fx-border-width: 0");
                    else if (i != rowCount - 1 && j != columnCount - 1)
                        this.getChildren().get(i * columnCount + j).setStyle("-fx-border-width: 0 1 1 0;" + BORDER_COLOR);
                    else if (i == rowCount - 1)
                        this.getChildren().get(i * columnCount + j).setStyle("-fx-border-width: 0 1 0 0;" + BORDER_COLOR);
                    else if (j == columnCount - 1)
                        this.getChildren().get(i * columnCount + j).setStyle("-fx-border-width: 0 0 1 0;" + BORDER_COLOR);

                }
            }
        }else{
            for (Node node : this.getChildren()){
                node.setStyle("-fx-border-width: 0");
            }
        }
    }

    public void setInnerPadding(double innerPadding){
        for (Node node : this.getChildren()){
            ((StackPane) node).setPadding(new Insets(innerPadding));
        }
        this.innerPaddingProp.set(innerPadding);
    }

    public void setGrowInner(){
        for(Node node : this.getChildren()){
            GridPane.setHgrow(node, Priority.ALWAYS);
            GridPane.setVgrow(node, Priority.ALWAYS);
        }
    }
}
