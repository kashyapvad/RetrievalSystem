package com.company;

import java.io.*;
import java.util.*;

public class Utilities {

    static HashMap<String, TokenInfo> invertedIndex = new HashMap<>();

    static void showPage(String request, PrintStream out) throws Exception{
        //these two strings act as the mime types for the files that needs to be sent to the browser to display
        String s1 = "HTTP/1.1 200 OK" + "\n" + "Date: Fri, 16 Sep 2005 18:09:38 GMT" + "\n" +
                "Server: Apache/2.0.53  HP-UX_Apache-based_Web_Server (Unix) PHP/4.3.8" + "\n" +
                "Last-Modified: Fri, 16 Sep 2005 18:08:50 GMT" + "\n" +
                "ETag: \"5e1c1-2f-7c517080\"" + "\n" + "Accept-Ranges: bytes" + "\n" +
                "Content-Length: 10000" + "\n" +
                "Content-Type: text/html" + "\n\n";
        String s2 = "HTTP/1.1 200 OK" + "\n" + "Date: Fri, 16 Sep 2005 18:09:38 GMT" + "\n" +
                "Server: Apache/2.0.53  HP-UX_Apache-based_Web_Server (Unix) PHP/4.3.8" + "\n" +
                "Last-Modified: Fri, 16 Sep 2005 18:08:50 GMT" + "\n" +
                "ETag: \"5e1c1-2f-7c517080\"" + "\n" + "Accept-Ranges: bytes" + "\n" +
                "Content-Length: 10000" + "\n" +
                "Content-Type: text/plain" + "\n\n";

        if (request.substring(4, request.length() - 9).equals("/")) {
            s1 = s1 + "\n" +
                    "<html>\n" +
                    "<head><TITLE> CSC435 Sample Form for AddNum </TITLE></head>\n" +
                    "<BODY>\n" +
                    "<H1> Retrieval System </H1>\n" +
                    "\n" +
                    "<FORM method=\"GET\" action=\"http://localhost:3000/retrieved-documents\">\n" +
                    "\n" +
                    "Type In The Query\n" +
                    "\n" +
                    "<INPUT TYPE=\"text\" NAME=\"Query\" size=200 value=\"Query\"><P>\n" +
                    "\n" +

                    "<INPUT TYPE=\"submit\" VALUE=\"Search\">\n" +
                    "\n" +
                    "</FORM> </BODY></html>\n";
            out.println(s1);
        } else if(request.contains("/DocumentFiles")){
            String file = "." + request.substring(4,request.length() - 9);
            String matter = readFile(file);
            //ArrayList<String> paragraph = new ArrayList<>();
            //paragraph.addAll(Arrays.asList(matter.toLowerCase().split("")));
            /*for (String s:paragraph) {
                s = "<p>" + s + "</p>";
                s1+=s;
            }*/
            s2+=matter;
            out.println(s2);
        }

        else {
            String query = request.substring(request.indexOf("?Query=") +7,request.length()-9);
            ArrayList<String> relevantDocs = processQuery(query);
            s1+= readFiles("./DocumentFiles",relevantDocs);
            out.println(s1);
        }
    }


    //to read all the files in the root directory and returning a html string to display those files as hyper links
    static String readFiles(String dirName,ArrayList<String> relevantDocs) {

        String filedir = "<h1>Search Results</h1>\n\n";
        // Create a file object for your root directory

        File f1 = new File(dirName);

        // Get all the files and directories and storing it in an array of files
        File[] strFilesDirs = f1.listFiles();

        //looping through files to form hyperlink reference tags

        for (int i = 0; i < strFilesDirs.length; i++) {
            if (relevantDocs.contains(strFilesDirs[i].toString())) {
                filedir = filedir + "<a href=\"" + strFilesDirs[i].toString() + "\">" +
                        strFilesDirs[i].toString().substring(2) + "</a> <br>";
            }
        }

        return filedir;
    }


    //this methods reads a file and stores it in a string and returns the string
    //for example if a request for cat.html cam in it will read the file cat.html and returns the matter
    //in that file as a string so that the browser can display it
    static String readFile(String filename) {
        String records = "";
        try {
            //opens the file object to read inside it
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            //loops over all the lines present in the file and concatnates them together in records object and returns it
            while ((line = reader.readLine()) != null) {
                records = records + line + "\n";
            }
            reader.close();
            return records;

        } catch (Exception e) {
            System.err.format("Exception occurred trying to read '%s'.", filename);
            e.printStackTrace();
            return null;

        }
    }

