package ddejonge.ggp.tools.visual;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class Console extends JFrame {

		static Random random = new Random();
	
		private JTextPane textPane;
		public PrintStream out;
		
		public Console(String title){
			
			//The contentPane has a JScrollPane, which has a JTextPane
			textPane = new JTextPane();
			JScrollPane scrollPane = new JScrollPane(textPane);						
			this.getContentPane().add(scrollPane);
		
			int rx =  random.nextInt(200);
			int ry =  random.nextInt(200);
			
			this.setBounds(200 + rx, 200 +ry , 500, 300);
			this.setVisible(true);
			this.setTitle(title);
			
			out = getPrintStream();
		}
		
		public void updateTextPane(final String text) {
			  SwingUtilities.invokeLater(new Runnable() {
			    public void run() {
			      Document doc = textPane.getDocument();
			      try {
		    	    doc.insertString(doc.getLength(), text, null);			        
			      } catch (BadLocationException e) {
			        throw new RuntimeException(e);
			      }
			      textPane.setCaretPosition(doc.getLength() - 1);
			    }
			  });
		}
		
		
		public PrintStream getPrintStream(){
			
			OutputStream out = new OutputStream() {
				@Override
				public void write(int b) throws IOException {
					updateTextPane(String.valueOf((char) b));
				}
		 
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					updateTextPane(new String(b, off, len));
				}
		 
				@Override
				public void write(byte[] b) throws IOException {
					write(b, 0, b.length);
				}
			};
			
			return new PrintStream(out, true);
			
			
		}
			 
		
	
}
