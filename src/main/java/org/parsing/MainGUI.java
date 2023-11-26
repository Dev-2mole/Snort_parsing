package org.parsing;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class MainGUI extends JFrame {
    
    private JTextArea logArea;
    private JCheckBox parseCheckBox, skipParseCheckBox;
    private JButton fileUploadButton, validateButton, saveResultsButton;
    private JLabel lastValidationDateLabel, lastValidationMD5Label;
    private String selectedFilePath;
    private JCheckBox validationNewURLCheckBox;     // 검증 (신규 URL만)
    private JCheckBox validationExistingFailCheckBox; // 검증 (기존 Fail만)
    private JCheckBox validationExistingSuccessCheckBox; // 검증 (기존 Succes만)
    private JCheckBox validateAllCheckBox;           // 전체 검증
    
    // oldFilePath 설정
    String lastValidationDate = readMD5Info("Date");
    String oldFilePath = "C:\\Temp\\Snort_Parsing\\" + lastValidationDate + "\\ParsedData.xlsx";
    // newFilePath 설정
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String newFilePath = "C:\\Temp\\Snort_Parsing\\" + today + "\\ParsedData.xlsx";

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

        validateButton = new JButton("실행");
        validateButton.addActionListener(e -> performValidation());

        saveResultsButton = new JButton("결과 저장");
        saveResultsButton.addActionListener(e -> saveResults(newFilePath));

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
            String fileName = selectedFile.getName();

            // 파일 이름 확인
            if (!fileName.startsWith("snort_out")) {
                // 경고 메시지 표시
                JOptionPane.showMessageDialog(this, 
                    "업로드된 파일 이름이 'snort_out'이 아닙니다.", 
                    "경고", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                selectedFilePath = selectedFile.getAbsolutePath();
                logArea.append("Selected file: " + selectedFilePath + "\n");
                
                // MD5 비교
                boolean isMD5Match = compareFileMD5(selectedFilePath);
                if (isMD5Match) {
                    logArea.append("MD5 값이 일치합니다.\n");
                } else {
                    logArea.append("MD5 값이 다릅니다.\n");
                }
            }
        }
    }
    

    private void performValidation() {
        boolean isParseSelected = parseCheckBox.isSelected();
        boolean isSkipParseSelected = skipParseCheckBox.isSelected();
        
        
        if (isParseSelected) {
            logArea.append("파싱 및 MD5 검증을 시작합니다...\n");
            try {
                DataParser.parseAndSaveData(selectedFilePath); // 파싱 수행
                logArea.append("파싱 완료!\n");

                // 파싱이 완료된 후 MD5가 다를 경우 New 체크 진행
                if (!compareFileMD5(selectedFilePath)) {
                    logArea.append("새로운 파일이 생성되었습니다. New 체크를 진행합니다.\n");

                    // FileComparator 로직 추가
                    FileComparator.compareExcelFiles(oldFilePath, newFilePath);
                    logArea.append("파싱 완료: " + newFilePath + "\n");
                    logArea.append("New 체크 진행완료 했습니다.\n");

                }
            } catch (IOException e) {
                logArea.append("파싱 중 에러 발생: " + e.getMessage() + "\n");
            }
        }
        else if (isSkipParseSelected) {
            logArea.append("파싱 작업을 생략합니다.\n");
        } else {
            logArea.append("파싱 옵션이 선택되지 않았습니다.\n");
            return; // 파싱 옵션을 선택하지 않았을 경우 함수 종료

        }// Validation 클래스 인스턴스 생성
        

        // Validation 클래스에 파싱 결과 파일 경로 전달
        String newfolderpath = newFilePath.replace("\\ParsedData.xlsx", "");
        Validation validation = new Validation(newfolderpath);
        String validationOption = getValidationOption();

        if (!validationOption.isEmpty()) {
            validation.baseValidation(validationOption, isParseSelected);
            int successCount = validation.getSuccessCount();
            int failureCount = validation.getFailureCount();
            logArea.append("검증 결과: 성공 " + successCount + "개, 실패 " + failureCount + "개\n");
            logArea.append("URL 검증 완료!\n");
            MD5Updater.updateMD5AndDate(selectedFilePath, "Default_Snort_out_MD5.txt");
            logArea.append("최신 검증 날짜와 MD5 값이 갱신되었습니다.\n");
            // 최신 MD5 정보와 날짜를 라벨에 반영
            String newDate = readMD5Info("Date");
            String newMD5 = readMD5Info("MD5");

            lastValidationDateLabel.setText("최근 검증 날짜: " + newDate);
            lastValidationMD5Label.setText("기본 MD5: " + newMD5);
        } else {
            logArea.append("검증 옵션이 선택되지 않았습니다.\n");
        }
    }
    
    private String getValidationOption() {
        StringBuilder optionBuilder = new StringBuilder();
        if (validationNewURLCheckBox.isSelected()) {
            optionBuilder.append("new+");
        }
        if (validationExistingFailCheckBox.isSelected()) {
            optionBuilder.append("fail+");
        }
        if (validationExistingSuccessCheckBox.isSelected()) {
            optionBuilder.append("success+");
        }
        if (validateAllCheckBox.isSelected()) {
            optionBuilder.append("all+");
        }
    
        if (optionBuilder.length() > 0) {
            // 마지막 '+' 제거
            optionBuilder.setLength(optionBuilder.length() - 1);
        }
    
        return optionBuilder.toString();
    }

    
    private void saveResults(String newFilePath) {
        try {
            // 원본 파일 읽기
            FileInputStream inputStream = new FileInputStream(new File(newFilePath));
            Workbook workbook = new XSSFWorkbook(inputStream);
    
            // 사용자에게 저장할 파일 위치 선택 요청
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("저장할 파일 위치 선택");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setAcceptAllFileFilterUsed(false);
            fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Excel Files", "xlsx"));
    
            int userSelection = fileChooser.showSaveDialog(null);
    
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String saveFilePath = fileToSave.getAbsolutePath();
                // 확장자 .xlsx 추가 (필요한 경우)
                if (!saveFilePath.endsWith(".xlsx")) {
                    saveFilePath += ".xlsx";
                }
    
                try (FileOutputStream out = new FileOutputStream(new File(saveFilePath))) {
                    workbook.write(out);
                    logArea.append("Results saved to: " + saveFilePath + "\n");
                }
            }
    
            // 자원 해제
            workbook.close();
            inputStream.close();
    
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
