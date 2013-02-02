package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class MotorConfigurationPanel extends JPanel {
	
	private static final Translator trans = Application.getTranslator();
	
	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;
	
	private final MotorConfigurationTableModel configurationTableModel;
	private final JButton selectMotorButton, removeMotorButton, selectIgnitionButton, resetIgnitionButton;
	
	final MotorMount[] mounts;
	
	MotorConfigurationPanel(FlightConfigurationDialog flightConfigurationDialog, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;
		
		DescriptionArea desc = new DescriptionArea(trans.get("description"), 3, -1);
		this.add(desc, "spanx, growx, wrap para");
		
		mounts = getPotentialMotorMounts();
		
		////  Motor mount selection
		JLabel label = new StyledLabel(trans.get("lbl.motorMounts"), Style.BOLD);
		this.add(label, "");
		
		//// Motor selection
		label = new StyledLabel(trans.get("lbl.motorConfiguration"), Style.BOLD);
		this.add(label, "wrap rel");
		
		
		//// Motor Mount selection 
		JTable table = new JTable(new MotorMountTableModel(this));
		table.setTableHeader(null);
		table.setShowVerticalLines(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn col0 = columnModel.getColumn(0);
		int w = table.getRowHeight() + 2;
		col0.setMinWidth(w);
		col0.setPreferredWidth(w);
		col0.setMaxWidth(w);
		
		table.addMouseListener(new GUIUtil.BooleanTableClickListener(table));
		JScrollPane scroll = new JScrollPane(table);
		this.add(scroll, "w 200lp, h 150lp, grow");
		
		
		//// Motor selection table.
		configurationTableModel = new MotorConfigurationTableModel(rocket);
		final JTable configurationTable = new JTable(configurationTableModel);
		configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configurationTable.setRowSelectionAllowed(true);
		
		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = configurationTable.getSelectedRow();
				
				if (e.getClickCount() == 1) {
					
					// Single click updates selection
					updateButtonState();
					
				} else if (e.getClickCount() == 2) {
					
					// Double-click edits motor
					selectMotor();
					
				}
				
			}
		});
		
		scroll = new JScrollPane(configurationTable);
		this.add(scroll, "w 500lp, h 150lp, grow, wrap");
		
		//// Select motor
		selectMotorButton = new JButton(trans.get("MotorConfigurationPanel.btn.selectMotor"));
		selectMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectMotor();
			}
		});
		this.add(selectMotorButton, "skip, split, sizegroup button");
		
		//// Remove motor button
		removeMotorButton = new JButton(trans.get("MotorConfigurationPanel.btn.removeMotor"));
		removeMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMotor();
			}
		});
		this.add(removeMotorButton, "sizegroup button");
		
		//// Select Ignition button
		selectIgnitionButton = new JButton(trans.get("MotorConfigurationPanel.btn.selectIgnition"));
		selectIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectIgnition();
			}
		});
		this.add(selectIgnitionButton, "sizegroup button");
		
		//// Reset Ignition button
		resetIgnitionButton = new JButton(trans.get("MotorConfigurationPanel.btn.resetIgnition"));
		resetIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// FIXME
				selectIgnition();
			}
		});
		this.add(resetIgnitionButton, "sizegroup button, wrap");
		
	}
	
	public void fireTableDataChanged() {
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	public void updateButtonState() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		selectMotorButton.setEnabled(currentMount != null && currentID != null);
		removeMotorButton.setEnabled(currentMount != null && currentID != null);
		selectIgnitionButton.setEnabled(currentMount != null && currentID != null);
		resetIgnitionButton.setEnabled(currentMount != null && currentID != null);
	}
	
	
	private void selectMotor() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentID == null || currentMount == null)
			return;
		
		MotorChooserDialog dialog = new MotorChooserDialog(
				currentMount.getMotor(currentID),
				currentMount.getMotorDelay(currentID),
				currentMount.getMotorMountDiameter(),
				flightConfigurationDialog);
		dialog.setVisible(true);
		Motor m = dialog.getSelectedMotor();
		double d = dialog.getSelectedDelay();
		
		if (m != null) {
			currentMount.setMotor(currentID, m);
			currentMount.setMotorDelay(currentID, d);
		}
		
		flightConfigurationDialog.fireContentsUpdated();
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	private void removeMotor() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentID == null || currentMount == null)
			return;
		
		currentMount.setMotor(currentID, null);
		
		flightConfigurationDialog.fireContentsUpdated();
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	private void selectIgnition() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentID == null || currentMount == null)
			return;
		
		SelectIgnitionConfigDialog dialog = new SelectIgnitionConfigDialog(
				this.flightConfigurationDialog,
				rocket,
				currentMount);
		dialog.setVisible(true);
		
		flightConfigurationDialog.fireContentsUpdated();
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	public void makeMotorMount(MotorMount mount, boolean isMotorMount) {
		mount.setMotorMount(isMotorMount);
		configurationTableModel.fireTableStructureChanged();
		updateButtonState();
	}
	
	private MotorMount[] getPotentialMotorMounts() {
		List<MotorMount> list = new ArrayList<MotorMount>();
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount) {
				list.add((MotorMount) c);
			}
		}
		return list.toArray(new MotorMount[0]);
	}
	
	public MotorMount findMount(int row) {
		MotorMount mount = null;
		
		int count = row;
		for (MotorMount m : mounts) {
			if (m.isMotorMount())
				count--;
			if (count < 0) {
				mount = m;
				break;
			}
		}
		
		if (mount == null) {
			throw new IndexOutOfBoundsException("motor mount not found, row=" + row);
		}
		return mount;
	}
	
	
}
