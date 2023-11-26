package org.parsing;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;

public class Validation {
    private int suc;
    private int fail;
    private int i;
    private String folderPath;
    private final Excel excel = new Excel();

    public Validation(String folderPath){
        this.suc=0;
        this.fail=0;
        this.i=0;
        this.folderPath=folderPath;
    }

    void baseValidation(String option, boolean isParseSelected) {
        System.out.println(folderPath);
        File[] files = excel.getFileList(folderPath);
        
        if (files == null) {
            System.out.println("파일 목록이 없거나 폴더를 읽을 수 없습니다.");
            return;
        }

        if (!isParseSelected) {
            System.out.println("파싱 작업이 생략되었습니다.");
            return;
        }

        for (File file : files) {

            if(file.toString().contains("MD5HashValues")){
                continue;
            }

            // | url | Last Check | New | 성공/실패 | 응답코드/에러메시지 | 리다이렉트 경로 | 리다이렉트 응답코드/에러메시지 | 파싱 경로
            String[][] dataArr = excel.readExcel(file);

            if (dataArr == null || dataArr.length == 0) {
                System.out.println(file + " | 데이터 없음 또는 오류.");
                continue;
            }


            switch (option) {
                case "all":
                    validateAll(dataArr);
                    break;
                case "success":
                    validateSuccess(dataArr);
                    break;
                case "fail":
                    validateFail(dataArr);
                    break;
                case "new":
                    validateNew(dataArr);
                    break;
                case "new+success":
                    validateNewSuccess(dataArr);
                    break;
                case "new+fail":
                    validateNewFail(dataArr);
                    break;
            }

            System.out.println(file + " | 성공: " + suc + "  실패: " + fail);

            excel.toExcel(file, dataArr);
        }
    }

    void validateAll(String[][] arr){
        for(i=1; i<arr.length; i++){
            arr[i][1] = LocalDate.now().toString();
            validation(arr);
        }
    }

    void validateSuccess(String[][] arr){
        for(i=1; i<arr.length; i++){
            if(arr[i][3].equals("성공")) {
                arr[i][1] = LocalDate.now().toString();
                validation(arr);
            }
        }
    }

    void validateFail(String[][] arr){
        for(i=1; i<arr.length; i++){
            if(arr[i][3].equals("실패")) {
                arr[i][1] = LocalDate.now().toString();
                validation(arr);
            }
        }
    }

    void validateNew(String[][] arr){
        for(i=1; i<arr.length; i++){
            if(arr[i][2].equals("New")) {
                arr[i][1] = LocalDate.now().toString();
                validation(arr);
            }
        }
    }

    void validateNewSuccess(String[][] arr){
        for(i=1; i<arr.length; i++){
            if(arr[i][2].equals("New") || arr[i][3].equals("성공")) {
                arr[i][1] = LocalDate.now().toString();
                validation(arr);
            }
        }
    }

    void validateNewFail(String[][] arr){
        for(i=1; i<arr.length; i++){
            if(arr[i][2].equals("New") || arr[i][3].equals("실패")) {
                arr[i][1] = LocalDate.now().toString();
                validation(arr);
            }
        }
    }

    void validation(String[][] arr){
        Thread requestThread = new Thread(() -> {
            try {
                URL siteURL = new URL(arr[i][0]);
                HttpURLConnection connection = (HttpURLConnection) siteURL.openConnection();
                connection.setConnectTimeout(10000);
                connection.setRequestMethod("GET");
                connection.connect();

                //응답코드 받아오기
                int code = connection.getResponseCode();
                arr[i][4] = Integer.toString(code);

                if (199 < code && code < 300) {
                    //200번대의 경우 성공으로 판단
                    arr[i][3] = "성공";
                    suc++;
                } else if (code < 400) {
                    /*
                    300번대의 경우 리다이렉트한 경로를 확인하고
                    해당 경로에 대한 응답코드를 한 번 더 확인하여 200, 300번대인 경우 성공으로 판단
                     */
                    String redirect = connection.getHeaderField("Location");
                    arr[i][5] = redirect;

                    try {
                        URL redirectURL = new URL(redirect);
                        HttpURLConnection con = (HttpURLConnection) redirectURL.openConnection();
                        con.setConnectTimeout(10000);
                        con.setRequestMethod("GET");
                        con.connect();

                        int redirectedcode = con.getResponseCode();
                        arr[i][6] = Integer.toString(redirectedcode);

                        if (199 < redirectedcode && redirectedcode < 400) {
                            arr[i][3] = "성공";
                            suc++;
                        } else {
                            arr[i][3] = "실패";
                            fail++;
                        }
                        con.disconnect();
                    } catch (Exception e) {
                        arr[i][3] = "실패";
                        arr[i][6] = e.toString();
                        fail++;
                    }

                } else { //400번대 이상은 실패로 판단
                    arr[i][3] = "실패";
                    fail++;
                }
                connection.disconnect();
            } catch (Exception e) {
                //그 외 모든 에러는 exception으로 처리하여 실패 처리 후 다음 url 진행
                arr[i][3] = "실패";
                arr[i][4] = e.toString();
                fail++;
            }
        });

        requestThread.start();

        // 지정된 시간이 지나면 요청 스레드 종료
        try {
            requestThread.join(30000);
            if (requestThread.isAlive()) {
                // 타임아웃 발생 시 스레드 종료
                arr[i][3] = "실패";
                if(arr[i][4].equals(null)){
                    arr[i][4] = "timeout";
                }
                else{
                    arr[i][6] = "timeout";
                }
                requestThread.interrupt();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public int getSuccessCount() {
        return suc;
    }

    public int getFailureCount() {
        return fail;
    }
}
