/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.databus;

import io.netty.channel.ChannelFuture;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import sy.databus.entity.ProcessorId;
import sy.databus.global.Constants;
import sy.databus.global.GlobalState;
import sy.databus.global.ProcessorSJsonUtil;
import sy.databus.global.WorkMode;
import sy.databus.organize.ExecutorManager;
import sy.databus.organize.ProcessorManager;
import sy.databus.organize.TaskManager;
import sy.databus.process.AbstractIntegratedProcessor;
import sy.databus.process.IEventProc;
import sy.databus.process.analyse.ParamsAnalyzer;
import sy.databus.selections.SelectionCopier;
import sy.databus.view.customskins.DefaultSkinController;
import sy.databus.view.customskins.SkinController;
import sy.databus.view.customskins.TitledSkinController;
import sy.databus.view.customskins.titled.TitledNodeSkin;
import sy.databus.view.customskins.titled.TitledSkinConstants;
import sy.databus.view.customskins.tree.TreeConnectorValidator;
import sy.databus.view.customskins.tree.TreeSkinConstants;
import sy.databus.view.logger.LogView;
import sy.databus.view.monitor.MonitorsManager;
import sy.databus.view.utils.AwesomeIcon;
import sy.grapheditor.api.*;
import sy.grapheditor.core.DefaultGraphEditor;
import sy.grapheditor.core.skins.GraphEditorSkinManager;
import sy.grapheditor.core.skins.defaults.connection.SimpleConnectionSkin;
import sy.grapheditor.core.view.GraphEditorContainer;
import sy.grapheditor.model.GModel;
import sy.grapheditor.model.GNode;
import sy.grapheditor.model.GraphFactory;

import java.awt.*;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static sy.databus.global.Constants.*;

@Log4j2
public class MainPaneController {

    public static MainPaneController INSTANCE = null;
    @FXML
    @Getter
    private ScrollPane visPane;
    @FXML
    @Getter
    private Button btnVisualize;
    @FXML
    @Getter
    private ListView<String> lvToVis;
    @FXML
    @Getter
    private Button btnOpenToVis;
    @FXML
    @Getter
    private Label lbPathToVis;
    private FileChooser toVisChooser = new FileChooser();
    private File fileToVis;
    @FXML
    @Getter
    private SplitPane centerSplitPane;
    @FXML
    @Getter
    private StackPane watcherContainer;
    @FXML
    private RadioMenuItem topologyBtn;
    @FXML
    private RadioMenuItem monitorBtn;
    @FXML
    private Menu viewMenu;
    @FXML
    private Menu fileMenu;
    @FXML
    @Getter
    private ScrollPane logPane;

    @FXML
    @Getter
    private Tab tabOriDataView;
    public void selectTabOriDataView() {
        pluginArea.getSelectionModel().select(tabOriDataView);
    }
    @FXML
    @Getter
    private TabPane pluginArea;
    @FXML
    @Getter
    private Tab tabLog;
    public void selectTabLog() {
        pluginArea.getSelectionModel().select(tabLog);
    }
    @FXML
    @Getter
    private Tab tabParamsView;
    public void selectParamsView() {
        pluginArea.getSelectionModel().select(tabParamsView);
    }
    @FXML
    @Getter
    private TabPane tabPaneParamsViews;
    @FXML
    @Getter
    private Label modeLabel;
    @FXML
    private ToggleButton expandBtn; // 工作台“展开/折叠”
    @FXML
    private RadioMenuItem editModeButton;
    @FXML
    private RadioMenuItem analysisModeButton;
    @FXML
    private RadioMenuItem missionModeButton;
    @FXML
    private Menu editMenu;

    // 当前工作模式对应的worktable
    @Getter @Setter
    private Pane currentWorktable;

    private Map<WorkMode, Pane> worktableMap = new HashMap<>();

    public MainPaneController() {
        INSTANCE = this;
    }
    private static final String STYLE_CLASS_TITLED_SKINS = "titled-skins";

