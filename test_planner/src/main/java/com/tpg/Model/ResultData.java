package com.tpg.Model;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.*;

public class ResultData {
    JTextArea PreviewArea;
    JTextArea logArea;

    public ResultData() {
        super();
    }

    public ResultData(JTextArea PreviewArea, JTextArea logArea) {
        this.logArea = logArea;
        this.PreviewArea = PreviewArea;
    }

    public String getPreviewAreaText() {
        return PreviewArea.getText().trim();
    }

    public JTextArea getPreviewArea() {
        return PreviewArea;
    }

    public String getLogAreaText() {
        return logArea.getText().trim();
    }

    public void setPreviewArea(String PreviewAreaText) {
        this.PreviewArea.setText(PreviewArea.getText() + PreviewAreaText);
    }

    public void setLogArea(String logAreaText) {
        this.logArea.setText(logArea.getText() + "\n" + LocalDateTime.now() + ": " + logAreaText);
    }

    public void setPreviewText(LinkedHashMap<String, Validation> data, JTextArea PreviewArea) {

        StringBuilder outText = new StringBuilder();
        outText.append("Preview of cases to be added in Test Plan:\n ");
        for (Map.Entry<String, Validation> validationEntry : data.entrySet()) {

            Validation currentValidation = validationEntry.getValue();
            outText.append("\nTest Case ID                                 : " + currentValidation.getEdiCase() + "\n");
            if (!currentValidation.getEdiCondition().isEmpty())
                outText.append("\nTest Case Condition/Description  : " + currentValidation.getEdiCondition());
            if (!currentValidation.getEdiMessage().isEmpty())
                outText.append("\nTest Case Message                      : " + currentValidation.getEdiMessage());
            if (!currentValidation.getValidationType().isEmpty())
                outText.append("\nAction                                            : "
                        + currentValidation.getValidationType() + " case created");
            outText.append("\n____________________________________________________________________\n");

        }
        PreviewArea.setText(outText.toString());
    }

}