package com.sungard.scs.bds.ui;

import java.io.File;
import java.util.List;

import javax.swing.table.AbstractTableModel;

class FileListTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 883849839449027099L;
	private final List<File> files;

	public FileListTableModel(List<File> files) {
		this.files = files;
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		File f = (File) this.files.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return f.getAbsolutePath();
		case 1:
			return Long.valueOf(f.length() / 1024L);
		}
		return "N/A";
	}

	public int getRowCount() {
		return this.files.size();
	}

	public int getColumnCount() {
		return 2;
	}

	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "File Path";
		case 1:
			return "Size (KiB)";
		}
		return "N/A";
	}
}