    @FXML
    @Getter @Setter
    private VBox topVBox;
    @FXML
    @Getter @Setter
    private BorderPane topHalfPane;

    @FXML
    private AnchorPane topHalfPart;
    @FXML
    private MenuBar menuBar;
    @FXML
    private MenuItem addConnectorButton;
    @FXML
    private MenuItem clearConnectorsButton;
    @FXML
    private Menu connectorTypeMenu;
    @FXML
    private Menu connectorPositionMenu;
    @FXML
    private RadioMenuItem inputConnectorTypeButton;
    @FXML
    private RadioMenuItem outputConnectorTypeButton;
    @FXML
    private RadioMenuItem leftConnectorPositionButton;
    @FXML
    private RadioMenuItem rightConnectorPositionButton;
    @FXML
    private RadioMenuItem topConnectorPositionButton;
    @FXML
    private RadioMenuItem bottomConnectorPositionButton;
    @FXML
    private RadioMenuItem showGridButton;
    @FXML
    private RadioMenuItem snapToGridButton;
    @FXML
    private Menu readOnlyMenu;
    @FXML
    private RadioMenuItem defaultSkinButton;
    @FXML
    private RadioMenuItem treeSkinButton;
    @FXML
    private RadioMenuItem titledSkinButton;
    @FXML
    private Menu intersectionStyle;
    @FXML
    private RadioMenuItem gappedStyleButton;
    @FXML
    private RadioMenuItem detouredStyleButton;
    @FXML
    private ToggleButton minimapButton;

    private GraphEditorContainer graphEditorContainer;

    private TabPane monitorContainer;
    @Getter
    private MonitorsManager monitorsManager;

    @Getter
    private final GraphEditor graphEditor = new DefaultGraphEditor();

	private final SelectionCopier selectionCopier = new SelectionCopier(graphEditor.getSkinLookup(),
			graphEditor.getSelectionManager());

	public static void clearSelection(){
        INSTANCE.graphEditor.getSelectionManager().clearSelection();
    }
    private final GraphEditorPersistence graphEditorPersistence = new GraphEditorPersistence();

    private DefaultSkinController defaultSkinController;
    private TreeSkinController treeSkinController;
    private TitledSkinController titledSkinController;

    private final ObjectProperty<SkinController> activeSkinController = new SimpleObjectProperty<>()
    {

        @Override
        protected void invalidated() {
            super.invalidated();
            if(get() != null) {
                get().activate();
            }
        }

    };

    public static SkinLookup getSkinLookup() {
        return INSTANCE.graphEditor.getSkinLookup();
    }

    public static GraphEditorSkinManager getDefaultSkinManager() {
        var skinLookup = getSkinLookup();
        if (skinLookup instanceof GraphEditorSkinManager defaultSkinManager)
            return defaultSkinManager;
        else throw new RuntimeException("Unknown SkinManager!");
    }

    public static Map<GNode, GNodeSkin> getNodeSkins() {
        return getDefaultSkinManager().getMNodeSkins();
    }

