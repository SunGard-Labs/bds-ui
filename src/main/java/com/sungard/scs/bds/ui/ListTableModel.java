package com.sungard.scs.bds.ui;

import java.util.List;

import javax.swing.table.AbstractTableModel;

public class ListTableModel <T> extends AbstractTableModel {
	private static final long serialVersionUID = 8182429733714619564L;
	private List<T> source;

    public ListTableModel(List<T> source) {

        this.source = source;

    }


    //Override 'getRowCount' 
    //The row count would be calculated as the size of the outer list.
    @Override
    public int getRowCount() {
        return source.size();
    }

    //Override 'getColumnCount'
    //The column count would be calculated as the max size of the inner lists
    @Override
    public int getColumnCount() {
        int max = 1;
        return max;
    }

    //Override 'getColumnName'
    //Lets go ahead and just give a unique name to each column based on the index.
    //This could be populated from an input taken by the constructor, but we
    //won't worry about that now.
    @Override
    public String getColumnName(int columnIndex) {
        return "Column " + columnIndex;
    }

    //Override 'getColumnClass'
    //The class would technically be the generic type 'T', so to get this we 
    //will simply just get the calss of the first element.
    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return source.get(0).getClass();
    }


    //Override 'isCellEditable'
    //I'm going to assume we don't want cell to be editable.
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public T getValueAt(int rowIndex, int columnIndex) {
        T row = source.get(rowIndex);
        return row;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        //required but we will assume that you cannot change the source list
        //if we needed to, it wouldn't be too difficult to implement.
    }

}