    static void createDocs(String filename) throws Exception {

        String docs = readFile(filename);
        String id = "*TEXT";
        String body = "63 PAGE";
        int ind1 = docs.indexOf(id);
        int ind2 = docs.indexOf(body);
        int ind3 = docs.indexOf("*STOP");
        int docID = 1;
        while (ind2 >= 0) {



            File file = new File("./DocumentFiles/" + docID + ".txt");
            String str="";
            if (docs.indexOf(id, ind1 + 1) >= 0) {
                str = docs.substring(ind1 + (ind2 - ind1) + 13, docs.indexOf(id, ind1 + 1) - 2);
            } else {
                str = docs.substring(ind1 + (ind2 - ind1) + 13, ind3-2);
            }
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(str);
            writer.close();

            ind1 = docs.indexOf(id, ind1 + 1);
            ind2 = docs.indexOf(body, ind2 + 1);
            docID += 1;
        }


    }


    //builds the inverted index and stores it in a file
    static void invertedIndex(String dirName) throws Exception {

        File f1 = new File(dirName);
        File[] strFilesDirs = f1.listFiles();
        ArrayList<DocumentReference> documentList = new ArrayList<>();
        for (int i = 0; i < strFilesDirs.length; i++) {
            if (!strFilesDirs[i].toString().contains("DS_Store")) {
                DocumentReference documentReference = new DocumentReference(strFilesDirs[i], 0);
                documentList.add(documentReference);
                ArrayList<String> tokens;
                tokens = Stemmer.main(strFilesDirs[i]);
                Set<String> tokensUnique = new HashSet<String>(tokens);
                for (String st : tokensUnique) {
                    TokenOccurrence tokenOccurrence = new TokenOccurrence(documentReference, Collections.frequency(tokens, st));
                    if (!invertedIndex.containsKey(st)) {
                        TokenInfo tokenInfo = new TokenInfo();
                        invertedIndex.put(st, tokenInfo);
                        tokenInfo.occList.add(tokenOccurrence);
                    } else {
                        invertedIndex.get(st).occList.add(tokenOccurrence);

                    }

                }
            }
        }
        double numberOfDocuments = documentList.size();


        for (Map.Entry<String, TokenInfo> entry : invertedIndex.entrySet()) {
            TokenInfo tokenInfo = entry.getValue();
            for (TokenOccurrence to:tokenInfo.occList) {
                System.out.println(to.documentReference);
                System.out.println(to.documentReference.file);
            };
            double documentFrequency = tokenInfo.occList.size();
            tokenInfo.idf = (Math.log(numberOfDocuments / documentFrequency) / Math.log(2));
        }

        for (Map.Entry<String, TokenInfo> entry : invertedIndex.entrySet()) {
            TokenInfo tokenInfo = entry.getValue();
            for (TokenOccurrence to : tokenInfo.occList) {
                to.documentReference.length += Math.pow(to.count * tokenInfo.idf, 2);
            }
        }

        for (DocumentReference dr : documentList) {
            dr.length = Math.sqrt(dr.length);
        }

        File file = new File("./invertedIndex.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        for (Map.Entry<String, TokenInfo> entry : invertedIndex.entrySet()) {
            String term = entry.getKey();
            TokenInfo tokenInfo = entry.getValue();
            String str = "";
            str += term + " ";
            str += tokenInfo.idf + " ";
            ArrayList<TokenOccurrence> occList = tokenInfo.occList;
            for (TokenOccurrence to : occList) {
                str += to.documentReference.file + " ";
                str += to.documentReference.length + " ";
                str += to.count + " ";
            }
            str += "\n";
            writer.write(str);
        }

        writer.close();
    }

    //retrieves the inverted index that is built by the above method in runtime
    static void getInvertedIndex(String filename) {
        String index = readFile(filename);
        String[] invertedIndexString = index.split("\n");
        HashMap<File,DocumentReference> documentReferences = new HashMap<>();

        for (int i = 0; i < invertedIndexString.length; i++) {

            ArrayList<TokenOccurrence> occList = new ArrayList<>();
            String[] tokens = invertedIndexString[i].split(" ");
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.idf = Double.parseDouble(tokens[1]);
            for (int j = 2; j < tokens.length; j++) {
                if ((j + 1) % 3 == 0) {
                    File file = new File(tokens[j]);
                    DocumentReference documentReference;
                    if(!documentReferences.keySet().contains(file)) {
                        documentReference = new DocumentReference(file, Double.parseDouble(tokens[j + 1]));
                        documentReferences.put(file,documentReference);
                    }else{
                        documentReference = documentReferences.get(file);
                    }

                    TokenOccurrence tokenOccurrence = new TokenOccurrence(documentReference, Integer.parseInt(tokens[j + 2]));
                    occList.add(tokenOccurrence);
                }
            }
            tokenInfo.occList = occList;
            invertedIndex.put(tokens[0], tokenInfo);


        }
    }


