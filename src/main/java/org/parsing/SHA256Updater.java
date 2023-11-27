package org.parsing;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SHA256Updater {

    public static void updateSHA256AndDate(String filePath, String sha256FilePath) {
        try {
            // SHA-256 값 계산
            String sha256 = calculateSHA256(filePath);

            // 현재 날짜 구하기
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

            // SHA-256 값과 날짜를 파일에 저장
            try (PrintWriter out = new PrintWriter(new FileWriter(sha256FilePath))) {
                out.println("Date: " + currentDate);
                out.println("SHA256: " + sha256);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String calculateSHA256(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nread);
            }
        }
        byte[] sha256Bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : sha256Bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
