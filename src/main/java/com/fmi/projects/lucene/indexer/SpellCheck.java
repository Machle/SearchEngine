package com.fmi.projects.lucene.indexer;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LevensteinDistance;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

public class SpellCheck {
    public static SpellChecker spell;
    public  static String spellCheckDir = LuceneConstants.SPELL_CHECK_INDEX_DIR;
    public static LuceneDictionary dictTitles;
    public static LuceneDictionary dictAuthors;
    public static LuceneDictionary dictDescriptions;

    public static void createSpellCheckingIndex(String indexDir){
        try {
            Directory dir = FSDirectory.open(new File(spellCheckDir));
            spell = new SpellChecker(dir);

            Directory dir2 = FSDirectory.open(new File(indexDir));
            IndexReader reader = IndexReader.open(dir2);

            dictTitles = new LuceneDictionary(reader,LuceneConstants.TITLE);
            dictAuthors = new LuceneDictionary(reader,LuceneConstants.AUTHOR);
            dictDescriptions = new LuceneDictionary(reader,LuceneConstants.DESCRIPTION);

            try{

                spell.indexDictionary(dictTitles, new IndexWriterConfig(
                                Version.LUCENE_36, null), true);
                spell.indexDictionary(dictAuthors, new IndexWriterConfig(
                        Version.LUCENE_36, null), true);
                spell.indexDictionary(dictDescriptions, new IndexWriterConfig(
                        Version.LUCENE_36, null), true);

            } finally {
                reader.close();
            }
            dir.close();
            dir2.close();
        }catch (IOException e){
            System.out.println(e.getMessage());
        }

    }

    public static String[] generateSuggestions(String wordToRespell){
        try {
            Directory dir = FSDirectory.open(new File(spellCheckDir));
            spell = new SpellChecker(dir);
            spell.setStringDistance(new LevensteinDistance());
            String[] suggestions = spell.suggestSimilar(wordToRespell,20);
            return suggestions;

        } catch (IOException e){
            System.out.println(e.getMessage());
        }
        return null;
    }


}
