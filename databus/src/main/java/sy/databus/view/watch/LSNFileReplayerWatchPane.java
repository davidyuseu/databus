package sy.databus.view.watch;

import lombok.extern.log4j.Log4j2;
import sy.databus.entity.ReplayFileItem;

@Log4j2
@WatchPaneConfig(initialHeight = 360.0, initialWidth = 388.0)
public class LSNFileReplayerWatchPane extends FileReplayerWatchPane {

    @Override
    protected ReplayFileItem organizeFileInfo(ReplayFileItem fileItem) {
        try {
            fileItem.setStartTime(fileItem.getName().substring(9, 23));
            fileItem.setEndTime(fileItem.getName().substring(24, 38));
        } catch (Exception e) {
            log.error("Failed to organize the file info of '{}'({})!", fileItem.getName(), e.getMessage());
        }
        return fileItem;
    }
}
