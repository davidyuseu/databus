package sy.databus;

import java.io.*;
import java.net.URL;

import javafx.application.HostServices;
import lombok.extern.log4j.Log4j2;
import sy.common.util.SFileUtils;
import sy.databus.global.GlobalState;
import sy.databus.organize.ComponentsAccordion;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.IEventProc;
import sy.databus.projects.ProjectsMark;
import sy.grapheditor.api.GraphEditor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * A demo application to show uses of the {@link GraphEditor} library.
 */
@Log4j2
public class App extends Application {

    private static final String APPLICATION_TITLE = "databus";
    private static final String DEMO_STYLESHEET = "/view/base.css";
    private static final String TREE_SKIN_STYLESHEET = "/view/treeskins.css";
    private static final String TITLED_SKIN_STYLESHEET = "/view/titledskins.css";
    private static final String FONT_AWESOME = "/view/font/fontawesome.ttf";

    public static File DEFAULT_LOADING_FILE = new File(".\\res\\saved\\sample.graph");

    public static HostServices hostServices;

    /** 左组件栏*/

    @Override
    public void start(final Stage stage) throws Exception {
        hostServices = getHostServices();

        File cacheDir = new File(".\\dataFusionCaches");
        SFileUtils.deleteFiles(cacheDir);

        GlobalState.Init();
        if (GlobalState.initConfig.isOutputSysLog())
            outputSysLogInfo();

        final URL location = getClass().getResource("/view/MainPane.fxml");
        final FXMLLoader loader = new FXMLLoader(location);
        final Parent root = loader.load();

        final Scene scene = new Scene(root,
                GlobalState.initConfig.getWindowWeight(),
                GlobalState.initConfig.getWindowHeight());

        scene.getStylesheets().add(getClass().getResource(DEMO_STYLESHEET).toExternalForm());
//        scene.getStylesheets().add(getClass().getResource(TREE_SKIN_STYLESHEET).toExternalForm());
        scene.getStylesheets().add(getClass().getResource(TITLED_SKIN_STYLESHEET).toExternalForm());
        Font.loadFont(getClass().getResource(FONT_AWESOME).toExternalForm(), 12);

        if (DEFAULT_LOADING_FILE.exists())
            MainPaneController.INSTANCE.loadFile(DEFAULT_LOADING_FILE);


        UISchedules.initUIExecutors();

        stage.setScene(scene);
        stage.setTitle(APPLICATION_TITLE);

        stage.show();
        stage.setOnCloseRequest(e -> {
            log.info("程序关闭。");
            UISchedules.stopLogViewTimer();
            System.exit(0);
        });

        /* Processing and retouching this ui */
        // 左组件栏
        ComponentsAccordion.arrangeProcessorClassesWithWatchPane(LeftPaneController.INSTANCE.getOuterContainers(),
                AbstractIntegratedProcessor.class);
        ComponentsAccordion.arrangeHandlerClasses(LeftPaneController.INSTANCE.getOuterContainers(),
                IEventProc.class);
        ComponentsAccordion.arrangeProcessorClassesWithWatchPane(LeftPaneController.INSTANCE.getOuterContainers(),
                ProjectsMark.class);
        ComponentsAccordion.arrangeHandlerClasses(LeftPaneController.INSTANCE.getOuterContainers(),
                ProjectsMark.class);
        // 布置绑定的组件
        ComponentsAccordion.arrangeComponentWithBinding();
        // 右组件栏

        // graph editor pane
        final MainPaneController controller = loader.getController();
        controller.panToCenter();

        UISchedules.startLogViewTimer();
        log.info("程序启动！");
        if (GlobalState.initConfig.getEditModeKey() != 54)
            MainPaneController.INSTANCE.limitedToAnalysisMode();
    }

    private void outputSysLogInfo() throws FileNotFoundException {
        OutputStream outputstream = new FileOutputStream(".\\abc.txt");
        PrintStream printstream = new PrintStream(outputstream);
        System.setErr(printstream);
    }

    public static void main(final String[] args) {
        launch(args);
    }
}
