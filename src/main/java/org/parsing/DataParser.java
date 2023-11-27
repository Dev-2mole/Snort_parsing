package org.parsing;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataParser {

    // 파일 저장 함수로, 파일 매개변수를 받아옴
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
            //###으로 구분 되어 있으면 신규 URL로 확인
            if (line.startsWith("###")) {
                if (!currentUrl.isEmpty()) {
                    currentUrl = normalizeUrl(currentUrl);
                    urlToSourceCodeMap.put(currentUrl, currentSourceCodeName);
                    currentUrl = "";
                }
                continue;
            }
            // Pattern이 들어간 라인을 만날 경우 파싱 진행
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
        // 오늘 날짜의 경로 지정
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String savePath = "C:\\Temp\\Snort_Parsing\\" + today + "\\";

        // 해당 경로가 없다면 생성
        File directory = new File(savePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        // 작성된 파일을 해당 위치에 저장
        try (FileOutputStream outputStream = new FileOutputStream(savePath + "ParsedData.xlsx")) {
            workbook.write(outputStream);
        }
        workbook.close();

        System.out.println("Processing completed.");
    }

    // 맨 마지막 / 삭제
    private static String normalizeUrl(String url) {
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    // 엑셀 해더 세팅
    private static void createHeader(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        String[] headers = {"URL", "Last Check Date", "New", "Success/Failure", "Response Code/Error", 
                            "Redirection URL", "Redirection Response/Error", "Source Code Name"};
        for (int i = 0; i < headers.length; i++) {
            headerRow.createCell(i).setCellValue(headers[i]);
        }
    }
    // 각 해더별 데이터 저장
    private static void saveUrl(Sheet sheet, AtomicInteger rowNum, String sourceCodeName, 
                                String url, Set<String> existingUrls, Pattern domainPattern) {
        Matcher domainMatcher = domainPattern.matcher(url);
                                
        if (domainMatcher.find() && existingUrls.add(url)) {
            // URL에 http:// 또는 https://가 없으면 추가
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                url = "https://" + url; 
            }
            // URL 해더에 최종 Url 데이터 추가
            Row row = sheet.createRow(rowNum.getAndIncrement());
            row.createCell(0).setCellValue(url); 
            // URL, SourceCodeName 해더를 제외한 나머지 값은 null값 입력
            for (int i = 1; i <= 6; i++) {
                row.createCell(i).setCellValue("");
            }
            // Source Code Name 해더에 소스코드 이름 입력
            row.createCell(7).setCellValue(sourceCodeName); 
        }
    }
}
