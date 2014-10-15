package com.sungard.scs.bds.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.DropMode;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.AbstractTableModel;

public abstract class DropTransferHandler extends TransferHandler {
	private final List<File> files;
	private final JTable fileList;
	private static final long serialVersionUID = -4271364865436298519L;

	DropTransferHandler(List<File> files, JTable fileList) {
		this.files = files;
		this.fileList = fileList;
		this.fileList.setDropMode(DropMode.INSERT);
	}

	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}

		return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
	}

	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}

		try {
			@SuppressWarnings("unchecked")
			List<File> data = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);

			int oldSize = this.files.size();
			for (File d : data) {
				if (d.isDirectory())
					handleDirectoryImport(d);
				else {
					this.files.add(d);
				}
			}
			AbstractTableModel atm = (AbstractTableModel) this.fileList.getModel();
			atm.fireTableRowsInserted(oldSize, this.files.size());

			return true;
		} catch (UnsupportedFlavorException e) {
			return false;
		} catch (IOException e) {
			return false;
		}

	}

	protected abstract void handleDirectoryImport(File paramFile);
}
