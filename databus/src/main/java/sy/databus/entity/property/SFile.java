package sy.databus.entity.property;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.SneakyThrows;
import sy.databus.process.AbstractHandler;
import sy.databus.process.AbstractIntegratedProcessor;

import java.io.File;
import java.util.Objects;

/**
 * controller: {@link sy.databus.view.controller.SFileController}
 * */
public class SFile extends File {
    public static File DEFAULT_EMPTY_FILE = new File(".\\res\\Empty.t");

    public static File RES_DIR = new File(".\\res");

    @Getter
    private boolean fileFlag;
    @Getter
    private boolean directoryFlag;

    private static final String DEFAULT_DESCRIPTION = "文件类型";
    @Getter
    private String description;

    private static final String DEFAULT_EXTENSION = "*";
    @Getter
    private String[] extensions;
    @Getter @Setter
    private boolean valid = false;
    @Getter @Setter
    private boolean actionOnce = false;

    @SneakyThrows
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof File file)
            return this.getCanonicalFile().equals(file.getCanonicalFile());
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @FunctionalInterface
    public interface Action<T> {
        void changed(T t) throws Exception;
    }
    /**
     * 可设置当前选择的file改变时，发生的动作
     * ps: 一般在{@link AbstractIntegratedProcessor} 或 {@link AbstractHandler} 中的initialize()方法中设置。
     * */
    @Getter @Setter
    private Action<File> fileChangedAction;
    /** 设置Action的同时尝试执行一次，一般在processor中的initialize方法中调用*/
    public void setFileChangedActionAndDoOnce(@NonNull Action<File> action) throws Exception {
        this.fileChangedAction = action;
        if (!this.equals(SFile.DEFAULT_EMPTY_FILE))
            action.changed(this);
    }

    // 创建文件
    @SneakyThrows
    public static SFile buildFile(File file,
                                  String description,
                                  Action<File> fileChangedAction,
                                  boolean valid,
                                  boolean actionOnce,
                                  String... extensions) {
        if (file != null && file.isFile()) {
            SFile sFile = new SFile(file, "", description, fileChangedAction, valid, actionOnce, extensions);
            sFile.fileFlag = true;
            return sFile;
        } else {
            throw new PropertyException("Failed to initialize property, The file must be a real file!");
        }
    }

    // 创建无事件动作的SFile
    public static SFile buildFile(File file, String description, String... extensions) {
        return buildFile(file, description, null, false, false, extensions);
    }

    public static SFile buildFile(File file, String description, boolean valid, boolean actionOnce, String... extensions) {
        return buildFile(file, description, null, valid, actionOnce, extensions);
    }


    public static SFile buildDefaultFile(String... extensions) {
        return buildDefaultFile(null, extensions);
    }

    @SneakyThrows
    public static SFile buildDefaultFile(Action<File> fileChangedAction, String... extensions) {
        File tFile = DEFAULT_EMPTY_FILE;
        if (!tFile.exists()) {
            tFile.createNewFile(); // TODO 在linux下考虑写权限问题
        }
        return buildFile(tFile, DEFAULT_DESCRIPTION, fileChangedAction, false, false, extensions);
    }

    // 创建目录
    @SneakyThrows
    public static SFile buildDirectory(File dir, Action<File> fileChangedAction, boolean valid, boolean actionOnce) {
        if (dir != null && dir.isDirectory()) {
            SFile sDir = new SFile(dir, "", fileChangedAction, valid, actionOnce);
            sDir.directoryFlag = true;
            return sDir;
        } else {
            throw new PropertyException("Failed to initialize property, The file must be a directory!");
        }
    }

    public static SFile buildDirectory(File dir) {
        return buildDirectory(dir, null, false, false);
    }

    public static SFile buildDirectory(File dir, boolean valid, boolean actionOnce) {
        return buildDirectory(dir, null, valid, actionOnce);
    }

    public SFile buildDefaultDirectory() {
        return buildDirectory(new File(".\\"), null, false, false);
    }

    // directory构造器
    private SFile(File parent, String child, Action<File> fileChangedAction, boolean valid, boolean actionOnce) {
        super(parent, child);
        this.fileChangedAction = fileChangedAction;
        this.valid = valid;
        this.actionOnce = actionOnce;
    }

    // file构造器
    private SFile(File parent, String child, String description, Action<File> fileChangedAction, boolean valid, boolean actionOnce, String[] extensions) {
        super(parent, child);
        this.fileChangedAction = fileChangedAction;
        this.description = description;
        this.extensions = extensions;
        this.valid = valid;
        this.actionOnce = actionOnce;
    }

}
