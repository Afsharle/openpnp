package org.openpnp.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.openpnp.ConfigurationListener;
import org.openpnp.Length;
import org.openpnp.gui.support.LengthCellValue;
import org.openpnp.machine.reference.camera.AbstractCamera;
import org.openpnp.model.Configuration;
import org.openpnp.model.Location;
import org.openpnp.spi.Camera;
import org.openpnp.spi.Camera.Looking;
import org.openpnp.util.LengthUtil;

class CamerasTableModel extends AbstractTableModel implements ConfigurationListener {
	final private Configuration configuration;
	
	// TODO: add head
	private String[] columnNames = new String[] { "Name", "Looking", "X", "Y", "Z", "θ" };
	private List<Camera> cameras;

	public CamerasTableModel(Configuration configuration) {
		this.configuration = configuration;
		configuration.addListener(this);
	}

	public void configurationLoaded(Configuration configuration) {
		cameras = new ArrayList<Camera>(configuration.getMachine().getCameras());
		fireTableDataChanged();
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public int getRowCount() {
		return (cameras == null) ? 0 : cameras.size();
	}
	
	public Camera getCamera(int index) {
		return cameras.get(index);
	}
	
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}
	
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 2 || columnIndex == 3 || columnIndex == 4) {
			return LengthCellValue.class;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		try {
			// TODO: Evil hack until we move the settable properties for Camera
			// into the Camera interface.
			AbstractCamera camera = (AbstractCamera) cameras.get(rowIndex);
			if (columnIndex == 0) {
				camera.setName((String) aValue);
			}
			else if (columnIndex == 1) {
				camera.setLooking((Looking) aValue);
			}
			else if (columnIndex == 2) {
				Length length = ((LengthCellValue) aValue).getLength();
				Location location = camera.getLocation();
				if (location.getUnits() == null) {
					location.setUnits(length.getUnits());
				}
				else {
					location = LengthUtil.convertLocation(location, length.getUnits());
				}
				location.setX(length.getValue());
				camera.setLocation(location);
			}
			else if (columnIndex == 3) {
				Length length = ((LengthCellValue) aValue).getLength();
				Location location = camera.getLocation();
				if (location.getUnits() == null) {
					location.setUnits(length.getUnits());
				}
				else {
					location = LengthUtil.convertLocation(location, length.getUnits());
				}
				location.setY(length.getValue());
				camera.setLocation(location);
			}
			else if (columnIndex == 4) {
				Length length = ((LengthCellValue) aValue).getLength();
				Location location = camera.getLocation();
				if (location.getUnits() == null) {
					location.setUnits(length.getUnits());
				}
				else {
					location = LengthUtil.convertLocation(location, length.getUnits());
				}
				location.setZ(length.getValue());
				camera.setLocation(location);
			}
			else if (columnIndex == 5) {
				camera.getLocation().setRotation(Double.parseDouble(aValue.toString()));
			}
			configuration.setDirty(true);
		}
		catch (Exception e) {
			// TODO: dialog, bad input
		}
	}
	
	public Object getValueAt(int row, int col) {
		Camera camera = cameras.get(row);
		Location loc = camera.getLocation();
		switch (col) {
		case 0:
			return camera.getName();
		case 1:
			return camera.getLooking();
		case 2:
			return new LengthCellValue(loc.getX(), loc.getUnits());
		case 3:
			return new LengthCellValue(loc.getY(), loc.getUnits());
		case 4:
			return new LengthCellValue(loc.getZ(), loc.getUnits());
		case 5:
			return String.format("%2.3f", camera.getLocation().getRotation());
			
		default:
			return null;
		}
	}
}