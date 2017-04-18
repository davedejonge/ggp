package ddejonge.ggp.tools.downloadGames;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.ggp.base.util.gdl.grammar.Gdl;

import ddejonge.ggp.tools.FileIO;
import ddejonge.ggp.tools.GameParser;
import ddejonge.ggp.tools.SystemInfo;

public class DownloadGames {
	
	static final String GGP_BASE = "http://games.ggp.org/base/games/";
	static final String GAMES_FOLDER = SystemInfo.JAVA_PROJECTS_FOLDER + "GGP\\ggp-base-master\\games\\games\\";
	
	static final String GAME_NAME = "queens08lg";
	
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
		 
		if(!toFolder.endsWith(File.separator)){
			toFolder += File.separator;
		}
		
		File gameFolder = new File(toFolder + gameName);
		gameFolder.mkdirs();
		
		//First download the METADATA file.
		File metaDataFile = downloadFile(gameFolder, "METADATA");
		
		//Parse the metaData file.
		Collection<String> fileNames = GameParser.parseMetaData(metaDataFile).values();
		
		
		File downloadedKifFile = null;
		
		for(String fileName : fileNames){
			
			File downloadedFile = downloadFile(gameFolder, fileName);
			
			if(fileName.endsWith(".kif")){
				downloadedKifFile = downloadedFile;
			}
			
			
		}
		
		System.out.println("DownloadGames.downloadGame() Download Finished at: " + gameFolder.getAbsolutePath());	

		

		
		
		return downloadedKifFile;
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
