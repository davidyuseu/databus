module common {
    requires transitive javafx.base;
    requires lombok;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.dataformat.yaml;
    requires org.yaml.snakeyaml;
    requires org.slf4j;
    requires org.apache.logging.log4j;
    requires transitive io.netty.buffer;
    requires transitive io.netty.common;
    requires com.fasterxml.jackson.dataformat.xml;


    exports sy.common.concurrent.vector;
    exports sy.common.concurrent.queue;
    exports sy.common.util;
    exports sy.common.fx.ui;
    exports sy.common.cache;
    exports sy.common.util.jackson;
//    exports sy.common.tmresolve.charNum;
//    exports sy.common.tmresolve.gen.vector;
//    exports sy.common.tmresolve.gen;
//    exports sy.common.tmresolve;

    opens sy.common.tmresolve.charNum;

}