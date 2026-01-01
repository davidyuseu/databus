package sy.databus;

import javafx.application.Platform;
import lombok.Getter;
import lombok.Setter;
import sy.databus.OriDataViewController;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OriDataViewTask extends UITask{

    public static final int COUNT_ORI_DATA_VIEW = 20;

    @Getter
    private OriDataViewController viewController;

    public OriDataViewTask(OriDataViewController vController) {
        this.viewController = vController;
        task = () -> {
            synchronized (viewController.getViewLocker()) {
                var handler = viewController.getDataViewHandler();
                List<String> msgs = null;
                if (handler != null) {
                    msgs = handler.getListAndClear();
                } else {
                    return;
                }
                if (msgs != null && !msgs.isEmpty()) {
                    List<String> finalMsgs = msgs;
                    Platform.runLater(() -> {
                        var obList = viewController.getDataListView().getItems();
                        if (finalMsgs.size() + obList.size() > COUNT_ORI_DATA_VIEW) {
                            obList.clear();
                        }
                        obList.addAll(finalMsgs);
                    });
                }
            }
        };
    }

}
