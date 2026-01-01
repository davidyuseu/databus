package sy.grapheditor.core.view.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sy.grapheditor.api.GConnectionSkin;
import sy.grapheditor.api.SkinLookup;
import sy.grapheditor.api.VirtualSkin;
import sy.grapheditor.core.DefaultGraphEditor;
import sy.grapheditor.core.view.ConnectionLayouter;
import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GModel;
import javafx.geometry.Point2D;


/**
 * {@link ConnectionLayouter} 的默认实现
 */
public class DefaultConnectionLayouter implements ConnectionLayouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultConnectionLayouter.class);

    private final Set<GConnection> mDirty = new HashSet<>();
    private boolean mRedrawAll = false;
    private final Map<GConnectionSkin, Point2D[]> mConnectionPoints = new HashMap<>();
    private final SkinLookup mSkinLookup;
    private GModel mModel;

    /**
     * 创建一个新的 {@link DefaultConnectionLayouter} 实例。
     * 每个 {@link DefaultGraphEditor} 实例应该只存在一个实例。
     *
     * @param pSkinLookup 用于查找skin的 {@link SkinLookup}
     */
    public DefaultConnectionLayouter(final SkinLookup pSkinLookup) {
        mSkinLookup = pSkinLookup;
    }

    @Override
    public void initialize(final GModel pModel) {
        mModel = pModel;
    }

    @Override
    public void redraw(final Collection<GConnection> pConnections) {
        mDirty.addAll(pConnections);
    }

    @Override
    public void redraw(final GConnection pConnection) {
        mDirty.add(pConnection);
    }

    @Override
    public void redrawAll() {
        mRedrawAll = true;
    }

    @Override
    public void draw() {
        if (mModel == null || mModel.getConnections().isEmpty()) {
            return;
        }

        try {
            if (mRedrawAll) {
                mConnectionPoints.clear();
                if (!mModel.getConnections().isEmpty()) {
                    redrawAllConnections();
                }
                mRedrawAll = false;
            } else if (!mDirty.isEmpty()) {
                final List<GConnectionSkin> repaint = new ArrayList<>(mDirty.size());
                for (final GConnection conn : mDirty) {
                    final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(conn);
                    if (connectionSkin != null && !(connectionSkin instanceof VirtualSkin)) {
                        final Point2D[] points = connectionSkin.update();
                        if (points != null) {
                            mConnectionPoints.put(connectionSkin, points);
                        }

                        repaint.add(connectionSkin);
                    }
                }

                for (final GConnectionSkin skin : repaint) {
                    skin.draw(mConnectionPoints);
                }
                mDirty.clear();
            }

        } catch (Exception e) {
            LOGGER.debug("无法重绘连接：{} ", e); //$NON-NLS-1$
        }
    }

    private void redrawAllConnections() {
        for (final GConnection connection : mModel.getConnections()) {
            final GConnectionSkin connectionSkin = mSkinLookup.lookupConnection(connection);
            if (connectionSkin != null) {
                final Point2D[] points = connectionSkin.update();
                if (points != null) {
                    mConnectionPoints.put(connectionSkin, points);
                }
            }
        }

        for (final GConnectionSkin skin : mConnectionPoints.keySet()) {
            skin.draw(mConnectionPoints);
        }
    }
}
