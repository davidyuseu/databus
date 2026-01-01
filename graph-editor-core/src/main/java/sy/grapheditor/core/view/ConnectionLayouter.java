/*
 * Copyright (C) 2005 - 2014 by TESIS DYNAware GmbH
 */
package sy.grapheditor.core.view;

import java.util.Collection;

import sy.grapheditor.model.GConnection;
import sy.grapheditor.model.GModel;

/**
 * 负责通知connection skins绘制自己。
 */
public interface ConnectionLayouter
{

    /**
     * 初始化给定模型的连接布局管理器。
     *
     * @param pModel
     *            当前正在编辑的 {@link GModel}
     */
    void initialize(final GModel pModel);

    /**
     * 根据最新的布局值绘制所有连接。
     */
    void draw();

    /**
     * 将完整的连接布局标记为脏。
     * 下一个布局过程将重新布局所有连接（在 {@link #draw()} 期间）。
     *
     */
    void redrawAll();

    /**
     * 根据最新的布局值重绘给定的连接。
     *
     * @param pConnection
     *            待重绘的连接
     */
    void redraw(final GConnection pConnection);

    /**
     * 根据最新的布局值重绘给定的各连接。
     *
     * @param pConnections
     *            待重绘的连接
     */
    void redraw(final Collection<GConnection> pConnections);
}
