package ddejonge.ggp.tools;
import java.io.File;




public class SystemInfo {

	
	public enum Location {UNKNOWN, DESKTOP, LAPTOP};
	
	public static Location currentLocation;
	
	public static String DROPBOX_FOLDER;
	public static String JAVA_PROJECTS_FOLDER;
	public static String NEGO_GAMES_FOLDER;
	public static String GAMES_FOLDER;
	public static String EXPERIMENTS_FOLDER; 
	
	public static String PATH_TO_GRINGO;
	
	static{
		
		String myUserFolder_desktop = "C:\\Users\\dave_\\";
		String myUserFolder_laptop = "C:\\Users\\Dave\\";
				
		
		if((new File(myUserFolder_desktop)).exists()){
			
			currentLocation = Location.DESKTOP;
			
			DROPBOX_FOLDER = myUserFolder_desktop + "Dropbox\\";
			JAVA_PROJECTS_FOLDER = DROPBOX_FOLDER + "java projects\\";
			NEGO_GAMES_FOLDER = JAVA_PROJECTS_FOLDER + "GNG\\GGP_Negotiations\\negoGames\\";
			GAMES_FOLDER = JAVA_PROJECTS_FOLDER + "GGP\\ggp-base-master\\games\\games\\";
			
			EXPERIMENTS_FOLDER = "C:\\Users\\dave_\\Experiments\\";
			
			PATH_TO_GRINGO = "C:\\Program Files\\clingo\\gringo.exe";
		
		}else if((new File(myUserFolder_laptop)).exists()){
			
			currentLocation = Location.LAPTOP;
			
			DROPBOX_FOLDER = myUserFolder_laptop + "Dropbox\\";
			JAVA_PROJECTS_FOLDER = DROPBOX_FOLDER + "java projects\\";
			NEGO_GAMES_FOLDER = JAVA_PROJECTS_FOLDER + "GNG\\GGP_Negotiations\\negoGames\\";
			GAMES_FOLDER = JAVA_PROJECTS_FOLDER + "GGP\\ggp-base-master\\games\\games\\";
			
			EXPERIMENTS_FOLDER = "C:\\Users\\Dave\\Experiments\\";
			
			PATH_TO_GRINGO = "";
		
		}else{
			
			
			System.out.println("ERROR: working on an unknown computer. Please make changes to the class SystemInfo." );
			
			
		}
		

	}
	
	
	
	
	
}