    // 原码显示组件
    @Getter
    private OriDataViewController oriDataViewController = new OriDataViewController();
    // 日志显示控件
    @Getter
    private LogView logView = new LogView();
    /**
     * Called by JavaFX when FXML is loaded.
     */
    @FXML
    public void initialize() {
        VBox.setVgrow(topHalfPane, Priority.ALWAYS);

        final GModel model = GraphFactory.eINSTANCE.createGModel();

        // 创建拓扑图形编辑区域
        if (graphEditorContainer == null) {
            monitorContainer = new TabPane();
            watcherContainer.getChildren().add(monitorContainer);
            monitorsManager = new MonitorsManager(monitorContainer);
            graphEditorContainer = new GraphEditorContainer();
            watcherContainer.getChildren().add(graphEditorContainer);
        }
        graphEditor.setModel(model);
        graphEditorContainer.setGraphEditor(graphEditor);

        setDetouredStyle();

        defaultSkinController = new DefaultSkinController(graphEditor, graphEditorContainer);
        treeSkinController = new TreeSkinController(graphEditor, graphEditorContainer);
        titledSkinController = new TitledSkinController(graphEditor, graphEditorContainer);

        activeSkinController.set(defaultSkinController);

		graphEditor.modelProperty().addListener((w, o, n) -> selectionCopier.initialize(n));
        selectionCopier.initialize(model);

        addActiveSkinControllerListener();
        initializeMenuBar();

        // 初始化原码显示界面
        oriDataViewController.init();
        tabOriDataView.selectedProperty().addListener((observableValue, oldValue, newValue) -> {
            if (newValue) {
                if (!GlobalState.currentWorkMode.get().equals(WorkMode.EDIT))
                    UISchedules.startOriDataViewTimers();
            } else {
                UISchedules.stopOriDataViewTimers();
            }
        });
        // 初始化日志界面
        logPane.setContent(logView);
        logView.prefHeightProperty().bind(logPane.heightProperty());
        logView.prefWidthProperty().bind(logPane.widthProperty());

        // 初始化可视化界面
        lbPathToVis.prefWidthProperty().bind(visPane.widthProperty().subtract(38d));
        lvToVis.prefWidthProperty().bind(visPane.widthProperty().subtract(11d));
        lvToVis.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        toVisChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("解参结果", "*.txt"));
        toVisChooser.setInitialDirectory(new File(TABLE_TXT_DIR_PATH));
        btnOpenToVis.setOnAction(event -> {
            fileToVis = toVisChooser.showOpenDialog(btnOpenToVis.getScene().getWindow());
            if (fileToVis != null) {
                String[] heads = getHeads(fileToVis);
                if (heads != null) {
                    lbPathToVis.setText(fileToVis.getAbsolutePath());
                    lbPathToVis.setStyle("-fx-border-color: transparent");
                    fillVisListView(heads);
                } else {
                    String err = "请选择支持格式的解参结果文件！";
                    lbPathToVis.setText(err);
                    lbPathToVis.setStyle("-fx-border-color: red");
                    log.error(err);
                }
            }
        });

