package org.parsing;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.*;

public class MD5Util {

    public static String calculateMD5(String filePath) {
        try (InputStream fis = new FileInputStream(filePath)) {
            MessageDigest digest = MessageDigest.getInstance("MD5");

            byte[] bytesBuffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] md5Bytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                sb.append(String.format("%02x", md5Byte));
            }
            return sb.toString();

        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

