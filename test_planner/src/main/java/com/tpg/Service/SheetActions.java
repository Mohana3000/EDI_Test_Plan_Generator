package com.tpg.Service;

import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.*;

public class SheetActions {

    public static int[] getGroupBoundaries(Sheet testCaseSheet, String adoNumber,
            int matchRowIndex) throws Exception {
        int[] groupBounds = { 0, 0 };

        groupBounds = findGroupBoundaries(testCaseSheet, matchRowIndex);

        // Ungroup the existing group

            testCaseSheet.ungroupRow(groupBounds[0], groupBounds[1]);
            System.out.println("Ungrouped rows from " + (groupBounds[0] + 1) + " to " + (groupBounds[1] + 1));
        

        // Add a small delay before grouping to ensure all operations are complete
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // groupAndMinimizeRows(testCaseSheet, groupStartRow+1, newGroupEndRow);
        return groupBounds;
    }

    public static int findTextInFirstColumn(Sheet testCaseSheet, String text) {
        for (int rowIndex = 0; rowIndex <=  testCaseSheet.getLastRowNum(); rowIndex++) {
            Row row = testCaseSheet.getRow(rowIndex);
            if (row != null) {
                Cell cell = row.getCell(0); // First column (index 0)
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell);
                    // Check if cell value contains the ADO string
                    if (cellValue.contains(text)) {
                        return rowIndex;
                    }
                }
            }
        }
        return -1; // Not found
    }

    public static List<Integer> findTextInFirstColumn(Sheet testCaseSheet, String text,int startRowToSearch, int endRowToSearch) {
        List<Integer> resultList = new ArrayList<Integer>();
  
        for (int rowIndex = startRowToSearch; rowIndex <=  endRowToSearch; rowIndex++) {
            Row row = testCaseSheet.getRow(rowIndex);
            if (row != null) {
                Cell cell = row.getCell(0); // First column (index 0)
                if (cell != null) {
                    String cellValue = getCellValueAsString(cell);
                    // Check if cell value contains the ADO string
                    if (cellValue.contains(text)) {
                        resultList.add(rowIndex);
                    }
                }
            }
        }
        return resultList; // Not found
    }

    public static String getCellValueAsString(Cell cell) {
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.valueOf(cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }

    private static int[] findGroupBoundaries(Sheet testCaseSheet, int rowIndex) {
        int groupStart = rowIndex;
        int groupEnd = rowIndex;

        // Find group start - look backwards for rows that are part of the same group
        for (int i = rowIndex; i >= 0; i--) {
            Row row = testCaseSheet.getRow(i);
            if (row != null && row.getOutlineLevel() > 0) {
                groupStart = i;
            } else {
                break;
            }
        }

        // Find group end - look forwards for rows that are part of the same group
        for (int i = rowIndex + 1; i <= testCaseSheet.getLastRowNum(); i++) {
            Row row = testCaseSheet.getRow(i);
            if (row != null && row.getOutlineLevel() > 0) {
                groupEnd = i;
            } else {
                break;
            }
        }

        return new int[] { groupStart, groupEnd };
    }

    public static void moveRowsDown(Sheet testCaseSheet, int startRow, int numberOfRows) {
        int lastRowNum = testCaseSheet.getLastRowNum();

        // Store outline levels before moving rows
        int[] outlineLevels = new int[lastRowNum - startRow + 1];
        for (int i = startRow; i <= lastRowNum; i++) {
            Row row = testCaseSheet.getRow(i);
            outlineLevels[i - startRow] = (row != null) ? row.getOutlineLevel() : 0;
        }

        // Work backwards to avoid overwriting data
        for (int rowIndex = lastRowNum; rowIndex >= startRow; rowIndex--) {
            Row sourceRow = testCaseSheet.getRow(rowIndex);
            if (sourceRow != null) {
                int newRowIndex = rowIndex + numberOfRows;
                Row newRow = testCaseSheet.createRow(newRowIndex);

                // Copy all cells from source row to new row
                copyRow(sourceRow, newRow);

                // Copy row height and style
                newRow.setHeight(sourceRow.getHeight());
                newRow.setRowStyle(sourceRow.getRowStyle());

                // Remove the old row
                testCaseSheet.removeRow(sourceRow);
            }
        }

        // Restore outline levels by re-grouping the moved rows
        restoreGrouping(testCaseSheet, startRow, numberOfRows, outlineLevels);

        System.out.println("Moved rows starting from " + (startRow + 1) + " down by " + numberOfRows + " positions");
    }

    private static void restoreGrouping(Sheet testCaseSheet, int originalStartRow, int offset, int[] outlineLevels) {
        try {
            int groupStart = -1;
            int currentLevel = 0;

            for (int i = 0; i < outlineLevels.length; i++) {
                int currentRowIndex = originalStartRow + offset + i;
                int level = outlineLevels[i];

                if (level > 0) {
                    if (groupStart == -1) {
                        groupStart = currentRowIndex;
                        currentLevel = level;
                    } else if (level != currentLevel) {
                        // Level changed, close previous group if needed
                        if (groupStart != -1 && groupStart < currentRowIndex - 1) {
                            testCaseSheet.groupRow(groupStart, currentRowIndex - 1);
                        }
                        groupStart = currentRowIndex;
                        currentLevel = level;
                    }
                } else {
                    // End of group
                    if (groupStart != -1 && groupStart < currentRowIndex - 1) {
                        testCaseSheet.groupRow(groupStart, currentRowIndex - 1);
                    }
                    groupStart = -1;
                    currentLevel = 0;
                }
            }

            // Close any remaining group
            if (groupStart != -1) {
                int lastRowIndex = originalStartRow + offset + outlineLevels.length - 1;
                if (groupStart < lastRowIndex) {
                    testCaseSheet.groupRow(groupStart, lastRowIndex);
                }
            }

        } catch (Exception e) {
            System.out.println("Warning: Could not fully restore grouping: " + e.getMessage());
        }
    }

    private static void copyRow(Row sourceRow, Row destRow) {
        for (int cellIndex = 0; cellIndex < sourceRow.getLastCellNum(); cellIndex++) {
            Cell sourceCell = sourceRow.getCell(cellIndex);
            if (sourceCell != null) {
                Cell destCell = destRow.createCell(cellIndex);

                // Copy cell value based on type
                switch (sourceCell.getCellType()) {
                    case STRING:
                        destCell.setCellValue(sourceCell.getStringCellValue());
                        break;
                    case NUMERIC:
                        if (DateUtil.isCellDateFormatted(sourceCell)) {
                            destCell.setCellValue(sourceCell.getDateCellValue());
                        } else {
                            destCell.setCellValue(sourceCell.getNumericCellValue());
                        }
                        break;
                    case BOOLEAN:
                        destCell.setCellValue(sourceCell.getBooleanCellValue());
                        break;
                    case FORMULA:
                        destCell.setCellFormula(sourceCell.getCellFormula());
                        break;
                    case BLANK:
                        destCell.setBlank();
                        break;
                    default:
                        break;
                }

                // Copy cell style
                destCell.setCellStyle(sourceCell.getCellStyle());
            }
        }
    }
}
