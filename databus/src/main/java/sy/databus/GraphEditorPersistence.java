package sy.databus;

import javafx.scene.Scene;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import lombok.extern.log4j.Log4j2;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.domain.EditingDomain;
import sy.databus.global.ProcessorSJsonUtil;
import sy.databus.view.customskins.titled.TitledSkinConstants;
import sy.grapheditor.api.GraphEditor;
import sy.grapheditor.model.GModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;

@Log4j2
public class GraphEditorPersistence {

    private static final String FILE_EXTENSION = ".graph"; //$NON-NLS-1$
    private static final String CHOOSER_TEXT = "Graph Model Files (*" + FILE_EXTENSION + ")"; //$NON-NLS-1$ //$NON-NLS-2$

    private static final String SAMPLE_FILE = "/sample/sample" + FILE_EXTENSION;
    private static final String SAMPLE_FILE_LARGE = "/sample/sample-large" + FILE_EXTENSION; //$NON-NLS-1$
    private static final String TREE_FILE = "/sample/tree" + FILE_EXTENSION; //$NON-NLS-1$
    public static final String TITLED_FILE = "/sample/titled" + FILE_EXTENSION; //$NON-NLS-1$

    private File initialDirectory = null;

    /**
     * Saves the graph editor's {@link GModel} state to an XML file via the {@link FileChooser}.
     *
     * @param graphEditor the graph editor whose model state is to be saved
     */
    public void saveToFile(final GraphEditor graphEditor) {

        final Scene scene = graphEditor.getView().getScene();

        if (scene != null) {

            final File file = showFileChooser(scene.getWindow(), true);

            if (file != null && graphEditor.getModel() != null) {
                saveModel(file, graphEditor.getModel());
            }
        }
    }

    public File loadFromFile(final GraphEditor graphEditor) throws IOException {

        final Scene scene = graphEditor.getView().getScene();

        if (scene != null) {

            final File file = showFileChooser(scene.getWindow(), false);

            if (file != null) {
                loadModel(file, graphEditor);
                return file;
            }
        }
        return null;
    }

    public void loadFile(final GraphEditor graphEditor, File file) throws IOException {
        if (file != null && file.length() > 0) {
            loadModel(file, graphEditor);
        }
    }

    public void loadSample(final GraphEditor graphEditor) {
        loadSample(SAMPLE_FILE, graphEditor);
    }

    public void loadProject(String path, final GraphEditor graphEditor) throws IOException {
        loadSample(path, graphEditor);
//        loadModel(new File(path), graphEditor);
    }


    public void loadSampleLarge(final GraphEditor graphEditor) {
        loadSample(SAMPLE_FILE_LARGE, graphEditor);
    }

    public void loadTree(final GraphEditor graphEditor) {
        loadSample(TREE_FILE, graphEditor);
    }

    public void loadTitled(final GraphEditor graphEditor) {
        loadSample(TITLED_FILE, graphEditor);
    }

    private void loadSample(final String file, final GraphEditor graphEditor) {

        Path baseDir = Paths.get(System.getProperty("user.dir"));
        Path filePath = baseDir.resolve(file);

        final URI fileUri = URI.createFileURI(filePath.toString());
        loadUri(graphEditor, fileUri);
    }

    private void loadUri(GraphEditor graphEditor, URI fileUri) {
        final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
        final Resource resource = resourceFactory.createResource(fileUri);

        try {
            resource.load(Collections.EMPTY_MAP);
        } catch (final IOException e) {
            log.error(e.getMessage());
        }

        if (!resource.getContents().isEmpty() && resource.getContents().get(0) instanceof GModel) {
            final GModel model = (GModel) resource.getContents().get(0);
            graphEditor.setModel(model);
        }
    }

    private File showFileChooser(final Window window, final boolean save) {

        final FileChooser fileChooser = new FileChooser();

        final FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(CHOOSER_TEXT, "*" + FILE_EXTENSION); //$NON-NLS-1$
        fileChooser.getExtensionFilters().add(filter);

        if (initialDirectory != null && initialDirectory.exists()) {
            fileChooser.setInitialDirectory(initialDirectory);
        }

        if (save) {
            return fileChooser.showSaveDialog(window);
        }
        // ELSE:
        return fileChooser.showOpenDialog(window);
    }

    public void saveModel(final File file, final GModel model) {

        String absolutePath = file.getAbsolutePath();
        if (!absolutePath.endsWith(FILE_EXTENSION)) {
            absolutePath += FILE_EXTENSION;
        }

        model.getNodes().stream().forEach(gNode -> {
            if (gNode != null && TitledSkinConstants.TITLED_NODE.equals(gNode.getType())) {
                gNode.setProcessorJson(
                        ProcessorSJsonUtil.objToStr(
                                TitledGNodeAttachmentUtil.getTitledGNodeProcessor(gNode)));
            } else {
                log.warn("An exception occur in the saving!");
            }
        });

        final EditingDomain editingDomain = AdapterFactoryEditingDomain.getEditingDomainFor(model);

        final URI fileUri = URI.createFileURI(absolutePath);
        final XMIResourceFactoryImpl resourceFactory = new XMIResourceFactoryImpl();
        final Resource resource = resourceFactory.createResource(fileUri);
        resource.getContents().add(model);

        try {
            resource.save(Collections.EMPTY_MAP);
        } catch (final IOException e) {
            log.error(e);
        }

        editingDomain.getResourceSet().getResources().clear();
        editingDomain.getResourceSet().getResources().add(resource);

        initialDirectory = file.getParentFile();
    }

    public void loadModel(final File file, final GraphEditor graphEditor) throws IOException {

        final URI fileUri = URI.createFileURI(file.getAbsolutePath());

        loadUri(graphEditor, fileUri);
        initialDirectory = file.getParentFile();
    }
}
