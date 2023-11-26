package org.parsing;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import java.io.*;
import java.util.*;

public class FileComparator {

    public static void compareExcelFiles(String oldFilePath, String newFilePath) throws IOException {
        // 엑셀 파일을 읽기 위한 Workbook 객체 생성
        Workbook oldWorkbook = new XSSFWorkbook(new FileInputStream(oldFilePath));
        Workbook newWorkbook = new XSSFWorkbook(new FileInputStream(newFilePath));

        Sheet oldSheet = oldWorkbook.getSheetAt(0);
        Sheet newSheet = newWorkbook.getSheetAt(0);

        Set<String> oldUrls = extractUrls(oldSheet);
        Set<String> newUrls = extractUrls(newSheet);

        // 새로운 URL 찾기
        newUrls.removeAll(oldUrls);

        // 새로운 URL에 대해 "New" 표시
        for (String url : newUrls) {
            Row row = findRow(newSheet, url);
            if (row != null) {
                Cell cell = row.createCell(2); // "New"가 들어갈 셀 (인덱스는 예시)
                cell.setCellValue("New");
            }
        }

        // 변경된 Workbook 저장
        try (FileOutputStream outputStream = new FileOutputStream(newFilePath)) {
            newWorkbook.write(outputStream);
        }

        oldWorkbook.close();
        newWorkbook.close();
    }

    private static Set<String> extractUrls(Sheet sheet) {
        Set<String> urls = new HashSet<>();
        Iterator<Row> rowIterator = sheet.iterator();
        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();
            Cell cell = row.getCell(0); // URL이 들어있는 첫 번째 셀
            if (cell != null) {
                urls.add(cell.getStringCellValue());
            }
        }
        return urls;
    }

    private static Row findRow(Sheet sheet, String url) {
        for (Row row : sheet) {
            Cell cell = row.getCell(0);
            if (cell != null && cell.getStringCellValue().equals(url)) {
                return row;
            }
        }
        return null;
    }
}
