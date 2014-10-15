package demo.language;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 * Uses reflection to show information about a class name you supply.
 */
public class Reflection implements ActionListener {
	private JTextField textField;
	private JTextArea messageArea;

	public static void main(String[] args) {
		//Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	new Reflection();
            }
        });
	}
	public Reflection() {
		JFrame frame = new JFrame("Reflection");
		JLabel textLabel = new JLabel("Class name:");
		textField = new JTextField(20);
		messageArea = new JTextArea(10, 20);
		JButton goButton = new JButton("Go");
		JButton clearButton = new JButton("Clear");
		Container container = frame.getContentPane();
		JPanel controlPanel = new JPanel();
		container.add(controlPanel, BorderLayout.NORTH);
		container.add(messageArea, BorderLayout.CENTER);
		controlPanel.add(textLabel);
		controlPanel.add(textField);
		controlPanel.add(goButton);
		controlPanel.add(clearButton);
		goButton.addActionListener(this); // action is click on button...
		textField.addActionListener(this); // ... or press Enter
		clearButton.addActionListener(this);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true); // start event handling thread
	}
	@Override
	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		if (action.equals("Clear")) {
			textField.setText("");
			messageArea.setText("");
		} else {
			messageArea.setText("");
			String className = textField.getText();
			Class<?> clazz;
			try {
				clazz = Class.forName(className);
				Method[] methods = clazz.getMethods();
				for (Method method: methods) {
					messageArea.append(method.getName() + "(");
					Class<?>[] params = method.getParameterTypes();
					for (int i = 0; i < params.length; i++) {
						Class<?> parType = params[i];
						messageArea.append(parType.getName());
						if ((i+1) < params.length) {
							messageArea.append(", ");
						}
					}
					messageArea.append(");\n");
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}

/*

import java.lang.reflect.Method;

public class Reflection {

}
*/
