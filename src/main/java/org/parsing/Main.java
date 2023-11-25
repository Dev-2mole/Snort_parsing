package org.parsing;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try {
            DataParser.parseAndSaveData();
        } catch (IOException e) {
            e.printStackTrace(); // 예외 스택 트레이스 출력
        }
    }
}
