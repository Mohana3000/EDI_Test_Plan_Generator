package com.tpg.View.MethodHandler;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class ButtonMethods extends JFrame {

    private String adoNumber;
    private boolean createPlanFlag;
    private String columnChoice;

    public void handleGenerateClick() {
        // Show input dialog for ADO Number
        String adoNumber = (String) JOptionPane.showInputDialog(
            this,
            "Enter ADO Number",
            "ADO Number Input",
            JOptionPane.PLAIN_MESSAGE,
            null,
            null,
            ""
        );
        
        // Check if dialog was cancelled or input is empty
        if (adoNumber == null) {
            return; // User cancelled
        }
        
        if (adoNumber.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "ADO Number is required Input. Re-try again",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }
        setAdoNumber(adoNumber);
        
        
        // Show confirmation dialog for default columns
        String[] options = {"Use Default Order", "Enter Custom Order"};
        int choice = JOptionPane.showOptionDialog(
            this,
            "Default column for test plan is:\n\nTest Case - column 0\nDescription - column 6\nExpected Results - column 7\n\nDo you wish to proceed with same?",
            "Column Configuration",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,null,
    options,
    options[0]
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            setCreatePlanFlag(true);
            setColumnChoice("0/6/7");
        } else if (choice == JOptionPane.NO_OPTION) {
            handleCustomColumnInput();
        }
    }
    
    public String getAdoNumber() {
        return adoNumber;
    }

    public void setAdoNumber(String adoNumber) {
        this.adoNumber = adoNumber;
    }

    public boolean isCreatePlanFlag() {
        return createPlanFlag;
    }

    public void setCreatePlanFlag(boolean createPlanFlag) {
        this.createPlanFlag = createPlanFlag;
    }

    public String getColumnChoice() {
        return columnChoice;
    }

    public void setColumnChoice(String columnChoice) {
        this.columnChoice = columnChoice;
    }

    public void handleCustomColumnInput() {
        // Show input dialog for custom column numbers
        String columnInput = (String) JOptionPane.showInputDialog(
                this,
                "Enter the column number for TestCase/Description/Expected Results.\nExample: 2/5/6\n\nNote: Column order starts with 0",
                "Custom Column Input",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                "");

        // Check if dialog was cancelled
        if (columnInput == null) {
            return; // User cancelled
        }

        if (columnInput.trim().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Column numbers are mandatory if you want custom arrangement",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validate format: number/number/number
        if (isValidColumnFormat(columnInput.trim())) {
            setColumnChoice(columnInput);
            setCreatePlanFlag(true);
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Invalid format. Please enter in format: number/number/number (e.g., 2/5/6)",
                    "Format Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean isValidColumnFormat(String input) {
        // Check if input matches pattern: number/number/number
        String[] parts = input.split("/");

        if (parts.length != 3) {
            return false;
        }

        for (String part : parts) {
            try {
                int num = Integer.parseInt(part.trim());
                if (num <= 0) {
                    return false; // Column numbers should be positive
                }
            } catch (NumberFormatException e) {
                return false;
            }
        }

        return true;
    }
}
