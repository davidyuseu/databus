module org.cetc.dataproc.api
{
    requires transitive org.cetc.dataproc.model;
    requires org.eclipse.emf.common;
    requires org.eclipse.emf.edit;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires transitive javafx.graphics;
    requires transitive javafx.controls;
    requires lombok;


    exports sy.grapheditor.api.utils;
    exports sy.grapheditor.api.window;
    exports sy.grapheditor.api;
}
