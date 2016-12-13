package ddejonge.ggp.tools.downloadGames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import ddejonge.ggp.tools.FileIO;

public class DownloadGames {
	
	static final String GGP_BASE = "http://games.ggp.org/base/games/";
	//static final String GAMES_FOLDER = "C:\\Users\\30044279\\Dropbox\\java projects\\GGP_Negotiations\\negoGames\\";
	static final String GAMES_FOLDER = "C:\\Users\\30044279\\Dropbox\\java projects\\ggp-base-master\\games\\games\\";
	
	static final String GAME_NAME = "AIPS Rover";
	
	public static void main(String[] args) {
		downloadGame(GAME_NAME);
	}
	
	
	static String[] extensions = new String[]{".kif", ".js", ".txt", ".xsl", ""};
	
		
	public static File downloadGame(String gameName){
		return downloadGame(gameName, GAMES_FOLDER);
	}
	
	/**
	 * Downloads the description of a game together with auxiliary files to the given path. 
	 * @param gameName
	 * @param toFolder The folder where to download the game to. This function Will create a new folder inside this folder and will store the downloaded files there.
	 * @return
	 */
	public static File downloadGame(String gameName, String toFolder){
		 
		
		File gameFolder = new File(GAMES_FOLDER + gameName);
		gameFolder.mkdirs();
		
		//First download the METADATA file.
		File metaDataFile = downloadFile(gameFolder, "METADATA");
		
		//Parse the metaData file.
		ArrayList<String> fileNames = parseMetaData(metaDataFile);
		
		
		File downloadedKifFile = null;
		
		for(String fileName : fileNames){
			
			File downloadedFile = downloadFile(gameFolder, fileName);
			
			if(fileName.endsWith(".kif")){
				downloadedKifFile = downloadedFile;
			}
			
			
		}
		
		
		/*
		for(String ext : extensions){
			
			String fileName;
			if(ext.equals("")){
				fileName = "METADATA";
			}else{
				fileName = GAME_NAME;
			}
		}
		*/

		System.out.println("DownloadGames.downloadGame() Download Finished.");	

		

		
		
		return downloadedKifFile;
	}
	
	
	public static ArrayList<String> parseMetaData(File metaData){
		
		ArrayList<String> fileNames = new ArrayList<String>();
		
		
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
				fileNames.add(pieces[1]);
			}
			
		}
		
		return fileNames;
	}
	
	static File downloadFile(File gameFolder, String fileName){
		
		//1. create the URL object.
		URL url;
		try {
			url = new URL(GGP_BASE + GAME_NAME + "/" + fileName);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			
			return null;
		}
		
		
		//Create a File object for the location where the downloaded file is going to be stored.
		File fileToDownload = new File(gameFolder, fileName);
		
		/*
		if(ext.equals(".kif")){
			downloadedKifFile = fileToDownload;
		}*/
		
		
		//Check if the file doesn't already exists.
		if(fileToDownload.exists()){
			
			System.out.println("DownloadGames.downloadGame() File already exists! " + fileName);
			
		}else{
		
			//If not, then download it.
			try {
				
				FileUtils.copyURLToFile(url, fileToDownload);
				System.out.println("DownloadGames.downloadGame() Downloaded: " + fileName);	
				
				
			} catch (FileNotFoundException e) {
				
				System.out.println("DownloadGames.downloadGame() no file found with name " + fileName);
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
		}
		
		return fileToDownload;
	}
	
}
