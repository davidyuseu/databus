package sy.databus;

import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.core.util.CachedClock;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import static sy.databus.global.Constants.VISUALIZATION_OUTPUT_PATH;
import static sy.databus.global.Constants.gCharset;

@Log4j2
public class Visualization {
    private static final DateFormat dateFormat2Mn4File = new SimpleDateFormat("yyyyMMdd_HH_mm_ss_SSS", Locale.CHINA);
    
    private static final String jsSrc = ".\\plotly-latest.min[1].js";

    private static String HTML_OUTPUT_PART1 =
            "<!DOCTYPE html>\n" +
                    "<html lang=\"zh-CN\">\n" +
                    "<head>\n" +
                    "    <meta charset=\"GBK\">\n" +
                    "    <title>参数解析结果</title>\n" +
                    "    <script src=\"#jsSrc\"></script>\n" +
                    "</head>\n" +
                    "<body>\n";
    private static String HTML_OUTPUT_PART2 =
                    "    <div id='#target' ></div>\n" +
                    "        <script>\n" +
                    "        var #target_#target = document.getElementById('#target');\n" +
                    "        var layout = {\n" +
                    "    title: '#Example',\n" +
                    "    height: 600,\n" +
                    "    width: 1080,\n" +
                    "    xaxis: {\n" +
                    "    title: 'Time',\n" +
                    "    },\n" +
                    "\n" +
                    "    yaxis: {\n" +
                    "    title: 'Values',\n" +
                    "    },\n" +
                    "\n" +
                    "};\n" +
                    "\n" +
                    "var trace0 =\n" +
                    "{";
    private static String HTML_OUTPUT_PART4 =
            "mode: 'lines',\n" +
                    "xaxis: 'x',\n" +
                    "yaxis: 'y',\n" +
                    "type: 'scatter',\n" +
                    "name: '',\n" +
                    "};\n" +
                    "\n" +
                    "        var data = [ trace0];\n" +
                    "Plotly.newPlot(#target_#target, data, layout);            </script>\n";
    private static String HTML_OUTPUT_PART5 =
            "</body>\n" +
                    "</html>";
    private static final String TITLE = "#Example"; // 替换为参数名
    private static final String JS_SOURCE = "#jsSrc"; // 替换为js脚本位置
    private static final String VAR_TARGET = "#target"; // 替换为target + 当前参数索引号
    private static final String VAR_TARGET1 = "target";

    public static File outputVisHtml(List<String> params, List<Integer> indices, File dataFile) {
        File output = new File(VISUALIZATION_OUTPUT_PATH
                + dateFormat2Mn4File.format(CachedClock.instance().currentTimeMillis()) + ".html");
        FileWriter fileWriter;
        try {
            output.createNewFile();
            fileWriter = new FileWriter(output, gCharset);
            File js = new File(jsSrc);
            String jsPath = js.getAbsolutePath();
            jsPath = jsPath.replace("\\","\\\\");
            String part1 = HTML_OUTPUT_PART1.replaceAll(JS_SOURCE, jsPath);
            fileWriter.write(part1);
            StringBuilder time = new StringBuilder("x: [");
            StringBuilder values = new StringBuilder("y: [");
            for (int i = 0; i < params.size(); i++) {
                time.setLength(4);
                values.setLength(4);
                String part2 = HTML_OUTPUT_PART2.replaceAll(TITLE, params.get(i)).replaceAll(VAR_TARGET, VAR_TARGET1 + i);
                fileWriter.write(part2);
                try (BufferedReader br = new BufferedReader(new FileReader(dataFile, gCharset))) {
                    br.readLine();
                    String line;
                    while ((line = br.readLine()) != null) {
                        String[] items = line.split("\t");
                        time.append("\"").append(items[0]).append("\",");
                        values.append("\"").append(items[indices.get(i)]).append("\",");
                    }
                    time.setLength(time.length() - 1);
                    time.append("],");
                    values.setLength(values.length() - 1);
                    values.append("],");
                    fileWriter.write(time.toString());
                    fileWriter.write(values.toString());
                    fileWriter.flush();
                }
                String part4 = HTML_OUTPUT_PART4.replaceAll(VAR_TARGET, VAR_TARGET1 + i);
                fileWriter.write(part4);
            }
            fileWriter.write(HTML_OUTPUT_PART5);
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return output;
    }
}
