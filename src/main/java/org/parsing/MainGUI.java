package org.parsing;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class MainGUI extends JFrame {
    
    private JTextArea logArea;
    private JCheckBox parseCheckBox, skipParseCheckBox, validationOptionsCheckBox;
    private JButton fileUploadButton, validateButton, saveResultsButton;
    private JLabel lastValidationDateLabel, lastValidationMD5Label;
    private String selectedFilePath;
    private JCheckBox validationNewURLCheckBox;     // 검증 (신규 URL만)
    private JCheckBox validationExistingFailCheckBox; // 검증 (기존 Fail만)
    private JCheckBox validationExistingSuccessCheckBox; // 검증 (기존 Succes만)
    private JCheckBox validateAllCheckBox;           // 전체 검증

    public MainGUI() {
        // Initialize the GUI components
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600); // Set the window size
    
        // Status labels for the last validation date and MD5
        lastValidationDateLabel = new JLabel("최근 검증 날짜: " + readMD5Info("Date"));
        lastValidationMD5Label = new JLabel("기본 MD5: " + readMD5Info("MD5"));
    
        // Log area
        logArea = new JTextArea(20, 30); // 가로 길이를 줄임
        JScrollPane logScrollPane = new JScrollPane(logArea);
        add(logScrollPane, BorderLayout.CENTER);
    
        // Initialize checkboxes
        parseCheckBox = new JCheckBox("파싱 진행");
        skipParseCheckBox = new JCheckBox("파싱 작업 생략");
        validationNewURLCheckBox = new JCheckBox("검증 (신규 URL만)");
        validationExistingFailCheckBox = new JCheckBox("검증 (기존 Fail만)");
        validationExistingSuccessCheckBox = new JCheckBox("검증 (기존 Success만)"); 
        validateAllCheckBox = new JCheckBox("전체 검증");
        validationOptionsCheckBox = new JCheckBox("검증 옵션");
    
        // Disable validation checkboxes initially
        validationNewURLCheckBox.setEnabled(false);
        validationExistingFailCheckBox.setEnabled(false);
        validationExistingSuccessCheckBox.setEnabled(false);
        validateAllCheckBox.setEnabled(false);
    
       // Add parsing listener to parsing checkboxes
        ItemListener parsingListener = e -> {
            boolean isParsingSelected = parseCheckBox.isSelected() || skipParseCheckBox.isSelected();
            validationNewURLCheckBox.setEnabled(isParsingSelected);
            validationExistingFailCheckBox.setEnabled(isParsingSelected);
            validationExistingSuccessCheckBox.setEnabled(isParsingSelected);
            validateAllCheckBox.setEnabled(isParsingSelected);
        };

        // Add the listener to the parse and skip parse checkboxes
        parseCheckBox.addItemListener(parsingListener);
        skipParseCheckBox.addItemListener(parsingListener);

        // 파일 업로드, 검증 및 결과 저장 버튼 초기화
        fileUploadButton = new JButton("파일 업로드");
        fileUploadButton.addActionListener(e -> uploadFile());

        validateButton = new JButton("검증 진행");
        validateButton.addActionListener(e -> performValidation());

        saveResultsButton = new JButton("결과 저장");
        saveResultsButton.addActionListener(e -> saveResults());

        // controlPanel 초기화 및 컴포넌트 추가
        JPanel controlPanel = new JPanel();
        controlPanel.add(fileUploadButton);
        controlPanel.add(validateButton);
        controlPanel.add(saveResultsButton);

        // Add checkbox panel to the frame
        JPanel checkBoxPanel = new JPanel();
        checkBoxPanel.setLayout(new BoxLayout(checkBoxPanel, BoxLayout.Y_AXIS));
        checkBoxPanel.add(parseCheckBox);
        checkBoxPanel.add(skipParseCheckBox);
        checkBoxPanel.add(validationNewURLCheckBox);
        checkBoxPanel.add(validationExistingFailCheckBox);
        checkBoxPanel.add(validationExistingSuccessCheckBox);
        checkBoxPanel.add(validateAllCheckBox);

        // 패널들을 프레임에 추가
        add(checkBoxPanel, BorderLayout.EAST);
        add(controlPanel, BorderLayout.SOUTH);

        // Add components to panels
        JPanel statusPanel = new JPanel(new GridLayout(0, 1));
        statusPanel.add(lastValidationDateLabel);
        statusPanel.add(lastValidationMD5Label);

        // Add panels to the frame
        add(statusPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // Display the window
        setVisible(true);
    }

    // 이 메소드는 파일 업로드 버튼의 ActionListener에서 호출됩니다.
    private void uploadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedFilePath = selectedFile.getAbsolutePath();
            logArea.append("Selected file: " + selectedFilePath + "\n");
    
            // MD5 비교 로직 추가
            if (!compareFileMD5(selectedFilePath)) {
                logArea.append("MD5 값이 다릅니다. 파일을 비교합니다.\n");
    
                try {
                    // 예시 경로 (실제 경로는 상황에 맞게 수정)
                    String oldFilePath = "C:\\Temp\\Snort_Parsing\\20231125\\ParsedData.xlsx";
                    String newFilePath = selectedFilePath; // 또는 새로 생성된 엑셀 파일의 경로
    
                    FileComparator.compareExcelFiles(oldFilePath, newFilePath);
                    logArea.append("파일 비교 완료.\n");
                } catch (IOException e) {
                    logArea.append("파일 비교 중 에러 발생: " + e.getMessage() + "\n");
                }
            }
        }
    }

    private void performValidation() {
        if (parseCheckBox.isSelected()) {
            logArea.append("파싱을 시작합니다...\n");
            try {
                // DataParser 클래스의 parseAndSaveData 메소드 호출
                DataParser.parseAndSaveData(selectedFilePath);
                logArea.append("파싱 완료!\n");
            } catch (IOException e) {
                logArea.append("파싱 중 에러 발생: " + e.getMessage() + "\n");
            }
        }
        
        if (validationOptionsCheckBox.isSelected()) {
            logArea.append("검증을 시작합니다...\n");
            // MD5Util 클래스의 calculateMD5 메소드 호출
            String md5 = MD5Util.calculateMD5(selectedFilePath);
            logArea.append("파일 MD5: " + md5 + "\n");
        }
    }

    private void saveResults() {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Results");
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue("Some Result");

        try (FileOutputStream out = new FileOutputStream(new File("Results.xlsx"))) {
            workbook.write(out);
            logArea.append("Results saved to Excel.\n");
        } catch (IOException e) {
            logArea.append("Failed to save results: " + e.getMessage() + "\n");
        }
    }

    private String readMD5Info(String key) {
        File file = new File("Default_Snort_out_MD5.txt"); // Assuming the file is in the project root directory
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.startsWith(key)) {
                    return line.split(":")[1].trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Error reading file: " + e.getMessage();
        }
        return null;
    }
    
    private boolean compareFileMD5(String uploadedFilePath) {
        String existingMD5 = readMD5Info("MD5");
        String uploadedFileMD5 = MD5Util.calculateMD5(uploadedFilePath);
        return existingMD5 != null && existingMD5.equals(uploadedFileMD5);
    }
    
    
    

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}
