package ddejonge.ggp.tools.visual;

import java.awt.Dimension;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Monitor {
	
	//FIELDS FOR THE WINDOW
	private JFrame frame;
	private JSplitPane splitPane;
	JTextPane dataMonitorTextPane;
	JTextPane consoleTextPane;
	
	private boolean enabled = true;
	
	DecimalFormat formatter;
	
	//FIELDS FOR THE MEMORY MONITOR
	Runtime runtime = Runtime.getRuntime();;
	int memoryLine = -1; //the line on which the amount of used memory is printed. If this value is negative memory isn't printed at all.
	
	//FIELDS FOR THE DATA MONITOR.
	private HashMap<String, Object> properties = new HashMap<>();
	private String[] line2prop = new String[1]; //maps each line number to its corresponding property.
	private int numLines=0;
	private ArrayList<String> monitorStrings = new ArrayList<>();
	
	//FIELDS FOR THE CONSOLE
	ArrayList<String> logContent = new ArrayList<>(100);
	

	//FIELDS FOR LOGGING
 	String logFolderPath = "log" + File.separator + getDateString();		//e.g. ddjonge/Documents/NSPfiles/results/
	String logFileName = "log.txt";     	//e.g. myNewExperiment.log
	String dataFileName = "data.txt";
	
	
	
	//JUST FOR TESTING
	public static void main(String[] args) throws InterruptedException {
		
		Monitor logger = new Monitor("hello world");
		
		logger.setProperty("port");
		logger.setMemoryProperty();
		logger.setProperty("round");
		
		logger.setValue("port", 9147);
		
		for(int i=0; i<20; i++){
			logger.printlnConsole("test " + i);
			Thread.sleep(500);
			
			logger.setValue("round", i, true);
			
			logger.writeDataFile();
		}
		
		logger.writeLogFile();
		
	}
	
	public Monitor(String title, int x, int y, int width, int dataMonitorHeight, int consoleHeight){
		init(title, x, y, width, dataMonitorHeight, consoleHeight);
	}
	
	public Monitor(String title){
		init(title, 100, 100, 400, 150, 400);
	}
	
	private void init(String title, int x, int y, int width, int dataMonitorHeight, int consoleHeight){

		int totalHeight = dataMonitorHeight + consoleHeight;
		
		dataMonitorTextPane = new JTextPane();
		JScrollPane scrollPane1 = new JScrollPane(dataMonitorTextPane);
		
		consoleTextPane = new JTextPane();
		JScrollPane scrollPane2 = new JScrollPane(consoleTextPane);		
		
		//Create the splitpane
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scrollPane1, scrollPane2);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(dataMonitorHeight);

        //Provide minimum sizes for the two components in the split pane.
        Dimension minimumSize = new Dimension(100, 50);
        scrollPane1.setMinimumSize(minimumSize);
        scrollPane2.setMinimumSize(minimumSize);

        //Provide a preferred size for the split pane.
        splitPane.setPreferredSize(new Dimension(width, totalHeight));
		
        
        frame = new JFrame(title);
		frame.getContentPane().add(splitPane);
		
		frame.setLocation(x, y);
		
        //Display the window.
        frame.pack();
        frame.setVisible(true);
        
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        formatter = new DecimalFormat("###,###,###,###.##");
        
	}
	
	
	public void enable(boolean enable){
		this.enabled = enable;
		this.setVisible(enable);
	}
	
	public void setTitle(String title){
		this.frame.setTitle(title);
	}
	
	
	public void setConsoleFileName(String consoleFileName){
		this.logFileName = consoleFileName;
	}
	
	public void setDataFileName(String dataFileName){
		this.dataFileName = dataFileName;
	}
	
	public void setLocation(int x, int y){
		frame.setLocation(x, y);
	}
	
	public void setVisible(boolean visible){
		frame.setVisible(visible);
	}
	
	public void setLogFolderPath(String folderPath){
		this.logFolderPath = folderPath;
	}
	
	
	public void setMemoryProperty(){
		
		this.memoryLine = numLines++;
		
		setProperty(memoryLine, "Used memory");
	}
	
	/**
	 * Sets a property of which the value is updated with the amount of used memory.
	 * 
	 * @param lineNumber
	 */
	public void setMemoryProperty(int lineNumber){
		setProperty(lineNumber, "Used memory");
		memoryLine = lineNumber;
	}
	
	public void setProperty(String property){
		setProperty(numLines++,property);
	}
	
	
	public void setProperty(int lineNumber, String propertyName){
		
		if(lineNumber >= line2prop.length){
			resizeArray(lineNumber + 1);
		}
		
		if(lineNumber > numLines){
			numLines = lineNumber;
		}
		
		line2prop[lineNumber] = propertyName;
		properties.put(propertyName, "");
	}
	
	/**
	 * Displays the new value of the property in the window.
	 * 
	 * @param property the name of the property to display.
	 * @param value the value of the property to display.
	 */
	public void setValue(String property, Object value){
		setValue(property, value, true);
	}
	
	/**
	 * Sets the new value of the property in the window. <br/>
	 * 
	 * If update is set to 'true' or if not specified, then the new value will be displayed immediately.
	 * If update is set to 'false' the value will not be displayed in the window
	 * until the method update() is called, or until this method is called again with 'true', or without this parameter specified at all.
	 * 
	 * @param property the name of the property to display.
	 * @param value the value of the property to display.
	 * @param update the window will be updated with the new value immediately if set to 'true'. Otherwise, the new value will not be displayed in the window until the method update() is called. 
	 */
	public void setValue(String property, Object value, boolean update){
		
		if(!this.enabled){
			return;
		}
		
		if(properties.get(property) == null){
			
			throw new IllegalArgumentException("DataMonitor.setValue() Error! Unknown property: " + property);
			
		}else{
			
			//the property already existed, set the value only.
			properties.put(property, value);
		}
		
		if(update){
			updateMonitor();
		}
		
	}

	public void setValues(HashMap<String, Object> hashMap){
		
		if(!this.enabled){
			return;
		}
		
		for(String key : hashMap.keySet()){
			this.properties.put(key, hashMap.get(key));
		}
		
		updateMonitor();
	}
	
	/**
	 * Returns the number of MegaBytes of used memory.
	 * 
	 * @return
	 */
	public int getUsedMemoryMB(){
		return (int) ((runtime.totalMemory() - runtime.freeMemory()) / 1e6);
	}
	
	
	public void dispose(){
		this.frame.dispose();
	}
	
	
	/**
	 * Makes sure that the current values assigned to the properties are displayed. 
	 * 	
	 */
	public void updateMonitor(){
		
		if(!this.enabled){
			return;
		}
		
		if(memoryLine >= 0){
			String key = line2prop[memoryLine];
			properties.put(key, formatter.format(getUsedMemoryMB()) + " MB");
		}
		
		this.monitorStrings.clear(); //this stores the text as an array of strings (in order to write to file).
		String text = "";			//this stores the text as a single string (in order to write to text pane).
		
		for(int i=0; i<line2prop.length; i++){
			
			String  s = "";
			if(line2prop[i] != null){
				Object value = properties.get(line2prop[i]);
				
				String valueString;
				
				if(value instanceof Number){
					valueString = formatter.format(value);
				}else{
					valueString = value.toString();
				}
				
				s = line2prop[i] + " " + valueString;
			}
			
			s += System.lineSeparator();
			monitorStrings.add(s);
			
			text += s;
		}
		
		monitorStrings.add(System.lineSeparator());
		monitorStrings.add("************************" +  System.lineSeparator());
		monitorStrings.add(System.lineSeparator());
		
		updateDMTextPane(text);
	}
		
	public void removeProperty(String key){
		properties.remove(key);
	}
	
	public void clearProperties(){
		Arrays.fill(line2prop, null);
		properties.clear();
	}
	
	private void resizeArray(int newSize){
		
		String[] newContent = new String[newSize];
		
		for(int i=0; i<line2prop.length; i++){
			newContent[i] = line2prop[i];
		}
		
		line2prop = newContent;
	}
	
	
	
	
	
	
	
	//***CONSOLE METHODS

	
	/**
	 * Print line to both the console and the log file.
	 * @param line
	 */
	public void println(Object line){
		printlnConsole(line);
		printlnLogFile(line);
	}
	
	public void println(){
		printlnConsole();
		printlnLogFile();
	}
	
	public void print(Object line){
		printConsole(line);
		printLogFile(line);
	}
	
	/**
	 * Print line to console.
	 * @param line
	 */
	public void printlnConsole(Object line){
		updateConsoleTextPane(line.toString() + System.lineSeparator());
	}
	
	public void printlnConsole(){
		updateConsoleTextPane("" + System.lineSeparator());
	}
	
	public void printConsole(Object object){
		updateConsoleTextPane(object.toString());
	}
	
	/**
	 * Print line to log file.
	 * @param line
	 */
	public void printlnLogFile(Object line){
		logContent.add(line.toString() + System.lineSeparator());
	}
	
	public void printlnLogFile(){
		logContent.add("" + System.lineSeparator());
	}
	
	public void printLogFile(Object line){
		logContent.add(line.toString());
	}
	
	
	/*
	public void writeToConsole(String text){
		consolePrintStream.println(text);
		//updateConsoleTextPane(text + System.lineSeparator());
	}*/
	
	
	/**
	 * Returns a print stream that allows you to print to the console.
	 * This is useful to redirect the system output stream for example.
	 * @return
	 */
	public PrintStream getConsolePrintStream(){
		
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				printConsole(String.valueOf((char) b));
			}
	 
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				printConsole(new String(b, off, len));
			}
	 
			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
		
		return new PrintStream(out, true);
		
	}
	
	/**
	 * Returns a print stream that allows you to print to the log file.
	 * This is useful to redirect the system output stream for example.
	 * @return
	 */
	public PrintStream getLogFilePrintStream(){
		
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				printLogFile(String.valueOf((char) b));
			}
	 
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				printLogFile(new String(b, off, len));
			}
	 
			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
		
		return new PrintStream(out, true);
		
	}
	
	/**
	 * Returns a print stream that allows you to print to both the console and the log file.
	 * This is useful to redirect the system output stream for example.
	 * @return
	 */
	public PrintStream getPrintStream(){
		
		OutputStream out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				print(String.valueOf((char) b));
			}
	 
			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				print(new String(b, off, len));
			}
	 
			@Override
			public void write(byte[] b) throws IOException {
				write(b, 0, b.length);
			}
		};
		
		return new PrintStream(out, true);
		
	}
	
	 public static String getDateString(){
			//Get the current time to put in the filename of the log file
			Calendar now = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd__HH-mm-ss");
			return sdf.format(now.getTime());
	 }
	 
	 
	public String getLogFolderPath(){
		return this.logFolderPath;
	}
	 
	public void writeLogFile(){
		writeToFile(logFolderPath, this.logFileName, logContent);
		logContent.clear();
	}
	
	public void writeDataFile(){
		writeToFile(logFolderPath, this.dataFileName, monitorStrings);
	}
	 
	void writeToFile(String folderPath, String fileName, ArrayList<String> lines){
		
		if(!enabled){return;}
		
		//create the folder
		File folder = new File(folderPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}
		
		//create the file
		File file = new File(folder, fileName);
		if(!file.exists()){
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
		
		try (PrintWriter out = new PrintWriter(new FileWriter(file, true))){
			
	
			for(String s : lines){
				out.print(s);
			}	
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void updateDMTextPane(final String text) {
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      Document doc = dataMonitorTextPane.getDocument();
		      try {
		    	  
		    	  //remove all the content.
		    	  doc.remove(0, doc.getLength());
		    	  
		    	  //insert new content
		    	  doc.insertString(doc.getLength(), text, null);
		    	  
		      } catch (BadLocationException e) {
		        throw new RuntimeException(e);
		      }
		    }
		  });
	}
	
	
	public void clearConsole(){
		  SwingUtilities.invokeLater(new Runnable() {
			   public void run() {
			    	consoleTextPane.setText("");
			   }
		  });
	}
	
	private void updateConsoleTextPane(final String text) {
		
		  SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		      Document doc = consoleTextPane.getDocument();
		      try {
	    	    doc.insertString(doc.getLength(), text, null);			        
		      } catch (BadLocationException e) {
		        throw new RuntimeException(e);
		      }
		      consoleTextPane.setCaretPosition(doc.getLength() - 1);
		    }
		  });
	}
}
