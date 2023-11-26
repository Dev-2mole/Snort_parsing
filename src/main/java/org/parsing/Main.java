package org.parsing;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 메인 메소드에서 GUI 인스턴스를 생성하고 표시
        SwingUtilities.invokeLater(() -> {
            MainGUI mainGUI = new MainGUI();
            mainGUI.setVisible(true);
        });
    }
}

