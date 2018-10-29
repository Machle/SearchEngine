package com.fmi.projects.lucene.indexer;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {

    IndexSearcher indexSearcher;
    MultiFieldQueryParser queryParser;
    Query query;

    public Searcher(String indexDirectoryPath)
            throws IOException {
        Directory indexDirectory =
                FSDirectory.open(new File(indexDirectoryPath));
        indexSearcher = new IndexSearcher(indexDirectory);

        queryParser = new MultiFieldQueryParser(Version.LUCENE_36, new String[] {LuceneConstants.TITLE,
                LuceneConstants.AUTHOR, LuceneConstants.DESCRIPTION, LuceneConstants.RATING, LuceneConstants.URL},
                new MyAnalyzer());
        /*1
        queryParser = new QueryParser(Version.LUCENE_36,
                LuceneConstants.CONTENTS,
                new StandardAnalyzer(Version.LUCENE_36));
               */
    }

    public TopDocs search(String searchQuery)
            throws IOException, ParseException {
        query = queryParser.parse(searchQuery);
        TopDocs topDocs = indexSearcher.search(query, LuceneConstants.MAX_SEARCH);

        return topDocs;
    }

    public Document getDocument(ScoreDoc scoreDoc)
            throws CorruptIndexException, IOException {
        return indexSearcher.doc(scoreDoc.doc);
    }

    public Explanation ExplainSearch(Query query, ScoreDoc scoreDoc){
        Explanation explanation;
        try{
            explanation = indexSearcher.explain(query, scoreDoc.doc);
            return explanation;
        }catch(IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }

    public void close() throws IOException {
        indexSearcher.close();
    }
}