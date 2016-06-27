import java.io.*;

import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;

import java.awt.event.*;
import java.awt.*;

import javax.swing.border.*;

import java.awt.image.*;

/** The class for the main window of the program. Most of the GUI components are
 * set up here. */
class PaintGUI extends JFrame implements ActionListener, ChangeListener {

	private final int iconSize= 48; // Size of icons for buttons.

	private final int drawRegionWidth= 700; // Width of drawing region.
	private final int drawRegionHeight= 520; // Height of drawing region.


	private final int defImgWidth= 640; // Default image width.
	private final int defImgHeight= 480; // Default image height.
	private final Color defImgBckColor = Color.WHITE; // Default background color.

	private int lastImgWidth = defImgWidth; // Width of last blank image created.
	private int lastImgHeight = defImgHeight; // Height of last blank image created.

	private DrawingCanvas canvas; // The drawing canvas.

	private JLabel sizeLabel;           // Label for dimensions of image.
	private JLabel mousePositionLabel;  // Label for position of mouse.
	private JLabel toolSizeLabel;     // Label for size of tool.
	private JLabel unsavedLabel;  // Label to inform user of unsaved changes.
	private final String unsavedMsg = "SAVE"; // Default message if unsaved changes. 

	private final int defToolSize= 1; // Default tool size.

	private JToggleButton pencil; // Pencil button.
	private JToggleButton eraser; // Eraser button.
	private JToggleButton colorPicker; // Color picker button.
	private JToggleButton airbrush; // Airbrush button.
	private JToggleButton line; // Line button.
	private JToggleButton circle; // Circle button.
	private JButton colorButton;  // Foreground color button.
	private JButton backColorButton; // Background color button.

	private JSlider toolSizeSlider; // Slider for choosing tool size
	private final int sliderMin= 0; // Minimum value for slider
	private final int sliderMax= 50; // Maximum value for slider
	private final int sliderInit= defToolSize-1; // Initial value for slider

	final String defTitle= "CS 2110 Paint"; // Default window title.

	File lastUsedFile;  // Last used file. */
	final String defFileName= "untitled.png"; // Default file name to save to.
	boolean imageUnsaved= false; // Whether the image has unsaved changes or not.

	/** Constructor:  the main window of the program. */
	public PaintGUI() {
		super("CS 2110 Paint");
		setLayout(new BorderLayout());

		JMenuBar menuBar= setUpMenuBar();

		// Canvas & scroller
		canvas= new DrawingCanvas(this,defImgWidth, defImgHeight, defImgBckColor, defToolSize-1);
		JScrollPane scroller= new JScrollPane(canvas);
		scroller.setPreferredSize(new Dimension(drawRegionWidth, drawRegionHeight));

		// Set up tool bar.
		JToolBar toolBar= setUpToolBar();

		// Tool Size slider
		toolSizeSlider= new JSlider(JSlider.VERTICAL, sliderMin, sliderMax, sliderInit);
		toolSizeSlider.addChangeListener(this);
		toolSizeSlider.setMinorTickSpacing(1);
		toolSizeSlider.setPaintTicks(true);
		toolSizeSlider.setSnapToTicks(true);

		// Status bar panel
		JPanel statusPanel= new JPanel();
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(getWidth(), 18));
		statusPanel.setLayout(new GridLayout(1, 4));

		mousePositionLabel= new JLabel("Position:");
		mousePositionLabel.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(mousePositionLabel);

		toolSizeLabel= new JLabel("Tool Size: " + canvas.getToolSize());
		statusPanel.add(toolSizeLabel);

		sizeLabel= new JLabel();
		updateSizeLabel();
		statusPanel.add(sizeLabel);

		unsavedLabel= new JLabel("");
		unsavedLabel.setForeground(Color.RED);
		statusPanel.add(unsavedLabel);

