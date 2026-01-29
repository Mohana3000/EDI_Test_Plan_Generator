package com.tpg.Controller;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JTextArea;
import com.tpg.Model.*;

public class AppHandler {
    private LinkedHashMap<String, Validation> validations;
    String mapName;
    public static int count = 1;

    public static String getCount() {
        return String.format("%02d", count);
    }

    public static void setCount(int value) {
        count = value;
    }

    public static void incrementCounter() {
        count++;
    }

    public static void decrementCounter() {
        count--;
    }

    public void startApp(JTextArea resultArea, JTextArea logArea) {
        String username = System.getProperty("user.name");
        if (username.contains("-"))
            username = username.substring(4);

        username = username.substring(0, 1).toUpperCase() + username.substring(1, username.length() - 1) + " "
                + username.substring(username.length() - 1).toUpperCase();
        logArea.setText("\nHello " + username
                + ". Launching TPG, your handy test plan generator..\n\n1. Start by selecting the operation - New/Enhancement you want to perform.\n2. Browse to select the map you want the program to read.\n3. If the the operation Enhancment is selected, choosing existing test plan is optional.If no existing test plan is selected, you can confirm for proceeding with creating cases in a new test plan.\n4. Click the 'Generate cases' button to read the map and produce the cases.\n5. Provide additional cases if needed, by clicking the 'Add case' / 'Remove Last Case' buttons to handle the custom cases needed.\n6. Once done, click the 'Generate Test Plan' button. You'll find the test plan written in path selected above.\n");

       logArea.setText(logArea.getText()+"\nInitializing\n");

    }

    public void performMapOperations(UserInput iData, JTextArea resultArea, ResultData outData) {
        if (iData.getMapPath().isEmpty()) {
            outData.setLogArea("Please check the path in the field 'Map Path' is not empy and re-try.");
        }
        MapController mapControllerObj = new MapController(iData.getMapPath(), iData.getOperationType(), outData);
        validations = mapControllerObj.getValidations();
        mapName = mapControllerObj.getMapName();
        if (!validations.isEmpty())
            new ResultData().setPreviewText(validations, resultArea);

    }

    public void addCustomCase(CustomCase ccData, JTextArea resultArea) {
        Validation customCase = new Validation();
        customCase.setEdiCase(ccData.getId());
        AppHandler.incrementCounter();

        customCase.setEdiCondition(ccData.getCondition());
        customCase.setEdiMessage(ccData.getMessage());
        customCase.setValidationType("User");
        validations.put(customCase.getEdiCase(), customCase);
        if (!validations.isEmpty())
            new ResultData().setPreviewText(validations, resultArea);
    }

    public void removeLastCustomCase(JTextArea resultArea) {
        if (!validations.isEmpty()) {
            Iterator<Map.Entry<String, Validation>> iterator = validations.entrySet().iterator();
            Map.Entry<String, Validation> lastEntry = null;

            while (iterator.hasNext()) {
                lastEntry = iterator.next();
            }

            if (lastEntry != null) {
                iterator.remove();
                AppHandler.decrementCounter();
            }

            if (!validations.isEmpty())
                new ResultData().setPreviewText(validations, resultArea);

        }
    }

    public void generateTestPlan(UserInput iData, ResultData outData, String adoNumber, String columnChoice,
            int exisistingPlanFlag) {
        new PlanController(iData.getOperationType(), iData.getPlanPath(), iData.getOutputPath(),
                adoNumber, mapName, validations, outData, columnChoice, exisistingPlanFlag);

    }

}
