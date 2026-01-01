import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import sy.databus.process.Console;
import sy.databus.process.frame.handler.common.TestHandler;

import java.lang.reflect.Field;

class BasicTypeInputControllerTest {

    public static void main(String[] args) {
        Application.launch(BasicTypeInputControllerTestApp.class);
    }

    public static class BasicTypeInputControllerTestApp extends Application {

        @Override
        public void start(Stage stage) throws Exception {
            VBox vBox = new VBox();
            TestHandler testHandler = new TestHandler();
            Field field = testHandler.getClass().getDeclaredField("planeNum");
            Console console = field.getAnnotation(Console.class);
            BasicInputController controller = new BasicInputController(console,
                    field,
                    testHandler,
                    BasicInputController.DataFormattingStrategy.INTEGER);
            vBox.getChildren().add(controller);

            stage.setScene(new Scene(vBox));
            stage.show();
        }
    }
}