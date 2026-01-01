package other;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class CSVOperatorTest {

    public static CSVOperatorTemp csvOperator;
    static final int ROW_COUNT = 2 << 19;//2 << 14; // 行数
    static final int COLUMN_COUNT = 100;//2 << 14; // 列数

    public static void main(String[] args) {
        Application.launch(CSVOperatorTestApp.class);
    }


    public static class CSVOperatorTestApp extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            HBox pane = new HBox();
            Button button = new Button("clean");
            button.setOnAction(e -> {
                System.gc();
            });
            pane.getChildren().add(button);
            Scene scene = new Scene(pane);
            stage.setScene(scene);
            stage.show();

            csvOperator = new CSVOperatorTemp();
            csvOperator.createParaTableCSV(COLUMN_COUNT);

            List<ArrayList<String>> csvData = new ArrayList<>();
            ArrayList<String> row = new ArrayList<>();
            for (int i = 0; i < ROW_COUNT - 1; i++) {
                long var = System.currentTimeMillis();
                for (int j = 0; j < COLUMN_COUNT; j++) {
//                row.add("row_" + i + "-" + "column_" + j);
                    row.add(String.valueOf(var + j));
                }
                csvData.add(row);
                csvOperator.writeData(csvData);
                row.clear();
                csvData.clear();
            }
            csvOperator.finish();
        }
    }
}
