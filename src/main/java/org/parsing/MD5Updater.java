package org.parsing;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MD5Updater {

    public static void updateMD5AndDate(String filePath, String md5FilePath) {
        try {
            // MD5 값 계산
            String md5 = calculateMD5(filePath);

            // 현재 날짜 구하기
            String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());

            // MD5 값과 날짜를 파일에 저장
            try (PrintWriter out = new PrintWriter(new FileWriter(md5FilePath))) {
                out.println("Date: " + currentDate);
                out.println("MD5: " + md5);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String calculateMD5(String filePath) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            int nread;
            while ((nread = fis.read(buffer)) != -1) {
                md.update(buffer, 0, nread);
            }
        }
        byte[] md5Bytes = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : md5Bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
