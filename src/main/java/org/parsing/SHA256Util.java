package org.parsing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;

public class SHA256Util {
    // SHA-256 계산 메소드
    public static String calculateSHA256(String filePath) {
        try (InputStream fis = new FileInputStream(filePath)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] shaBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte shaByte : shaBytes) {
                sb.append(String.format("%02x", shaByte));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
