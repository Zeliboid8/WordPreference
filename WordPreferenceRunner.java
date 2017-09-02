import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    private static Set<String> markedWords = new LinkedHashSet<String>(); // Set of marked words
    private static String baseWord;	// Non-conjugated, non-pluralized, etc.
    
    // Menu items
    private static MenuItem translate;
    private static MenuItem delete;
    private static MenuItem webpage;
    private static MenuBar menuBar;
    private static Menu menuFile;
    private static MenuItem review;
    private static MenuItem save;
    private static MenuItem close;
    private static Menu menuEdit;
    private static MenuItem mark;
    private static Menu menuOptions;
    private static CheckMenuItem spaToEng;
    private static CheckMenuItem engToSpa;
    private static CheckMenuItem freToEng;
    private static CheckMenuItem engToFre;
    private static Menu menuMore;
    private static CheckMenuItem showInfo;
    private static CheckMenuItem markBaseWord;
    private static CheckMenuItem markAll;
    private static CheckMenuItem autoSave;
    
    // Buttons
    private static ToggleButton markButton;
    private static Button searchButton;
    
    private static boolean enableShowInfo = true;
    private static boolean enableMarkBaseWord = true;
    private static boolean enableMarkAll = false;
    private static boolean enableAutoSave = false;
    private static int languageSetting = 1;
    
    // Potential URL Strings
    private static final String url1 = "http://www.wordreference.com/es/en/translation.asp?spen=";	// Spanish to English
    private static final String url2 = "http://www.wordreference.com/es/translation.asp?tranword=";	// English to Spanish
    private static final String url3 = "http://www.wordreference.com/fren/";	// French to English
    private static final String url4 = "http://www.wordreference.com/enfr/";	// English to French
    private static URL url;
    
    // Text field instructions
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
        // Setting icon
        window.getIcons().add(new Image(WordPreferenceRunner.class.getResourceAsStream("Icon.png")));

        // Translated Table (on right)
        list.setItems(words);
        list.setMaxHeight(Double.MAX_VALUE);
        
     // Right-click features
        list.setCellFactory(listView -> {
            ListCell<String> cell = new ListCell<>();	 // These seemed to have to be initialized inside the cellFactory
            ContextMenu contextMenu = new ContextMenu(); // Right-click menu
            translate = new MenuItem();
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
            delete = new MenuItem();
            delete.textProperty().bind(Bindings.format("Delete", cell.itemProperty()));
            delete.setOnAction(event -> {
            	String word = cell.getItem();
            	System.out.println(word + " removed.");
            	markedWords.remove(word);
            	list.getItems().remove(word);
            	});
            webpage = new MenuItem();
            webpage.textProperty().bind(Bindings.format("Webpage", cell.itemProperty()));
            webpage.setOnAction(event -> {
            	String item = cell.getItem();
            	try 
            	{
					setURL(item);
				} 
            	catch (Exception e1) {}
            	try 
            	{
					java.awt.Desktop.getDesktop().browse(java.net.URI.create(url.toString()));
				} 
            	catch (Exception e2) {}
            	});
            contextMenu.getItems().addAll(translate, delete, webpage);

            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) 
                {
                    cell.setContextMenu(null);
                } 
                else 
                {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
        
     // Menu Section
        menuBar = new MenuBar();
        
        // File menu
        menuFile = new Menu("File");
        review = new MenuItem("Review");
        review.setOnAction(new EventHandler<ActionEvent>() {
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
        review.setAccelerator(KeyCombination.keyCombination("Ctrl+R"));
        save = new MenuItem("Save");
        save.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		saveFile();
        	}
        });
        save.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        close = new MenuItem("Close");
        close.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		window.close();
        	}
        });
        close.setAccelerator(KeyCombination.keyCombination("Ctrl+W"));
        menuFile.getItems().addAll(review, save, close);
        
        // Edit menu
        menuEdit = new Menu("Edit");
        mark = new MenuItem("Mark");
        mark.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		markWord(wordInput.getText());
        	}
        });
        mark.setAccelerator(KeyCombination.keyCombination("Ctrl+M"));
        menuEdit.getItems().addAll(mark);
        
        // Options menu
        menuOptions = new Menu("Options");
        spaToEng = new CheckMenuItem("Spanish to English");
        engToSpa = new CheckMenuItem("English to Spanish");
        freToEng = new CheckMenuItem("French to English");
        engToFre = new CheckMenuItem("English to French");
        spaToEng.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		setLanguage(1);
        	}
        });
        engToSpa.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		setLanguage(2);
        	}
        });
        freToEng.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		setLanguage(3);
        	}
        });
        engToFre.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		setLanguage(4);
        	}
        });
        setLanguage(languageSetting);
        menuOptions.getItems().addAll(spaToEng, engToSpa, new SeparatorMenuItem(), freToEng, engToFre);
        
        // More menu
        menuMore = new Menu("More");
        
        showInfo = new CheckMenuItem("Show More Information");
        showInfo.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		enableShowInfo = !enableShowInfo;
        		update();
        	}
        });
        showInfo.setAccelerator(KeyCombination.keyCombination("Ctrl+I"));
        if (enableShowInfo)
        {
        	showInfo.setSelected(true);
        }
        
        markBaseWord = new CheckMenuItem("Mark Base Word");
        markBaseWord.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		enableMarkBaseWord = !enableMarkBaseWord;
        	}
        });
        if (enableMarkBaseWord)
        {
        	markBaseWord.setSelected(true);
        }
        
        markAll = new CheckMenuItem("Mark Searched Words");
        markAll.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		enableMarkAll = !enableMarkAll;
        	}
        });
        if (enableMarkAll)
        {
        	markAll.setSelected(true);
        }
        
        autoSave = new CheckMenuItem("Enable Autosave");
        autoSave.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		enableAutoSave = !enableAutoSave;
        	}
        });
        if (enableAutoSave)
        {
        	autoSave.setSelected(true);
        }
        
        menuMore.getItems().addAll(showInfo, markBaseWord, markAll, autoSave);
        menuBar.getMenus().addAll(menuFile, menuEdit, menuOptions, menuMore);
        
        // Mark/Favorite Button
        markButton = new ToggleButton("Mark");
        markButton.setMaxHeight(35);
        markButton.setMaxWidth(Double.MAX_VALUE);
        markButton.setStyle("-fx-base: lightblue;");
        markButton.setOnAction(e -> {
            markButton.setSelected(!markButton.isSelected());
        	markWord(wordInput.getText());
        });
        
     // Regular Search Button
        searchButton = new Button("Search");
        searchButton.setMaxHeight(30);
        searchButton.setPrefWidth(80);
        searchButton.setOnAction(new EventHandler<ActionEvent>() {
        	public void handle(ActionEvent click)
        	{
        		try {
        			search(wordInput.getText());
				} catch (Exception e1) 
        		{
					System.out.println("Word search failed.");
				}
        		if (markedWords.contains(wordInput.getText()))
        		{
        			markButton.setSelected(true);
        		}
        		else
        		{
        			markButton.setSelected(false);
        		}
        	}
        });
        
        // Word Input Section
        updateInstructions();
        wordInput.setPrefHeight(30);
        wordInput.setMaxHeight(30);
        wordInput.setMaxWidth(Double.MAX_VALUE);

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
            	}
            }
        };  
        wordInput.addEventFilter(KeyEvent.KEY_PRESSED, eventHandlerEnter);
        HBox hbox = new HBox();
        HBox.setHgrow(wordInput, Priority.ALWAYS);
        hbox.setSpacing(10);
        hbox.getChildren().addAll(wordInput, searchButton);
        hbox.setAlignment(Pos.CENTER);
        
        Scene scene = new Scene(new VBox(), 420, 290);
        scene.setFill(Color.OLDLACE);
        final VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.getChildren().addAll(hbox, markButton, list);
        ((VBox) scene.getRoot()).getChildren().addAll(menuBar, vbox);
        scene.getStylesheets().add("Theme.css");
        window.setScene(scene);
        window.show();
    }
    
    public static void search(String word) throws Exception 
    {
    	translations.clear();
    	boolean foundBaseWord = false;
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
        			String beginning = inputLine.substring(inputLine.indexOf("class='ToWrd'") + 15);
        			String end = beginning.substring(0, beginning.indexOf("<"));
        			String finalWord = convert(end);
        			String beginningCutGender = beginning.substring(beginning.indexOf("POS2'") + 6);
        			String endCutGender = beginningCutGender.substring(0, beginningCutGender.indexOf("<"));
        			translations.put(finalWord, endCutGender);
        		}
        	}
        	if (!foundBaseWord && inputLine.contains("<td class='FrWrd' ><strong>"))
        	{
        		String baseWordBeginning = inputLine.substring(inputLine.indexOf("<td class='FrWrd' ><strong>") + 27);
        		String baseWordEnd = baseWordBeginning.substring(0, baseWordBeginning.indexOf("<"));
        		baseWord = convert(baseWordEnd);
        		foundBaseWord = true;
        	}
        }
        if (enableMarkAll)
    	{
        	if (translations.size() != 0)
        	{
        		markedWords.add(word);
        		markButton.setSelected(true);
        		if (enableMarkBaseWord)
        		{
        			System.out.println(baseWord + " added.");
        		}
        		else
        		{
        			System.out.println(word + " added.");
        		}
        	}
    	}
        if (markedWords.contains(wordInput.getText()))
		{
			markButton.setSelected(true);
		}
		else
		{
			markButton.setSelected(false);
		}
        update();
        in.close();
    }
    
    public static void setLanguage(int language)
    {
    	if (language == 1)
    	{
	    	if (languageSetting == 1)	// Prevents having no language selected
			{
				spaToEng.setSelected(true);
			}
			languageSetting = 1;
			engToSpa.setSelected(false);
			freToEng.setSelected(false);
			engToFre.setSelected(false);
    	}
    	else if (language == 2)
    	{
			if (languageSetting == 2)	// Prevents having no language selected
			{
				engToSpa.setSelected(true);
			}
			languageSetting = 2;
			spaToEng.setSelected(false);
			freToEng.setSelected(false);
			engToFre.setSelected(false);
    	}
    	else if (language == 3)
    	{
			if (languageSetting == 3)	// Prevents having no language selected
			{
				freToEng.setSelected(true);
			}
			languageSetting = 3;
			spaToEng.setSelected(false);
			engToSpa.setSelected(false);
			engToFre.setSelected(false);
    	}
    	else if (language == 4)
    	{
			if (languageSetting == 4)	// Prevents having no language selected
			{
				engToFre.setSelected(true);
			}
			languageSetting = 4;
			spaToEng.setSelected(false);
			engToSpa.setSelected(false);
			freToEng.setSelected(false);
    	}
    	updateInstructions();
    }
    
    public static void markWord(String word)
    {
    	if (word.length() != 0)
    	{
    		if (markButton.isSelected())
	    	{
	    		if (enableMarkBaseWord)
	    		{
	    			markedWords.remove(baseWord);
	    			System.out.println(baseWord + " removed.");
	    		}
	    		else
	    		{
	    			markedWords.remove(word);
	    			System.out.println(word + " removed.");
	    		}
	    		markButton.setSelected(false);
	    	}
	    	else if (!markButton.isSelected())
	    	{
	    		if (enableMarkBaseWord)
	    		{
	    			markedWords.add(baseWord);
	    			System.out.println(baseWord + " added.");
	    		}
	    		else
	    		{
	    			markedWords.add(word);
	    			System.out.println(word + " added.");
	    		}
	    		markButton.setSelected(true);
	    	}
    	}
    	else
    	{
    		markButton.setSelected(false);
    	}
    }
    
    public static void update()
    {
    	words.clear();
    	for (Map.Entry<String, String> e : translations.entrySet())
        {
        	if (enableShowInfo && e.getValue().length() != 0)
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
    	word = word.replace(" ", "%20");
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
    		Formatter file = new Formatter("settings.txt");
    		file.format("%s" + System.getProperty("line.separator"), languageSetting);		// Remembering the language setting
    		file.format("%s" + System.getProperty("line.separator"), enableShowInfo);		// Remembering whether extra information should be shown
    		file.format("%s" + System.getProperty("line.separator"), enableMarkBaseWord);	// Remembering mark base word settings
    		file.format("%s" + System.getProperty("line.separator"), enableMarkAll);		// Remembering mark all settings
    		file.format("%s" + System.getProperty("line.separator"), enableAutoSave);		// Remembering auto-save settings
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
    	File file = new File("settings.txt");
    	try
    	{
	    	Scanner sc = new Scanner(file);
	    	if (sc.hasNextLine())
	    	{
	    		languageSetting = Integer.parseInt(sc.nextLine());		// Setting language setting
	    		enableShowInfo = Boolean.valueOf(sc.nextLine());		// Setting extra information setting
	    		enableMarkBaseWord = Boolean.valueOf(sc.nextLine());	// Setting mark base word setting
	    		enableMarkAll = Boolean.valueOf(sc.nextLine());			// Setting mark all setting
	    		enableAutoSave = Boolean.valueOf(sc.nextLine());		// Setting auto-save setting
	    	}
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
    
    public static void updateInstructions()
    {
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
    }
    
    public static String convert(String original)
    {
    	if (original.contains("Ã"))
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
	    	original = original.replace("Ã±", "ñ");
	    	original = original.replace("Ã¡", "á");
	    	original = original.replace("Ã¢", "â");
	    	original = original.replace("Ã", "à"); // Must come last
    	}
    	if (original.contains("Â"))
    	{
    		original = original.replace("Â¿", "¿");
    		original = original.replace("Â¡", "¡");
    	}
    	if (original.contains("â"))
    	{
    		original = original.replace("â€¦", "...");
    		original = original.replace("â€", "–");
    		original = original.replace("â€™", "’");
    	}
    	original = original.replace("%20", " ");
    	return original;
    }
    
    public void stop()
    {
        if (enableAutoSave)
        {
        	saveFile();
        }
    }
}