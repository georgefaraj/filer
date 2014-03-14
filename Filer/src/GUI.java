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
import javax.swing.UIManager;
import javax.swing.border.BevelBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

public class GUI implements ActionListener, KeyListener
{
	JFrame frame;
	JMenuItem newItem, openItem, saveItem, saveAsItem, undoItem, redoItem, aboutItem, syncItem, exitItem;
	JTextPane textPane;
	JFileChooser fileChooser;
	File openedFile;
	JPanel statusBar;
	JLabel statusLabel;
	boolean isSaved = true;

	public GUI()
	{
		frame = new JFrame();

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
		statusBar.setBorder(new BevelBorder(BevelBorder.RAISED));
		statusBar.setPreferredSize(new Dimension(frame.getWidth(), 25));
		statusBar.setLayout((new BoxLayout(statusBar, BoxLayout.X_AXIS)));
		statusLabel = new JLabel("Idle");
		statusBar.add(statusLabel);
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
		openItem = new JMenuItem("Open");
		openItem.addActionListener(this);
		saveItem = new JMenuItem("Save");
		saveItem.addActionListener(this);
		saveAsItem = new JMenuItem("Save As");
		saveAsItem.addActionListener(this);
		exitItem = new JMenuItem("Exit");
		exitItem.addActionListener(this);
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(exitItem);

		// editMenu
		JMenu editMenu = new JMenu("Edit");
		undoItem = new JMenuItem("Undo");
		redoItem = new JMenuItem("Redo");
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
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT files", "txt");
		fileChooser.setFileFilter(filter);

		frame.setTitle("FileSharer");
		frame.setSize(800, 600);
		frame.setLocationRelativeTo(null);
		frame.setJMenuBar(menuBar);
		frame.setDefaultCloseOperation(frame.DO_NOTHING_ON_CLOSE);
		frame.setVisible(true);

	}

	// returns true if you should continue (closing or making a new document), false otherwise
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
			statusLabel.setText("Opening file");

			int returnVal = fileChooser.showOpenDialog(fileChooser);
			openedFile = fileChooser.getSelectedFile();
			if (returnVal == fileChooser.APPROVE_OPTION)
			{
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

	}

	private boolean saveFile()
	{
		statusLabel.setText("Saving file");

		if (openedFile == null) return saveFileAs();
		else
		{
			writeFile(openedFile);
			return true;
		}
	}

	private void newFile()
	{
		boolean temp = saveCheck();
		if (temp)
		{
			System.out.println("Is saved?:" + isSaved);
			System.out.println("Save check: " + temp);
			textPane.setText("");
			statusLabel.setText("New file created");
		}
	}

	// returns true if the file was saved, or if the user doesn't want to save. Should probably be refactored..
	private boolean saveFileAs()
	{
		statusLabel.setText("Saving file");

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
			statusLabel.setText(file.getName() + " Opened sucessfully");
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
			statusLabel.setText("Saving file");
			PrintWriter outputStream = new PrintWriter(new FileWriter(file));
			outputStream.print(textPane.getText());
			outputStream.close();
			statusLabel.setText(file.getName() + " Saved sucessfully");
			isSaved = true;
		}
		catch (Exception ex)
		{
		}
	}

	private void checkShortcut(KeyEvent e)
	{

		if (e.getKeyCode() == KeyEvent.VK_N)
		{
			if (e.getModifiers() == KeyEvent.CTRL_DOWN_MASK)
			{
				newFile();
			}
		}
	}

	public void keyPressed(KeyEvent e)
	{
		checkShortcut(e);
	}

	public void keyReleased(KeyEvent e)
	{
		isSaved = false;
	}

	public void keyTyped(KeyEvent e)
	{

	}
}