    static ArrayList<String> processQuery(String query) throws Exception {

        File file = new File("./query.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(query);
        writer.close();
        ArrayList<String> queryTokens = Stemmer.main(file);
        file.delete();
        Set<String> queryUnique = new HashSet<>(queryTokens);
        HashMap<DocumentReference, Double> retrievedDocuments = new HashMap<>();
        double lengthOfQuery = 0.0;
        for (String t : queryUnique) {
            if (invertedIndex.keySet().contains(t)) {
                TokenInfo tokenInfo = invertedIndex.get(t);
                double weightOftInQ = Collections.frequency(queryTokens, t) * tokenInfo.idf;
                ArrayList<TokenOccurrence> occList = tokenInfo.occList;
                for (TokenOccurrence to : occList) {
                    DocumentReference documentReference = to.documentReference;
                    double count = to.count;
                    if (!(retrievedDocuments.keySet().contains(documentReference))) {
                        retrievedDocuments.put(documentReference, 0.0);
                    }
                    retrievedDocuments.put(documentReference, retrievedDocuments.get(documentReference) + weightOftInQ * tokenInfo.idf * count);
                }
                lengthOfQuery += Math.pow(weightOftInQ, 2);
            }
        }

        lengthOfQuery = Math.sqrt(lengthOfQuery);

        for (Map.Entry<DocumentReference, Double> entry : retrievedDocuments.entrySet()) {
            DocumentReference documentReference = entry.getKey();
            double score = entry.getValue();
            double lengthOfDoc = documentReference.length;
            double normalizedScore = score / (lengthOfDoc * lengthOfQuery);
            retrievedDocuments.put(documentReference, normalizedScore);

        }
        Map<DocumentReference, Double> sortedDocuments = sortByComparator(retrievedDocuments, false);
        ArrayList<String> relevantDocs = new ArrayList<>();
        int number = 0;
        int integer = 0;
        for (Map.Entry<DocumentReference, Double> entry : sortedDocuments.entrySet()) {
            if (number < 8) {
                if (entry.getValue() >= 0.225) {
                    relevantDocs.add(entry.getKey().file.toString());
                }
            }
            number += 1;
        }
        if (relevantDocs.size() == 0) {
            for (Map.Entry<DocumentReference, Double> entry : sortedDocuments.entrySet()) {

                if (integer < 3) {
                    relevantDocs.add(entry.getKey().file.toString());
                }
                integer += 1;
            }
        }
        return relevantDocs;
    }


     static Map<DocumentReference, Double> sortByComparator(Map<DocumentReference, Double> unsortMap, final boolean order){

        List<Map.Entry<DocumentReference, Double>> list = new LinkedList<>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, (o1, o2) -> {
            if (order)
            {
                return o1.getValue().compareTo(o2.getValue());
            }
            else
            {
                return o2.getValue().compareTo(o1.getValue());

            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<DocumentReference, Double> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<DocumentReference, Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    static void relevanceAssesment() throws Exception{

        String assesmentString = readFile("./time/TIME.REL.txt");
        ArrayList<String> assesment = new ArrayList<>();
        assesment.addAll(Arrays.asList(assesmentString.split("\n\n")));
        //System.out.println(Arrays.asList(assesment.get(0).split(" ")));
        String queriesString = readFile("./time/TIME.QUE.txt");
        ArrayList<String> queries = new ArrayList<>();
        queries.addAll(Arrays.asList(queriesString.split("\\*FIND")));
        queries.remove(0);
        HashMap<Integer,ArrayList<String>> myResults = new HashMap<>();
        int queryNumber =1;
        for (String query:queries) {
            String quer = query.substring(9,query.length()-1);
            myResults.put(queryNumber,processQuery(quer));
            queryNumber+=1;
        }

        double meanPrecison=0;
        double meanRecall =0;
        File file = new File("./relevanceAssesment.txt");
        File file2 = new File("./myRetrievedResults.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        BufferedWriter writer2 = new BufferedWriter(new FileWriter(file2));
        //System.out.println(myResults.get(1).get(0).substring(16,myResults.get(1).get(0).length()-4));

        for (int i = 0; i <assesment.size() ; i++) {
            double numberOfRelevantDocs =0;
            ArrayList<String> myRetrievedResults = new ArrayList<>();
            myRetrievedResults=myResults.get(i+1);
            ArrayList<String> assesedResults = new ArrayList<>();
            int j = i+1;
            String str2 = j + " ";
            assesedResults.addAll(Arrays.asList(assesment.get(i).split(" ")));
            for (String result:myRetrievedResults) {
                str2+= result.substring(16,result.length()-4) + " ";
                if(assesedResults.contains(result.substring(16,result.length()-4))){
                    numberOfRelevantDocs+=1;
                }
            }

            double precison = numberOfRelevantDocs/myRetrievedResults.size();
            meanPrecison+=precison;
            double recall = numberOfRelevantDocs/(assesedResults.size()-1);
            meanRecall+=recall;

            String str = j +  " " + precison + " " + recall +"\n";
            str2 +="\n";
            writer2.write(str2);
            writer.write(str);
        }

        meanPrecison = meanPrecison/83;

        meanRecall = meanRecall/83;
        String s = "Mean Precision: " + meanPrecison +"\n"+"Mean Recall: "+meanRecall;
        writer.write(s);
        writer.close();
        writer2.close();

    }

}

