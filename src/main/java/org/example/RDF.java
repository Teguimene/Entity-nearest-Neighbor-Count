package org.example;

import org.apache.jena.query.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.FileManager;

import java.util.HashSet;
import java.util.Set;

public class RDF {
    private static final double INTERACTION_THRESHOLD = 0.5; // 50% de correspondance
    public static  Model model;
    public static void main(String[] args) {
        sparqlTest();
    }

    static void sparqlTest() {
        FileManager.getInternal().addLocatorClassLoader(RDF.class.getClassLoader());
        Model model = FileManager.getInternal().loadModelInternal(Utils.genericPath+"/task3/hpkg.owl");
//        String queryString =
//                "PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
//                        "PREFIX up: <https://www.uniprot.org/uniprotkb/> \n" +
//                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
//                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
//                        "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
//                        "\n" +
//                        "SELECT DISTINCT ?property\n" +
//                        "WHERE {\n" +
//                        "  ?restriction a owl:Restriction ;\n" +
//                        "               owl:someValuesFrom <http://purl.obolibrary.org/obo/GO_0005887> ;\n" +
//                        "               owl:onProperty ?property .\n" +
//                        "}";
        String queryString =
                "PREFIX obo: <http://purl.obolibrary.org/obo/> \n" +
                        "PREFIX up: <https://www.uniprot.org/uniprotkb/> \n" +
                        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" +
                        "PREFIX owl: <http://www.w3.org/2002/07/owl#> \n" +
                        "PREFIX obo: <http://purl.obolibrary.org/obo/>" +
                        "\n" +
                        "SELECT ?onPropertyValue\n" +
                        "WHERE {\n" +
                        "  <http://purl.obolibrary.org/obo/HP_0001249> owl:equivalentClass ?restriction .\n" +
                        "  ?restriction a owl:Restriction ;\n" +
                        "               owl:onProperty ?onPropertyValue .\n" +
                        "}";


//        String queryString = "PREFIX rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
//                "PREFIX organism: <http://string-db.org/rdf/organism/>\n" +
//                "PREFIX taxon: <http://identifiers.org/taxonomy/>\n" +
//                "PREFIX protein: <http://string-db.org/network/>\n" +
//                "PREFIX uniprotkb: <http://purl.uniprot.org/uniprot/P24941>\n" +
//                "PREFIX ip_highest: <http://string-db.org/rdf/interaction/physical-highest-confidence-cutoff>\n" +
//                "\n" +
//                "SELECT ?partnerUP ?partnerLabel WHERE {\n" +
//                "     ?protein organism: taxon:9606 .\n" +
//                "     ?protein rdfs:seeAlso uniprotkb:P24941 .\n" +
//                "     ?protein ip_highest: ?partner .\n" +
//                "     ?partner rdfs:label ?partnerLabel .\n" +
//                "     ?partner rdfs:seeAlso ?partnerUP .\n" +
//                "     FILTER(STRSTARTS(str(?partnerUP), str(uniprotkb:)))\n" +
//                "}";

        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.create(query, model);


        try {
            ResultSet resultSet = qexec.execSelect();
            while(resultSet.hasNext()) {
                QuerySolution soln = resultSet.nextSolution();
                    System.out.println("hp result : " + soln);
            }

        } finally {
            qexec.close();
        }
    }
}
