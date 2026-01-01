package sy.databus.view.logger;

import javafx.scene.control.TextArea;
import lombok.extern.log4j.Log4j2;

/** 日志显示控件，每次最多显示{@link #ROW_MAX_COUNT}行*/
@Log4j2
public class LogView extends TextArea {
    public static final int ROW_MAX_COUNT = 80;

    private int rowCnt = 0;

    public LogView() {
        setEditable(false);
        setWrapText(true);
        getStyleClass().add("logView");
    }

    public void addFirstLine (String line) {
        insertText(0, line);
    }

    public void addFirstLogLine(LogBuilder logBuilder) {
        rowCnt += logBuilder.getAppendingCount();
        if (rowCnt > ROW_MAX_COUNT) {
            rowCnt = logBuilder.getAppendingCount();
            clear();
            appendText(logBuilder.takeString());
        } else {
            addFirstLine(logBuilder.takeString());
        }
    }

}
