package com.tpg.Service;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.tpg.Controller.AppHandler;
import com.tpg.Model.ResultData;
import com.tpg.Model.Validation;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

public class WorkbookActions {

    // Read Excel workbook from path
    public static Workbook readWorkbook(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        Workbook workbook = new XSSFWorkbook(fis);
        fis.close();
        return workbook;
    }

    // Get the Excel template from resources
    public static Workbook readTemplate() throws IOException {

        InputStream fis = WorkbookActions.class.getClassLoader().getResourceAsStream("TemplateTestPlan.xlsx");
        Workbook template = new XSSFWorkbook(fis);
        fis.close();
        return template;

    }

    // Create validationSheet based on the validation cases list
    public static int createValidationSheet(Workbook testplan,
            LinkedHashMap<String, Validation> validationList, String adoNumber,
            String operationtype, String columnChoice, ResultData outData) {
        AppHandler.setCount(1);

        Sheet validationSheet = testplan.getSheetAt(1); // Get the first sheet

        int testCaseColumn = Integer.parseInt(columnChoice.split("/")[0]);
        int descriptionColumn = Integer.parseInt(columnChoice.split("/")[1]);
        int expectedResultColumn = Integer.parseInt(columnChoice.split("/")[2]);

        validationSheet.setColumnWidth(testCaseColumn, 20000);
        validationSheet.setColumnWidth(descriptionColumn, 12000);
        validationSheet.setColumnWidth(expectedResultColumn, 8000);

        // Set cell styles
        Font fontArial10 = testplan.createFont();
        fontArial10.setFontName("Arial");
        fontArial10.setFontHeightInPoints((short) 10);

        Font fontArial10BoldWhite = testplan.createFont();
        fontArial10BoldWhite.setFontName("Arial");
        fontArial10BoldWhite.setFontHeightInPoints((short) 10);
        fontArial10BoldWhite.setBold(true);
        fontArial10BoldWhite.setColor(IndexedColors.WHITE.getIndex());

        CellStyle dataCell = testplan.createCellStyle();
        dataCell.setFont(fontArial10);
        dataCell.setWrapText(true);

        CellStyle validationHeader = testplan.createCellStyle();
        validationHeader.setFont(fontArial10BoldWhite);
        validationHeader.setFillForegroundColor(IndexedColors.BLUE_GREY.getIndex());
        validationHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Font strickenfont = testplan.createFont();
        strickenfont.setFontName("Arial");
        strickenfont.setFontHeightInPoints((short) 10);
        strickenfont.setStrikeout(true);

        CellStyle strickenCell = testplan.createCellStyle();
        strickenCell.setFont(strickenfont);
        strickenCell.setWrapText(true);

        int caseHeaderRow = validationSheet.getLastRowNum();
        int rowCount = 1;
        String header = operationtype.equalsIgnoreCase("N") ? "First Install - ADO " + adoNumber
                : "Enhancement - ADO " + adoNumber;
        int rowGroupingStart = validationSheet.getLastRowNum() + 2;
        int rowGroupEnd = 0;
        int externalCaseGroupBoundEnd =0;
        boolean externalTestCaseFlag = false;

        if (operationtype.equalsIgnoreCase("E")) {
            try {
                int matchRowIndex = SheetActions.findTextInFirstColumn(validationSheet, adoNumber);
                if (matchRowIndex == -1)
                    outData.setLogArea(
                            "ADO '" + adoNumber
                                    + "' not found in Sheet 2, first column. Proceeding 'Default Write' mode");
                else {
                    int externalCaseChoice = JOptionPane.showConfirmDialog(null,
                            "Found ADO '" + adoNumber + "' at row: " + (matchRowIndex + 1)
                                    + ". Click Yes to proceed 'External Testing - Modified Cases' mode.",
                            "Confirm Write", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    outData.setLogArea(
                            "ADO '" + adoNumber + "' found in Sheet 2 at row:" + (matchRowIndex + 1));

                    // If Yes, Get the group boundaries of the ADO, move the remaining rows down the
                    // size of cases about to be written.
                    if (externalCaseChoice == JOptionPane.YES_OPTION) {
                        outData.setLogArea("Proceeding 'External Testing - Modified Cases' mode");

                        int[] groupBounds = SheetActions.getGroupBoundaries(validationSheet, adoNumber,
                                matchRowIndex);
                        outData.setLogArea("Group boundaries determined [" + (groupBounds[0] + 1) + ","
                                + (groupBounds[1] + 1) + "]");
                        externalCaseGroupBoundEnd = groupBounds[1];

                                //Get the last row of the group boundary with data
                        try {
                            String lastRowText = "";
                            for (int i = groupBounds[1]; i >= 0; i--) {
                                if (validationSheet.getRow(i) != null) {
                                    if (validationSheet.getRow(i).getCell(0) != null) {

                                        String cellText = SheetActions
                                                .getCellValueAsString(validationSheet.getRow(i).getCell(0));
                                        System.out.println("cellText:" + cellText);
                                        if (!cellText.isEmpty()) {
                                            lastRowText = cellText;
                                            outData.setLogArea(
                                                    "Attempting to extract case counter from last row with text: "
                                                            + lastRowText);
                                            break;
                                        }
                                    }
                                }
                            }

                            //Get the counter value from the text present in last row of the group. 
                            String prefixText = lastRowText.split("_", 2)[0].trim();
                            outData.setLogArea("Last test case in group: " + prefixText);
                            AppHandler.setCount(Integer.parseInt(prefixText.substring(4)));
                            AppHandler.incrementCounter();
                            outData.setLogArea(
                                    "Case Counter set to start from " + AppHandler.getCount());

                        } catch (Exception e) {
                            outData.setLogArea(
                                    "Error extracting last text in group boundary for 'External Testing - Modified Cases' mode");
                            outData.setLogArea(e.toString());
                            JOptionPane.showMessageDialog(null,
                                    "A problem occured while processing. Check log for more details");

                            outData.setLogArea("Error Occured: " + e.toString());
                            e.printStackTrace();
                            return -1;

                        }
                        //Set row group start and end for external testing - modified cases mode
                        rowGroupingStart = matchRowIndex + 1;
                        rowGroupEnd = groupBounds[1] + validationList.size() + 3; //+3 because, 1 for the next row after group end boundary, 1 for header, 1 for blank space
                        outData.setLogArea("Found ADO '" + adoNumber + "' at row: " + (matchRowIndex + 1)
                                + ". Proceeding 'External Testing - Modified Cases' mode for data range("
                                + rowGroupingStart
                                + "-" + (groupBounds[1]+1) + ")");
                        
                        //Shift rows down
                        if ((groupBounds[1] + 1) <= validationSheet.getLastRowNum()) {
                            SheetActions.moveRowsDown(validationSheet, (groupBounds[1] + 1), validationList.size() + 3);
                            caseHeaderRow = groupBounds[1] + 1;

                            header = "External Testing - Modified Cases for ADO " + adoNumber + " #" + (SheetActions
                                    .findTextInFirstColumn(validationSheet, adoNumber, rowGroupingStart, rowGroupEnd)
                                    .size()
                                    + 1);

                            validationHeader.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());

                            outData.setLogArea(
                                    "Shifted existing data in range from " + (groupBounds[1] + 1) + " by moving it"
                                            + (validationList.size() + 2) + " lines below.");
                            externalTestCaseFlag = true;
                        } else {
                            outData.setLogArea("Invalid data movement from " + (groupBounds[1] + 1) + " to "
                                    + validationList.size() + 1 + ". Last row with data:"
                                    + validationSheet.getLastRowNum());
                            JOptionPane.showMessageDialog(null,
                                    "A problem occured while processing. Check log for more details");
                            return -1;

                        }
                    } else {
                        outData.setLogArea("write operation cancelled by user");
                        return -1;
                    }
                }
            } catch (Exception e) {
                outData.setLogArea(
                        "Error extracting last text in group boundary for 'External Testing - Modified Cases' mode");
                outData.setLogArea(e.toString());
                JOptionPane.showMessageDialog(null, "A problem occured while processing. Check log for more details");

                outData.setLogArea("Error Occured: " + e.toString());
                e.printStackTrace();
                return -1;
            }
        }

        Row headerRow = validationSheet.createRow(caseHeaderRow + rowCount);
        outData.setLogArea("Writing " + header);

        // Create cells for header row and set data
        for (int c = 0; c <= 13; c++) {
            Cell h1 = headerRow.createCell(c);
            h1.setCellStyle(validationHeader);
        }
        headerRow.getCell(0).setCellValue(header);
        rowCount++;

        if (!externalTestCaseFlag) {
            AppHandler.setCount(1);
            outData.setLogArea("Case Counter set to start from 1");
        }

        // Iterate through linkedHashMap
        for (Map.Entry<String, Validation> validationEntry : validationList.entrySet()) {
            Validation currentValidation = validationEntry.getValue();

            // Stricking out the previous case present
            if (externalTestCaseFlag) {
                List<Integer> indexList = new ArrayList<Integer>();

                indexList = SheetActions.findTextInFirstColumn(validationSheet, currentValidation.getEdiCase(),
                        rowGroupingStart,
                        externalCaseGroupBoundEnd);
                if (indexList.size() > 0) {
                    System.out.println(indexList.toString());
                    for (int i : indexList) {

                        Row matchedRow = validationSheet.getRow(i);
                        for (Cell matchedcell : matchedRow) {
                            matchedcell.setCellStyle(strickenCell);
                        }
                        outData.setLogArea(
                                "Test case:" + currentValidation.getEdiCase() + " matching with text in row " + (i + 1) + ": "+ validationSheet.getRow(i).getCell(0).getStringCellValue()
                                        + " is stricken.");
                    }

                }
            }

            // Creating new row and corresponding cells to enter validation case

            Row newRow = validationSheet.createRow(caseHeaderRow + rowCount);
            Cell testCaseCell = newRow.createCell(testCaseColumn);// test case column
            Cell descriptionCell = newRow.createCell(descriptionColumn);// description column
            Cell expectedResultsCell = newRow.createCell(expectedResultColumn);// Expected result column
            testCaseCell.setCellStyle(dataCell);
            descriptionCell.setCellStyle(dataCell);
            expectedResultsCell.setCellStyle(dataCell);

            testCaseCell.setCellValue("Test" + AppHandler.getCount() + "_"
                    + currentValidation.getEdiCase());
            outData.setLogArea("Test Case " + AppHandler.getCount() + " written");

            expectedResultsCell.setCellValue(currentValidation.getEdiMessage());

            if (operationtype.equalsIgnoreCase("N"))
                descriptionCell.setCellValue(currentValidation.getEdiCondition());
            else
                descriptionCell.setCellValue(currentValidation.getEdiCondition());

            rowCount++;
            AppHandler.incrementCounter();

        }

        if (rowGroupEnd == 0)
            rowGroupEnd = rowGroupingStart + rowCount - 2;// -1 for removing header as it is not collapsed. Another -1
                                                          // as groupRow is 0 based index and rowCount is not.

        validationSheet.groupRow(rowGroupingStart, rowGroupEnd); // Group rows for collapse
        validationSheet.setRowGroupCollapsed(rowGroupingStart, true); // Set the group to be collapsed
        outData.setLogArea(
                "Group range created (" + (rowGroupingStart + 1) + "-" + (rowGroupEnd + 1)
                        + "). Group set to collapse state");

        return rowCount;

    }

