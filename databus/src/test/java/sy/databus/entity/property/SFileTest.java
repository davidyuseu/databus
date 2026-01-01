package sy.databus.entity.property;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class SFileTest {

    @Test
    void equals() {
        SFile sFile = SFile.buildFile(new File("e:/Link_LLZT.ini"),"file", "*.ini");

        File file = new File("e:/Link_LLZT.ini");

        System.out.println(file.equals(sFile));
    }

}