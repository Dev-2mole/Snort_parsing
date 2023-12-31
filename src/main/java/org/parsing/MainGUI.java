package org.parsing;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

public class MainGUI extends JFrame {
    
    // JFrame
    private JTextArea logArea;
    private JCheckBox parseCheckBox, skipParseCheckBox;
    private JButton fileUploadButton, validateButton, saveResultsButton;
    private JLabel lastValidationDateLabel, lastValidationSHA256Label;    

    private String selectedFilePath;                // 업로드 파일 경로
    private JCheckBox validationNewURLCheckBox;     // 검증 (신규 URL만)
    private JCheckBox validationExistingFailCheckBox; // 검증 (기존 Fail만)
    private JCheckBox validationExistingSuccessCheckBox; // 검증 (기존 Succes만)
    private JCheckBox validateAllCheckBox;           // 전체 검증
    
    // oldFilePath 설정 (디폴트 SHA256에서 값(DATE)을 가져옴)
    String lastValidationDate = readSHA256Info("Date");
    String oldFilePath = "C:\\Temp\\Snort_Parsing\\" + lastValidationDate + "\\ParsedData.xlsx";
    // newFilePath 설정 (실행하는 오늘 날짜 가져옴)
    String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String newFilePath = "C:\\Temp\\Snort_Parsing\\" + today + "\\ParsedData.xlsx";

    public MainGUI() {
        // Initialize the GUI components
        initComponents();
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setSize(800, 600); // 윈도우 창 크기 지정 800:600
        setTitle("Snort OpenAppID 파싱 및 검증 프로그램");      // 프로그램 타이틀 지정

        // 아이콘 추가
        ImageIcon image = new ImageIcon("icon/AppDB.png");
        setIconImage(image.getImage());

        Font font = new Font("맑은 고딕", Font.BOLD, 12);
    
        // 가장 마지막에 검증한 날짜와, SHA256 해시값을 가져온다. 
        lastValidationDateLabel = new JLabel("  최근 검증 날짜: " + readSHA256Info("Date"));
        lastValidationDateLabel.setFont(font);
        lastValidationSHA256Label = new JLabel("  SHA256: " + readSHA256Info("SHA256"));
        lastValidationSHA256Label.setFont(font);
    
        // 로그 화면 영역
        logArea = new JTextArea(20, 30);
        logArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        add(logScrollPane, BorderLayout.CENTER);

        logArea.append("=======================================================\n");
        logArea.append(" ###### ###### ##  ##      ##                ##     #####  #####   ####    #####  \n");
        logArea.append("     ###     ##    ### ##    ####             ####   ##  ##  ##  ##  ## ##   ##  ##  \n");
        logArea.append("     ##      ##    ######   ##  ##           ##  ##  ##  ##  ##  ##  ##  ##  #####   \n");
        logArea.append("    ##       ##    ## ###   ######         ######  ####    ####    ##  ##  ##  ##  \n");
        logArea.append("  ###       ##    ##   ##   ##   ##         ##   ##   ##       ##       ## ##  ##  ##  \n");
        logArea.append(" ######   ##    ##   ##   ##   ##         ##   ##   ##       ##       ####   #####   \n");
        logArea.append("=======================================================\n");

        logArea.append("1. 파일 선택: URL을 파싱할 파일을 선택해 주세요.\n");
        logArea.append("2. URL 파싱: 선택한 파일에서 URL을 파싱합니다.\n");
        logArea.append("3. URL 검증: 파싱한 URL이 유효한지 검증합니다.\n");
    
        // 체크박스 선언
        parseCheckBox = new JCheckBox("파싱 진행");
        parseCheckBox.setFont(font);
        skipParseCheckBox = new JCheckBox("파싱 작업 생략");
        skipParseCheckBox.setFont(font);
        validationNewURLCheckBox = new JCheckBox("검증 (신규 URL만)");
        validationNewURLCheckBox.setFont(font);
        validationExistingFailCheckBox = new JCheckBox("검증 (기존 Fail만)");
        validationExistingFailCheckBox.setFont(font);
        validationExistingSuccessCheckBox = new JCheckBox("검증 (기존 Success만)"); 
        validationExistingSuccessCheckBox.setFont(font);
        validateAllCheckBox = new JCheckBox("전체 검증");
        validateAllCheckBox.setFont(font);

        // 파싱관련 체크박스 리스너
        ItemListener parsingListener = e -> {
            boolean isEitherSelected = parseCheckBox.isSelected() || skipParseCheckBox.isSelected();

            skipParseCheckBox.setEnabled(!parseCheckBox.isSelected());
            parseCheckBox.setEnabled(!skipParseCheckBox.isSelected());

            setValidationCheckBoxesEnabled(isEitherSelected);
            updateCheckBoxesBasedOnParsingSelection();
        };

        // 리스너 추가
        parseCheckBox.addItemListener(parsingListener);
        skipParseCheckBox.addItemListener(parsingListener);
        
        // 초기 상태에서는 모든 체크박스 비활성화
        setAllCheckBoxesEnabled(false);

        // 파일 업로드, 검증 및 결과 저장 버튼 초기화
        fileUploadButton = new JButton("파일 업로드");
        fileUploadButton.setBackground(Color.white);
        fileUploadButton.setFont(font);
        fileUploadButton.addActionListener(e -> uploadFile());

        validateButton = new JButton("실행");
        validateButton.setBackground(Color.white);
        validateButton.setFont(font);
        validateButton.addActionListener(e -> performValidation());

        saveResultsButton = new JButton("결과 저장");
        saveResultsButton.setBackground(Color.white);
        saveResultsButton.setFont(font);
        saveResultsButton.addActionListener(e -> saveResults(newFilePath));

        // controlPanel 초기화 및 컴포넌트 추가
        JPanel controlPanel = new JPanel();
        controlPanel.add(fileUploadButton);
        controlPanel.add(validateButton);
        controlPanel.add(saveResultsButton);

        // 프레임에 체크박스 추가
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

        // 패널 구성요소 추가 : 최근검증날짜,SHA256
        JPanel statusPanel = new JPanel(new GridLayout(0, 1));
        statusPanel.add(lastValidationDateLabel);
        statusPanel.add(lastValidationSHA256Label);

        // Add panels to the frame
        add(statusPanel, BorderLayout.NORTH);
        add(new JScrollPane(logArea), BorderLayout.CENTER);

        // 디스플레이 띄우기
        setVisible(true);
    }

