/**
 * 
 */
package bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.gui.pc;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

import javax.swing.*;
import javax.swing.plaf.FontUIResource;

import bg.uni_sofia.phys.fttme.shadjiiski.btstroberemote.connection.pc.HC06BTConnector;

/**
 * @author Stanislav Hadjiiski
 *
 */
public class FullscreenStrobeRemote extends JFrame
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
	
	private static final Font bigMessagesFont = new Font(null, Font.ITALIC, 200);
	private static final Font hugeFont = new Font(Font.MONOSPACED, Font.PLAIN, 250);
	private static final Font buttonFont = new Font(Font.MONOSPACED, Font.PLAIN, 25);

	private double frequency;
	private int duty;

	private JLabel freqLabel;
	private JSlider freqSlider;
	private JLabel dutyLabel;
	private JSlider dutySlider;

	public FullscreenStrobeRemote()
	{
		super("BT Strobe remote");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				super.windowClosing(e);
				JLabel exiting = new JLabel("Disconnecting...");
				exiting.setFont(bigMessagesFont);
				Container content = getContentPane();
				content.removeAll();
				content.add(exiting);
				pack();
				setLocationRelativeTo(null);
				content.validate();
				
				if(connector != null)
				{
					SwingUtilities.invokeLater(new Runnable()
					{
						@Override
						public void run()
						{
							try
							{
								connector.disconnect(1000);
								dispose();
							} catch (IOException ex)
							{
								ex.printStackTrace();
							}
						}
					});
				}
			}
		});
		
		frequency = FREQ_DEFAULT;
		duty = DUTY_DEFAULT;

		Container content = getContentPane();
		JLabel startup = new JLabel("Connecting...");
		startup.setFont(bigMessagesFont);
		content.add(startup);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
		try
		{
			connector = new HC06BTConnector();
			connector.connect();
			sendToRemote();
		} catch (IOException ex)
		{
			ex.printStackTrace();
		}
		content.removeAll();
		content.add(createLayout());
		pack();
		setLocationRelativeTo(null);
		setExtendedState(MAXIMIZED_BOTH);
		validate();
	}
	
	private JPanel createLayout()
	{
		Insets insets = new Insets(4, 4, 4, 4);
		UIManager.put("Button.font", new FontUIResource(buttonFont));
		UIManager.put("Label.font", new FontUIResource(buttonFont));
		Dimension btnSize = new FrequencySetButton(null).getPreferredSize();
		
		JPanel mainPanel = new JPanel(new GridBagLayout());

		JPanel freqPanel = new JPanel(new GridBagLayout());
		freqPanel.setBorder(BorderFactory.createTitledBorder("Frequency settings"));
		freqLabel = new JLabel(String.format((FREQ_DEFAULT < 100 ? " " : "") + "%.1f Hz", FREQ_DEFAULT));
		freqLabel.setFont(hugeFont);
		freqSlider = new JSlider((int)(10 * FREQ_MIN), (int)(10 * FREQ_MAX), (int) (10 * FREQ_DEFAULT));
		FrequencySpinner[] freqSpinners = new FrequencySpinner[]{
			new FrequencySpinner(20), new FrequencySpinner(50), new FrequencySpinner(100)	
		};
		FrequencySetButton[] freqSetBtns = new FrequencySetButton[]{
			new FrequencySetButton(freqSpinners[0]), new FrequencySetButton(freqSpinners[1]), new FrequencySetButton(freqSpinners[2])	
		};
		
		ActionListener freqPlus = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency + 0.1);
			}
		};
		ActionListener freqMin = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency - 0.1);
			}
		};
		ActionListener freqPlusPlus = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency + 1.0);
			}
		};
		ActionListener freqMinMin = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency - 1.0);
			}
		};
		ActionListener freqMult = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency * 2);
			}
		};
		ActionListener freqDiv = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setFrequency(frequency / 2);
			}
		};

		freqPanel.add(freqLabel, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));

		freqPanel.add(freqSlider, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		freqPanel.add(new CustomButton("-", btnSize, freqMin), new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new CustomButton("+", btnSize, freqPlus), new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));

		freqPanel.add(new JPanel(), new GridBagConstraints(0, 2, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		freqPanel.add(new CustomButton("--", btnSize, freqMinMin), new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new CustomButton("++", btnSize, freqPlusPlus), new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));

		freqPanel.add(new JPanel(), new GridBagConstraints(0, 3, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		freqPanel.add(new CustomButton("/2", btnSize, freqDiv), new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new CustomButton("x2", btnSize, freqMult), new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));

		freqPanel.add(freqSpinners[0], new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new JLabel("Hz"), new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqSetBtns[0], new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new JPanel(), new GridBagConstraints(3, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		freqPanel.add(freqSpinners[1], new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new JLabel("Hz"), new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqSetBtns[1], new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new JPanel(), new GridBagConstraints(3, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		freqPanel.add(freqSpinners[2], new GridBagConstraints(0, 6, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new JLabel("Hz"), new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(freqSetBtns[2], new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		freqPanel.add(new JPanel(), new GridBagConstraints(3, 6, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		freqPanel.add(new JPanel(), new GridBagConstraints(0, 99, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		
		JPanel dutyPanel = new JPanel(new GridBagLayout());
		dutyPanel.setBorder(BorderFactory.createTitledBorder("Duty cycle settings"));
		dutyLabel = new JLabel((DUTY_DEFAULT < 10 ? " " : "") + DUTY_DEFAULT + " %");
		dutySlider = new JSlider(DUTY_MIN, DUTY_MAX, DUTY_DEFAULT);
		dutyLabel.setFont(hugeFont);
		DutySpinner[] dutySpinners = new DutySpinner[]{
			new DutySpinner(1), new DutySpinner(5), new DutySpinner(50)
		};
		DutySetButton[] dutySetBtns = new DutySetButton[]{
			new DutySetButton(dutySpinners[0]), new DutySetButton(dutySpinners[1]), new DutySetButton(dutySpinners[2])	
		};
		ActionListener dutyDiv = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDutyCycle(duty / 2);
			}
		};
		ActionListener dutyMult = new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setDutyCycle(duty * 2);
			}
		};

		dutyPanel.add(dutyLabel, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));

		dutyPanel.add(dutySlider, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));
		dutyPanel.add(new CustomButton("/2", btnSize, dutyDiv), new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new CustomButton("x2", btnSize, dutyMult), new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		
		dutyPanel.add(dutySpinners[0], new GridBagConstraints(0, 4, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new JLabel("%"), new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(dutySetBtns[0], new GridBagConstraints(2, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new JPanel(), new GridBagConstraints(3, 4, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		dutyPanel.add(dutySpinners[1], new GridBagConstraints(0, 5, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new JLabel("%"), new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(dutySetBtns[1], new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new JPanel(), new GridBagConstraints(3, 5, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		dutyPanel.add(dutySpinners[2], new GridBagConstraints(0, 6, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new JLabel("%"), new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(dutySetBtns[2], new GridBagConstraints(2, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, insets, 0, 0));
		dutyPanel.add(new JPanel(), new GridBagConstraints(3, 6, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, insets, 0, 0));

		dutyPanel.add(new JPanel(), new GridBagConstraints(0, 99, 4, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));

		mainPanel.add(freqPanel, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		mainPanel.add(dutyPanel, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, insets, 0, 0));
		return mainPanel;
	}
	
	private void setFrequency(double frequency)
	{
		frequency = ((int)(10 * frequency)) / 10.0;
		if(frequency < FREQ_MIN || frequency > FREQ_MAX)
			return;
		this.frequency = frequency;
		freqSlider.setValue((int) (frequency * 10));
		freqLabel.setText(String.format((frequency < 100 ? (frequency < 10 ? "  " : " ") : "") + "%.1f Hz", frequency));
		sendToRemote();
	}
	
	private void setDutyCycle(int duty)
	{
		if(duty < DUTY_MIN || duty > DUTY_MAX)
			return;
		this.duty = duty;
		dutySlider.setValue(duty);
		dutyLabel.setText((duty < 10 ? " " : "") + duty + " %");
		sendToRemote();
	}
	
	private void sendToRemote()
	{
		if(connector == null)
			return;
		connector.setStrobeParameters(frequency, duty);
	}

	public class FrequencySpinner extends JSpinner
	{
		public FrequencySpinner(double value)
		{
			super(new SpinnerNumberModel(value, 1, 100, 0.1));
			setFont(buttonFont);
		}
	}
	
	public class DutySpinner extends JSpinner
	{
		public DutySpinner(int value)
		{
			super(new SpinnerNumberModel(value, 1, 99, 1));
			setFont(buttonFont);
		}
	}
	
	public class FrequencySetButton extends JButton
	{
		public FrequencySetButton(FrequencySpinner freqSpinner)
		{
			super("SET");
			if(freqSpinner != null)
			{
				addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						setFrequency(((SpinnerNumberModel)freqSpinner.getModel()).getNumber().doubleValue());
					}
				});
			}
		}
	}
	
	public class DutySetButton extends JButton
	{
		public DutySetButton(DutySpinner dutySpinner)
		{
			super("SET");
			if(dutySpinner != null)
			{
				addActionListener(new ActionListener()
				{
					@Override
					public void actionPerformed(ActionEvent e)
					{
						setDutyCycle(((SpinnerNumberModel)dutySpinner.getModel()).getNumber().intValue());
					}
				});
			}
		}
	}
	
	public class CustomButton extends JButton
	{
		public CustomButton(String text, Dimension size, ActionListener listener)
		{
			super(text);
			if(size != null)
				setPreferredSize(size);
			if(listener != null)
				addActionListener(listener);
		}
	}
	
	public static void main(String[] args)
	{
		new FullscreenStrobeRemote();
	}

}
