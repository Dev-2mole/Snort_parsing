package org.parsing;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataParser {

    public static void parseAndSaveData() throws IOException {
        String filePath = "C:\\Users\\user\\Desktop\\Github\\snort_out"; // 파일 경로 설정
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Parsed Data");
        int rowNum = 0;

        Row row = sheet.createRow(rowNum++);
        row.createCell(0).setCellValue("Source Code Name");
        row.createCell(1).setCellValue("URL Pattern");
        row.createCell(2).setCellValue("Pattern Name");

        String line;
        String currentSourceCodeName = "";
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("activate lua detector. name=")) {
                currentSourceCodeName = line.substring(line.indexOf('=') + 1).trim();
            } else if (line.startsWith("###")) {
                // Do nothing, it's a separator
            } else {
                Matcher patternMatcher = Pattern.compile("pattern = (.+)").matcher(line);
                if (patternMatcher.find()) {
                    row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(currentSourceCodeName);
                    row.createCell(1).setCellValue(patternMatcher.group(1));

                    // Pattern name 파싱 (예: http_pattern, url_application 등)
                    // 현재 파일 예시에는 없으므로, 필요시 구현
                }
            }
        }
        reader.close();

        // Excel 파일 작성
        try (FileOutputStream outputStream = new FileOutputStream("ParsedData.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();
    }
}
