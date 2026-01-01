module tablefax {
    requires org.apache.commons.collections4;
    requires commons.csv;
    requires lombok;
    requires poi;
    requires java.xml;
    requires poi.ooxml;
    requires org.slf4j;
    requires poi.ooxml.schemas;
    requires ehcache;
    requires cglib;
    requires org.apache.commons.compress;

    exports sy.tablefax;
    exports excel.write.metadata;
    exports excel;
    exports excel.support;
}