        btnVisualize.setOnAction(event -> {
            if (fileToVis == null || !fileToVis.exists())
                return;
            var items = lvToVis.getSelectionModel().getSelectedItems();
            if (items.isEmpty())
                return;
            var indices = lvToVis.getSelectionModel().getSelectedIndices();
            File html = Visualization.outputVisHtml(items, indices, fileToVis);
            App.hostServices.showDocument("file:///" + html.getAbsolutePath());
        });
    }

    private String[] getHeads(File tFile) {
        String[] heads;
        try (BufferedReader br = new BufferedReader((new FileReader(tFile, gCharset)))) {
            String firstLine = br.readLine();
            if (firstLine != null) {
                heads = firstLine.split("\t");
                if (heads.length > 0 && heads[0].equals(ParamsAnalyzer.FIRST_HEAD.trim()))
                    return heads;
                else return null;
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    private void fillVisListView(String[] heads) {
        lvToVis.getItems().clear();
        for (int i = 0; i < heads.length; i++) {
            lvToVis.getItems().add(heads[i]);
        }
    }

    public void panToCenter()
    {
        graphEditorContainer.panTo(Pos.CENTER);
    }

    public void loadFile(File file) throws IOException {
        activeSkinController.set(titledSkinController);
        graphEditorPersistence.loadFile(graphEditor, file);
    }

    private static final String RLY_PARSING_FILE = "sample/gx1/RlyParsing.graph";
    private static final String RT_PARSING_FILE = "sample/gx1/RTParsing.graph";

    public void loadRlyParsing(ActionEvent event) {
        try {
            activeSkinController.set(titledSkinController);
            graphEditorPersistence.loadProject(RLY_PARSING_FILE, graphEditor);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void loadRTParsing(ActionEvent event) {
        try {
            activeSkinController.set(titledSkinController);
            graphEditorPersistence.loadProject(RT_PARSING_FILE, graphEditor);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @FXML
    // Load title node by default
    public void load() {
        activeSkinController.set(titledSkinController);
        File file = null;
        try {
            file = graphEditorPersistence.loadFromFile(graphEditor);
        } catch (Exception e) {
            log.error(e.getMessage());
            return;
        }

        if (file != null && graphEditor.getModel() != null) {
            if (!App.DEFAULT_LOADING_FILE.exists()) {
                App.DEFAULT_LOADING_FILE.getParentFile().mkdirs();
                try {
                    App.DEFAULT_LOADING_FILE.createNewFile();
                } catch (IOException e) {
                    log.error(e.getMessage());
                    return;
                }
            }
            try (FileWriter fw = new FileWriter(App.DEFAULT_LOADING_FILE);
                 FileReader fr = new FileReader(file)){
                char[] data = new char[(int) file.length()];
                fr.read(data);
                fw.write(data);
            } catch (IOException e) {
                log.error(e.getMessage());
                return;
            }
            try {
                log.info("Succeed to load the file '{}'!",
                        file.getCanonicalPath());
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }

    }

    @FXML
    public void loadSample() {
//        ProcessorManager.clearAll();
        defaultSkinButton.setSelected(true);
        setDefaultSkin();
        graphEditorPersistence.loadSample(graphEditor);
    }

    @FXML
    public void loadSampleLarge() {
//        ProcessorManager.clearAll();
        defaultSkinButton.setSelected(true);
        setDefaultSkin();
        graphEditorPersistence.loadSampleLarge(graphEditor);
    }

    @FXML
    public void loadTree() {
//        ProcessorManager.clearAll();
        treeSkinButton.setSelected(true);
        setTreeSkin();
        graphEditorPersistence.loadTree(graphEditor);
    }

    @FXML
    public void loadTitled() {
//        ProcessorManager.clearAll();
        titledSkinButton.setSelected(true);
        setTitledSkin();
        graphEditorPersistence.loadTitled(graphEditor);
    }

    @FXML
    public void save() {
        graphEditorPersistence.saveToFile(graphEditor);
    }

    @FXML
    public void clearAll() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，禁用清空操作。");
            return;
        }
        /** #-mark(复制，删除，清理操作前缓存序列化的processor) */
        // 清理操作前缓存序列化的processor
        for (GNode node : graphEditor.getModel().getNodes()) {
            if (TitledSkinConstants.TITLED_NODE.equals(node.getType())) {
                node.setProcessorJson(
                        ProcessorSJsonUtil.objToStr(
                                TitledGNodeAttachmentUtil.getTitledGNodeProcessor(node)));
            }
        }
        Commands.clear(graphEditor.getModel());
    }

    @FXML
    public void exit() {
        Platform.exit();
    }

    @FXML
    public void undo() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，禁用回退操作。");
            return;
        }
        Commands.undo(graphEditor.getModel());
    }

    @FXML
    public void redo() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，禁用重做操作。");
            return;
        }
        Commands.redo(graphEditor.getModel());
    }

    @FXML
    public void copy() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，不可复制节点。");
            return;
        }
        selectionCopier.copy();
    }

    @FXML
    public void paste() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，禁用粘贴操作。");
            return;
        }
        activeSkinController.get().handlePaste(selectionCopier);
    }

    @FXML
    public void selectAll() {
        activeSkinController.get().handleSelectAll();
    }

    @FXML
    public void deleteSelection() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，禁用删除操作。");
            return;
        }
        final List<EObject> selection = new ArrayList<>(graphEditor.getSelectionManager().getSelectedItems());
        /** #-mark(复制，删除，清理操作前缓存序列化的processor) */
        // 删除前缓存序列化的processor
        for (EObject eObj : selection) {
            if (eObj instanceof GNode && TitledSkinConstants.TITLED_NODE.equals(((GNode) eObj).getType())) {
                    ((GNode) eObj).setProcessorJson(
                            ProcessorSJsonUtil.objToStr(
                                    TitledGNodeAttachmentUtil.getTitledGNodeProcessor((GNode) eObj)));
            }
        }
        graphEditor.delete(selection);
    }

    @FXML
    public void addNode() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，不可添加节点。");
            return;
        }
        activeSkinController.set(titledSkinController);
        activeSkinController.get().addNode(graphEditor.getView().getLocalToSceneTransform().getMxx());
    }

    @FXML
    public void addConnector() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，不可添加连接。");
            return;
        }
        activeSkinController.get().addConnector(getSelectedConnectorPosition(), inputConnectorTypeButton.isSelected());
    }

    @FXML
    public void clearConnectors() {
        if (!editModeButton.isSelected()) {
            log.info("当前不是编辑模式，不可清除连接器。");
            return;
        }
        activeSkinController.get().clearConnectors();
    }

    @FXML
    public void setDefaultSkin() {
        activeSkinController.set(defaultSkinController);
    }

    @FXML
    public void setTreeSkin() {
        activeSkinController.set(treeSkinController);
    }

    @FXML
    public void setTitledSkin() {
        activeSkinController.set(titledSkinController);
    }

    @FXML
    public void setGappedStyle() {
        graphEditor.getProperties().getCustomProperties().remove(SimpleConnectionSkin.SHOW_DETOURS_KEY);
        graphEditor.reload();
    }

    @FXML
    public void setDetouredStyle() {

        final Map<String, String> customProperties = graphEditor.getProperties().getCustomProperties();
        customProperties.put(SimpleConnectionSkin.SHOW_DETOURS_KEY, Boolean.toString(true));
        graphEditor.reload();
    }

    @FXML
    public void toggleMinimap() {
        graphEditorContainer.getMinimap().visibleProperty().bind(minimapButton.selectedProperty());
    }

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    // 切换分析模式
    public void switchToAnalysisMode() {
        analysisModeButton.setSelected(true);
    }

    // 限定于分析模式
    public void limitedToAnalysisMode() {
//        analysisModeButton.setSelected(true);
//        editModeButton.setDisable(true);
        if (GlobalState.initConfig.getEditModeKey() == 54) {
            MainPaneController.INSTANCE.getTopHalfPane().getLeft().setVisible(false);
            MainPaneController.INSTANCE.getTopHalfPane().getLeft().setManaged(false);
        }
    }


    @SneakyThrows
    private void switchWorkMode(WorkMode targetMode) {
        WorkMode oriMode = GlobalState.currentWorkMode.get();
        // 编辑模式
        if (oriMode != WorkMode.EDIT && oriMode != WorkMode.ORIGINAL) { // 原模式非编辑和原始模式发生切换时，先停止所有活动再行切换
            log.info("切换【编辑模式】。");
            UISchedules.stopUIScheduledTasks();
            UISchedules.stopOriDataViewTimers();
            oriDataViewController.unload();
            stopTitledNodeSkins();
            // step1:
            resetAllProcessors(targetMode, oriMode);
            // step2:
            completeAllClosing();
            // step3, 4:
            syncCloseAllTasks();
            // step5:
            clearAllTriggerConditions();
            // step6:
            ExecutorManager.deallocateExecutors();
        }
        // 分析模式
        else if (targetMode == WorkMode.ANALYSIS) {
            log.info("切换【分析模式】。");
            TaskManager.start();

            allocateExecutorsAndBootProcs();

            startUISchedules();
        }
        // 任务模式
        else if (targetMode == WorkMode.MISSION) {
            log.info("切换【任务模式】。");
        }

        modeLabel.setText(targetMode.getDescription());
        GlobalState.currentWorkMode.set(targetMode);
    }

    private void startUISchedules() {
        UISchedules.startUIScheduledTasks();
        if (tabOriDataView.isSelected())
            UISchedules.startOriDataViewTimers();
    }

    public static void reloadOriDataView(IEventProc eventProc) {
        var controller = INSTANCE.getOriDataViewController();
        if (eventProc != controller.getViewedProc()) {
            controller.reloadIn(eventProc);
        }
    }

    private void allocateExecutorsAndBootProcs() throws Exception {
        // 分配执行器并boot
        for (ProcessorId pId : ProcessorManager.getAllProcessorIds().values()) {
            ExecutorManager.allocateExecutor(pId.getOwner());
        }
        for (ProcessorId pId : ProcessorManager.getAllProcessorIds().values()) {
            pId.getOwner().boot();
        }
    }

    private void clearAllTriggerConditions() {
        /** step5: 清理各processor中的信号触发条件*/
        for (var proc : ProcessorManager.getAllProcessor()) {
            proc.clearTriggerConditions();
        }
    }

    private void syncCloseAllTasks() {
        /**
         * step3: 各源头producer处理CLEAN_PIPELINE信号，确保所有数据流水线上无数据
         * step4: TaskManager以同步方式确保所有task的结束
         * */
        TaskManager.syncCloseTasks(); // 此后关闭TaskManager
    }

    private void completeAllClosing() throws InterruptedException, ExecutionException {
        /** step2: 重新遍历获取各processor的closingFuture并同步，确保每个producer不再产生数据 */
        for (var processor : ProcessorManager.getAllProcessor()) {
            Future closingFuture = processor.getClosingFuture();
            if (closingFuture == null)
                continue;
            if (closingFuture instanceof ChannelFuture chFr) {
                chFr.sync();
            } else {
                closingFuture.get();
            }
        }
    }

    private void resetAllProcessors(WorkMode targetMode, WorkMode oriMode) throws InterruptedException, ExecutionException {
        /** 通过一个线程池来完成所有的{@link AbstractIntegratedProcessor#reset(WorkMode, WorkMode)}
         * 防止其中有阻塞操作使耗时过长*/
        List<Future> futures = new ArrayList<>();
        ExecutorService switchingPool = Executors.newWorkStealingPool(16);
        /** step1: 遍历各processor，异步执行其reset方法，提高关闭工作效率 */
        for (var processor : ProcessorManager.getAllProcessor()) {
            futures.add(
                    switchingPool.submit(
                            // 调用每个processor的resetToSwitchMode，将它们状态归零，其中包括将执行器executor中的任务终结
                            () -> {
                                processor.reset0(oriMode, targetMode);
                                processor.getSubHandlers().forEach(handler -> handler.reset(oriMode, targetMode));
                            }
                    )
            );
        }
        for (Future fr : futures) { // 这步不能省略，否则后续获取的processor中的closingFuture可能尚未被设置
            fr.get();
        }
        switchingPool.shutdown();
    }

    private void stopTitledNodeSkins() {
        var nodeSkins = getNodeSkins();
        for (var entry : nodeSkins.entrySet()) {
            if (entry.getValue() instanceof TitledNodeSkin titledNodeSkin) {
                titledNodeSkin.getContentRoot().stopForEditMode();
            }
        }
    }

    private void initializeMenuBar() {
        final ToggleGroup viewGroup = new ToggleGroup();
        viewGroup.getToggles().addAll(topologyBtn, monitorBtn);
        viewGroup.selectedToggleProperty().addListener((observableValue, oldView, newView) -> {

            if (topologyBtn.equals(newView)) { // 拓扑模式
                log.info("打开【拓扑】视图。");
                GlobalState.recycleRightPaneResources();
                RightPaneController.setTitle(Constants.CONSOLE_TITLE);
                UISchedules.blockMonitoring();
                monitorsManager.putBackTheControls();
                graphEditorContainer.toFront();
            } else if (monitorBtn.equals(newView)) { // 监控模式
                log.info("打开【监控】视图。");
                clearSelection();
                UISchedules.activeMonitoring();
                monitorsManager.shiftTheControls();
                monitorContainer.toFront();
            } else {
                log.error("unknown view!");
            }
        });

        final ToggleGroup workModeGroup = new ToggleGroup();
        workModeGroup.selectedToggleProperty().addListener((observableValue, oldMode, newMode) -> {
            if (newMode != null) {
                if (editModeButton.equals(newMode)) { // 编辑模式
                    editMenu.setDisable(false); // 允许顶栏的"编辑"
                    fileMenu.setDisable(false); // 允许顶栏的“文件”
                    viewGroup.selectToggle(topologyBtn);
                    viewMenu.setDisable(true); // 禁用顶栏"视图"，仅能通过拓扑进行编辑
                    for (MenuItem menuItem : readOnlyMenu.getItems()) { // 取消所有readOnly勾选
                        ((CheckMenuItem) menuItem).setSelected(false);
                    }
                    // 打开左编辑组件栏
                    if (GlobalState.initConfig.getEditModeKey() != 54) {
                        MainPaneController.INSTANCE.getTopHalfPane().getLeft().setVisible(false);
                        MainPaneController.INSTANCE.getTopHalfPane().getLeft().setManaged(false);
                    }
                    switchWorkMode(WorkMode.EDIT);
                    // 清空监控视图
                    monitorsManager.clearAllMonitorTabs();
                    // 进入编辑态时需要让外部窗口获取焦点，否则无法响应“删除”等操作
                    RightPaneController.INSTANCE.getOuterContainers().requestFocus();
                } else { // 非编辑模式
                    editMenu.setDisable(true); // 禁用顶栏的"编辑"
                    viewMenu.setDisable(false);// 允许顶栏"视图"
                    fileMenu.setDisable(true); // 允许顶栏的“文件”
                    for (MenuItem menuItem : readOnlyMenu.getItems()) { // 勾选所有readOnly
                        ((CheckMenuItem) menuItem).setSelected(true);
                    }
                    // 隐藏左编辑组件栏
                    MainPaneController.INSTANCE.getTopHalfPane().getLeft().setVisible(false);
                    MainPaneController.INSTANCE.getTopHalfPane().getLeft().setManaged(false);
                    // 创建监控视图
                    monitorsManager.generateMonitorTabs();

                    if (analysisModeButton.equals(newMode)) { // 分析模式
                        switchWorkMode(WorkMode.ANALYSIS);
                    } else if (missionModeButton.equals(newMode)) { // 任务模式
                        switchWorkMode(WorkMode.MISSION);
                    } else {
                        log.error("undefined work mode!");
                    }
                }
            }
        });
        workModeGroup.getToggles().addAll(editModeButton, analysisModeButton, missionModeButton);

        final ToggleGroup skinGroup = new ToggleGroup();
        skinGroup.getToggles().addAll(defaultSkinButton, treeSkinButton, titledSkinButton);

        final ToggleGroup connectionStyleGroup = new ToggleGroup();
        connectionStyleGroup.getToggles().addAll(gappedStyleButton, detouredStyleButton);

        final ToggleGroup connectorTypeGroup = new ToggleGroup();
        connectorTypeGroup.getToggles().addAll(inputConnectorTypeButton, outputConnectorTypeButton);

        final ToggleGroup positionGroup = new ToggleGroup();
        positionGroup.getToggles().addAll(leftConnectorPositionButton, rightConnectorPositionButton);
        positionGroup.getToggles().addAll(topConnectorPositionButton, bottomConnectorPositionButton);

        graphEditor.getProperties().gridVisibleProperty().bind(showGridButton.selectedProperty());
        graphEditor.getProperties().snapToGridProperty().bind(snapToGridButton.selectedProperty());

        for (final EditorElement type : EditorElement.values())
        {
            final CheckMenuItem readOnly = new CheckMenuItem(type.name());
            graphEditor.getProperties().readOnlyProperty(type).bind(readOnly.selectedProperty());
            readOnlyMenu.getItems().add(readOnly);
        }

        minimapButton.setGraphic(AwesomeIcon.MAP.node());

        final SetChangeListener<? super EObject> selectedNodesListener = change -> checkConnectorButtonsToDisable();
        graphEditor.getSelectionManager().getSelectedItems().addListener(selectedNodesListener);
        checkConnectorButtonsToDisable();
    }

    private void addActiveSkinControllerListener() {

        activeSkinController.addListener((observable, oldValue, newValue) -> {
            handleActiveSkinControllerChange();
        });
    }

    private void handleActiveSkinControllerChange() {

        if (treeSkinController.equals(activeSkinController.get())) {

            graphEditor.setConnectorValidator(new TreeConnectorValidator());
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_TITLED_SKINS);
            treeSkinButton.setSelected(true);

        } else if (titledSkinController.equals(activeSkinController.get())) {

            graphEditor.setConnectorValidator(null);
            if (!graphEditor.getView().getStyleClass().contains(STYLE_CLASS_TITLED_SKINS)) {
                graphEditor.getView().getStyleClass().add(STYLE_CLASS_TITLED_SKINS);
            }
            titledSkinButton.setSelected(true);

        } else {

            graphEditor.setConnectorValidator(null);
            graphEditor.getView().getStyleClass().remove(STYLE_CLASS_TITLED_SKINS);
            defaultSkinButton.setSelected(true);
        }

        clearAll();
        flushCommandStack();
        checkConnectorButtonsToDisable();
        selectionCopier.clearMemory();
    }

    private void checkSkinType() {

        if (!graphEditor.getModel().getNodes().isEmpty()) {

            final GNode firstNode = graphEditor.getModel().getNodes().get(0);
            final String type = firstNode.getType();

            if (TreeSkinConstants.TREE_NODE.equals(type)) {
                activeSkinController.set(treeSkinController);
            } else if (TitledSkinConstants.TITLED_NODE.equals(type)) {
                activeSkinController.set(titledSkinController);
            } else {
                activeSkinController.set(defaultSkinController);
            }
        }
    }

    private void checkConnectorButtonsToDisable() {

		final boolean nothingSelected = graphEditor.getSelectionManager().getSelectedItems().stream()
				.noneMatch(e -> e instanceof GNode);

        final boolean treeSkinActive = treeSkinController.equals(activeSkinController.get());
        final boolean titledSkinActive = titledSkinController.equals(activeSkinController.get());

        if (titledSkinActive || treeSkinActive) {
            addConnectorButton.setDisable(true);
            clearConnectorsButton.setDisable(true);
            connectorTypeMenu.setDisable(true);
            connectorPositionMenu.setDisable(true);
        } else if (nothingSelected) {
            addConnectorButton.setDisable(true);
            clearConnectorsButton.setDisable(true);
            connectorTypeMenu.setDisable(false);
            connectorPositionMenu.setDisable(false);
        } else {
            addConnectorButton.setDisable(false);
            clearConnectorsButton.setDisable(false);
            connectorTypeMenu.setDisable(false);
            connectorPositionMenu.setDisable(false);
        }

        intersectionStyle.setDisable(treeSkinActive);
    }

    private void flushCommandStack() {

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(graphEditor.getModel());
        if (editingDomain != null) {
            editingDomain.getCommandStack().flush();
        }
    }

    private Side getSelectedConnectorPosition() {
        if (leftConnectorPositionButton.isSelected()) {
            return Side.LEFT;
        } else if (rightConnectorPositionButton.isSelected()) {
            return Side.RIGHT;
        } else if (topConnectorPositionButton.isSelected()) {
            return Side.TOP;
        } else {
            return Side.BOTTOM;
        }
    }

    public void openExcelDir(ActionEvent event) {
        try {
            Desktop.getDesktop().open(new File(EXCEL_DIR_PATH));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void openTableTXTDir(ActionEvent event) {
        try {
            Desktop.getDesktop().open(new File(TABLE_TXT_DIR_PATH));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void openVisDir(ActionEvent event) {
        try {
            Desktop.getDesktop().open(new File(VISUALIZATION_OUTPUT_PATH));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
