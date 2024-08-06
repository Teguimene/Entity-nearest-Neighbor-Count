package org.example;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.apache.jena.base.Sys;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.example.response.FirstAndRestPredictResponse;
import org.example.response.ResponseAccuracy;
import org.example.response.ScoreSDBResponse;
import org.example.task1.Protein;
import org.example.task1.ProteinKey;
import org.example.task2.DiseasePatient;
import org.example.task2.DiseasePatientKey;
import org.example.task2.MainDiseasePatient;
import org.example.task2.MatchDiseasePatient;
import org.example.task3.GeneDisease;
import org.example.task3.GeneDiseasekey;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
    public static String API_URL = "https://string-db.org/api/json/network?identifiers=%s%%0D%s";
    public static String API_URL_ENRICHMENT = "https://string-db.org/api/json/ppi_enrichment?identifiers=%s%%0D%s";
    public static String genericPath = "/home/furel/Documents/Dev/UY1/Challenge/NEGKNOW-main/Data/files";
//    public static String genericPathTask2 = "/home/furel/Documents/Dev/UY1/Challenge/NEGKNOW-main/Data/disease-prediction";
    final static String uri = "https://www.ebi.ac.uk/QuickGO/services/annotation/search?geneProductId=";
    final static String proteinUri = "https://www.uniprot.org/uniprotkb/";
    final static String dpUri = "http://entity/";
    final static String fileToSearchTask1 = "gokg.owl";
    final static String fileToSearchTask2 = "hpkg.owl";
    public static int NB_SAME_ELEMENTS = 2;
    public static List<GeneDisease> geneDiseases = new ArrayList<>();
    //file generate to insert the result of algorithm
    public static String resultCsvFilePath = genericPath + "/task1/prot-prot(test_result_1).tsv";
    public static String resultCsvFilePathTask3 = genericPath + "/task3/gene-disease(test_submission).tsv";
    public static String resultCsvFilePath2 = genericPath + "/task1/prot-prot(test_result_2).tsv";
    public static String resultCsvFilePathTask2P4 = genericPath + "/task2/patient-disease(prog_test_result).tsv";
    public static String resultCsvFilePathTask1P2 = genericPath + "/task1/prot-prot(test_result_2).tsv";
    public static String resultCsvFilePathTask1P3 = genericPath + "/task1/prot-prot(test_result_3).tsv";
    public static List<Protein> proteins = new ArrayList<>();
    public static List<DiseasePatient> diseasePatients = new ArrayList<>();

    /************************************* START TASK 1 *************************************/

    // function to extract goIds for the protein

    public static boolean checkParentId(String protein_1, String protein_2) {
        HttpRequest request_pro1 = HttpRequest.newBuilder()
                .uri(URI.create("https://www.ebi.ac.uk/QuickGO/services/geneproduct/"+protein_1))
                .build();
        JSONArray result1 = getResponse(request_pro1);
        String parentIdProtein1 = result1.getJSONObject(0).getString("parentId");

        /* for protein 2 */
        HttpRequest request_pro2 = HttpRequest.newBuilder()
                .uri(URI.create("https://www.ebi.ac.uk/QuickGO/services/geneproduct/"+protein_2))
                .build();
        JSONArray result2 = getResponse(request_pro2);
        String parentIdProtein2 = result2.getJSONObject(0).getString("parentId");

        return ((!parentIdProtein1.isEmpty() && !parentIdProtein2.isEmpty()) && parentIdProtein1.equals(parentIdProtein2));
    }

    // To check Go terms similitude's
    public static int getNumberOfSameTerms(List<String> goIdsProtOne, List<String> goIdsProtTwo) {
        List<String> sameValues = new ArrayList<>();

        if (goIdsProtOne.size() > goIdsProtTwo.size()) {
            for (String value : goIdsProtOne) {
                if (goIdsProtTwo.contains(value)) {
                    sameValues.add(value);
                }
            }
        } else {
            for (String value : goIdsProtTwo) {
                if (goIdsProtOne.contains(value)) {
                    sameValues.add(value);
                }
            }
        }

        return sameValues.size();
    }

    // predict if a relation exist between two proteins
    public static void relation_prediction(String protein_1, String protein_2, String filePath) throws JSONException {
        Protein protein = new Protein();
        protein.setUriProteinOne(proteinUri+protein_1);
        protein.setUriProteinTwo(proteinUri+protein_2);

//        List<String> goIds_protein_one = getGoIds(protein_1);
//        List<String> goIds_protein_two= getGoIds(protein_2);

        String queryString =
                "PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
                "PREFIX up: <https://www.uniprot.org/uniprotkb/> \n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#>" +
                "SELECT ?protein1Label ?protein2Label (COUNT(DISTINCT ?sourceIndividual) as ?commonCount)\n" +
                "WHERE {\n" +
                "  ?assertion1 rdf:type owl:PropertyAssertion ;\n" +
                "              owl:sourceIndividual ?sourceIndividual ;\n" +
                "              owl:assertionProperty <http://purl.obolibrary.org/obo/go.owl#has_function> ;\n" +
                "              owl:targetIndividual <https://www.uniprot.org/uniprotkb/"+protein_1+"> .\n" +
                "  \n" +
                "  ?assertion2 rdf:type owl:PropertyAssertion ;\n" +
                "              owl:sourceIndividual ?sourceIndividual ;\n" +
                "              owl:assertionProperty <http://purl.obolibrary.org/obo/go.owl#has_function> ;\n" +
                "              owl:targetIndividual <https://www.uniprot.org/uniprotkb/"+protein_2+"> .\n" +
                "  OPTIONAL { <https://www.uniprot.org/uniprotkb/"+protein_1+"> rdf:resource ?protein1Label }\n" +
                "  OPTIONAL { <https://www.uniprot.org/uniprotkb/"+protein_2+"> rdf:resource ?protein2Label }" +
                "} GROUP BY ?protein1Label ?protein2Label \n";

        int totalSimilitude = Utils.numberOfGoSimilitudeSparQl(queryString, genericPath+"/task1/"+fileToSearchTask1);
        ScoreSDBResponse scores = getInteractedTrustScore(protein_1,protein_2);

//        boolean sameParent = checkParentId(protein_1,protein_2);

        System.out.println("totalSimilitude: " + totalSimilitude + "\t score : " + scores.getScore() + " tscore : " + scores.getTscore());
        /*if(scores.getScore() >= 0.9 || scores.getScore() >= 0.72) {
            protein.setValuePrediction("1");
        } else */
            if((totalSimilitude >= NB_SAME_ELEMENTS) && ((scores.getScore() >= 0.43))) {
                protein.setValuePrediction("1");
            }
       /*             protein.setValuePrediction("1");
        } else
            if ((totalSimilitude < NB_SAME_ELEMENTS) && (scores.getScore() >= 0.53)) {
            protein.setValuePrediction("1");
        } */
        else {
            protein.setValuePrediction("0");
        }
        System.out.println("protein to write in csv file " + protein);
        Utils.writeInCsv(filePath, protein);
        proteins.add(protein);
        System.out.println("Proteins size : " + proteins.size() + "\n");
    }

    //To calculate precision and recall
    public static List<String> calculate_precision_recall(long truePositives, long falsePositives, int falseNegatives) {
        List<String> result = new ArrayList<>();
        DecimalFormat df = new DecimalFormat("#.###");
        double precision = (double) truePositives / (truePositives + falsePositives);
        double recall = (double) truePositives / (truePositives + falseNegatives);
        result.add(df.format(precision)); result.add(df.format(recall));
        return  result;
    }

    //Read csv dataset protein file
    public static List<Protein> readCsvFile(String csvFilePath) {
        Charset charset = StandardCharsets.UTF_8;
        List<Protein> beans = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath, charset))) {
            CsvToBean<Protein> csvToBean = new CsvToBeanBuilder<Protein>(reader)
                    .withType(Protein.class)
                    .build();

            beans = csvToBean.parse();
//            for(Protein prot : beans) {
//                System.out.println(prot);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return beans;
    }

    //WriteDataInCsvFile
    public static void writeInCsv(String csvFilePath, Protein protein) {
        // first create file object for file placed at location
        // specified by filepath
        //ref : https://www.geeksforgeeks.org/writing-a-csv-file-in-java-using-opencsv/
        File file = new File(csvFilePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file, true);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // add data to csv
            String[] line = {
                    protein.getUriProteinOne(),
                    protein.getUriProteinTwo(),
                    protein.getValuePrediction()
            };
            writer.writeNext(line);
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Calculate accuracy metric
    public static ResponseAccuracy accuracy_metric(List<Protein> trainProteins, List<Protein> partOfTrainProteins) {
        Map<ProteinKey, Protein> map1 = trainProteins.stream()
                .collect(Collectors.toMap(
                        ProteinKey::new,
                        Function.identity()
                ));
        double accuracy = 0;
        ResponseAccuracy result = new ResponseAccuracy();
        long numberOfGoodPredictions = partOfTrainProteins.stream()
                .filter(p -> map1.containsKey(new ProteinKey(p)))
                .filter(p -> map1.get(new ProteinKey(p)).equals(p))
                .count();

        System.out.println("numberOfGoodPredictions " + numberOfGoodPredictions);
        if(numberOfGoodPredictions > 0) {
            accuracy = (double) numberOfGoodPredictions/trainProteins.size();
        }
        result.setAccuracy(accuracy); result.setGoodPrediction(numberOfGoodPredictions);
        return result;
    }

    //get viability score with two proteins
    public static ScoreSDBResponse getInteractedTrustScore(String proteinIdOne, String proteinIdTwo) {
        ScoreSDBResponse scores = new ScoreSDBResponse();
        try {
            String url = String.format(API_URL, proteinIdOne, proteinIdTwo);
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // get p_value between two proteins
            String url_enrichment = String.format(API_URL_ENRICHMENT, proteinIdOne, proteinIdTwo);
            HttpClient client2 = HttpClient.newHttpClient();
            HttpRequest request2 = HttpRequest.newBuilder()
                    .uri(URI.create(url_enrichment))
                    .build();
            HttpResponse<String> response2 = client2.send(request2, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                String responseBody = response.body();

                // Parse JSON response
                JSONArray jsonResponse = new JSONArray(responseBody);

                if(!jsonResponse.isEmpty()) {
                    JSONObject object = jsonResponse.getJSONObject(0);
                    System.out.println("STRING resulte : " + object);
                    // Extraction of trust score
                    double score = jsonResponse.getJSONObject(0).getDouble("score");
                    double tscore = jsonResponse.getJSONObject(0).getDouble("tscore");

                    scores.setScore(score);
                    scores.setTscore(tscore);
                }
            } else {
                System.err.println("Error when fetch score: " + response.statusCode() + " - " + response.body());
            }

            if(response2.statusCode() == 200) {
                String responseBody = response2.body();

                // Parse JSON response
                JSONArray jsonResponse = new JSONArray(responseBody);

                if(!jsonResponse.isEmpty()) {
                    double p_value = jsonResponse.getJSONObject(0).getDouble("p_value");
                    scores.setP_value(p_value);
                    System.out.println("p_value : " + p_value);
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return scores;
    }

    public static JSONArray getResponse(HttpRequest request) {
        HttpResponse<String> response = null;

        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Convertir la réponse JSON en objet JSON
        assert response != null;
        JSONObject jsonResponse2 = new JSONObject(response.body());

        // Extraire le tableau de résultats
        return jsonResponse2.getJSONArray("results");
    }

    public static int numberOfGoSimilitudeSparQl(String queryString, String fileToSeach) {
        FileManager.getInternal().addLocatorClassLoader(RDF.class.getClassLoader());
        Model model = FileManager.getInternal().loadModelInternal(fileToSeach);

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);
        int count = 0;

        try {
            ResultSet resultSet = qexec.execSelect();
            if(!resultSet.hasNext()) {
                count = 0;
            }
            while(resultSet.hasNext()) {
                QuerySolution soln = resultSet.nextSolution();
                count = soln.getLiteral("commonCount").getInt();
//                System.out.println(count);
            }
        } finally {
            qexec.close();
        }
        return count;
    }

    /************************************* END TASK 1 *************************************/


    /************************************* START TASK 2 *************************************/
    // predict if a relation exist between disease and patient
    public static void relation_prediction_DP(String patientId, String diseaseId, String filePath) throws JSONException {
        DiseasePatient diseasePatient = new DiseasePatient();
        diseasePatient.setUriDisease(dpUri+diseaseId);
        diseasePatient.setUriPatient(dpUri+patientId);

        String queryString =
                "PREFIX enti: <http://entity/> \n" +
                        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX hp: <http://purl.obolibrary.org/obo/hp.owl#>" +

                        "SELECT ?patient (COUNT(DISTINCT ?phenotype) as ?commonCount) \n" +
                        "   WHERE {\n" +
                        "   ?assertion1 rdf:type owl:PropertyAssertion ;\n" +
                        "   owl:sourceIndividual ?phenotype ;\n" +
                        "   owl:assertionProperty hp:has_phenotype ;\n" +
                        "   owl:targetIndividual <http://entity/"+diseaseId+"> .\n" +

                        "   ?assertion2 rdf:type owl:PropertyAssertion ;\n" +
                        "   owl:sourceIndividual ?phenotype ;\n" +
                        "   owl:assertionProperty hp:has_phenotype ;\n" +
                        "   owl:targetIndividual <http://entity/"+patientId+"> .\n" +
                        "  OPTIONAL { <http://entity/"+patientId+"> rdf:resource ?patient }" +
                        "} GROUP BY ?patient \n";

        int totalSimilitude = Utils.numberOfGoSimilitudeSparQl(queryString, genericPath+"/task2/"+fileToSearchTask2);

        System.out.println("totalSimilitude : " + totalSimilitude);

        if((totalSimilitude >= 1)) {
            diseasePatient.setValuePrediction("1");
        } else {
            diseasePatient.setValuePrediction("0");
        }
        System.out.println("Writing in csv file " + diseasePatient);
        Utils.writeDPInCsv(filePath, diseasePatient);

        diseasePatients.add(diseasePatient);
        System.out.println("diseasePatients size : " + diseasePatients.size());
    }

    //Read csv dataset disease patient file
    public static List<DiseasePatient> readDpCsvFile(String csvFilePath) {
        Charset charset = StandardCharsets.UTF_8;
        List<DiseasePatient> beans = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath, charset))) {
            CsvToBean<DiseasePatient> csvToBean = new CsvToBeanBuilder<DiseasePatient>(reader)
                    .withSeparator(',')
                    .withType(DiseasePatient.class)
                    .build();

            beans = csvToBean.parse();
            /*int countAppear = 0;
            DiseasePatient dp = new DiseasePatient();
            dp.setUriPatient("http://entity/P97");
            dp.setUriDisease("http://entity/OMIM216550");
            for(DiseasePatient prot : beans) {
                if(Objects.equals(prot.getUriPatient(), dp.getUriPatient())) {
                    countAppear += 1;
                }
//                System.out.println(prot);
            }
            System.out.println("nombre d'apparition : " + countAppear);*/
        } catch (IOException e) {
            e.printStackTrace();
        }

        return beans;
    }

    //Read csv dataset disease patient file
    public static List<MatchDiseasePatient> readDpmCsvFile(String csvFilePath) {
        Charset charset = StandardCharsets.UTF_8;
        List<MatchDiseasePatient> beans = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath, charset))) {
            CsvToBean<MatchDiseasePatient> csvToBean = new CsvToBeanBuilder<MatchDiseasePatient>(reader)
                    .withType(MatchDiseasePatient.class)
                    .build();

            beans = csvToBean.parse();

//            for(MatchDiseasePatient dp : beans) {
//                System.out.println(dp);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return beans;
    }


    //WriteDataInCsvFile to disease patient
    public static void writeDPInCsv(String csvFilePath, DiseasePatient diseasePatient) {
        // first create file object for file placed at location
        // specified by filepath
        //ref : https://www.geeksforgeeks.org/writing-a-csv-file-in-java-using-opencsv/
        File file = new File(csvFilePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file, true);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // add data to csv
//            for (DiseasePatient diseasePatient : diseasePatients) {
                String[] line = {
                        diseasePatient.getUriPatient(),
                        diseasePatient.getUriDisease(),
                        diseasePatient.getValuePrediction()
                };
                writer.writeNext(line);
//            }
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Calculate accuracy metric
    public static ResponseAccuracy accuracy_metric_dp(List<DiseasePatient> trainDisease, List<DiseasePatient> partOfTrainDisease) {
//        Map<DiseasePatientKey, DiseasePatient> map1 = trainDisease.stream()
//                .collect(Collectors.toMap(
//                        DiseasePatientKey::new,
//                        Function.identity()
//                ));
        double accuracy = 0;
        long numberOfGoodPredictions = 0;
        ResponseAccuracy result = new ResponseAccuracy();
        for(DiseasePatient dp : trainDisease) {
            numberOfGoodPredictions += match_accuracy(dp, partOfTrainDisease);
        }
//        long numberOfGoodPredictions = partOfTrainDisease.stream()
//                .filter(p -> map1.containsKey(new DiseasePatientKey(p)))
//                .filter(p -> map1.get(new DiseasePatientKey(p)).equals(p))
//                .count();

        System.out.println("numberOfGoodPredictions " + numberOfGoodPredictions);
        if(numberOfGoodPredictions > 0) {
            accuracy = (double) numberOfGoodPredictions/trainDisease.size();
        }
        result.setAccuracy(accuracy); result.setGoodPrediction(numberOfGoodPredictions);
        return result;
    }

    /* Set diseasePatients list with the comparison*/
    public static void getFirstPrediction(String trainPath, String testPath) {

        List<DiseasePatient> trainList = readDpCsvFile(trainPath);
        List<DiseasePatient> diseasePatients = readDpCsvFile(testPath);
        List<DiseasePatient> foundDiagnosticsPatient = new ArrayList<>();



        //Filtrer le fichier de train pour prendre ceux respectant le pattern
        for(DiseasePatient dp : trainList) {
            if(dp.getUriDisease().replace("http://entity/", "").startsWith("OMIM") && Objects.equals(dp.getValuePrediction(), "1")) {
                foundDiagnosticsPatient.add(dp);
            }
        }

//        List<DiseasePatient> identicalElements = new ArrayList<>();
        List<DiseasePatient> firstPredictions = new ArrayList<>();
        List<DiseasePatient> restOfPredictions = new ArrayList<>();

        //Recuperation des element a attribuer 1 contenu dans le train
        /*System.out.println("First matching");
        for(DiseasePatient md  : foundDiagnosticsPatient) {
            DiseasePatient element = match(md, diseasePatients);
            if((element.getUriPatient() != null) && (element.getUriDisease() != null)) {
                Utils.diseasePatients.add(element);
            }
        }
        System.out.println("End First matching " + Utils.diseasePatients.size());

        System.out.println("Second matching");
        if(!Utils.diseasePatients.isEmpty()) {
            for(DiseasePatient md  : Utils.diseasePatients) {
                firstPredictions.addAll(match_zero(md, diseasePatients).getFirstPredictions());
                if(!restOfPredictions.contains(md)) {
                    restOfPredictions.add(md);
                }
                restOfPredictions.addAll(match_zero(md, diseasePatients).getRestOfPredictions());
            }
        }
        System.out.println("End Second matching");
        Utils.diseasePatients.addAll(firstPredictions);

        System.out.println("first predictions " + Utils.diseasePatients.size() + " restOfPredictions size " + restOfPredictions.size());*/
    }

    public static DiseasePatient match(DiseasePatient matchDiseasePatient, List<DiseasePatient> matchDiseasePatients) {
        DiseasePatient dp = new DiseasePatient();
        for(DiseasePatient mp : matchDiseasePatients) {
//            System.out.println("Current el : " + mp + " el to match " + matchDiseasePatient);
            if((Objects.equals(mp.getUriPatient(), matchDiseasePatient.getUriPatient()))
                    && (Objects.equals(mp.getUriDisease(), matchDiseasePatient.getUriDisease()))
            ) {
//                System.out.println("good match " + mp + " el to match " + matchDiseasePatient);
                dp.setUriPatient(matchDiseasePatient.getUriPatient());
                dp.setUriDisease(matchDiseasePatient.getUriDisease());
                dp.setValuePrediction(matchDiseasePatient.getValuePrediction());
            }
        }
       return dp;
    }
/*
    public static FirstAndRestPredictResponse match_zero(DiseasePatient matchDiseasePatient, List<DiseasePatient> matchDiseasePatients) {
        FirstAndRestPredictResponse frp = new FirstAndRestPredictResponse();
        for(DiseasePatient mp : matchDiseasePatients) {
            System.out.println("element " + (i+1));
            if((Objects.equals(mp.getUriPatient(), matchDiseasePatient.getUriPatient())) && !(Objects.equals(mp.getUriDisease(), matchDiseasePatient.getUriDisease()))) {
                DiseasePatient dp = new DiseasePatient();

                dp.setUriPatient(mp.getUriPatient());
                dp.setUriDisease(mp.getUriDisease());
                dp.setValuePrediction("0");
            } else if(!(Objects.equals(mp.getUriPatient(), matchDiseasePatient.getUriPatient())) && !(Objects.equals(mp.getUriDisease(), matchDiseasePatient.getUriDisease()))) {
                DiseasePatient dp = new DiseasePatient();

                dp.setUriPatient(mp.getUriPatient());
                dp.setUriDisease(mp.getUriDisease());
                dp.setValuePrediction("0");
                if(!restOfPredictions.contains(dp)) {
                    restOfPredictions.add(dp);
                }
            }
            i++;
        }
//        System.out.println("first predictions " + firstPredictions.size() + " restOfPredictions size " + restOfPredictions.size());
        frp.setFirstPredictions(firstPredictions);
        frp.setRestOfPredictions(restOfPredictions);

        return frp;
    }*/

    public static long match_accuracy(DiseasePatient matchDiseasePatient, List<DiseasePatient> matchDiseasePatients) {
        long result = 0;
        for(DiseasePatient mp : matchDiseasePatients) {
            if((Objects.equals(mp.getUriPatient(), matchDiseasePatient.getUriPatient()))
                    && (Objects.equals(mp.getUriDisease(), matchDiseasePatient.getUriDisease()))
                    && (Objects.equals(mp.getValuePrediction(), matchDiseasePatient.getValuePrediction()))
            ) {
              result++;
            }
        }

//        System.out.println("result " + result);
        return result;
    }

    public static int count_number_1(List<Protein> diseasePatients) {
        int count = 0;
        for(Protein d : diseasePatients) {
            if(Objects.equals(d.getValuePrediction(), "1")) {
                count++;
            }
        }
        return count;
    }

    /************************************* END TASK 2 *************************************/

    /************************************* START TASK 2 *************************************/
    public static List<String> getGoIds(String gene_path) throws JSONException {
        FileManager.getInternal().addLocatorClassLoader(RDF.class.getClassLoader());
        Model model = FileManager.getInternal().loadModelInternal(Utils.genericPath+"/task3/gokg.owl");
        List<String> goIdentifiers = new ArrayList<>();


        System.out.println("gene_path : " + gene_path);
        String queryString =
                "PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
                        "PREFIX up: <https://www.uniprot.org/uniprotkb/> \n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX go: <http://purl.obolibrary.org/obo/go.owl#>" +
                        "SELECT DISTINCT ?sourceIndividual\n" +
                        "WHERE {\n" +
                        "  ?assertion rdf:type owl:PropertyAssertion ;\n" +
                        "              owl:sourceIndividual ?sourceIndividual ;\n" +
                        "              owl:assertionProperty <http://purl.obolibrary.org/obo/go.owl#has_function> ;\n" +
                        "              owl:targetIndividual <"+gene_path+"> .\n" +
                        "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        try {
            ResultSet resultSet = qexec.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution soln = resultSet.nextSolution();
//                System.out.println("go result : " + soln);
                String goUri = soln.getResource("sourceIndividual").getURI();

                goIdentifiers.add(goUri);
            }
        } finally {
            qexec.close();
        }
        return goIdentifiers;
    }
    public static List<String> getPhenotypesUri(String disease_path) throws JSONException {
        FileManager.getInternal().addLocatorClassLoader(RDF.class.getClassLoader());
        Model model = FileManager.getInternal().loadModelInternal(Utils.genericPath+"/task3/hpkg.owl");
        List<String> phenotypeIds = new ArrayList<>();
        System.out.println("disease_path : " + disease_path);


        String queryString =
                        "PREFIX rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" +
                        "PREFIX owl:  <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX hp: <http://purl.obolibrary.org/obo/hp.owl#>" +

                        "SELECT DISTINCT ?phenotype \n" +
                        "   WHERE {\n" +
                        "   ?assertion rdf:type owl:PropertyAssertion ;\n" +
                        "   owl:sourceIndividual ?phenotype ;\n" +
                        "   owl:assertionProperty hp:has_phenotype ;\n" +
                        "   owl:targetIndividual <"+disease_path+"> .\n }";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);

        try {
            ResultSet resultSet = qexec.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution soln = resultSet.nextSolution();
//                System.out.println("go result : " + soln);
                String phenotypeUri = soln.getResource("phenotype").getURI();

                phenotypeIds.add(phenotypeUri);
            }
        } finally {
            qexec.close();
        }

        return phenotypeIds;
    }
    public static int getSimilitudeTas3(List<String> goIds, List<String> phenotypeIds) {
        List<String> goRestrictions = new ArrayList<>();
        List<String> hpRestrictions = new ArrayList<>();
        int count = 0;
        FileManager.getInternal().addLocatorClassLoader(RDF.class.getClassLoader());
        Model model_go = FileManager.getInternal().loadModelInternal(Utils.genericPath + "/task3/gokg.owl");
        Model model_hp = FileManager.getInternal().loadModelInternal(Utils.genericPath + "/task3/hpkg.owl");

        if(!(goIds.isEmpty() && phenotypeIds.isEmpty())) {
            for (String goUri : goIds) {
                String queryString =
                        "PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
                                "PREFIX up: <https://www.uniprot.org/uniprotkb/> \n" +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                                "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
                                "\n" +
                                "SELECT DISTINCT ?property\n" +
                                "WHERE {\n" +
                                "  ?restriction a owl:Restriction ;\n" +
                                "               owl:someValuesFrom <" + goUri + "> ;\n" +
                                "               owl:onProperty ?property .\n" +
                                "}";

                Query query = QueryFactory.create(queryString);
                QueryExecution qexec = QueryExecutionFactory.create(query, model_go);

                try {
                    ResultSet resultSet = qexec.execSelect();
                    while (resultSet.hasNext()) {
                        QuerySolution soln = resultSet.nextSolution();
                        String restUri = soln.getResource("property").getURI();

                        if(!goRestrictions.contains(restUri)) {
                            goRestrictions.add(restUri);
                        }
                    }
                } finally {
                    qexec.close();
                }
            }

            for (String hpUri : phenotypeIds) {
                String queryStringHp =
                        "PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
                                "PREFIX up: <https://www.uniprot.org/uniprotkb/> \n" +
                                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                                "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                                "\n" +
                                "SELECT DISTINCT ?onPropertyValue\n" +
                                "WHERE {\n" +
                                "  <" + hpUri + "> owl:equivalentClass ?restriction .\n" +
                                "  ?restriction a owl:Restriction ;\n" +
                                "               owl:onProperty ?onPropertyValue .\n" +
                                "}";

                Query query_hp = QueryFactory.create(queryStringHp);
                QueryExecution qexec_hp = QueryExecutionFactory.create(query_hp, model_hp);

                try {
                    ResultSet resultSet = qexec_hp.execSelect();
                    while (resultSet.hasNext()) {
                        QuerySolution soln = resultSet.nextSolution();
                        String restUri = soln.getResource("onPropertyVal").getURI();

//                        System.out.println("restUri hp: " + restUri);

                        if (!(hpRestrictions.contains(restUri))) {
                            hpRestrictions.add(restUri);
                        }
                    }
                } finally {
                    qexec_hp.close();
                }
            }

            if(!goRestrictions.isEmpty()) {
                for(String go : goRestrictions) {
                    if(hpRestrictions.contains(go)) {
                        System.out.println("same : " + go);
                        count++;
                    }
                }
            }
        }
        return count;
    }
    //Read csv dataset disease patient file
    public static List<GeneDisease> readGdCsvFile(String csvFilePath) {
        Charset charset = StandardCharsets.UTF_8;
        List<GeneDisease> beans = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(csvFilePath, charset))) {
            CsvToBean<GeneDisease> csvToBean = new CsvToBeanBuilder<GeneDisease>(reader)
                    .withSeparator(',')
                    .withType(GeneDisease.class)
                    .build();

            beans = csvToBean.parse();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return beans;
    }
    //WriteDataInCsvFile to disease patient
    public static void writeGdCsv(String csvFilePath, GeneDisease geneDisease) {
        // first create file object for file placed at location
        // specified by filepath
        //ref : https://www.geeksforgeeks.org/writing-a-csv-file-in-java-using-opencsv/
        File file = new File(csvFilePath);
        try {
            // create FileWriter object with file as parameter
            FileWriter outputfile = new FileWriter(file, true);

            // create CSVWriter object filewriter object as parameter
            CSVWriter writer = new CSVWriter(outputfile);

            // add data to csv
//            for (DiseasePatient diseasePatient : diseasePatients) {
            String[] line = {
                    geneDisease.getUriGene(),
                    geneDisease.getUriDisease(),
                    geneDisease.getValuePrediction()
            };
            writer.writeNext(line);
//            }
            // closing writer connection
            writer.close();
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //Calculate accuracy metric
    public static ResponseAccuracy accuracy_metric_gd(List<GeneDisease> trainDisease, List<GeneDisease> partOfTrainDisease) {
        Map<GeneDiseasekey, GeneDisease> map1 = trainDisease.stream()
                .collect(Collectors.toMap(
                        GeneDiseasekey::new,
                        Function.identity()
                ));
        double accuracy = 0;
        long numberOfGoodPredictions = 0;
        ResponseAccuracy result = new ResponseAccuracy();

        numberOfGoodPredictions = partOfTrainDisease.stream()
                .filter(p -> map1.containsKey(new GeneDiseasekey(p)))
                .filter(p -> map1.get(new GeneDiseasekey(p)).equals(p))
                .count();

        System.out.println("numberOfGoodPredictions " + numberOfGoodPredictions);

        if(numberOfGoodPredictions > 0) {
            accuracy = (double) numberOfGoodPredictions/trainDisease.size();
        }
        result.setAccuracy(accuracy); result.setGoodPrediction(numberOfGoodPredictions);
        return result;
    }


//    public static List<String> getOnPropertyGo();
    /************************************* END TASK 2 *************************************/
}