package org.example.task1;

import org.example.Utils;
import org.example.response.ResponseAccuracy;
import org.json.JSONException;

import java.util.List;

public class Pack3 {
    public static void main(String[] args) throws JSONException {
        long startTime = System.currentTimeMillis();
        System.out.println("**************************************** Start process for task 1 ****************************************\n");
        //Part of train csv file
//        String csvFilePath = Utils.genericPath + "/prot-prot(train1).tsv";

        //Train csv file
        String trainCsvFilePath = Utils.genericPath + "/task1/prot-prot(pack3).tsv";

        //Read part of train csv file and construct object to path (To manipulate)
        List<Protein> proteins = Utils.readCsvFile(trainCsvFilePath);

//        System.out.println(proteins.size());

        //Read train csv file and construct object to path to compare
        List<Protein> trainProteins = Utils.readCsvFile(trainCsvFilePath);

        //Predict and save the result for each line of data
        for(Protein protein : proteins) {
            String proteinOne = protein.getUriProteinOne().replace("https://www.uniprot.org/uniprotkb/", "");
            String proteinTwo = protein.getUriProteinTwo().replace("https://www.uniprot.org/uniprotkb/", "");

            // verification of a relation between this two proteins
            Utils.relation_prediction(proteinOne, proteinTwo, Utils.resultCsvFilePathTask1P3);
        }

        System.out.println("\n**************************************** Writing elements in the csv file ****************************************");
//        Utils.writeInCsv(Utils.resultCsvFilePath, Utils.proteins);
        ResponseAccuracy response = Utils.accuracy_metric(trainProteins, Utils.proteins);
        long falsePositives = proteins.size() - response.getGoodPrediction();
        int falseNegatives = trainProteins.size() - proteins.size();
        List<String> result = Utils.calculate_precision_recall(response.getGoodPrediction(),falsePositives,falseNegatives);
        long endTime = System.currentTimeMillis();
        System.out.println("\n**************************************** End process ****************************************");
        System.out.println("Number of element train : " + proteins.size());
        System.out.println("Precision : " + result.get(0) + "\n");
        System.out.println("Recall : " + result.get(1));
        System.out.println("Accuracy : " + response.getAccuracy());
        System.out.println("\nExecution time : " + ((endTime-startTime)/1000)+"s");
        System.out.println("****************************************************");

    }
}