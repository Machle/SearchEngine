package com.fmi.projects.lucene.indexer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {

    private IndexWriter writer;


    public Indexer(String indexDirectoryPath) throws IOException {
        //this directory will contain the indexes
        Directory indexDirectory =
                FSDirectory.open(new File(indexDirectoryPath));

        //create the indexer
        //new StandardAnalyzer(Version.LUCENE_36)
        writer = new IndexWriter(indexDirectory,
                new MyAnalyzer(),true,
                IndexWriter.MaxFieldLength.UNLIMITED);

        /*writer = new IndexWriter(indexDirectory,
                new StandardAnalyzer(Version.LUCENE_36),true,
                IndexWriter.MaxFieldLength.UNLIMITED);*/
        //writer.optimize();
    }

    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }

    private Document getDocument(File file) throws IOException {
        Document document = new Document();

        //index file contents
        //index file name
        Field fileNameField = new Field(LuceneConstants.FILE_NAME,
                file.getName(),Field.Store.YES,Field.Index.NOT_ANALYZED);

        ExtractFromFile extr = new ExtractFromFile();
        List<String> fields = extr.extract(file.getName());

        Field title = new Field(LuceneConstants.TITLE, fields.get(0), Field.Store.YES, Field.Index.ANALYZED);
        Field author = new Field(LuceneConstants.AUTHOR, fields.get(1), Field.Store.YES, Field.Index.ANALYZED);
        Field rating = new Field(LuceneConstants.RATING, fields.get(2), Field.Store.YES, Field.Index.NOT_ANALYZED);
        Field description = new Field(LuceneConstants.DESCRIPTION, fields.get(3), Field.Store.YES, Field.Index.ANALYZED);
        Field url = new Field(LuceneConstants.URL, fields.get(4), Field.Store.YES, Field.Index.NOT_ANALYZED);
        //index file path
        title.setBoost(1.5F);
        author.setBoost(1.2F);
        description.setBoost(0.8F);
        Field filePathField = new Field(LuceneConstants.FILE_PATH,
                file.getCanonicalPath(),Field.Store.YES,Field.Index.NOT_ANALYZED);


        document.add(fileNameField);
        document.add(filePathField);
        document.add(title);
        document.add(author);
        document.add(description);
        document.add(rating);
        document.add(url);

        return document;
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    public int createIndex(String dataDirPath, FileFilter filter)
            throws IOException {
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();

        for (File file : files) {
            if(!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
                    ){
                indexFile(file);
            }
        }
        return writer.numDocs();
    }
}