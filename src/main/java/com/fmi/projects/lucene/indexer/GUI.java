package com.fmi.projects.lucene.indexer;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

public class GUI extends Application{
    Stage window;
    TextArea output;
    private PrintStream ps;
    String searchby;
    static SpellCheck spellCheck;


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) throws Exception {
        window = primaryStage;
        window.setTitle("Book Search");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label nameLabel = new Label("Search query: ");
        GridPane.setConstraints(nameLabel, 0, 1);

        Label spellLabel= new Label("Did you mean: ");
        GridPane.setConstraints(spellLabel, 2, 2);


        Menu properties = new Menu("Search by");

        //Menu items
        searchby = "";
        MenuItem everything = new MenuItem("Everything");
        everything.setOnAction( e-> setValue(""));
        properties.getItems().add(everything);

        MenuItem title = new MenuItem("Title");
        title.setOnAction( e-> setValue("title:"));
        properties.getItems().add(title);

        MenuItem author = new MenuItem("Author");
        author.setOnAction( e-> setValue("author:"));
        properties.getItems().add(author);

        MenuItem description = new MenuItem("Description");
        description.setOnAction( e-> setValue("description:"));
        properties.getItems().add(description);

        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(properties);

        BorderPane layout = new BorderPane();
        layout.setTop(menuBar);

        //Search input
        TextField searchInput = new TextField();
        GridPane.setConstraints(searchInput, 1, 1);

        TextField spellCheckField = new TextField();
        GridPane.setConstraints(spellCheckField, 3, 2);

        Button searchButton = new Button("Search");
        GridPane.setConstraints(searchButton, 1,2);

        Button spellButton = new Button("YES");
        GridPane.setConstraints(spellButton, 3,3);


        output = new TextArea();
        output.setPrefHeight(600);
        output.setPrefWidth(500);
        GridPane.setConstraints(output, 1, 5);


        searchButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                String searchQuery = searchInput.getText(); //get the search query
                String input = searchby.concat(searchQuery);
                System.out.println(input);
                String[] searchQueryArr = searchQuery.split(" ");
                ArrayList<String> spellSuggestions = new ArrayList<>();
                String didYouMean ="";
                for(String s: searchQueryArr){
                    System.out.println(s);

                    String[] sugg =spellCheck.generateSuggestions(s);
                    for(String str: sugg) {
                        if(str.contains("'")){
                            str.replace("'", "");
                        }
                        spellSuggestions.add(str);
                        System.out.print(str + " ");
                    }
                    System.out.println();
                    if(spellSuggestions.contains(s)){
                        didYouMean=didYouMean+ " " + s;
                    } else {
                        String bestSugg = LuceneTester.getBestMatch(s, spellSuggestions);
                        if(didYouMean.isEmpty()){
                            didYouMean=bestSugg;
                        } else {
                            didYouMean = didYouMean + " " + bestSugg;
                        }
                    }
                    spellSuggestions.clear();
                }
                output.clear();


                try {
                    Searcher searcher = new Searcher(LuceneConstants.INDEX_DIR);
                    TopDocs hits = searcher.search(input);
                    System.out.println(hits.getMaxScore());
                    if(hits.getMaxScore()< 1 || hits.totalHits<3){
                        spellCheckField.setText(didYouMean);
                        spellButton.setOnAction(new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent event) {
                                searchInput.setText(spellCheckField.getText());
                                searchButton.fire();
                            }
                        });
                    }
                    if(hits.totalHits == 0){
                        output.setText("0 documents found ...");
                    }
                    for(ScoreDoc scoreDoc : hits.scoreDocs) {
                        Document doc = searcher.getDocument(scoreDoc);
                        String title = doc.get(LuceneConstants.TITLE);
                        String author = doc.get(LuceneConstants.AUTHOR);
                        String description = doc.get(LuceneConstants.DESCRIPTION);
                        String URL= doc.get(LuceneConstants.URL);


                        output.appendText(title + "\n");
                        output.appendText(author + "\n");
                        output.appendText(description + "\n");
                        output.appendText(URL + "\n");
                        output.appendText("----------------------------------------------------------------" + "\n");

                    }
                    searcher.close();
                } catch (IOException e){

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        grid.getChildren().addAll(nameLabel, searchInput, searchButton, output, menuBar, spellCheckField, spellLabel, spellButton);

        Scene scene = new Scene(grid, 900, 800);
        window.setScene(scene);
        window.show();
    }

    private void setValue(String s2){
        searchby = s2;
    }

}