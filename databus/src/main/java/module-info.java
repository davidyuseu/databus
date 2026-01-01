module databus {
    requires javafx.fxml;
    requires org.eclipse.emf.common;
    requires org.eclipse.emf.ecore;
    requires org.eclipse.emf.edit;
    requires org.cetc.dataproc.api;
    requires org.cetc.dataproc.core;

    requires tablefax;
    requires common;

    requires lombok;
    requires org.yaml.snakeyaml;
    requires com.fasterxml.jackson.databind;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires org.reflections;
    requires com.google.common;
    requires io.netty.transport;
    requires java.desktop;
    requires org.apache.logging.log4j.core;
    requires java.sql;
    requires java.sql.rowset;
    requires DmJdbcDriver18;

    exports sy.databus;
    exports sy.databus.process;
    exports sy.databus.view.customskins.titled;
    exports sy.databus.organize;
    exports sy.databus.entity.message;

    opens sy.databus.process to com.fasterxml.jackson.databind;
    opens sy.databus.process.frame to com.fasterxml.jackson.databind;
    exports sy.databus.view.watch;

    exports sy.databus.process.frame.handler.decoder to com.fasterxml.jackson.databind;
    exports sy.databus.global;
    opens sy.databus.global to javafx.fxml, javafx.graphics;
    exports sy.databus.entity.signal;
    opens sy.databus.entity.signal to com.fasterxml.jackson.databind;
    exports sy.databus.entity;
    exports sy.databus.process.fsm;
    opens sy.databus.process.fsm to com.fasterxml.jackson.databind;
    exports sy.databus.process.fsm.producer;
    opens sy.databus.process.fsm.producer to com.fasterxml.jackson.databind;
    exports sy.databus.entity.property;
    opens sy.databus.entity.property to com.fasterxml.jackson.databind;
    exports sy.databus.process.analyse;
    opens sy.databus.process.analyse;
    exports sy.databus.process.dev to com.fasterxml.jackson.databind;
    opens sy.databus.process.dev;
    opens sy.databus.organize;
    opens sy.databus.entity;
    exports sy.databus.projects.jz005;
    opens sy.databus.projects.jz005;
    exports sy.databus.process.frame.handler.filter to com.fasterxml.jackson.databind;
    exports sy.databus.view.monitor;
    opens sy.databus.view.monitor to javafx.fxml, javafx.graphics;
    opens sy.databus.organize.monitor to javafx.base;
    opens sy.databus;
    exports sy.databus.projects.cy9;
    opens sy.databus.projects.cy9;
    exports sy.databus.process.frame.handler.common;
    opens sy.databus.process.frame.handler.common;
    exports sy.databus.projects.xxpt to com.fasterxml.jackson.databind;
    exports sy.databus.process.frame;
    exports sy.databus.projects.y3500 to com.fasterxml.jackson.databind;

}
