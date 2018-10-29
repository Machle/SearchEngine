package com.fmi.projects.lucene.indexer;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class LuceneTester {

    String indexDir = LuceneConstants.INDEX_DIR;
    String dataDir = LuceneConstants.DATA_DIR;
    Indexer indexer;
    Searcher searcher;
    static SpellCheck spellCheck;

    public static void main(String[] args) {
        LuceneTester tester;
        try {
            tester = new LuceneTester();
            tester.createIndex();
            Scanner in = new Scanner(System.in);

            while(in!=null) {
                System.out.println("Search for: ");
                String searchQuery = in.nextLine();
                String[] searchQueryArr = searchQuery.split(" ");
                ArrayList<String> spellSuggestions = new ArrayList<>();
                String didYouMean ="";
                for(String s: searchQueryArr){
                    //System.out.println(s);

                    String[] sugg =spellCheck.generateSuggestions(s);
                    for(String str: sugg) {
                        spellSuggestions.add(str);
                        //System.out.print(str + " ");
                    }
                    System.out.println();
                    if(spellSuggestions.contains(s)){
                        didYouMean=didYouMean+ " " + s;
                    } else {
                        String bestSugg = getBestMatch(s, spellSuggestions);
                        didYouMean=didYouMean + " " + bestSugg;
                    }
                    spellSuggestions.clear();
                }

                tester.search(searchQuery);

               // for(String s: spellSuggestions){
               //     System.out.print(s + " ");
               // }
                //System.out.println("Did you mean " + didYouMean+"?");
                System.out.println();
                spellSuggestions.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private void createIndex() throws IOException {
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        spellCheck = new SpellCheck();
        spellCheck.createSpellCheckingIndex(indexDir);
        //spellCheck.createSpellCheckingIndex(indexDir, LuceneConstants.AUTHOR);
        //spellCheck.createSpellCheckingIndex(indexDir, LuceneConstants.DESCRIPTION);
        System.out.println(numIndexed+" File indexed, time taken: "
                +(endTime-startTime)+" ms");
    }

    public void search(String searchQuery) throws IOException, ParseException {
        searcher = new Searcher(indexDir);
        long startTime = System.currentTimeMillis();
        TopDocs hits = searcher.search(searchQuery);

        long endTime = System.currentTimeMillis();

        System.out.println(hits.totalHits +
                " documents found. Time : " + (endTime - startTime) + "ms");
        for(ScoreDoc scoreDoc : hits.scoreDocs) {
            Document doc = searcher.getDocument(scoreDoc);
            String docDir = doc.get(LuceneConstants.FILE_PATH);
            String docName = doc.get(LuceneConstants.FILE_NAME);
            Explanation ex = searcher.indexSearcher.explain(searcher.query , scoreDoc.doc);

            List<String> fields = ExtractFromFile.extract(docName);
            System.out.println("-----------------------------------------------------------------------------------");
            System.out.println("File: "
                    + docDir + " URL: " + fields.get(4));
            System.out.println("*********************");
            Document doc1 = searcher.indexSearcher.doc(scoreDoc.doc);
            System.out.println(doc.get("title"));
            System.out.println(ex.toString());
            System.out.println("*********************");

        }
        searcher.close();
    }

    public static int editDistance(String word1, String word2){
        int len1 = word1.length();
        int len2 = word2.length();

        int[][] dp = new int[len1+1][len2+1];
        for(int i=0;i<=len1;i++){
            dp[i][0] = i;
        }
        for(int j=0;j<=len2;j++){
            dp[0][j] = j;
        }

        for(int i=0;i<len1;i++){
            char c1 = word1.charAt(i);
            for(int j=0;j<len2;j++){
                char c2 = word2.charAt(j);

                if(c1==c2){
                    dp[i+1][j+1] = dp[i][j];
                } else {
                    int replace = dp[i][j] + 1;
                    int insert = dp[i][j + 1] + 1;
                    int delete = dp[i + 1][j] + 1;

                    int min = replace > insert ? insert : replace;
                    min = delete > min ? min : delete;
                    dp[i + 1][j + 1] = min;
                }
            }
        }
        return dp[len1][len2];
    }

    public static String getBestMatch(String qword, ArrayList<String> sugg){
        int min = 100;
        int index = 0;
        for(int i=0; i<sugg.size();i++){
            int editDistance = editDistance(qword, sugg.get(i));
            if(min>editDistance) {
                min = editDistance;
                index = i;
            }
        }
        return sugg.get(index);
    }
}