package sy.databus.view.watch;

import sy.databus.process.ProcessorInitException;

import static sy.databus.view.customskins.TitledSkinController.DEFAULT_TITLED_NODE_HEIGHT;

@WatchPaneConfig(initialHeight = DEFAULT_TITLED_NODE_HEIGHT)
public class KWHDevSelectorWatchPane extends MessageSeriesProcessorWatchPane {

    @Override
    protected void checkProcessorType() {
        if (!(integratedProcessor instanceof KWHDevSelector))
            throw new ProcessorInitException("'" + MessageSeriesProcessorWatchPane.class.getSimpleName()
                    + "' must adapt '"
                    + KWHDevSelector.class.getSimpleName() + "'!");
    }
}
