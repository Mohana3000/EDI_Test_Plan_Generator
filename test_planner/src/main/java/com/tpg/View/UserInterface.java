package com.tpg.View;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.tpg.View.Component.CustomScrollBarUI;
import com.tpg.View.MethodHandler.ButtonMethods;
import com.tpg.Controller.AppHandler;
import com.tpg.Model.CustomCase;
import com.tpg.Model.ResultData;
import com.tpg.Model.UserInput;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;

public class UserInterface extends JFrame {

    private static final Font DEFAULT_FONT = new Font("Cambria", Font.BOLD, 19);
    private static final Font DISPLAY_TEXT_FONT = new Font("Aptos", Font.PLAIN, 18);
    private static final Color LAVENDAR = new Color(114, 109, 168);
    private static final Border LAVENDAR_BORDER = new LineBorder(Color.decode("#726DA8"), 1);
    private static final ImageIcon APP_ICON = new ImageIcon(
            UserInterface.class.getClassLoader().getResource("tpg_icon.png"));

    public UserInterface() {

        AppHandler appTPG = new AppHandler();

        setIconImage(APP_ICON.getImage());
        setTitle("Test Plan Generator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1600, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setBackground(Color.WHITE);

        JPanel gridPanel = new JPanel(new GridBagLayout());
        gridPanel.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(40, 25, 15, 35);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER;

        UIManager.put("OptionPane.messageFont", new Font("Arial", Font.BOLD, 14));

        int row = 1;

        // === Row 0: Radio Buttons in ButtonGroup ==============================
        gbc.gridy = row++;
        gbc.gridx = 0;
        JLabel operation = setFont(new JLabel("Select your operation:"));
        JRadioButton option1 = setFont(new JRadioButton("New"));
        JRadioButton option2 = setFont(new JRadioButton("Enhancement"));
        option1.setBackground(Color.WHITE);
        option2.setBackground(Color.WHITE);
        option1.setFocusable(false);
        option1.setOpaque(false);
        option2.setFocusable(false);
        option2.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        group.add(option1);
        group.add(option2);

        gridPanel.add(operation, gbc);
        gbc.gridx = 1;
        gridPanel.add(option1, gbc);
        gbc.gridx = 2;
        gridPanel.add(option2, gbc);

        // === Row 1: Word Doc File Picker ==============================
        gbc.insets = new Insets(5, 25, 15, 35);

        gbc.gridy = row++;
        gbc.gridx = 0;
        gridPanel.add(setFont(new JLabel("Choose Map Path:")), gbc);

        JTextField mapField = setFont(new JTextField());
        gbc.gridx = 1;
        gbc.weightx = 1;
        mapField.setBorder(LAVENDAR_BORDER);
        mapField.setFont(DISPLAY_TEXT_FONT);
        gridPanel.add(mapField, gbc);

        JButton browseMap = setFont(new JButton("Browse"));
        browseMap.setBackground(LAVENDAR);
        browseMap.setForeground(Color.WHITE);
        gbc.gridx = 2;
        gbc.weightx = 0;
        browseMap.addActionListener(e -> chooseFileWithFilter(mapField, "Word Documents", "doc", "docx"));
        gridPanel.add(browseMap, gbc);

        setSameHeight(mapField, browseMap);

        // === Row 2: Excel File Picker ==============================
        gbc.gridy = row++;
        gbc.gridx = 0;
        JLabel planLabel = setFont(new JLabel("Choose Existing TestPlan Path:"));
        gridPanel.add(planLabel, gbc);

        JTextField planField = setFont(new JTextField());
        gbc.gridx = 1;
        gbc.weightx = 1;
        planField.setBorder(LAVENDAR_BORDER);
        planField.setFont(DISPLAY_TEXT_FONT);
        gridPanel.add(planField, gbc);

        JButton browsePlan = setFont(new JButton("Browse"));
        browsePlan.setBackground(LAVENDAR);
        browsePlan.setForeground(Color.WHITE);
        gbc.gridx = 2;
        gbc.weightx = 0;
        browsePlan
                .addActionListener(e -> chooseFileWithFilter(planField, "Excel Files (*.xls, *.xlsx)", "xls", "xlsx"));
        gridPanel.add(browsePlan, gbc);

        setSameHeight(planField, browsePlan);

        // Initially disable Row 2 controls
        planLabel.setEnabled(false);
        planField.setEnabled(false);
        browsePlan.setEnabled(false);

        // Enable plan components if option2 is selected
        option2.addActionListener(e -> {
            planLabel.setEnabled(true);
            planField.setEnabled(true);
            browsePlan.setEnabled(true);
        });

        // Disable plan components if option1 is selected
        option1.addActionListener(e -> {
            planLabel.setEnabled(false);
            planField.setEnabled(false);
            browsePlan.setEnabled(false);
        });

        // === Row 3: Output path Picker ==============================
        gbc.gridy = row++;
        gbc.gridx = 0;
        JLabel outputLabel = setFont(new JLabel("Choose path to write generated test plan:"));
        gridPanel.add(outputLabel, gbc);

        JTextField ouptutField = setFont(new JTextField());
        gbc.gridx = 1;
        gbc.weightx = 1;
        ouptutField.setBorder(LAVENDAR_BORDER);
        ouptutField.setFont(DISPLAY_TEXT_FONT);
        gridPanel.add(ouptutField, gbc);

        JButton browseOutputPath = setFont(new JButton("Browse"));
        browseOutputPath.setBackground(LAVENDAR);
        browseOutputPath.setForeground(Color.WHITE);
        gbc.gridx = 2;
        gbc.weightx = 0;
        browseOutputPath.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int result = fileChooser.showOpenDialog(UserInterface.this);

            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFolder = fileChooser.getSelectedFile();
                ouptutField.setText(selectedFolder.getAbsolutePath());
            }

        });
        gridPanel.add(browseOutputPath, gbc);

        setSameHeight(ouptutField, browseOutputPath);

        // === Row 4: Generate Cases Button ==============================
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;

        JPanel generatePanel = new JPanel(new GridBagLayout());
        JButton generateCasesButton = setFont(new JButton("Generate Cases"));
        generateCasesButton.setBackground(LAVENDAR);
        generateCasesButton.setForeground(Color.WHITE);
        generatePanel.setBackground(Color.WHITE);
        Dimension buttonSize = new Dimension(180, 35);
        generateCasesButton.setPreferredSize(buttonSize);
        generatePanel.add(generateCasesButton);
        gridPanel.add(generatePanel, gbc);

        // === Row 5: Preview Label ==============================
        gbc.gridy = row++;
        gbc.gridx = 0;
        JLabel previewLabel = setFont(new JLabel("Preview:"));
        gridPanel.add(previewLabel, gbc);

        // === Row 6: Preview Text Area ==============================
        gbc.gridy = row++;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;
        JTextArea PreviewArea = new JTextArea(15, 6);
        PreviewArea.setFont(DISPLAY_TEXT_FONT);
        PreviewArea.setEditable(false);
        PreviewArea.setMargin(new Insets(10, 10, 10, 10));
        JScrollPane previewScroll = new JScrollPane(PreviewArea);
        previewScroll.setBorder(LAVENDAR_BORDER);
        previewScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        previewScroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        previewScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        gridPanel.add(previewScroll, gbc);

        // === Row 7: Custom Case Description Label ==============================
        gbc.gridy = row++;
        gbc.insets = new Insets(15, 25, 2, 35);
        gridPanel.add(setFont(new JLabel(
                "Add / Remove Custom Cases: (Test case number is auto generated)")),
                gbc);
        gbc.insets = new Insets(0, 25, 15, 35);

        // === Row 8: Custom Case ID, Condition, Message Labels

        gbc.gridy = row++;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        gridPanel.add(setFont(new JLabel("Enter Custom Case ID:")), gbc);
        gbc.gridx = 1;
        gridPanel.add(setFont(new JLabel("Enter Custom Case Condition:")), gbc);
        gbc.gridx = 2;
        gridPanel.add(setFont(new JLabel("Enter Custom Case Message:")), gbc);

        gbc.insets = new Insets(5, 25, 15, 35);

        // === Row 9: Custom Case ID, Condition, Message Fields and Area
        gbc.gridy = row++;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;

        gbc.gridx = 0;
        gbc.weightx = 0.3;
        JTextField idField = setFont(new JTextField(3));
        idField.setSize(10, 30);
        idField.setBorder(LAVENDAR_BORDER);
        gridPanel.add(idField, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.35;
        JTextArea conditionArea = setFont(new JTextArea(4, 3));
        conditionArea.setBorder(LAVENDAR_BORDER);
        gridPanel.add(conditionArea, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.35;
        JTextArea messageArea = setFont(new JTextArea(4, 3));
        messageArea.setBorder(LAVENDAR_BORDER);
        gridPanel.add(messageArea, gbc);

        idField.setEnabled(false);
        conditionArea.setEnabled(false);
        messageArea.setEnabled(false);

        // === Row 10: Add, Remove Last Custom Case and Generate Test Plan Buttons
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.weighty = 0;

        JPanel buttonPanel = new JPanel(new GridBagLayout());
        JButton addCustomCaseButton = setFont(new JButton("Add Custom Case"));
        JButton removeLastCustomCaseButton = setFont(new JButton("Remove Last Custom Case"));
        JButton createTPButton = setFont(new JButton("Generate Test Plan"));

        buttonSize = new Dimension(280, 35);
        addCustomCaseButton.setBackground(LAVENDAR);
        addCustomCaseButton.setForeground(Color.WHITE);
        addCustomCaseButton.setPreferredSize(buttonSize);
        removeLastCustomCaseButton.setBackground(LAVENDAR);
        removeLastCustomCaseButton.setForeground(Color.WHITE);
        removeLastCustomCaseButton.setPreferredSize(buttonSize);
        createTPButton.setPreferredSize(buttonSize);
        createTPButton.setBackground(LAVENDAR);
        createTPButton.setForeground(Color.WHITE);
        buttonPanel.setBackground(Color.WHITE);

        GridBagConstraints sub_Gbc = new GridBagConstraints();
        sub_Gbc.insets = new Insets(0, 10, 0, 10);
        buttonPanel.add(addCustomCaseButton, sub_Gbc);
        buttonPanel.add(removeLastCustomCaseButton, sub_Gbc);
        buttonPanel.add(createTPButton, sub_Gbc);
        gridPanel.add(buttonPanel, gbc);

        // === Row 11: Log Label ==============================
        gbc.gridy = row++;
        gbc.gridx = 0;
        gbc.gridwidth = 3;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gridPanel.add(setFont(new JLabel("Log:")), gbc);

        // === Row 12: Log Field ==============================
        gbc.gridy = row++;
        gbc.weighty = 0.2;
        gbc.fill = GridBagConstraints.BOTH;

        JTextArea logArea = new JTextArea(10, 10);
        logArea.setMargin(new Insets(10, 10, 10, 10));
        logArea.setFont(DISPLAY_TEXT_FONT);
        JScrollPane logScroll = new JScrollPane(logArea);
        logScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        logScroll.setForeground(LAVENDAR);
        logScroll.setBorder(LAVENDAR_BORDER);
        logScroll.getViewport().setBackground(Color.WHITE);
        logScroll.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        logScroll.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        gridPanel.add(logScroll, gbc);

        JScrollPane mainScrollPane = new JScrollPane(gridPanel);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mainScrollPane.setBorder(null);
        mainScrollPane.getViewport().setBackground(Color.WHITE);
        mainScrollPane.getViewport().setBorder(null);

        mainScrollPane.getVerticalScrollBar().setUI(new CustomScrollBarUI());
        mainScrollPane.getHorizontalScrollBar().setUI(new CustomScrollBarUI());
        mainScrollPane.getVerticalScrollBar().setBlockIncrement(200);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(30);
        add(mainScrollPane);

        // Generate cases
        generateCasesButton.addActionListener(e -> {

            String operationType = "";
            if (option1.isSelected())
                operationType = "N";
            else if (option2.isSelected())
                operationType = "E";
            else
                JOptionPane.showMessageDialog(this, "Select Operation Type");

            if (!operationType.isEmpty()) {
                if (mapField.getText().trim().isEmpty())
                    JOptionPane.showMessageDialog(this,
                            "Kindly select the map to proceed further using the browse button or you can paste the path in the field directly");

                else {
                    UserInput iData = new UserInput(
                            operationType,
                            mapField.getText().trim(),
                            planField.getText().trim(),
                            ouptutField.getText().trim()

                    );
                    ResultData outData = new ResultData(PreviewArea, logArea);
                    appTPG.performMapOperations(iData, PreviewArea, outData);
                    idField.setEnabled(true);
                    conditionArea.setEnabled(true);
                    messageArea.setEnabled(true);
                }

            }
        });

        addCustomCaseButton.addActionListener(e -> {
            String customId = idField.getText().trim();
            String customCondition = conditionArea.getText().trim();
            String customMessage = messageArea.getText().trim();

            if (customId.isEmpty())
                JOptionPane.showMessageDialog(this,
                        "Make sure Id is not empty!");
            else {

                CustomCase ccData = new CustomCase(customId, customCondition, customMessage);
                appTPG.addCustomCase(ccData, PreviewArea);

                idField.setText("");
                conditionArea.setText("");
                messageArea.setText("");
                logArea.setText(
                        logArea.getText() + "\n" + LocalDateTime.now() + ": " + "Added custom case -" + customId);
            }
        });

        removeLastCustomCaseButton.addActionListener(e -> {
            if (PreviewArea.getText().trim().isEmpty())
                JOptionPane.showMessageDialog(this,
                        "It seems like there are no cases present to remove!\nRe-try generating the cases");
            else {
                appTPG.removeLastCustomCase(PreviewArea);
                logArea.setText(
                        logArea.getText() + "\n" + LocalDateTime.now() + ": "
                                + "Removed last custom case entry present.");
            }

        });

        createTPButton.addActionListener(e -> {
            int exisistingPlanFlag = JOptionPane.YES_OPTION;

            if (PreviewArea.getText().trim().isEmpty())
                JOptionPane.showMessageDialog(this,
                        "It seems like there are no cases present to create test plan\nRe-try generating the cases");
            else if (ouptutField.getText().trim().isEmpty())
                JOptionPane.showMessageDialog(this,
                        "Please select the path you want to write the test plan to");
            else if (planField.getText().trim().isEmpty() && option2.isSelected()) {
                // If testplan path is empty, checking with user. If yes is selected,
                // existingPlanFlag =0, so template is used to create. If no is selected, user
                // should give the path.
                exisistingPlanFlag = JOptionPane.showConfirmDialog(this,
                        "You have chosen enhancement operation but have not selected existing test plan.\nDo you want to proceed creating cases in a new test plan?",
                        "Confirm Existing Test Plan", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (exisistingPlanFlag == JOptionPane.YES_OPTION) {
                    ButtonMethods bm = new ButtonMethods();
                    bm.handleGenerateClick();

                    String adoNumber = bm.getAdoNumber();
                    if (adoNumber != null & bm.isCreatePlanFlag() == true) {
                        logArea.setText(logArea.getText() + "\n" + LocalDateTime.now() + ": " + "Writing for ADO "
                                + adoNumber);
                        String operationType = "";
                        if (option1.isSelected())
                            operationType = "N";
                        else if (option2.isSelected())
                            operationType = "E";
                        UserInput iData = new UserInput(
                                operationType,
                                mapField.getText().trim(),
                                planField.getText().trim(),
                                ouptutField.getText().trim()

                        );
                        ResultData outData = new ResultData(PreviewArea, logArea);
                        appTPG.generateTestPlan(iData, outData, adoNumber, bm.getColumnChoice(), exisistingPlanFlag);
                    } else {
                        JOptionPane.showMessageDialog(this, "Choose the existing test plan to proceed");
                    }
                } else {
                    return;
                }
            } else {

                ButtonMethods bm = new ButtonMethods();
                bm.handleGenerateClick();

                String adoNumber = bm.getAdoNumber();
                if (adoNumber != null & bm.isCreatePlanFlag() == true) {
                    logArea.setText(logArea.getText() + "\n" + LocalDateTime.now() + ": " + "Writing for ADO "
                            + adoNumber);
                    String operationType = "";
                    if (option1.isSelected())
                        operationType = "N";
                    else if (option2.isSelected()) {
                        exisistingPlanFlag = 1; // FOr enhancement, since path value is present, output is written to
                                                // existing test plan
                        operationType = "E";
                    }
                    UserInput iData = new UserInput(
                            operationType,
                            mapField.getText().trim(),
                            planField.getText().trim(),
                            ouptutField.getText().trim()

                    );
                    ResultData outData = new ResultData(PreviewArea, logArea);
                    appTPG.generateTestPlan(iData, outData, adoNumber, bm.getColumnChoice(), exisistingPlanFlag);
                }
            }
        });

        setVisible(true);
        appTPG.startApp(PreviewArea, logArea);
    }

    private void setSameHeight(JTextField field, JButton button) {
        Dimension d = field.getPreferredSize();
        button.setPreferredSize(new Dimension(button.getPreferredSize().width, d.height));
    }

    private <T extends JComponent> T setFont(T comp) {
        comp.setFont(DEFAULT_FONT);
        return comp;
    }

    private void chooseFileWithFilter(JTextField targetField, String description, String... extensions) {
        JFileChooser fileChooser = new JFileChooser();

        FileNameExtensionFilter filter = new FileNameExtensionFilter(description, extensions);
        fileChooser.setFileFilter(filter);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            targetField.setText(file.getAbsolutePath());
        }
    }

}
