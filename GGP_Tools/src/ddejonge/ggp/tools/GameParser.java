package ddejonge.ggp.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Given the path to some GDL file on disk, will return a list of GDL rules.
 * 
 * @author Dave de Jonge, Western Sydney University
 *
 */
public class GameParser {
	
	/**
	 * 
	 * @param pathToGameFolder The path the folder in which the game description file is stored. e.g. C:\games\TicTacToe.
	 * @return
	 */
	public static List<Gdl> getRulesFromGameFolder(String pathToGameFolder){
		String fileName =  getGDLFileName(pathToGameFolder);
		
		if(!pathToGameFolder.endsWith(File.separator)){
			pathToGameFolder += File.separator;
		}
		
		return file2rules(pathToGameFolder + fileName);
	}
	
	
	public static List<Gdl> file2rules(String pathToGameDescriptionFile){
		Path p1 = Paths.get(pathToGameDescriptionFile);
		
		String fileContent = null;
		try {
			fileContent = new String(Files.readAllBytes(p1));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(fileContent == null){
			return null;
		}
		
		return fileContent2rules(fileContent);
	}
	
	
	/**
	 * Converts the text content of a Game Description File into GDL rules.
	 */
	public static List<Gdl> fileContent2rules(List<String> fileContent){
		
		StringBuilder sb = new StringBuilder();
		for (String line : fileContent) {
			sb.append(line);
		}
		
		return fileContent2rules(sb.toString());
	}
	
	
	/**
	 * Converts the text content of a Game Description File into GDL rules, where the content is given as one single String.
	 */
	public static List<Gdl> fileContent2rules(String fileContent){
		return Game.createEphemeralGame(Game.preprocessRulesheet(fileContent)).getRules();
	}
	

	
	/**
	 * Given the path to the folder in which a certain game is stored, it reads the METADATA file, and extracts from it the name of the file that contains the GDL description.
	 * @param pathToGameFolder
	 * @return
	 */
	public static String getGDLFileName(String pathToGameFolder){
		return getFilesFromGameFolder(pathToGameFolder).get("rulesheet");
	}
	
	
	/**
	 *  Returns a hashmap that maps each type of file to the file name.<br/>
	 * e.g.: <br/>
	 * "rulesheet" --> "ticTacToe.kif" <br/>
	 * "description" -->  "ticTacToe.txt" <br/>
	 * @param pathToGameFolder
	 * @return
	 */
	public static HashMap<String, String> getFilesFromGameFolder(String pathToGameFolder){
		File gameFolder = new File(pathToGameFolder);
		return getFilesFromGameFolder(gameFolder);
	}
	
	/**
	 * 
	 * Returns a hashmap that maps each type of file to the file name.<br/>
	 * e.g.: <br/>
	 * "rulesheet" --> "ticTacToe.kif" <br/>
	 * "description" -->  "ticTacToe.txt" <br/>
	 * 
	 * @param gameFolder A folder that contains the files for one game.
	 * @return
	 */
	public static HashMap<String, String> getFilesFromGameFolder(File gameFolder){
		File metaData = new File(gameFolder, "METADATA");
		return parseMetaData(metaData);
	}
	
	
	/**
	 * Returns a hashmap that maps each type of file to the file name.<br/>
	 * e.g.: <br/>
	 * "rulesheet" --> "ticTacToe.kif" <br/>
	 * "description" -->  "ticTacToe.txt" <br/>
	 * @param metaData
	 * @return
	 */
	public static HashMap<String, String> parseMetaData(File metaData){
		
		HashMap<String, String> fileNames = new HashMap<String, String>();
		
		
		ArrayList<String> lines = FileIO.file2Strings(metaData);
		
		//Some METADATA files are given as a single line, while others are nicely formatted over several lines.
		// Here, we will handle them as a single line, so if the file contains more than one line we first have to stick all
		// lines together.
		String stickedTogether = "";
		for(String line : lines){
			stickedTogether += line;
		}
		
		//Remove braces.
		stickedTogether = stickedTogether.replace("{", "");
		stickedTogether = stickedTogether.replace("}", "");
		
		//The file is formatted in attribute-value pairs
		//in the following form:   att1:val1, att2:val2, att3:val3
		String[] pairs = stickedTogether.split(",");
		
		for(String pair : pairs){
			//e.g. pair == "att1:val1"
			
			String[] pieces = pair.split(":");
			if(pieces.length < 2){
				continue;
			}
			
			//remove quotes from the strings and remove whitespaces.
			pieces[0] = pieces[0].replace("\"", "").trim();
			pieces[1] = pieces[1].replace("\"", "").trim();
			
			if(pieces[0].equals("description") || pieces[0].equals("rulesheet") || pieces[0].equals("stylesheet") || pieces[0].equals("user_interface")){
				fileNames.put(pieces[0], pieces[1]);
			}
			
		}
		
		return fileNames;
	}
}
