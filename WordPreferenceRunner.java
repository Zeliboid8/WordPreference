import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.util.*;
import java.net.*;

public class WordPreferenceRunner extends Application 
{
    private static Stage window;
    private static Set<String> translations = new LinkedHashSet<String>(); // Used to eliminate duplicates
    private static ObservableList<String> words = FXCollections.observableArrayList(); // Translated words
    private static ListView<String> list = new ListView<String>();	// List of possible meanings
    private static TextField wordInput = new TextField(); // Input field
    private static Set<String> markedWords = new HashSet<String>(); // Set of marked words
    private static int urlNumber = 1;
    
    private static final String url1 = "http://www.wordreference.com/es/en/translation.asp?spen=";
    private static final String url2 = "http://www.wordreference.com/es/translation.asp?tranword=";
    
    public static void main(String[] args) 
    {
    	loadFile();
    	launch(args);
    }
    
    public void start(Stage primaryStage) throws Exception 
    {
        window = primaryStage;
        // Setting window title
        window.setTitle("WordPreference");
        // Setting taskbar icon
        window.getIcons().add(new Image(WordPreferenceRunner.class.getResourceAsStream("Icon.png")));
        
        // Menu Section
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem reviewMarked = new MenuItem("Review");
        reviewMarked.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		translations.clear();
        		words.clear();
        		for (String word : markedWords)
        		{
        			words.add(word);
        		}
        		update();
        	}
        });
        reviewMarked.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
        MenuItem save = new MenuItem("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		saveFile();
        	}
        });
        save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        menuFile.getItems().addAll(reviewMarked, save);
        
        Menu menuOptions = new Menu("Options");
        MenuItem spaToEng = new MenuItem("Spanish to English");
        spaToEng.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		urlNumber = 1;
        	}
        });
        MenuItem engToSpa = new MenuItem("English to Spanish");
        engToSpa.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		urlNumber = 2;
        	}
        });
        menuOptions.getItems().addAll(spaToEng, engToSpa);
        
        menuBar.getMenus().addAll(menuFile, menuOptions);

        // Translated Table (on right)
        list.setItems(words);
        list.setMaxHeight(150);
        
        // Right-click features
        list.setCellFactory(listView -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu(); // Right-click menu
            MenuItem lookUp = new MenuItem();
            lookUp.textProperty().bind(Bindings.format("Look Up ", cell.itemProperty()));
            lookUp.setOnAction(event -> {
                String item = cell.getItem();
                wordInput.setText(item);
                try {
					search(item);
				} 
                catch (Exception e1) {
				}
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            deleteItem.setOnAction(event -> {
            	markedWords.remove(cell.getItem());
            	list.getItems().remove(cell.getItem());
            	});
            contextMenu.getItems().addAll(lookUp, deleteItem);

            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
        
        // Mark/Favorite Button
        ToggleButton mark = new ToggleButton("Mark");
        mark.setMaxHeight(35);
        mark.setMinWidth(400);
        mark.setStyle("-fx-base: lightblue;");
        mark.setOnAction(e -> {
            ToggleButton toggle = (ToggleButton) e.getSource();
            if (wordInput.getText().length() != 0)
            {
            	if (toggle.isSelected())
            	{
            		markedWords.add(wordInput.getText());
            		System.out.println(wordInput.getText() + " added.");
            	}
            	else if (!toggle.isSelected())
            	{
            		markedWords.remove(wordInput.getText());
            		System.out.println(wordInput.getText() + " removed.");
            	}
            }
        });
        
        // Word Input Section
        wordInput.setPromptText("Introduzca su palabra aqui.");
        wordInput.setPrefHeight(30);
        wordInput.setMaxHeight(30);

        EventHandler<KeyEvent> eventHandlerEnter = new EventHandler<KeyEvent>() 
        {
            @Override 
            public void handle(KeyEvent e)
            {
            	if (e.getCode() == KeyCode.ENTER)
            	{
            		try {
            			search(wordInput.getText());
					} catch (Exception e1) 
            		{
						System.out.println("Word search failed.");
					}
            		if (markedWords.contains(wordInput.getText()))
            		{
            			mark.setSelected(true);
            		}
            	}
            	else
            	{
            		mark.setSelected(false);
            	}
            }
        };  
        wordInput.addEventFilter(KeyEvent.KEY_PRESSED, eventHandlerEnter);
        
        
        Scene scene = new Scene(new VBox(), 420, 290);
        scene.setFill(Color.OLDLACE);
        final VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.getChildren().addAll(wordInput, mark, list);
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar, vbox);
        scene.getStylesheets().add("Theme.css");
        window.setScene(scene);
        window.show();
    }
    
    public static void search(String text) throws Exception 
    {
    	translations.clear();
    	words.clear();
    	String word = text;
    	URL url = null;
    	while (word.contains(" "))
    	{
    		word = word.substring(0, word.indexOf(" ")) + "%20" + word.substring(word.indexOf(" ") + 1);
    	}
    	if (urlNumber == 1)	// Spanish to English
    	{
    		url = new URL(url1 + word);
    	}
    	if (urlNumber == 2)	// English to Spanish
    	{
    		url = new URL(url2 + word);
    	}
    	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
        {
        	if (inputLine.contains("class='ToWrd'"))  // Every translation is preceded by "class='ToWrd'" in HTML
        	{
        		if (!inputLine.contains("English") && !inputLine.contains("Español"))
        		{
        			String beginningCut = inputLine.substring(inputLine.indexOf("class='ToWrd'") + 15);
        			String endCut = beginningCut.substring(0, beginningCut.indexOf("<"));
        			translations.add(endCut);
        		}
        	}
        }
        update();
        in.close();
    }
    
    public static void update()
    {
    	for (String translatedWord : translations)
        {
        	words.add(translatedWord);
        }
    	list.setItems(words);
    }
    
    public static void saveFile()
    {
    	Iterator<String> itr = markedWords.iterator();
    	try
    	{
    		Formatter file = new Formatter("Vocabulary Words.txt");
    		while (itr.hasNext())
    		{
    			file.format("%s\n", itr.next());
    		}
    		file.close();
    	}
    	catch(Exception e) {}
    	System.out.println("File saved.");
    }
    
    public static void loadFile()
    {
    	File file = new File("Vocabulary Words.txt");
    	try
    	{
	    	Scanner sc = new Scanner(file);
			while (sc.hasNextLine())
			{
				markedWords.add(sc.nextLine());
			}
			sc.close();
    	}
    	catch(FileNotFoundException exception)
    	{
    	}
    	System.out.println("File loaded.");
    }
}