package sy.common.util;

import lombok.extern.log4j.Log4j2;

import java.io.File;

@Log4j2
public class SFileUtils {
    public static String removeSuffix(String fileName){
        int pos = fileName.lastIndexOf('.');
        if(pos > -1){
            return fileName.substring(0, pos);
        }else{
            return fileName;
        }
    }

    public static void deleteFiles(File dir) {
        if (!dir.exists())
            return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (var file : files) {
                    deleteFiles(file);
                }
            }
        } else {
            dir.delete();
        }
    }
}
