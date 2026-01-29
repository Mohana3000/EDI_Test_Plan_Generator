package com.tpg.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import com.tpg.Model.Validation;

//This class has the logic to create validation case based on the text in paragraph having Create ED/Create Discard key words and the paragraph preceeding and succeeding it.
//A Validation object (caseObj) is created and instantiated everytime an object for Generate Case is created.
//The validation case details are bound to Validation object.
//The details can be obtained using Getters and Setters of the object. 

public class GenerateCase {

    String[] validationMessage;
    Pattern cdfDependantPattern = Pattern.compile("(?i)(Match |column)");

    // Create validation object and generate Getter.
    private Validation caseObj = new Validation();

    public Validation getCaseObj() {
        return caseObj;
    }

    //Constructor that assigns values to caseObj. 
    GenerateCase(List<XWPFParagraph> paragraphs, int i, List<String> cdfList, String operationType)
            throws IndexOutOfBoundsException {

        // Get previous, current and next paragraph
        XWPFParagraph currentParagraph = paragraphs.get(i);
        XWPFParagraph nextParagraph = paragraphs.get(i + 1);
        XWPFParagraph prevParagraph = paragraphs.get(i - 1);

        // Initialise cdfMatcher to check if the condtion has cdf related logic.
        Matcher cdfDependantMatcher = cdfDependantPattern.matcher(prevParagraph.getText());


        // Determine ediMessage and ediCase from text in next paragraph.
        String nextLineValue = nextParagraph.getText().trim();
        validationMessage = nextLineValue.replaceAll("\t", "").split(",");
        caseObj.setEdiMessage(extractEdiMessage(validationMessage));
        String extractedEdiId = extractEdiID(validationMessage);
        if(extractedEdiId.toLowerCase().contains("discard"))
        {
            nextLineValue.replaceAll("discard|DISCARD|Discard", "");
        }
        caseObj.setEdiCase(extractedEdiId);

        // Determine ediType from text in current paragraph.
        String currentParaString = currentParagraph.getText().trim().replace(',', ' ');
        if (currentParaString.toLowerCase().contains("ede"))
            caseObj.setEdiType("EDE");
        else if (currentParaString.toLowerCase().contains("edr"))
            caseObj.setEdiType("EDR");
        else if (currentParaString.toLowerCase().contains("edn"))
            caseObj.setEdiType("EDN");
        else if (currentParaString.toLowerCase().contains("discard")) {           
                caseObj.setEdiType("Discard");
        }

        // Determine ediCondition from text in previous paragraph.
        if (cdfDependantMatcher.find())
            caseObj.setEdiCondition(prevParagraph.getText().trim() + " in " + cdfList.get(cdfList.size() - 1));
        else
            caseObj.setEdiCondition(prevParagraph.getText().trim());

        // Determine validationType
        if (operationType.equalsIgnoreCase("N")) {
            caseObj.setValidationType("New");

        } else if (operationType.equalsIgnoreCase("E")) {

            // Check if highlight is present
            if (!isHighlighted(currentParagraph).isEmpty()) {

                // If there is no stricking, validation added in this enhancement
                if (!isStricked(currentParagraph))
                    caseObj.setValidationType("New");

                // As there is stricking, validation is removed in this enhancement
                else
                    caseObj.setValidationType("Removed");

            }

            // Validation Modified in this enhancement if enhancement changes are
            // present in messages or validation ID
            // Overwite caseObj values removing stricked text
            else if (!isHighlighted(prevParagraph).isEmpty() || !isHighlighted(nextParagraph).isEmpty()) {

                // Since modified validations contain stricked values, ediCase, ediMessage,
                // ediContion values are determined here after removing stricken text and
                // overwritten in place of above determined values.

                String unstrickedNextLineValue = removeStricked(nextParagraph);
                validationMessage = unstrickedNextLineValue.replaceAll("\t", "").split(",");
                caseObj.setEdiCase(extractEdiID(validationMessage));
                caseObj.setEdiMessage(extractEdiMessage(validationMessage));
                caseObj.setEdiCondition(removeStricked(prevParagraph));
                caseObj.setValidationType("Modified");
            }
            else{
                caseObj.setEdiCase("");
                caseObj.setEdiCondition("");
                caseObj.setEdiMessage("");
                caseObj.setValidationType("");
            }

        }

    }

    // Deep-Copy of Construction with Validation object
    GenerateCase(Validation anotherObj) {
        this.caseObj.setEdiCase(anotherObj.getEdiCase());
        this.caseObj.setEdiCondition(anotherObj.getEdiCondition());
        this.caseObj.setEdiMessage(anotherObj.getEdiMessage());
        this.caseObj.setEdiType(anotherObj.getEdiMessage());
        this.caseObj.setValidationType(anotherObj.getValidationType());
    }

    private static String isHighlighted(XWPFParagraph paragraph) {

        String highlightedText = "";
        for (XWPFRun run : paragraph.getRuns()) {
            if (run.isHighlighted()) {
                highlightedText = highlightedText + run.getText(0);
            }
        }
        return highlightedText;
    }

    public static boolean isStricked(XWPFParagraph paragraph) {
        boolean checkStriked = false;
        List<XWPFRun> runs = paragraph.getRuns();
        for (XWPFRun run : runs) {
            if (run.isStrikeThrough()) {
                checkStriked = true;
                break;
            }
        }
        return checkStriked;
    }

    public static String removeStricked(XWPFParagraph paragraph) {
        String modifiedtext = "";

        List<XWPFRun> runs = paragraph.getRuns();
        for (XWPFRun run : runs) {
            if (!run.isStrikeThrough()) {
                modifiedtext += run.getText(0);
            }
        }
        return modifiedtext;
    }

    public static String extractEdiMessage(String[] validationMessage)throws IndexOutOfBoundsException {
        return validationMessage[1].trim();

    }

    public static String extractEdiID(String[] validationMessage) throws IndexOutOfBoundsException {
      return validationMessage[0].trim().replace("\\s", "");              
    }

}
