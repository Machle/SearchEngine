//This crawler is created by tano for IR project @ FMI

package com.fmi.projects.jsoup.web.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.HashSet;

import java.util.concurrent.ThreadLocalRandom;

public class BasicWebCrawler {

    private HashSet<String> links;
    private HashSet<String> titles;
    private int n;

    public BasicWebCrawler() {
        links = new HashSet<String>();
        n=0;
    }

    public void parse(String URL) {
        //Check if we have already crawled the URLs
        if(n==1000){
            links.clear();
            n = 0;
        }
        if (!links.contains(URL)) {
            try {
                // If not add it to the index
                if (links.add(URL)) {
                    System.out.println(URL);
                }

                //Fetch the HTML code
                Document document = Jsoup.connect(URL)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.132 Safari/537.36")
                            .get();

                //Parse the HTML to extract links to other URLs
                Elements linksOnPage = document.select("a[href]");

                Elements title = document.select("h1");
                //System.out.println(title.text());

                Element author = document.selectFirst(".authorName");
                //System.out.println(author.text());

                Elements rating = document.select(".average");
                //System.out.println(rating.text());

                Element description = extract_description(URL,document);
                //System.out.println(description.text());

                //check for English edition
                //we dont want other language for now
                String lang;
                try {
                    Element language = document.selectFirst("div.infoBoxRowItem[itemprop=inLanguage]");
                    lang = language.text();
                } catch (NullPointerException e){
                    lang = "English";
                }
                if(!lang.isEmpty()) {
                    //System.out.println(lang);
                    if(lang.equals("English")) {
                        try {
                            PrintWriter out = new PrintWriter("Data\\Data\\book" + URL.split("/")[5] + ".txt");
                            out.println(title.text());
                            out.println(author.text());
                            out.println(rating.text());
                            out.println(description.text());
                            out.println(URL);
                            out.close();
                        } catch (FileNotFoundException e) {
                            System.out.println(e.getMessage());
                        }
                    }
                }



                //Calculating next page url
                for (Element page: linksOnPage){
                    String url = page.attr("abs:href");
                    String[] url_arr = url.split("/");

                    if(url.contains("/book/")
                            && url.contains("/show/")
                                && !url.contains("#")
                                    && !url.split("/")[5].contains(".")
                                        && !url.equals("https://www.goodreads.com/book/show/")
                                            && !url.equals("https://www.goodreads.com/book/show")
                                                && url_arr.length>=4 && url_arr.length<=6
                                                    && !url.contains("href")){
                        n++;
                        parse(url);
                    }
                }

                //String next_url = next_page(URL);
                //parse(next_url);

            } catch (IOException e) {
                //If error occurs catch Exception, and generete next page via next_page method
                System.err.println("For '" + URL + "': " + e.getMessage());
                String next_url = next_page(URL);
                n++;
                parse(next_url);
            } catch (UncheckedIOException e){
                System.err.println("For '" + URL + "': " + e.getMessage());
                String next_url = next_page(URL);
                n++;
                parse(next_url);
            }
        }
    }

    private static Element extract_description(String URL, Document document){
        Elements descr = document.select("#description");
        if(!descr.isEmpty()) {
            if (descr.size() < 2)
                return descr.get(0);
            else return descr.get(1);
        } else {
            Element empty = document.selectFirst("h1");
            return empty;
        }
    }

    //incrementing book id to get the next page url
    private static String next_page(String URL){
        String[] arr_url = URL.split("/");
        long id;
        String next_page;
        if(arr_url.length <6){
            id = ThreadLocalRandom.current().nextLong(0, 100000);
            next_page = URL + Long.toString(id);
        }else {
            //handling other urls we dont want to scrape
            if(arr_url[5].equals("“")){
                id = ThreadLocalRandom.current().nextLong(0, 100000);
            } else if (arr_url[5].contains(".")) {
                String[] index = arr_url[5].split(".");
                id = Long.parseLong(index[0]);
            } else if (arr_url[5].contains("-")) {
                String[] index = arr_url[5].split("-");
                id = Long.parseLong(index[0]);
            } else {
                id = Long.parseLong(arr_url[5]);
            }
            id++;

            next_page = URL.replace(arr_url[5], Long.toString(id));
        }
        return next_page;
    }

    public static void main(String[] args) {

        new BasicWebCrawler().parse("https://www.goodreads.com/book/show/2");
    }

}
