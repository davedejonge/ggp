package ddejonge.ggp.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class FileIO {

	//////////////////////////////////////////////////
	//Functions to write to files	
	//////////////////////////////////////////////////
	public static void appendToFile(File file, String text){
		
		try {
			
			PrintWriter out = new PrintWriter(new FileWriter(file, true));
			out.println(text);
			out.flush();
			out.close();
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
	
	public static void appendToFile(File file, List<String> text){
		
		try {
			
			PrintWriter out = new PrintWriter(new FileWriter(file, true));
			for(String line : text){
				out.println(line);
			}
			out.flush();
			out.close();
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
	
	
	public static void strings2file(File file, List<String> text){
		
		try {
			
			PrintWriter out = new PrintWriter(new FileWriter(file, false));
			for(String line : text){
				out.println(line);
			}
			out.flush();
			out.close();
			
		} catch (IOException e) {

			e.printStackTrace();
		}
		
	}
	
	
	public static File createFolder(String parentFolderPath, String newFolderName){
		
		File folder = new File(parentFolderPath , newFolderName);
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		return folder;		
	}
	
	public static File createFolder(File parentFolder, String newFolderName){
		
		
		File folder = new File(parentFolder, newFolderName);
		if (!folder.exists()) {
			folder.mkdir();
		}
		
		return folder;		
	}
	
	public static File createFile(File parentFolder, String fileName){
		
		File file = new File(parentFolder, fileName);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return file;		
	}
	
	 public static String getDateString(){
			//Get the current time to put in the filename of the log file
			Calendar now = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss");
			return sdf.format(now.getTime());
	 }
	 
	
	//////////////////////////////////////////////////
	//Functions to read files	
	//////////////////////////////////////////////////
		public static ArrayList<String> file2Strings(String pathTofile){
			
			File inputFile = new File(pathTofile);
			return file2Strings(inputFile);	
			
		}
		
		public static ArrayList<String> file2Strings(File inputFile){
			
			ArrayList<String> lines = null;
			
			try {
				BufferedReader br = new BufferedReader(new FileReader(inputFile));
				
				String line;
				lines = new ArrayList<>();
				while ((line = br.readLine()) != null) {
					lines.add(line);
				}
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			

			
			return lines;
			
		}
	
	
	

}
