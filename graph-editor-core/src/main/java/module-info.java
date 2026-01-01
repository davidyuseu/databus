module org.cetc.dataproc.core
{
    requires javafx.base;
    requires transitive org.cetc.dataproc.model;
    requires org.cetc.dataproc.api;
    requires transitive org.eclipse.emf.ecore.xmi;
    requires org.eclipse.emf.edit;
    requires org.eclipse.emf.ecore;
    requires transitive org.eclipse.emf.common;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires lombok;

    exports sy.grapheditor.core;
    exports sy.grapheditor.core.connections;
    exports sy.grapheditor.core.connectors;
    exports sy.grapheditor.core.skins;
    exports sy.grapheditor.core.skins.defaults;
    exports sy.grapheditor.core.skins.defaults.connection;
    exports sy.grapheditor.core.skins.defaults.connection.segment;
    exports sy.grapheditor.core.skins.defaults.tail;
    exports sy.grapheditor.core.view;
    exports sy.grapheditor.core.skins.defaults.utils;
}
