package other;

import excel.ExcelWriter;
import excel.support.ExcelTypeEnum;
import excel.write.metadata.WriteSheet;
import excel.write.metadata.WriteWorkbook;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CSVOperatorTemp {
    private WriteSheet csvSheet = new WriteSheet();
    private WriteWorkbook workbook = new WriteWorkbook();

    @Setter(AccessLevel.NONE)
    private int columCount = 0;//当前data有多少行

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private OutputStream outToCSV;

    @Getter(AccessLevel.NONE) @Setter(AccessLevel.NONE)
    private ExcelWriter csvWriter;

    private List<ArrayList<String>> csvData = new ArrayList<>();

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DateFormat dateFormatForCsvFile = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public CSVOperatorTemp() {
        workbook.setExcelType(ExcelTypeEnum.XLSX);
    }

    public synchronized void createParaTableCSV(int columnCount){
        if(columCount > 0){
//            writeToCSVFile();
        }
//        if(Global.editMode.get() == 1){
//            Global.msgInMain0.set("编辑模式下无法创建csv");
//            return;
//        }else{
//            Global.msgInMain0.set("");
//        }
        //创建文件
        String csvName = ".\\csv\\"
                + "ParamTable_"
                + dateFormatForCsvFile.format(new Date())
                + ".csv";
        try {
            outToCSV = new FileOutputStream(csvName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        workbook.setOutputStream(outToCSV);

//        if(csvWriter == null)//每次必须创建新的ExcelWriter，否则finish()会报异常
        csvWriter = new ExcelWriter(workbook);
        csvSheet.setSheetName("sheet1");

        //创建表头
        List<List<String>> head = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            ArrayList<String> headColumn = new ArrayList<>();
            headColumn.add("时标" + i);
            head.add(headColumn);
        }

        csvSheet.setHead(head);
    }

    public synchronized void addParaItemToCSVData(){
        ArrayList<String> columnItem = new ArrayList<>();
        columnItem.add(dateFormat.format(new Date())); // 第一列添加当前的时间信息
//        for (int i = 0; i < Global.sumTable.size(); i++) {
//            PARA_RESULT_T para_result = Global.sumTable.get(i).getParaResult();
//            long crntTime = System.currentTimeMillis();
//            if(crntTime - para_result.getRefreshTime() < 2100) {//判断当前参数是否超时
//                columnItem.add(Global.sumTable
//                        .get(i).getParaResult().getCharResult());
//            }else{
//                columnItem.add("null"); //若超时，在csv中写"null"
//            }
//        }
        csvData.add(columnItem);
        columCount++;
    }

    public synchronized void addColumnToCSVData(ArrayList<String> column) {
        csvData.add(column);
        columCount++;
    }

    public void writeData(List data) {
        if(csvWriter != null || outToCSV != null) {
            csvWriter.write(data, csvSheet);
        }
    }

    public void finish() {
        csvWriter.finish();
        try {
            outToCSV.flush();
            outToCSV.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void writeToCSVFile(){
        if(csvWriter != null || outToCSV != null){
            csvWriter.write(csvData, csvSheet);
            csvWriter.finish();
//            try {
//                outToCSV.flush();
//                outToCSV.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
            //清空子List，否则内存溢出
            for (int i = 0; i < csvData.size(); i++) {
                for (int j = 0; j < csvData.get(i).size(); j++) {
                    csvData.get(i).remove(j);
                }
            }
            csvData.clear();
            csvData = new ArrayList<>();
            csvWriter = null;
            columCount = 0;
            outToCSV = null;
        }
    }
}
