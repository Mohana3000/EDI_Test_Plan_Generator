package com.tpg.Controller;

import java.io.File;
import java.util.LinkedHashMap;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import com.tpg.Model.ResultData;
import com.tpg.Model.Validation;
import com.tpg.Service.MapActions;

public class MapController extends MapActions {
    private LinkedHashMap<String, Validation> validations = new LinkedHashMap<>();
    private String mapName;

    public String getMapName() {
        return mapName;
    }

    public LinkedHashMap<String, Validation> getValidations() {
        return validations;
    }
    
    public MapController(String mapPath, String operationType, ResultData outData) {
       

        this.mapName = new File(mapPath).getName();
        XWPFDocument mapDocument = readDocument(mapPath, outData);
        outData.setLogArea("Reading Map "+mapName);       
        outData.setLogArea("Checking for validations in map");
      
        LinkedHashMap<String, Validation> mapValidationCases = createMapBasedCases(mapDocument,
                operationType, outData);

        if (!mapValidationCases.isEmpty()) {
            outData.setLogArea("Generated Map cases");
        }

        validations.putAll(mapValidationCases);

        outData.setLogArea("Checking for standard cases . . .");
        XWPFTable standardTable = findTableUnderTitle(mapDocument, "Validation Action");
        if (standardTable != null) {
            outData.setLogArea("Adding standard validation cases . . .");
            LinkedHashMap<String, Validation> standardCases = createStandardValidation(standardTable,
                    operationType);
            validations.putAll(standardCases);
        }
        if (!validations.isEmpty()) {
            outData.setLogArea("Finished Reading Map");
            outData.setLogArea("Proceed to add custom cases");
        } else {
            outData.setLogArea("No cases created from map.");
        }

    }

    

}
