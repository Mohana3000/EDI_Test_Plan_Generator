package com.tpg.Model;

public class Validation {
    private String caseCount;
    private String ediType; //EDE, EDR, EDN, Discard
    private String ediCase; // Validation Key
    private String ediMessage; // Validation message
    private String ediCondition; // Validation condition
    private String validationType; // New, Deleted, Modified, Standard, Custom
   
    public Validation() {
        this.caseCount="";
        this.ediType = "";
        this.ediCase = "";
        this.ediMessage = "";
        this.ediCondition = "";
        this.validationType = "";
       
    }
  
    public String getEdiType() {
        return ediType;
    }

    public void setEdiType(String ediType) {
        this.ediType = ediType;
    }

    public String getEdiCase() {
        return ediCase;
    }

    public void setEdiCase(String ediCase) {
        this.ediCase = ediCase;
    }

    public String getEdiMessage() {
        return ediMessage;
    }

    public void setEdiMessage(String ediMessage) {
        this.ediMessage = ediMessage;
    }

    public String getEdiCondition() {
        return ediCondition;
    }

    public void setEdiCondition(String ediCondition) {
        this.ediCondition = ediCondition;
    }

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public String getCaseCount() {
        return caseCount;
    }

    public void setCaseCount(String caseCount) {
        this.caseCount = caseCount;
    }


}
