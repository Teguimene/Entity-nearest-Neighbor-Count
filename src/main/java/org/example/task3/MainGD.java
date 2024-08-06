package org.example.task3;

import org.example.Utils;
import org.example.response.ResponseAccuracy;

import java.util.List;

public class MainGD {
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        System.out.println("**************************************** Start process for task 3 ****************************************\n");

        //Train csv file
        String trainCsvFilePath = Utils.genericPath + "/task3/gene-disease(test).tsv";

//        List<DiseasePatient> diseasePatients = Utils.readDpCsvFile(trainCsvFilePath);
        List<GeneDisease> geneDiseases = Utils.readGdCsvFile(trainCsvFilePath);
//
//        int count = Utils.count_number_1(diseasePatients);
//        System.out.println(count);

        int i =0;
        for(GeneDisease gd : geneDiseases) {
            List<String> goIds = Utils.getGoIds(gd.getUriGene());
            List<String> phenotypes = Utils.getPhenotypesUri(gd.getUriDisease());

            int count = Utils.getSimilitudeTas3(goIds, phenotypes);

            if(count >= 1) {
                gd.setValuePrediction("1");
                Utils.geneDiseases.add(gd);
            } else {
                gd.setValuePrediction("0");
                Utils.geneDiseases.add(gd);
            }
            Utils.writeGdCsv(Utils.resultCsvFilePathTask3, gd);

            System.out.println("Element " +(i+1)+ "count : " + count);
            i++;
        }
        ResponseAccuracy response = Utils.accuracy_metric_gd(geneDiseases, Utils.geneDiseases);
        long falsePositives = Utils.geneDiseases.size() - response.getGoodPrediction();
        int falseNegatives = geneDiseases.size() - Utils.geneDiseases.size();
        List<String> result = Utils.calculate_precision_recall(response.getGoodPrediction(),falsePositives,falseNegatives);
        long endTime = System.currentTimeMillis();
        System.out.println("\n**************************************** End process ****************************************");
        System.out.println("Number of element train : " + Utils.geneDiseases.size());
        System.out.println("Precision : " + result.get(0));
        System.out.println("Recall : " + result.get(1));
        System.out.println("Accuracy : " + response.getAccuracy());
        System.out.println("\nExecution time: " + ((endTime-startTime)/1000)+"s");
        System.out.println("****************************************************");
    }
}
