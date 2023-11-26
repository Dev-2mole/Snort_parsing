package org.parsing;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
//import java.util.ArrayList;
//import java.util.List;

public class Excel {

    File[] getFileList(String folderPath){
        File rw = new File(folderPath);
        return rw.listFiles();
    }

    String[][] readExcel(File filePath){
        try{
            OPCPackage pkg = OPCPackage.open(filePath.toString());
            Workbook workbook = new XSSFWorkbook(pkg);
            Sheet sheet = workbook.getSheetAt(0);

            int numRows = sheet.getPhysicalNumberOfRows();
            int numCols = sheet.getRow(0).getPhysicalNumberOfCells();

            String[][] data = new String[numRows][numCols];

            for (int i = 0; i < numRows; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < numCols; j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null) {
                        switch (cell.getCellType()) {
                            case STRING:
                                data[i][j] = cell.getStringCellValue();
                                break;
                            case NUMERIC:
                                data[i][j] = String.valueOf(cell.getNumericCellValue());
                                break;
                            case BOOLEAN:
                                data[i][j] = String.valueOf(cell.getBooleanCellValue());
                                break;
                            default:
                                data[i][j] = "";
                        }
                    } else {
                        data[i][j] = ""; // 셀이 비어있을 경우 빈 문자열로 처리
                    }
                }
            }

            workbook.close();
            pkg.close();

            return data;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InvalidFormatException e) {
            throw new RuntimeException(e);
        }
    }
    void toExcel(File filePath, String[][] arr) {
        try{
            FileInputStream inputStream = new FileInputStream(filePath);
            Workbook workbook = new XSSFWorkbook(inputStream);
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 0; i < arr.length; i++) {
                Row row = sheet.getRow(i);
                for (int j = 0; j < arr[i].length; j++) {
                    Cell cell = row.getCell(j);
                    cell.setCellValue(arr[i][j]);
                }
            }

            FileOutputStream fileOut = new FileOutputStream(filePath);
            workbook.write(fileOut);

            // 자원 해제
            workbook.close();
            inputStream.close();

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
