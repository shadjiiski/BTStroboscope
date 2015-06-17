/**
 * 
 */
package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.gui.pc;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.pc.HC06BTConnector;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class SimpleStrobeRemote extends JFrame
{
	private HC06BTConnector connector;
	
	public static final double FREQ_MIN = 1.0; //Hz
	public static final double FREQ_MAX = 100.0; //Hz
	public static final double FREQ_STEP = 0.1; //Hz
	public static final double FREQ_DEFAULT = 30.0; //Hz

	public static final int DUTY_MIN = 1; //%
	public static final int DUTY_MAX = 99; //%
	public static final int DUTY_STEP = 1; //%
	public static final int DUTY_DEFAULT = 5; //%
	
	private double frequency;
	private int duty;

	private JSlider freqSlider;
	private JSpinner freqSpinner;
	private JSlider dutySlider;
	private JSpinner dutySpinner;
	
	public SimpleStrobeRemote()
	{
		super("Simple Strobe BT Remote");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setResizable(false);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				if(connector != null)
					try
					{
						connector.disconnect(1000);
					} catch (IOException ex)
					{
						ex.printStackTrace();
					}
			}
		});
		getContentPane().add(createLayout());
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		
		frequency = FREQ_DEFAULT;
		duty = DUTY_DEFAULT;

		try
		{
			connector = new HC06BTConnector();
			connector.connect();
			sendToRemote();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
	
	private JPanel createLayout()
	{
		Insets insets = new Insets(4, 4, 4, 4);
		
		JPanel mainPanel = new JPanel(new GridBagLayout());
		
		JPanel freqPanel = new JPanel(new GridBagLayout());
		freqPanel.setBorder(BorderFactory.createTitledBorder("Frequency settings"));
		freqSlider = new JSlider((int)(10 * FREQ_MIN), (int) (10 * FREQ_MAX), (int) (10 * FREQ_DEFAULT));
		JButton freqUp = new JButton("+ 1.0");
		JButton freqDown = new JButton("- 1.0");
		JButton freqFineUp = new JButton("+ 0.1");
		JButton freqFineDown = new JButton("- 0.1");
		JButton freqDoubler = new JButton("x2");
		JButton freqDivider = new JButton("/2");
		final SpinnerNumberModel freqModel = new SpinnerNumberModel(FREQ_DEFAULT, FREQ_MIN, FREQ_MAX, FREQ_STEP);
		freqSpinner = new JSpinner(freqModel);
		
		freqSlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				setFrequency(freqSlider.getValue() / 10.0);
			}
		});
		freqDivider.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency / 2.0);
			}
		});
		freqDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency - 1);
			}
		});
		freqFineDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency - 0.1);
			}
		});
		freqFineUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency + 0.1);
			}
		});
		freqUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency + 1);
			}
		});
		freqDoubler.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency * 2);
			}
		});
		freqModel.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				setFrequency(freqModel.getNumber().doubleValue());
			}
		});
		
		freqPanel.add(freqDivider, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqDown, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqFineDown, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqSpinner, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		freqPanel.add(new JLabel("Hz"), new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));		
		freqPanel.add(freqFineUp, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqUp, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqDoubler, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqSlider, new GridBagConstraints(0, 1, 8, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		
		JPanel dutyPanel = new JPanel(new GridBagLayout());
		dutyPanel.setBorder(BorderFactory.createTitledBorder("Duty cycle settings"));
		dutySlider = new JSlider(DUTY_MIN, DUTY_MAX, DUTY_DEFAULT);
		JButton dutyUp = new JButton("+ 1.0");
		JButton dutyDown = new JButton("- 1.0");
		final SpinnerNumberModel dutyModel = new SpinnerNumberModel(DUTY_DEFAULT, DUTY_MIN, DUTY_MAX, DUTY_STEP);
		dutySpinner = new JSpinner(dutyModel);
		dutySlider.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				setDutyCycle(dutySlider.getValue());
			}
		});
		dutyDown.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDutyCycle(duty - 1);
			}
		});
		dutyUp.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDutyCycle(duty + 1);
			}
		});
		dutyModel.addChangeListener(new ChangeListener()
		{
			@Override
			public void stateChanged(ChangeEvent e)
			{
				setDutyCycle(dutyModel.getNumber().intValue());
			}
		});
		
		dutyPanel.add(dutyDown, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(dutySpinner, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		dutyPanel.add(new JLabel("%"), new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));		
		dutyPanel.add(dutyUp, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(dutySlider, new GridBagConstraints(0, 1, 4, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		
		
		mainPanel.add(freqPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		mainPanel.add(dutyPanel, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		return mainPanel;
	}
	
	private void setFrequency(double frequency)
	{
		if(frequency < FREQ_MIN || frequency > FREQ_MAX)
			return;
		this.frequency = frequency;
		freqSlider.setValue((int) (frequency * 10));
		((SpinnerNumberModel)freqSpinner.getModel()).setValue(frequency);
		sendToRemote();
	}
	
	private void setDutyCycle(int duty)
	{
		if(duty < DUTY_MIN || duty > DUTY_MAX)
			return;
		this.duty = duty;
		dutySlider.setValue(duty);
		((SpinnerNumberModel)dutySpinner.getModel()).setValue(duty);
		sendToRemote();
	}
	
	private void sendToRemote()
	{
		if(connector == null)
			return;
		connector.setStrobeParameters(frequency, duty);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		new SimpleStrobeRemote();
	}

}
