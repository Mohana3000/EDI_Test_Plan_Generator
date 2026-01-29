package com.tpg.Service;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import com.tpg.Model.ResultData;
import com.tpg.Model.Validation;

public class MapActions {

    // Read document from path
    public static XWPFDocument readDocument(String filePath, ResultData outData) {

        try {
            FileInputStream fis = new FileInputStream(filePath);

            XWPFDocument document = new XWPFDocument(fis);

            return document;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "A problem occured while processing. Check log for more details");
            outData.setLogArea("Error Occured: "+ e.toString());
            e.printStackTrace();
        }
        return null;
    }

    // Create Validation cases based on the paragraphs present in map
    public static LinkedHashMap<String, Validation> createMapBasedCases(
            XWPFDocument document, String operationType, ResultData outData) {

        // Initialise
        LinkedHashMap<String, Validation> validationList = new LinkedHashMap<>();
        List<String> cdfList = new ArrayList<>();
        Pattern pattern = Pattern.compile("(?i)(Create ED|Create Discard)");
        Pattern cdfPattern = Pattern.compile("(?i).cdf");

        // Get paragraphs from document to process
        List<XWPFParagraph> paragraphs = document.getParagraphs();

        // Create Good Case as first case
        Validation goodCase = new Validation();
        goodCase.setEdiCase("Good_Output");
        goodCase.setEdiCondition("Generate good output");
        goodCase.setValidationType("New");
        validationList.put(goodCase.getEdiCase(), goodCase);
        

        for (int i = 0; i < paragraphs.size(); i++) {

            // Get current paragraph words and check if it matches pattern
            XWPFParagraph currentParagraph = paragraphs.get(i);
            Matcher matcher = pattern.matcher(currentParagraph.getText());
            Matcher cdfMatcher = cdfPattern.matcher(currentParagraph.getText());

            // If current paragraph has .cdf, extract the cdf name and add it to cdfList.
            if (cdfMatcher.find()) {
                String[] words = currentParagraph.getText().split("\\s+");
                for (String word : words) {
                    if (word.contains(".cdf")) {
                        word.replaceAll("'", "");
                        cdfList.add(word);
                        break;
                    }
                }
            }

            // If current paragraph has ediValidation match, Get currentCase and obtain it's
            // validation details.
            // For non-removed cases, check if subcases are possible, if so add the subcases
            // to ValidationList. else add the case directly to validationList.
            // For Removed cases, append _Good_Case at the end and add it to Validaiton
            // List.

            if (matcher.find()) {
                // validationCase currentCase = new validationCase(paragraphs, i, cdfList,
                // operationType);

                try {
                    GenerateCase currentCase = new GenerateCase(paragraphs, i, cdfList, operationType);
                    Validation currentValidation = currentCase.getCaseObj();

                    if (!currentValidation.getEdiCase().isEmpty() && currentValidation.getEdiCase() != null) { 

                        if (currentValidation.getValidationType().equals("Removed")) {

                            String parentTestCase = currentValidation.getEdiType()
                                    + "_"
                                    + currentValidation.getEdiCase() + "_Good_Case";
                            

                            currentValidation.setEdiCase(parentTestCase);
                            validationList.put(currentValidation.getEdiCase(), currentValidation);

                        } else {
                            List<String> parsedCases = parseEdiCondition(currentValidation.getEdiCondition());
                            if (parsedCases.size() > 0)
                                for (String ediParsedCondition : parsedCases) {
                                    String parentTestCase = 
                                            currentValidation.getEdiType()
                                            + "_"
                                            + currentValidation.getEdiCase() + ediParsedCondition;

                                    

                                    // Deep clone currentValidation for creating subcases with parsedConditions
                                    GenerateCase subCase = new GenerateCase(currentValidation);
                                    Validation parsedValidation = subCase.getCaseObj();
                                    parsedValidation.setEdiCase(parentTestCase);
                                    validationList.put(parsedValidation.getEdiCase(), parsedValidation);
                                }
                            else {

                                String parentTestCase = currentValidation.getEdiType() + "_"
                                        + currentValidation.getEdiCase();
                                

                                currentValidation.setEdiCase(parentTestCase);
                                validationList.put(currentValidation.getEdiCase(), currentValidation);
                            }

                        }
                    }

                }

                catch (Exception e) {
                    outData.setLogArea("Update text in map to be in correct format. The three important segments Test Case Condition,Create statement,Validation should be present after each other in below format"
                                    + "\nCorrect format is:\n\n<Edi Condition>\n      Create EDE/EDR/EDN/Discard\n    <[validation ID],[Validation Message]>\n\nTroubleshooting tip:Ensure the message and id and separated by comma & ensure there are no newlines present between the 3 segments. You don't have to worry about spaces/indendation checks.");
                    outData.setLogArea("Please check the section of map that contains the below text:\nParagraph 1: "
                            + paragraphs.get(i - 1).getText() + "\nParagraph 2: " + paragraphs.get(i).getText()
                            + "\nParagraph 3: " + paragraphs.get(i + 1).getText() + "\n");
                    JOptionPane.showMessageDialog(null, "A problem occured while processing. Check log for more details");
                    outData.setLogArea("Error Occured: "+ e.toString());
                    e.printStackTrace();

                    validationList.clear();
                    outData.setPreviewArea(
                            "Program Terminated with Error. Check the log details below for more information");
                    break;

                }
            }

        }

        return validationList;

    }

    // Extract data of table based on the column name present - Not called anywhere
    // currently.
    public static List<Map<String, String>> extractTableData(XWPFDocument document, String columnName,
            String oprationType) {

        List<XWPFTable> tables = document.getTables();
        List<Map<String, String>> tableData = new ArrayList<>();
        columnName = columnName.toUpperCase();

        for (XWPFTable table : tables) {

            List<XWPFTableRow> rows = table.getRows();
            if (!rows.isEmpty()) {
                List<String> headers = new ArrayList<>();
                XWPFTableRow headerRow = rows.get(0);
                for (XWPFTableCell cell : headerRow.getTableCells()) {
                    headers.add(cell.getText().trim().toUpperCase());
                }

                if (headers.contains(columnName)) {

                    for (int i = 1; i < rows.size(); i++) {

                        Map<String, String> rowData = new LinkedHashMap<>();
                        XWPFTableRow row = rows.get(i);

                        for (int j = 0; j < row.getTableCells().size(); j++) {
                            String header = headers.get(j);
                            String data = row.getCell(j).getText().trim();

                            if (oprationType.equalsIgnoreCase("E")) {
                                String nonStrickedvalue = "";
                                String strickedValue = "";

                                for (XWPFParagraph paragraph : row.getCell(j).getParagraphs()) {
                                    for (XWPFRun run : paragraph.getRuns()) {
                                        if (run.isHighlighted()) {
                                            if (!run.isStrikeThrough())
                                                nonStrickedvalue += run.getText(0);
                                            else
                                                strickedValue += run.getText(0);
                                        }
                                    }
                                }

                                if (data.equalsIgnoreCase(strickedValue) & !strickedValue.isEmpty())
                                    data = "Good Case: " + data + " - Removed";
                                else
                                    data = nonStrickedvalue;

                                rowData.put(header, data);
                            }

                            else if (oprationType.equalsIgnoreCase("N"))
                                rowData.put(header, data);

                        }
                        tableData.add(rowData);

                    }
                }
            }
        }

        return tableData;
    }

    // Get the table under a specific title/text
    public static XWPFTable findTableUnderTitle(XWPFDocument document, String title) {
        List<IBodyElement> bodyElements = document.getBodyElements();
        boolean titleFound = false;

        for (IBodyElement element : bodyElements) {
            if (element instanceof XWPFParagraph) {
                XWPFParagraph paragraph = (XWPFParagraph) element;
                String paragraphText = paragraph.getText().trim();

                // Check if this paragraph contains the target title
                if (paragraphText.toLowerCase().contains(title.toLowerCase())) {
                    titleFound = true;
                }
            } else if (element instanceof XWPFTable && titleFound) {
                // Return the first table found after the title
                return (XWPFTable) element;
            }
        }

        return null;
    }

    // Create standard validations as linkedHashMap from the table
    public static LinkedHashMap<String, Validation> createStandardValidation(XWPFTable table,
            String oprationType) {

        List<XWPFTableRow> rows = table.getRows();
        LinkedHashMap<String, Validation> standardValidations = new LinkedHashMap<>();

        if (rows.isEmpty()) {
            return standardValidations;
        }

        // Get headers from the first row
        List<String> headers = new ArrayList<>();
        XWPFTableRow headerRow = rows.get(0);
        for (XWPFTableCell cell : headerRow.getTableCells()) {
            headers.add(cell.getText().trim().toUpperCase());
        }

        // Process data rows
        for (int i = 1; i < rows.size(); i++) {
            XWPFTableRow row = rows.get(i);
            List<XWPFTableCell> cells = row.getTableCells();

            if (cells.size() >= 3) {
                String standardKey = extractCellText(cells.get(0), oprationType);

                // //Highlight and stricked checks for Enhancement in requiredParameters and
                // clientSpecificLogic - disabeld for now i.e, extractCellText is not used
                // instead getText() is used.
                // requiredParameters is obtained but usage has been remove in this version v4

                // String requiredParameters = cells.get(1).getText().trim();

                String clientSpecificLogic = cells.get(2).getText().trim();

                // Process checkboxes in the third column
                List<String> parsedStandardParameters = extractParsedParameters(clientSpecificLogic);

                // Initialise Validation object

                if (!standardKey.isEmpty()) {
                    if (parsedStandardParameters.isEmpty()) {
                        // If no checkboxes are checked, create a single entry
                        Validation currentValidation = new Validation();
                        currentValidation.setEdiCase("EDE_" + standardKey);

                        currentValidation.setValidationType("Standard");
                        currentValidation.setEdiType("EDE");
                        standardValidations.put(currentValidation.getEdiCase(), currentValidation);

                    } else {
                        // Create multiple entries for each checked item
                        for (String parameter : parsedStandardParameters) {
                            Validation currentValidation = new Validation();
                            String combinedKey = standardKey + "_" + parameter.replaceAll("[^a-zA-Z0-9_]", "_");
                            currentValidation.setEdiCase("EDE_" + combinedKey);

                            currentValidation.setValidationType("Standard");
                            currentValidation.setEdiType("EDE");
                            standardValidations.put(currentValidation.getEdiCase(), currentValidation);

                        }
                    }
                }
            }
        }

        return standardValidations;
    }

    // Removing stricken values and extracted only highlighted values - Used in
    // createStandardValidation
    public static String extractCellText(XWPFTableCell cell, String oprationType) {
        if (oprationType.equalsIgnoreCase("E")) {
            StringBuilder nonStrickedValue = new StringBuilder();
            StringBuilder strickedValue = new StringBuilder();

            for (XWPFParagraph paragraph : cell.getParagraphs()) {
                for (XWPFRun run : paragraph.getRuns()) {
                    if (run.isHighlighted()) {
                        String runText = run.getText(0);
                        if (runText != null) {
                            if (!run.isStrikeThrough()) {
                                nonStrickedValue.append(runText);
                            } else {
                                strickedValue.append(runText);
                            }
                        }
                    }
                }
            }

            String cellText = cell.getText().trim();
            String strickedText = strickedValue.toString();

            if (cellText.equalsIgnoreCase(strickedText) && !strickedText.isEmpty()) {
                return "Good_Case_" + cellText;
            } else {
                return nonStrickedValue.toString();
            }
        } else {
            return cell.getText().trim();
        }
    }

    // Get the text value next to checkboxes that are marked checked - Used in
    // createStandardValidation
    public static List<String> extractParsedParameters(String text) {
        List<String> checkedParameters = new ArrayList<>();
        Set<String> mode = new HashSet<>();
        List<String> conditionalCases = new ArrayList<>();

        // Pattern to match checked boxes (☒) followed by text
        Pattern checkedPattern = Pattern.compile("☒\\s*([^☐☒\\n]+)");
        Matcher matcher = checkedPattern.matcher(text);

        Set<String> excludeKeywords = new HashSet<>(Arrays.asList(
                "CHECK MODE", "CHECK INPUT PARAMETER", "CHECK PORT", "CHECK MODE/INPUT PARAMETER",
                "CHECK MODE/PORT", "CHECK", "OCEAN", "AIR", "TRUCK"));

        while (matcher.find()) {
            String item = matcher.group(1).trim();
            // Clean up the item text
            item = item.replaceAll("^>\\s*", "").trim(); // Remove leading > symbols
            if (!item.isEmpty()) {
                // Check if this item should be excluded from key generation
                boolean shouldExclude = false;
                String itemUpper = item.toUpperCase();

                for (String keyword : excludeKeywords) {
                    if (itemUpper.contains(keyword)) {
                        shouldExclude = true;
                        break;
                    }
                }

                // Only add items that are not mode/parameter indicators
                // Focus on specific port names or entity names
                if (!shouldExclude) {
                    checkedParameters.add(item);
                }
                // If mode is detected, add it to set
                else {
                    if (itemUpper.contains("OCEAN"))
                        mode.add("OCEAN");
                    else if (itemUpper.contains("AIR"))
                        mode.add("AIR");
                    else if (itemUpper.contains("TRUCK"))
                        mode.add("TRUCK");
                }
            }
        }

        // Create conditions based on paramters and modes
        if (checkedParameters.isEmpty()) {
            conditionalCases.addAll(mode);
        } else {
            for (String caseKey : checkedParameters) {
                if (!mode.isEmpty()) {
                    for (String currentMode : mode)
                        conditionalCases.add(caseKey + "_" + currentMode);
                } else {
                    conditionalCases.add(caseKey);
                }

            }
        }

        return conditionalCases;
    }

    // Generates multiple cases based on key words found on condition text - Used in
    // createMapCaseList
    public static List<String> parseEdiCondition(String conditionPart) {
        List<String> result = new ArrayList<>();

        if (conditionPart == null || conditionPart.isEmpty())
            return result;

        // Normalize quotes, braces, and punctuation
        conditionPart = conditionPart.replaceAll("[{}“”‘’\"']", "");
        conditionPart = conditionPart.replaceAll("[().]", ""); // remove parentheses and dots

        // Convert to lowercase for case-insensitive checks
        String normalized = conditionPart.toLowerCase();

        // Check for <> or "not equals" variations
        if (normalized.contains("<>") ||
                normalized.contains("not equals to") ||
                normalized.contains("not equals")) {
            result.add("_Invalid_Value");
            result.add("_Good");
            return result;
        }

        if (normalized.contains("format")) {
            result.add("_Invalid_Format");
            return result;
        }

        // Check for 'not found'
        if (normalized.contains("not found")) {
            result.add("_Match_Not_Found");
        }

        // Look for common failure keywords (case-insensitive)
        String[] knownConditions = { "blank", "missing", "invalid" };
        for (String keyword : knownConditions) {
            if (normalized.contains(keyword)) {
                // Capitalize first letter in output
                result.add("_" + capitalize(keyword) + "_Value");
            }
        }

        if (!result.isEmpty())
            return deduplicate(result);
        else {
            result.add("");
            return result;
        }
    }

    // Preserve order and remove duplicates - Used in parseEdiCondition
    private static List<String> deduplicate(List<String> list) {
        return new ArrayList<>(new LinkedHashSet<>(list)); // preserve order, remove duplicates
    }

    // capitalize the first letter of each word - Used in parseEdiCondition
    private static String capitalize(String word) {
        if (word == null || word.isEmpty())
            return word;
        return word.substring(0, 1).toUpperCase() + word.substring(1);
    }
}