    // 파일을 업로드 하지 않았을 경우, 파싱 체크박스 비활성화 
    private void setParsingCheckBoxesEnabled(boolean enable) {
        parseCheckBox.setEnabled(enable);
        skipParseCheckBox.setEnabled(enable);
    }

    // 파싱 선택/미선택에 따른 체크박스 비활성화 및 검증하는 체크박스 초기화
    private void updateCheckBoxesBasedOnParsingSelection() {
        boolean isEitherSelected = parseCheckBox.isSelected() || skipParseCheckBox.isSelected();
        
        skipParseCheckBox.setEnabled(!parseCheckBox.isSelected());
        parseCheckBox.setEnabled(!skipParseCheckBox.isSelected());
        
        setValidationCheckBoxesEnabled(isEitherSelected);
        if (!isEitherSelected) {
            clearValidationCheckBoxes();
        }
    }
    // 검증하는 체크박스 초기화
    private void clearValidationCheckBoxes() {
        validationNewURLCheckBox.setSelected(false);
        validationExistingFailCheckBox.setSelected(false);
        validationExistingSuccessCheckBox.setSelected(false);
        validateAllCheckBox.setSelected(false);
    }
    // 검증하는 체크박스 비활성화일때
    private void setValidationCheckBoxesEnabled(boolean enable) {
        validationNewURLCheckBox.setEnabled(enable);
        validationExistingFailCheckBox.setEnabled(enable);
        validationExistingSuccessCheckBox.setEnabled(enable);
        validateAllCheckBox.setEnabled(enable);
    }
    //모든 체크박스가 비활성화일때
    private void setAllCheckBoxesEnabled(boolean enable) {
        setParsingCheckBoxesEnabled(enable);
        setValidationCheckBoxesEnabled(enable);
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
                UIManager.put("OptionPane.messageFont", new Font("맑은 고딕", Font.BOLD, 14));
                UIManager.put("OptionPane.buttonFont", new Font("맑은 고딕", Font.BOLD, 12));
                UIManager.put("Button.background", Color.WHITE);
                UIManager.put("Button.foreground", Color.BLACK);
        
                // 대화 상자 표시
                JOptionPane.showMessageDialog(this, 
                    "업로드된 파일 이름이 'snort_out'이 아닙니다.", 
                    "경고", 
                    JOptionPane.WARNING_MESSAGE);
            } else {
                selectedFilePath = selectedFile.getAbsolutePath();
                logArea.append("Selected file: " + selectedFilePath + "\n");
                
                // SHA256 비교
                boolean isSHA256Match = compareFileSHA256(selectedFilePath);
                if (isSHA256Match) {
                    logArea.append("SHA256 값이 일치합니다.\n");
                } else {
                    logArea.append("SHA256 값이 다릅니다.\n");
                }
                // 파일 업로드 성공 시 체크박스 활성화
                setParsingCheckBoxesEnabled(true);
            }
        }
        else{
            // 파일 업로드 실패 시 체크박스 비활성화
            setAllCheckBoxesEnabled(false);
        }
    }
    
    // 파싱 로직 진행
    private void performValidation() {
        boolean isParseSelected = parseCheckBox.isSelected();
        boolean isSkipParseSelected = skipParseCheckBox.isSelected();
        if (isParseSelected) {
            logArea.append("파싱 및 SHA256 검증을 시작합니다...\n");
            try {
                DataParser.parseAndSaveData(selectedFilePath); // 파싱 수행
                logArea.append("파싱 완료!\n");

                // 파싱이 완료된 후 SHA256가 다를 경우 New 체크 진행
                if (!compareFileSHA256(selectedFilePath)) {
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
            SHA256Updater.updateSHA256AndDate(selectedFilePath, "Default_Snort_out_SHA256.txt");
            logArea.append("최신 검증 날짜와 SHA256 값이 갱신되었습니다.\n");
            // 최신 SHA256 정보와 날짜를 라벨에 반영
            String newDate = readSHA256Info("Date");
            String newSHA256 = readSHA256Info("SHA256");

            lastValidationDateLabel.setText("최근 검증 날짜: " + newDate);
            lastValidationSHA256Label.setText("기본 SHA256: " + newSHA256);
        } else {
            logArea.append("검증 옵션이 선택되지 않았습니다.\n");
        }
    }
    
    // 체크박스 선택으로 
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
    

    private String readSHA256Info(String key) {
        File file = new File("Default_Snort_out_SHA256.txt"); // Assuming the file is in the project root directory
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

    // SHA256 비교 메소드
    private boolean compareFileSHA256(String uploadedFilePath) {
        String existingSHA256 = readSHA256Info("SHA256");
        String uploadedFileSHA256 = SHA256Util.calculateSHA256(uploadedFilePath);
        return existingSHA256 != null && existingSHA256.equals(uploadedFileSHA256);
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainGUI());
    }
}
