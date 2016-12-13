package ddejonge.ggp.tools;

import java.util.List;

import org.ggp.base.util.game.Game;
import org.ggp.base.util.gdl.grammar.Gdl;

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
	
	public static List<Gdl> file2rules(String path){
		Path p1 = Paths.get(path);
		
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
	
	public static List<Gdl> fileContent2rules(String fileContent){
		return Game.createEphemeralGame(Game.preprocessRulesheet(fileContent)).getRules();
	}

}
