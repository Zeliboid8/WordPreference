import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
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
    private static Map<String, String> translations = new LinkedHashMap<String, String>(); // Used to eliminate duplicates
    private static ObservableList<String> words = FXCollections.observableArrayList(); // Translated words
    private static ListView<String> list = new ListView<String>();	// List of possible meanings
    private static TextField wordInput = new TextField(); // Input field
    private static Set<String> markedWords = new HashSet<String>(); // Set of marked words
    private static int languageSetting = 1;
    private static boolean showMoreInfo = false;
    private static URL url;
    
    private static final String url1 = "http://www.wordreference.com/es/en/translation.asp?spen=";	// Spanish to English
    private static final String url2 = "http://www.wordreference.com/es/translation.asp?tranword=";	// English to Spanish
    private static final String url3 = "http://www.wordreference.com/fren/";	// French to English
    private static final String url4 = "http://www.wordreference.com/enfr/";	// English to French
    
    private static final String instructions1 = "Introduzca su palabra aqui.";
    private static final String instructions2 = "Type your word here.";
    private static final String instructions3 = "Tape tu mot ici.";
    private static final String instructions4 = "Type your word here.";
    
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

        // Translated Table (on right)
        list.setItems(words);
        list.setMaxHeight(Double.MAX_VALUE);
        
     // Right-click features
        list.setCellFactory(listView -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu(); // Right-click menu
            MenuItem translate = new MenuItem();
            translate.textProperty().bind(Bindings.format("Translate", cell.itemProperty()));
            translate.setOnAction(event -> {
                String item = cell.getItem();
                wordInput.setText(item);
                try {
					search(item);
				} 
                catch (Exception e1) {
				}
            });
            MenuItem delete = new MenuItem();
            delete.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            delete.setOnAction(event -> {
            	markedWords.remove(cell.getItem());
            	list.getItems().remove(cell.getItem());
            	});
            MenuItem webpage = new MenuItem();
            webpage.textProperty().bind(Bindings.format("Webpage", cell.itemProperty()));
            webpage.setOnAction(event -> {
            	String item = cell.getItem();
            	try {
					setURL(item);
				} catch (Exception e1) {}
            	try {
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url.toString()));
				} 
            	catch (Exception e2) {}
            	});
            contextMenu.getItems().addAll(translate, delete, webpage);

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
        		list.setItems(words);
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
        CheckMenuItem spaToEng = new CheckMenuItem("Spanish to English");
        CheckMenuItem engToSpa = new CheckMenuItem("English to Spanish");
        CheckMenuItem freToEng = new CheckMenuItem("French to English");
        CheckMenuItem engToFre = new CheckMenuItem("English to French");
        spaToEng.setSelected(true);
        spaToEng.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		languageSetting = 1;
        		wordInput.setPromptText(instructions1);
        		engToSpa.setSelected(false);
        		freToEng.setSelected(false);
        		engToFre.setSelected(false);
        	}
        });
        engToSpa.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		languageSetting = 2;
        		wordInput.setPromptText(instructions2);
        		spaToEng.setSelected(false);
        		freToEng.setSelected(false);
        		engToFre.setSelected(false);
        	}
        });
        freToEng.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		languageSetting = 3;
        		wordInput.setPromptText(instructions3);
        		spaToEng.setSelected(false);
        		engToSpa.setSelected(false);
        		engToFre.setSelected(false);
        	}
        });
        engToFre.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		languageSetting = 4;
        		wordInput.setPromptText(instructions4);
        		spaToEng.setSelected(false);
        		engToSpa.setSelected(false);
        		freToEng.setSelected(false);
        	}
        });
        menuOptions.getItems().addAll(spaToEng, engToSpa, new SeparatorMenuItem(), freToEng, engToFre);
        
        Menu menuMore = new Menu("More");
        CheckMenuItem showInfo = new CheckMenuItem("Show More Information");
        showInfo.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		showMoreInfo = !showMoreInfo;
        		update();
        	}
        });
        showInfo.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        menuMore.getItems().add(showInfo);
        menuBar.getMenus().addAll(menuFile, menuOptions, menuMore);
        
        // Mark/Favorite Button
        ToggleButton mark = new ToggleButton("Mark");
        mark.setMaxHeight(35);
        mark.setMaxWidth(Double.MAX_VALUE);
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
        switch (languageSetting) 
    	{
    		case 1:	wordInput.setPromptText(instructions1);
    				break;
    		case 2: wordInput.setPromptText(instructions2);
    				break;
    		case 3: wordInput.setPromptText(instructions3);
    				break;
    		case 4: wordInput.setPromptText(instructions4);
    				break;
    	}
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
            		else
            		{
            			mark.setSelected(false);
            		}
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
    	String word = text;
    	while (word.contains(" "))
    	{
    		word = word.substring(0, word.indexOf(" ")) + "%20" + word.substring(word.indexOf(" ") + 1);
    	}
    	setURL(word);
    	BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
        {
        	if (inputLine.contains("class='ToWrd'"))  // Every translation is preceded by "class='ToWrd'" in HTML
        	{
        		if (!inputLine.contains("weight:bold"))
        		{
        			String beginningCut = inputLine.substring(inputLine.indexOf("class='ToWrd'") + 15);
        			String endCut = beginningCut.substring(0, beginningCut.indexOf("<"));
        			String finalWord = convert(endCut);
        			String beginningCutGender = beginningCut.substring(beginningCut.indexOf("POS2'") + 6);
        			String endCutGender = beginningCutGender.substring(0, beginningCutGender.indexOf("<"));
        			translations.put(finalWord, endCutGender);
        		}
        	}
        }
        update();
        in.close();
    }
    
    public static void update()
    {
    	words.clear();
    	for (Map.Entry<String, String> e : translations.entrySet())
        {
        	if (showMoreInfo && e.getValue().length() != 0)
        	{
        		words.add(e.getKey() + " (" + e.getValue() + ")");
        	}
        	else
        	{
        		words.add(e.getKey());
        	}
        }
    	list.setItems(words);
    }
    
    public static void setURL(String word) throws Exception
    {
    	switch (languageSetting) 
    	{
    		case 1:	url = new URL(url1 + word);
    				break;
    		case 2: url = new URL(url2 + word);
    				break;
    		case 3: url = new URL(url3 + word);
    				break;
    		case 4: url = new URL(url4 + word);
    				break;
    	}
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
    
    public static String convert(String original)
    {
    	original = original.replace("Ã¨", "è");
    	original = original.replace("Ã©", "é");
    	original = original.replace("Ã¢", "â");
    	original = original.replace("Ã²", "ò");
    	original = original.replace("Ã³", "ó");
    	original = original.replace("Ã´", "ô");
    	original = original.replace("Ã§", "ç");
    	original = original.replace("Ã¼", "ü");
    	original = original.replace("Ãº", "ú");
    	original = original.replace("Ã¹", "ù");
    	original = original.replace("Ã»", "û");
    	original = original.replace("Ã¯", "ï");
    	original = original.replace("Ã¬", "ì");
    	original = original.replace("Ã­", "í");
    	original = original.replace("Ã®", "î");
    	original = original.replace("Â¿", "¿");
    	original = original.replace("Â¡", "¡");
    	original = original.replace("Ã±", "ñ");
    	original = original.replace("Ã¡", "á");
    	original = original.replace("Ã¢", "â");
    	original = original.replace("Ã", "à"); // Must come last
    	return original;
    }
}