    // Write the workbook
    public static void writeWorkbook(String filePath, String planName, Workbook testplan, ResultData outData)
            throws IOException {

        planName = planName.replace(".docx", "");
        planName = planName.replace("_Map", "");
        planName = planName.replace("_map", "");
        planName = planName.replace("_MAP", "");
        planName = planName.replace(".xlsx", "");
        planName = planName.replace(".xls", "");
        planName = planName.replace("_Test_Plan", "");

        filePath = filePath + "\\" + planName + "_Test_Plan.xlsx";
        File fileToSave = new File(filePath);

        if (fileToSave.exists()) {
            System.out.println("checking if file exists");
            int result = JOptionPane.showConfirmDialog(
                    null, // or null if no parent
                    "The file '" + fileToSave.getName() + "' already exists.\nDo you want to replace it?",
                    "File Already Exists",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (result == JOptionPane.YES_OPTION) {
                // User chose to overwrite - proceed with saving
                FileOutputStream fos = new FileOutputStream(filePath);
                System.out.println("Write Success @ " + filePath);
                outData.setLogArea(
                        "Test Plan generated and written in " + filePath);
                JOptionPane.showMessageDialog(null, "Test plan Generated successfully in " + filePath);

                testplan.write(fos);
                fos.close();
                testplan.close();
                AppHandler.setCount(1);
            }

            else {
                outData.setLogArea("File already exists. Move the existing file " + filePath
                        + " and re-try generating the test plan");
                JOptionPane.showMessageDialog(null, "A problem occured while processing. Check log for more details");

                // User chose not to overwrite - cancel the save operation
                return;
            }
        } else {
            FileOutputStream fos = new FileOutputStream(filePath);
            JOptionPane.showMessageDialog(null, "Test plan Generated successfully in " + filePath);
            outData.setLogArea(
                    "Test Plan generated and written to " + filePath);
            testplan.write(fos);
            fos.close();
            testplan.close();
        }

    }

}
