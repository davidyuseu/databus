package sy.databus.process.analyse;

import sy.common.concurrent.vector.SSyncObservableList;

public interface ParamsCorrelating {

    void setRemoteList(SSyncObservableList remoteList);

    void unbindToRemoteList(SSyncObservableList remoteList);
}
