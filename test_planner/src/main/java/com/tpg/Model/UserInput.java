package com.tpg.Model;

public class UserInput {
    private String operationType;
    private String mapPath;
    private String planPath;
    private String outputPath;

    public UserInput(String operationType, String mapPath, String planPath, String outputPath) {
        this.operationType = operationType;
        this.mapPath = mapPath;
        this.planPath = planPath;
        this.outputPath = outputPath;
    }

    public UserInput(){
    }

    public String getOperationType() {
        return operationType;
    }

    public String getMapPath() {
        return mapPath;
    }

    public String getPlanPath() {
        return planPath;
    }

    public String getOutputPath() {
        return outputPath;
    }



    
}
