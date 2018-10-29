package com.fmi.projects.lucene.indexer;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

public class ExtractFromFile {

        public static List<String> extract(String fileName) {

            List<String> lines = new LinkedList<String>();
            try {
                BufferedReader in = new BufferedReader(new FileReader(LuceneConstants.DATA_DIR + fileName));

                String line = in.readLine();
                while(line != null) {
                    lines.add(line);
                    line = in.readLine();
                }
                in.close();
            } catch (FileNotFoundException e) {
                System.out.println(e.getMessage());
            } catch (IOException e){
                System.out.println(e.getMessage());
            }

            return lines;
        }

}