		// Add to window
		add(menuBar, BorderLayout.NORTH);
		add(toolSizeSlider, BorderLayout.WEST);
		add(statusPanel, BorderLayout.SOUTH);
		add(toolBar, BorderLayout.EAST);
		add(scroller, BorderLayout.CENTER);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}


	/** Update the color of the foreground color button. */
	public void updateColor() {
		ImageIcon icon= getIcon(canvas.getColor(),iconSize);
		colorButton.setIcon(icon);		
	}

	/** Update the color of the background color button. */
	public void updateBackColor() {
		ImageIcon icon= getIcon(canvas.getBackColor(),iconSize);
		backColorButton.setIcon(icon);		
	}

	/**  Call it to indicate that the image has been saved. */
	private void setImageSaved() {
		imageUnsaved= false;
		unsavedLabel.setText("");
	}

	/** Call it to indicate that the image has unsaved changes. */
	public void setImageUnsaved() {
		imageUnsaved= true;
		unsavedLabel.setText(unsavedMsg);
	}

	/** Update the label that displays the position of the mouse to position (x, y)
	 */
	public void setMousePosition(int x, int y) {
		mousePositionLabel.setText("Position: (" + x + "," + y + ")");
	}

	/** Process e, which should be a use of the Slider tool. */
	public void stateChanged(ChangeEvent e) {
		Object s = e.getSource();

		if (s == toolSizeSlider) {
			canvas.setToolSize(toolSizeSlider.getValue());
			toolSizeLabel.setText("Tool Size: " + canvas.getToolSize());
		} else {
			System.err.println("stateChanged: "+s);
		}
	}

	/** Update the image size (dimensions) label. */
	private void updateSizeLabel() {    	
		sizeLabel.setText("Image: " + canvas.getImg().getWidth() + " x " + 
				canvas.getImg().getHeight());
	}

	/** Called to process ... */
	private void newAction(ActionEvent e) {
		System.out.println("Action: New");

		NewImageDialog dialog= new NewImageDialog(this, true, lastImgWidth, lastImgHeight);
		Dimension d= dialog.getDimension();
		System.out.println("Dimension given in dialog: "+d);

		if (d != null) {
			canvas.newBlankImage(d.width, d.height, defImgBckColor);
			updateSizeLabel();

			lastUsedFile= null;
			setTitle(defTitle);
			setImageSaved();
		}
	}

	/** Process a click of menu item File -> Open --to open a file chosen by the user.
	 */
	private void openAction(ActionEvent e) {
		System.out.println("Action: Open");

		JFileChooser chooser= new JFileChooser(".");
		FileNameExtensionFilter filter= new FileNameExtensionFilter("Image Files", "jpeg", "jpg", "gif", "png", "bmp");
		chooser.setFileFilter(filter);
		int returnVal= chooser.showOpenDialog(this);
		File selectedFile= chooser.getSelectedFile();
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		System.out.println("You chose to open this file: " + selectedFile.getName());
		BufferedImage img = null;
		try {
			img= ImageIO.read(selectedFile);
		} catch (IOException exc) {
			System.out.println(exc.getMessage());
			return;
		}

		lastUsedFile = selectedFile;
		setTitle(defTitle + " - " + lastUsedFile.getName());
		setImageSaved();

		canvas.newImage(img);
		updateSizeLabel();
	}

	/** Save the image to file file.
	 * 
	 * @param file File to save to.
	 * @throws IOException
	 */
	private void saveImg(File file) throws IOException {
		String fileName= file.getName();
		int dotPosition= fileName.lastIndexOf(".");
		String format= fileName.substring(dotPosition+1);
		System.out.println("Saving in: " + fileName);
		System.out.println("Format: " + format);

		ImageIO.write(canvas.getImg(),format,file);
	}

	/** Process a click of menu item File -> Save to save the file. */
	private void saveAction(ActionEvent e) {
		System.out.println("Action: Save");

		if (lastUsedFile == null) {
			saveAsAction(e);
		} else {
			try {
				saveImg(lastUsedFile);
			}
			catch(IOException exc) {
				System.err.println(exc.getMessage());
			}
			setImageSaved();
		}
	}

	/** Process a click of menu item File -> SaveAs. */
	private void saveAsAction(ActionEvent e) {
		System.out.println("Action: Save As");	

		JFileChooser chooser= new JFileChooser();
		if (lastUsedFile != null)
			chooser.setSelectedFile(lastUsedFile);
		else {
			File currentDir= new File("");
			String currentDirPath = currentDir.getAbsolutePath();
			File defaultFile= new File(currentDirPath + "/" + defFileName);
			chooser.setSelectedFile(defaultFile);
		}

		FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files","jpeg","jpg","gif","png","bmp");
		chooser.setFileFilter(filter);
		int returnVal = chooser.showSaveDialog(this);
		File selectedFile = chooser.getSelectedFile(); 
		if (returnVal != JFileChooser.APPROVE_OPTION) return;

		System.out.println("You chose to save to the file: " + selectedFile.getName());
		try {
			saveImg(selectedFile);

			lastUsedFile = selectedFile;
			setTitle(defTitle + " - " + lastUsedFile.getName());
			setImageSaved();
		}
		catch(IOException exc) {
			System.err.println(exc.getMessage());
		}
	}

	/** Process a click of menu item File -> Quit. */
	private void quitAction(ActionEvent e) {
		System.out.println("Action: Quit");
		System.exit(0);
		//System.err.println("Implement me!");
	}

	/** Process a click of menu item Help. */
	private void helpAction(ActionEvent e) {
		System.out.println("Action: Help");

		JOptionPane.showMessageDialog(this,"help...","Help",JOptionPane.PLAIN_MESSAGE);
	}

	/** Process a click on menu item Help -> About. */
	private void aboutAction(ActionEvent e) {
		System.out.println("Action: About");	

		JOptionPane.showMessageDialog(this,"about...","About",JOptionPane.PLAIN_MESSAGE);
	}

	/** Process event e from the toolbar */
	public void actionPerformed(ActionEvent e) {
		System.out.println("actionPerformed");

		Object s = e.getSource();

		if (s == pencil) {
			canvas.setActiveTool(Tool.PENCIL);
		}
		else if (s == eraser) {
			canvas.setActiveTool(Tool.ERASER);
		}
		else if (s == colorPicker) {
			canvas.setActiveTool(Tool.COLOR_PICKER);
		}
		else if (s == airbrush) {
			canvas.setActiveTool(Tool.AIRBRUSH);
		}
		else if (s == line) {
			canvas.setActiveTool(Tool.LINE);
		}
		else if (s == circle) {
			canvas.setActiveTool(Tool.CIRCLE);
		}
		else if (s == colorButton) {
			Color newColor = JColorChooser.showDialog(this,"Foreground Color",canvas.getColor());
			if(newColor != null){
				canvas.setColor(newColor);
				updateColor();
			}
		}
		else if (s == backColorButton) {
			Color newBackColor = JColorChooser.showDialog(this,"Background Color",canvas.getBackColor());
			if(newBackColor != null){
				canvas.setBackColor(newBackColor);
				updateBackColor();
			}
		}
		else {
			System.err.println(s);
		}
	}


	/** Set up and return the menu bar. */
	private JMenuBar setUpMenuBar() {
		// Menu bar
		JMenuBar menuBar= new JMenuBar();

		// File menu
		JMenu fileMenu= new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		JMenuItem newItem= new JMenuItem("New");
		newItem.setMnemonic(KeyEvent.VK_N);
		newItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,ActionEvent.CTRL_MASK));
		newItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newAction(e);
			}
		});
		JMenuItem openItem= new JMenuItem("Open");
		openItem.setMnemonic(KeyEvent.VK_O);
		openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,ActionEvent.CTRL_MASK));
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				openAction(e);
			}
		});
		JMenuItem saveItem= new JMenuItem("Save");
		saveItem.setMnemonic(KeyEvent.VK_S);
		saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK));
		saveItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAction(e);
			}
		});
		JMenuItem saveAsItem= new JMenuItem("Save As");
		saveAsItem.setMnemonic(KeyEvent.VK_A);
		saveAsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,ActionEvent.CTRL_MASK | ActionEvent.SHIFT_MASK));
		saveAsItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveAsAction(e);
			}
		});
		JMenuItem quitItem= new JMenuItem("Quit");
		quitItem.setMnemonic(KeyEvent.VK_Q);
		quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,ActionEvent.CTRL_MASK));
		quitItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quitAction(e);
			}
		});
		fileMenu.add(newItem);
		fileMenu.add(openItem);
		fileMenu.add(saveItem);
		fileMenu.add(saveAsItem);
		fileMenu.add(new JSeparator());
		fileMenu.add(quitItem);

		// Help menu
		JMenu helpMenu= new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem helpItem= new JMenuItem("Help");
		helpItem.setMnemonic(KeyEvent.VK_H);
		helpItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H,ActionEvent.CTRL_MASK));
		helpItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				helpAction(e);
			}
		});
		JMenuItem aboutItem= new JMenuItem("About");
		aboutItem.setMnemonic(KeyEvent.VK_A);
		aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,ActionEvent.CTRL_MASK));
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				aboutAction(e);
			}
		});
		helpMenu.add(helpItem);
		helpMenu.add(new JSeparator());
		helpMenu.add(aboutItem);

		// Add to menu bar
		menuBar.add(fileMenu);
		menuBar.add(helpMenu);

		return menuBar;
	}

	/** Create a single-color icon of dimension size x size and color, meant
	 * to be used as an icon for the foreground/background color buttons.  */
	private static ImageIcon getIcon(Color c, int size) {
		BufferedImage myImg = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = (Graphics2D) myImg.getGraphics();
		g2d.setColor(c);
		g2d.fillRect(0, 0, size, size);
		return new ImageIcon(myImg);
	}

	/** Sets up and return the tool bar. */
	private JToolBar setUpToolBar() {
		// Toolbar
		ButtonGroup tools= new ButtonGroup();

		pencil= new JToggleButton(new ImageIcon("pencil.png"));
		pencil.setToolTipText("pencil");
		pencil.addActionListener(this);

		colorPicker= new JToggleButton(new ImageIcon("picker.png"));
		colorPicker.setToolTipText("color picker");
		colorPicker.addActionListener(this);

		eraser= new JToggleButton(new ImageIcon("eraser.png"));
		eraser.setToolTipText("eraser");
		eraser.addActionListener(this);

		airbrush= new JToggleButton(new ImageIcon("airbrush.png"));
		airbrush.setToolTipText("airbrush");
		airbrush.addActionListener(this);

		line= new JToggleButton(new ImageIcon("line.png"));
		line.setToolTipText("line");
		line.addActionListener(this);

		circle= new JToggleButton(new ImageIcon("circle.png"));
		circle.setToolTipText("circle");
		circle.addActionListener(this);

		tools.add(pencil);
		tools.add(colorPicker);
		tools.add(eraser);
		tools.add(airbrush);
		tools.add(line);
		tools.add(circle);

		// Foreground color chooser
		ImageIcon icon= getIcon(canvas.getColor(),iconSize);
		colorButton= new JButton(icon);
		colorButton= new JButton("F. Color");		
		colorButton.setToolTipText("foreground color");
		colorButton.addActionListener(this);

		// Background color chooser
		ImageIcon backIcon= getIcon(canvas.getBackColor(),iconSize);
		backColorButton= new JButton(backIcon);
		backColorButton= new JButton("B. Color");
		backColorButton.setToolTipText("background color");
		backColorButton.addActionListener(this);

		JToolBar toolBar= new JToolBar(JToolBar.VERTICAL);
		toolBar.setFloatable(false);
		toolBar.setRollover(true);
		toolBar.add(pencil);
		toolBar.add(colorPicker);
		toolBar.add(eraser);
		toolBar.add(airbrush);
		toolBar.add(line);
		toolBar.add(circle);
		toolBar.add(colorButton);
		toolBar.add(backColorButton);

		return toolBar;
	}

	/** Start the GUI. */
	public static void main(String[] args) {
		PaintGUI mainWindow = new PaintGUI();
		mainWindow.canvas.revalidate();

		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
