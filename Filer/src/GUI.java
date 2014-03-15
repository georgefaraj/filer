import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI implements ActionListener, KeyListener
{
	JFrame frame, aboutFrame;
	JMenuItem newItem, openItem, saveItem, saveAsItem, undoItem, redoItem, aboutItem, syncItem, exitItem;
	JTextPane textPane;
	JFileChooser fileChooser;
	File openedFile;
	JPanel statusBar;
	String status = "Idle", oldStatus = "";
	JLabel statusLabel, wordCountLabel;
	boolean isSaved = true, timerRunning;
	Timer timer;

	public void aboutFrame()
	{
		if (aboutFrame == null)
		{
			aboutFrame = new JFrame("About Filer");
			aboutFrame.setLocationRelativeTo(frame);
			aboutFrame.setSize(300, 200);
			aboutFrame.setResizable(false);
			aboutFrame.setVisible(true);
		}
		else aboutFrame.toFront();
	}

	public GUI()
	{
		frame = new JFrame();
		timer = new Timer();

		try
		{
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		// statusBar
		statusBar = new JPanel();
		statusBar.setLayout(new BoxLayout(statusBar, BoxLayout.LINE_AXIS));
		statusBar.setPreferredSize(new Dimension(frame.getWidth(), 20));
		statusLabel = new JLabel("Status: Idle");
		wordCountLabel = new JLabel("Words: 0");
		statusBar.add(Box.createHorizontalStrut(10));
		statusBar.add(statusLabel);
		statusBar.add(Box.createHorizontalStrut(10));
		statusBar.add(new JLabel("|"));
		statusBar.add(Box.createHorizontalStrut(10));
		statusBar.add(wordCountLabel);

		frame.add(statusBar, BorderLayout.SOUTH);

		// textPane
		textPane = new JTextPane();
		textPane.addKeyListener(this);
		Font font = new Font("Arial", 10, 16);
		textPane.setFont(font);
		JScrollPane scrollPane = new JScrollPane(textPane);
		frame.add(scrollPane);

		JMenuBar menuBar = new JMenuBar();

		// fileMenu
		JMenu fileMenu = new JMenu("File");

		newItem = new JMenuItem("New");
		newItem.addActionListener(this);
		newItem.setAccelerator(KeyStroke.getKeyStroke('N', KeyEvent.CTRL_DOWN_MASK));

		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
		openItem.setAccelerator(KeyStroke.getKeyStroke('O', KeyEvent.CTRL_DOWN_MASK));

		saveItem = new JMenuItem("Save");
		saveItem.addActionListener(this);
		saveItem.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK));

		saveAsItem = new JMenuItem("Save As");
		saveAsItem.addActionListener(this);
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke('S', KeyEvent.CTRL_DOWN_MASK + KeyEvent.SHIFT_DOWN_MASK));

		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		exitItem.setAccelerator(KeyStroke.getKeyStroke('E', KeyEvent.CTRL_DOWN_MASK));

		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.addSeparator();
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.addSeparator();
		fileMenu.add(exitItem);

		// editMenu
		JMenu editMenu = new JMenu("Edit");

		undoItem = new JMenuItem("Undo");
		undoItem.setAccelerator(KeyStroke.getKeyStroke('Z', KeyEvent.CTRL_DOWN_MASK));

		redoItem = new JMenuItem("Redo");
		redoItem.setAccelerator(KeyStroke.getKeyStroke('Y', KeyEvent.CTRL_DOWN_MASK));

		editMenu.add(undoItem);
		editMenu.add(redoItem);

		// networkMenu
		JMenu networkMenu = new JMenu("Network");
		syncItem = new JMenuItem("Sync");
		networkMenu.add(syncItem);

		// helpMenu
		JMenu helpMenu = new JMenu("Help");
		aboutItem = new JMenuItem("About");
		aboutItem.addActionListener(this);
		helpMenu.add(aboutItem);

		// add menus to menuBar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(networkMenu);
		menuBar.add(helpMenu);

		frame.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent we)
			{
				if (saveCheck()) System.exit(0);
			}
		});

		fileChooser = new JFileChooser();
		FileNameExtensionFilter txtFilter = new FileNameExtensionFilter("TXT files", "txt");
		FileNameExtensionFilter javaFilter = new FileNameExtensionFilter("JAVA files", "java");
		fileChooser.addChoosableFileFilter(txtFilter);
		fileChooser.addChoosableFileFilter(javaFilter);

		frame.setTitle("Filer - Untitled.txt");
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);

	}

	private void updateStatus(String s)
	{
		statusLabel.setText("Status: " + s);
	}

	public class Task extends TimerTask
	{
		public void run()
		{
			updateStatus("Idle");
		}

	}

	private void startTimer()
	{
		timer.scheduleAtFixedRate(new Task(), 0, 10_000);
		timerRunning = true;
	}

	// returns true if you should continue (closing or making a new document), false if the user wants to cancel the action
	public boolean saveCheck()
	{
		boolean isDone = true;

		if (!isSaved)
		{
			int choiceVal = JOptionPane.showConfirmDialog(frame, "Would you like to save?");
			if (choiceVal == JOptionPane.YES_OPTION)
			{
				if (!saveFile()) isDone = false;
			}
			else if (choiceVal == JOptionPane.CANCEL_OPTION) isDone = false;
		}
		return isDone;
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == openItem)
		{
			updateStatus("Opening file");
			int returnVal = fileChooser.showOpenDialog(fileChooser);

			if (returnVal == fileChooser.APPROVE_OPTION)
			{
				openedFile = fileChooser.getSelectedFile();

				try
				{
					readFile(openedFile);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		}
		else if (e.getSource() == saveAsItem)
		{
			saveFileAs();
		}
		else if (e.getSource() == saveItem)
		{
			saveFile();
		}
		else if (e.getSource() == newItem)
		{
			newFile();
		}
		else if (e.getSource() == exitItem)
		{
			if (saveCheck()) System.exit(0);
		}
		else if (e.getSource() == aboutItem)
		{
			aboutFrame();
		}

	}

	private void newFile()
	{
		if (saveCheck())
		{
			textPane.setText("");
			updateStatus("New File Created");
		}
	}

	private boolean saveFile()
	{
		updateStatus("Saving File");

		if (openedFile == null) return saveFileAs();
		else
		{
			writeFile(openedFile);
			return true;
		}
	}

	// returns true if the file was saved, or if the user doesn't want to save. Should probably be refactored..
	private boolean saveFileAs()
	{
		updateStatus("Saving File");

		int returnVal = fileChooser.showSaveDialog(fileChooser);
		if (returnVal == fileChooser.APPROVE_OPTION)
		{
			writeFile(fileChooser.getSelectedFile());
			return true;
		}
		else if (returnVal == fileChooser.CANCEL_OPTION) return false;
		return true;

	}

	private void readFile(File file) throws Exception
	{
		String contents = "";
		BufferedReader inputStream = null;

		try
		{
			inputStream = new BufferedReader(new FileReader(file));

			String l;
			while ((l = inputStream.readLine()) != null)
			{
				contents += (l + "\r\n"); // \n required for newlines, \r (carriage return) required for notepad
			}
			textPane.setText(contents);
			updateStatus(file.getName() + " opened sucessfully");
			frame.setTitle("Filer - " + file.getName());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (inputStream != null) inputStream.close();
		}
	}

	private void writeFile(File file)
	{
		try
		{
			Pattern p = Pattern.compile("[^.]*");
			Matcher m = p.matcher(file.getPath());
			m.find();

			String extension = "";
			String descrip = fileChooser.getFileFilter().getDescription().substring(0, 3);
			if (descrip.equals("TXT")) extension = ".txt";
			else if (descrip.equals("JAV")) extension = ".java";

			file = new File(m.group(0) + extension);

			PrintWriter outputStream = new PrintWriter(new FileWriter(file));
			outputStream.print(textPane.getText());
			outputStream.close();
			updateStatus(file.getName() + " Saved sucessfully");
			frame.setTitle("Filer - " + file.getName());
			isSaved = true;
		}
		catch (Exception ex)
		{

		}
	}

	private void updateWordLabel()
	{
		wordCountLabel.setText("Words: " + findWordCount());
	}

	private int findWordCount()
	{
		return textPane.getText().split("\\s+").length;
	}

	public void keyPressed(KeyEvent e)
	{
		updateStatus("Typing");
		if (e.getKeyCode() == KeyEvent.VK_SPACE)
		{
			updateWordLabel();
		}
	}

	public void keyReleased(KeyEvent e)
	{
		isSaved = false;
		startTimer();
	}

	public void keyTyped(KeyEvent e)
	{

	}
}
