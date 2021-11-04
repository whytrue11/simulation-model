package tools;

import mortgage.Request;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class ResponsesWriter {
  private static String responsesFileName = "Responses.xls";
  private static Workbook workbook = new HSSFWorkbook();

  public static void setResponsesFileName(String responsesFileName) {
    ResponsesWriter.responsesFileName = responsesFileName;
  }

  public static String getResponsesFileName() {
    return responsesFileName;
  }

  public static Workbook getWorkbook() {
    return workbook;
  }

  public static synchronized void requestRejection(Request request) {
    Sheet sheet = ResponsesWriter.getWorkbook().createSheet("Request " + request.getNumber());
    Row row = sheet.createRow(0);
    Cell cell = row.createCell(0);
    cell.setCellValue("Request denied");
    try {
      ResponsesWriter.getWorkbook().write(new FileOutputStream(ResponsesWriter.getResponsesFileName()));
    } catch (IOException ioException) {
      ioException.printStackTrace();
    }
  }
}
