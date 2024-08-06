package org.example.task2;

import jdk.jshell.execution.Util;
import org.example.response.ResponseAccuracy;
import org.example.Utils;

import java.util.List;

public class MainDiseasePatient {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("**************************************** Start process for task 2 ****************************************\n");

        //Train csv file
        String trainCsvFilePath = Utils.genericPath + "/task2/patient-disease(test_result).tsv";

        List<DiseasePatient> diseasePatients = Utils.readDpCsvFile(trainCsvFilePath);


        //Read part of train csv file and construct object to path (To manipulate)
//          Utils.getFirstPrediction(trainCsvFilePath, testCsvFilePath);
//        for(DiseasePatient d : Utils.diseasePatients) {
//            System.out.println(d);
//        }

        //Read train csv file and construct object to path to compare
        List<DiseasePatient> trainDisease = Utils.readDpCsvFile(trainCsvFilePath);

        //Predict and save the result for each line of data
        for(DiseasePatient diseasePatient : diseasePatients) {
            String disease = diseasePatient.getUriDisease().replace("http://entity/", "");
            String patient = diseasePatient.getUriPatient().replace("http://entity/", "");

            // verification of a relation between this disease and patient
            Utils.relation_prediction_DP(disease, patient, Utils.resultCsvFilePathTask2P4);
        }

//       System.out.println("\n**************************************** Writing elements in the csv file ****************************************");
//        Utils.writeDPInCsv(Utils.resultCsvFilePathTask2P4, Utils.diseasePatients);
        ResponseAccuracy response = Utils.accuracy_metric_dp(trainDisease, Utils.diseasePatients);
        long falsePositives = Utils.diseasePatients.size() - response.getGoodPrediction();
        int falseNegatives = trainDisease.size() - Utils.diseasePatients.size();
        List<String> result = Utils.calculate_precision_recall(response.getGoodPrediction(),falsePositives,falseNegatives);
        long endTime = System.currentTimeMillis();
        System.out.println("\n**************************************** End process ****************************************");
        System.out.println("Number of element train : " + Utils.diseasePatients.size());
        System.out.println("Precision : " + result.get(0));
        System.out.println("Recall : " + result.get(1));
        System.out.println("Accuracy : " + response.getAccuracy());
        System.out.println("\nExecution time : " + ((endTime-startTime)/1000)+"s");
        System.out.println("****************************************************");
    }
}
