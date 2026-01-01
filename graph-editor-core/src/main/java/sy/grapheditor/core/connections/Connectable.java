package sy.grapheditor.core.connections;

import lombok.NonNull;
import sy.grapheditor.model.GNode;

public interface Connectable {
    boolean validateAsOutput(@NonNull GNode nextNode);

    boolean validateAsInput(@NonNull GNode parentNode);

    void connectedAsOutput(@NonNull GNode nextNode);

    void connectedAsInput(@NonNull GNode parentNode);

    void detachedAsOutput(@NonNull GNode nextNode);

    void detachedAsInput(@NonNull GNode parentNode);
}
