package org.parsing;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataParser {

    // 파일 경로를 매개변수로 받는 메소드로 수정
    public static void parseAndSaveData(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));

        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Parsed Data");
        AtomicInteger rowNum = new AtomicInteger(1);

        createHeader(sheet);

        String line;
        String currentSourceCodeName = "";
        String currentUrl = "";
        Set<String> existingUrls = new HashSet<>();
        Map<String, String> urlToSourceCodeMap = new HashMap<>();

        Pattern patternRegex = Pattern.compile("pattern = (.+)");
        Pattern domainPattern = Pattern.compile("^[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("activate lua detector. name=")) {
                if (!currentUrl.isEmpty()) {
                    currentUrl = normalizeUrl(currentUrl);
                    urlToSourceCodeMap.put(currentUrl, currentSourceCodeName);
                    currentUrl = "";
                }
                currentSourceCodeName = line.substring(line.indexOf('=') + 1).trim();
                continue;
            }

            if (line.startsWith("###")) {
                if (!currentUrl.isEmpty()) {
                    currentUrl = normalizeUrl(currentUrl);
                    urlToSourceCodeMap.put(currentUrl, currentSourceCodeName);
                    currentUrl = "";
                }
                continue;
            }

            if (line.contains("pattern =")) {
                Matcher patternMatcher = patternRegex.matcher(line);
                if (patternMatcher.find()) {
                    String pattern = patternMatcher.group(1).trim();
                    if (!currentUrl.isEmpty() && currentUrl.endsWith("/") && pattern.startsWith("/")) {
                        pattern = pattern.substring(1);
                    }
                    currentUrl += pattern;
                }
            }
        }

        // URL과 소스코드 이름을 엑셀에 저장
        for (Map.Entry<String, String> entry : urlToSourceCodeMap.entrySet()) {
            saveUrl(sheet, rowNum, entry.getValue(), entry.getKey(), existingUrls, domainPattern);
        }

        reader.close();

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String savePath = "C:\\Temp\\Snort_Parsing\\" + today + "\\";

        // 해당 경로가 없다면 생성
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        try (FileOutputStream outputStream = new FileOutputStream(savePath + "ParsedData.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();

        System.out.println("Processing completed.");
    }

    private static String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    private static void createHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"URL", "Last Check Date", "New", "Success/Failure", "Response Code/Error", 
                            "Redirection URL", "Redirection Response/Error", "Source Code Name"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }

    private static void saveUrl(Sheet sheet, AtomicInteger rowNum, String sourceCodeName, 
                                String url, Set<String> existingUrls, Pattern domainPattern) {
        Matcher domainMatcher = domainPattern.matcher(url);
                                
        if (domainMatcher.find() && existingUrls.add(url)) {
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url; // URL에 http:// 또는 https://가 없으면 추가
            }
        
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(url); // URL
        
            for (int i = 1; i <= 6; i++) {
                row.createCell(i).setCellType(CellType.BLANK); // 나머지 필드는 null로 설정
            }
        
            row.createCell(7).setCellValue(sourceCodeName); // Source Code Name
        }
    }
    
    public static void main(String[] args) {
        try {
            // 사용자로부터 파일 경로 입력받기
            String filePath = JOptionPane.showInputDialog("파일 경로를 입력하세요:");
            parseAndSaveData(filePath); // 사용자가 입력한 파일 경로로 메소드 호출
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
