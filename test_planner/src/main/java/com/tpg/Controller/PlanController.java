package com.tpg.Controller;

import java.util.LinkedHashMap;

import javax.swing.JOptionPane;

import java.io.File;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tpg.Model.ResultData;
import com.tpg.Model.Validation;
import com.tpg.Service.WorkbookActions;

public class PlanController extends WorkbookActions {
    public PlanController(String operationType, String existingTestPlanfilePath, String outputPath,
            String adoNumer, String mapName,
            LinkedHashMap<String, Validation> ValidationList, ResultData outData, String columnChoice,
            int exisistingPlanFlag) {

        String existingTestPlanName = "";

        try {
            @SuppressWarnings("resource")
            Workbook testplan = new XSSFWorkbook();

            if (operationType.equalsIgnoreCase("N")) {
                outData.setLogArea("Fetching Template testplan");
                testplan = readTemplate();

            }

            else {
                if (exisistingPlanFlag == 0) {
                    outData.setLogArea(
                            "Fetching Template testplan as no existing plan is selected for the enhancement");
                    existingTestPlanName = mapName;
                    testplan = readTemplate();

                } else {
                    outData.setLogArea("Reading existing test plan file" + existingTestPlanName + ". . . ");
                    testplan = readWorkbook(existingTestPlanfilePath);
                    existingTestPlanName = new File(existingTestPlanfilePath).getName();

                }
            }

            outData.setLogArea("Adding entries in validaiton sheet");
            outData.setLogArea("Preparing Test Plan");
            int proceed = createValidationSheet(testplan, ValidationList, adoNumer, operationType, columnChoice,
                    outData);

            outData.setLogArea("Proceeding #" + proceed);

            if (proceed > 0) {
                if (operationType.equalsIgnoreCase("N")) {
                    writeWorkbook(outputPath, mapName, testplan, outData);
                } else {
                    writeWorkbook(outputPath, existingTestPlanName, testplan, outData);
                }
            }

            testplan.close();

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "A problem occured while processing. Check log for more details");
            outData.setLogArea("Error Occured: " + e.toString());
            e.printStackTrace();

        }

    }

}
