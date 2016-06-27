import javax.swing.*; 
import java.awt.event.*;
import java.awt.*;

/**  DO NOT MODIFY THIS CLASS IN ANY WAY.
 * 
 * An instance is a dialog window for new image. */
public class NewImageDialog extends JDialog implements ActionListener, FocusListener {
	private int defWidth, defHeight;
	private JButton createButton;
	private JButton cancelButton;
	private JTextField widthField;
	private JTextField heightField;
	private Dimension d;
	
	/** Return the dimensions of the image. */
    public Dimension getDimension() {
    	return d;
    }
    
    public void focusGained(FocusEvent e) {
    	Object s = e.getSource();
    	if (s == widthField) {
    		widthField.selectAll();
    	}
    	else if (s == heightField) {
    		heightField.selectAll();
    	}
    	else {
    		System.err.println("focusGained: " + s);
    	}
    }
    
    public void focusLost(FocusEvent e) {
    	Object s = e.getSource();
    	if (s == widthField) {
    		checkAndFixWidth();
    	}
    	else if (s == heightField) {
    		checkAndFixHeight();
    	}
    	else {
    		System.err.println("focusLost: " + s);
    	}
    }
    
    public NewImageDialog(JFrame frame, boolean modal, int width, int height) {
        super(frame,modal);
        setTitle("Create New Image");
        
        defWidth= width;
        defHeight= height;
        
        JPanel myPanel= new JPanel();
        getContentPane().add(myPanel);
        
        myPanel.add(new JLabel("Width"));
        widthField= new JTextField(""+width,5);
        widthField.addFocusListener(this);
        myPanel.add(widthField);
        
        myPanel.add(new JLabel("Height"));
        heightField= new JTextField(""+height,5);
        heightField.addFocusListener(this);
        myPanel.add(heightField);
        
        createButton= new JButton("Create");
        createButton.addActionListener(this);
        myPanel.add(createButton);
        
        cancelButton= new JButton("Cancel");
        cancelButton.addActionListener(this);
        myPanel.add(cancelButton);
        
        // set default button
        getRootPane().setDefaultButton(createButton);
        createButton.requestFocus();
        
        pack();
        setLocationRelativeTo(frame);
        setVisible(true);
    }

    private int checkAndFixWidth() {
        int width = -1;
		try {
			width= Integer.parseInt(widthField.getText());
		}
		catch (NumberFormatException exc) {
			
		}
		if (width <= 0) {
			widthField.setText(""+defWidth);
			return -1;
		}
		
		return width;
    }
    
    private int checkAndFixHeight() {
        int height= -1;
		try {
			height= Integer.parseInt(heightField.getText());
		}
		catch (NumberFormatException exc) {
			
		}
		if (height <= 0) {
			heightField.setText(""+defHeight);
			return -1;
		}
		
		return height;
    }
    
    public void actionPerformed(ActionEvent e) {
    	System.out.println("actionPerformed");
    	Object s= e.getSource();
    	
        if (s == createButton) {
            System.out.println("User chose 'create'.");
            
            int width= checkAndFixWidth();
            if (width == -1) return;
            int height= checkAndFixHeight();
            if (height == -1) return;
            
            d= new Dimension(width, height);
            setVisible(false);
        }
        else if (s == cancelButton) {
            System.out.println("User chose 'cancel'.");
            d = null;
            setVisible(false);
        }
        else {
        	System.err.println(s);
        }
    }
    
}
