package org.parsing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {

    private static String selectedFilePath; // 사용자가 선택한 파일의 경로

    public static void main(String[] args) {
        // 프레임 생성
        JFrame frame = new JFrame("Data Parser");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 300);

        // 레이아웃 설정
        frame.setLayout(new FlowLayout());

        // 기본 MD5 라벨
        JLabel defaultMd5Label = new JLabel("기본 MD5: " + readDefaultMD5());
        frame.getContentPane().add(defaultMd5Label);

        // 파일 선택 버튼
        JButton selectButton = new JButton("파일 선택");
        frame.getContentPane().add(selectButton);

        // 선택된 파일의 MD5 라벨
        JLabel selectedFileMd5Label = new JLabel("");
        frame.getContentPane().add(selectedFileMd5Label);

        // 확인 버튼
        JButton confirmButton = new JButton("확인");
        frame.getContentPane().add(confirmButton);

        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
                int result = fileChooser.showOpenDialog(frame);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    selectedFilePath = selectedFile.getAbsolutePath();
                    String fileMd5 = MD5Util.calculateMD5(selectedFilePath);
                    selectedFileMd5Label.setText("선택된 파일의 MD5: " + fileMd5);
                }
            }
        });

        confirmButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (selectedFilePath != null && !selectedFilePath.isEmpty()) {
                    // DataParser 호출
                    try {
                        DataParser.parseAndSaveData(selectedFilePath);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else {
                    JOptionPane.showMessageDialog(frame, "파일이 선택되지 않았습니다.");
                }
            }
        });

        // 프레임 보이기
        frame.setVisible(true);
    }

    private static String readDefaultMD5() {
        try {
            File file = new File("Default_Snort_out_MD5.txt"); // 프로젝트 루트 디렉토리에 위치한다고 가정
            BufferedReader br = new BufferedReader(new FileReader(file));
            String md5 = br.readLine();
            br.close();
            return md5;
        } catch (IOException e) {
            e.printStackTrace();
            return "MD5를 읽을 수 없음";
        }
    }
}

