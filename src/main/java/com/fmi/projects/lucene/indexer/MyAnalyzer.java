package com.fmi.projects.lucene.indexer;


import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.Version;

import java.io.Reader;


public class MyAnalyzer extends Analyzer {

    @Override
    public final TokenStream tokenStream(String fieldName, Reader reader) {
        StandardTokenizer tokenizer = new StandardTokenizer(Version.LUCENE_36,reader);
        tokenizer.setMaxTokenLength(255);
        TokenStream result = new StandardFilter(tokenizer);
        result = new LowerCaseFilter(result);
        result = new StopFilter(true, result, StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        //Adding the StemFilter here
        result = new PorterStemFilter(result);
        return result;
    }
}
