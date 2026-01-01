package sy.databus;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import lombok.Getter;

public class LeftPaneController {
    public static LeftPaneController INSTANCE = null;

    @FXML @Getter
    private Accordion outerContainers;

    @FXML @Getter
    private TitledPane componentsPane;

    public LeftPaneController() {
        INSTANCE = this;
    }

    @FXML
    private void initialize() {

    }
}
