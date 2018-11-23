import java.util.ArrayList;

public class Blank2 implements Comparable {
	private int row;
	private int col;
	private int value;
	private ArrayList<Integer> possibleValues;

	public Blank2(int row, int col, int value, int boardSize) {
		this.row = row;
		this.col = col;
		this.value = value;
		this.possibleValues = new ArrayList<Integer>();
		for (int i = 1; i <= boardSize; i++) {
			this.possibleValues.add(i);
		}
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	public int getCol() {
		return col;
	}

	public void setCol(int col) {
		this.col = col;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public ArrayList<Integer> getPossibleValues() {
		return possibleValues;
	}

	public void setPossibleValues(ArrayList<Integer> possibleValues) {
		this.possibleValues = possibleValues;
	}

	@Override
	public int compareTo(Object b) {
		if (this.getPossibleValues().size() > ((Blank2) b).getPossibleValues().size()) {
			return 1;
		} else if (this.getPossibleValues().size() == ((Blank2) b).getPossibleValues().size()) {
			return 0;
		} else {
			return -1;
		}
	}
}
