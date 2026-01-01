package sy.databus.view.watch;

import sy.databus.process.ProcessorInitException;
import sy.databus.process.analyse.LSNDevSelector;

import static sy.databus.view.customskins.TitledSkinController.DEFAULT_TITLED_NODE_HEIGHT;

@WatchPaneConfig(initialHeight = DEFAULT_TITLED_NODE_HEIGHT)
public class LSNDevSelectorWatchPane extends MessageSeriesProcessorWatchPane {
    @Override
    protected void checkProcessorType() {
        if (!(integratedProcessor instanceof LSNDevSelector))
            throw new ProcessorInitException("'" + MessageSeriesProcessorWatchPane.class.getSimpleName()
                    + "' must adapt '"
                    + LSNDevSelector.class.getSimpleName() + "'!");
    }
}
