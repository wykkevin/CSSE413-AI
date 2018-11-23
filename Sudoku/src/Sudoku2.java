import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Sudoku2 {
	private static int boardSize = 0;
	private static int partitionSize = 0;

	public static void main(String[] args) throws IOException {
		String filename = "src/sudoku16ReallyHard.txt";
		File inputFile = new File(filename);
		Scanner input = null;
		int[][] vals = null;
		ArrayList<Blank2> empties = new ArrayList<>();

		int temp = 0;
		int count = 0;

		try {
			input = new Scanner(inputFile);
			temp = input.nextInt();
			boardSize = temp;
			partitionSize = (int) Math.sqrt(boardSize);
			System.out.println("Boardsize: " + temp + "x" + temp);
			vals = new int[boardSize][boardSize];

			System.out.println("Input:");
			int i = 0;
			int j = 0;
			while (input.hasNext()) {
				temp = input.nextInt();
				count++;
				System.out.printf("%3d", temp);
				vals[i][j] = temp;
				if (temp == 0) {
					empties.add(new Blank2(i, j, 0, boardSize));
				}
				j++;
				if (j == boardSize) {
					j = 0;
					i++;
					System.out.println();
				}
				if (j == boardSize) {
					break;
				}
			}
			input.close();
		} catch (FileNotFoundException exception) {
			System.out.println("Input file not found: " + filename);
		}
		if (count != boardSize * boardSize)
			throw new RuntimeException("Incorrect number of inputs.");
		
		for (int i=0;i<boardSize;i++) {
			for (int j=0;j<boardSize;j++) {
				if (vals[i][j]!=0) {
					restrict(i,j,vals[i][j],empties);
				}
			}
		}

		boolean newChange = inference(vals, empties);
		while (newChange) {
			newChange = inference(vals, empties);
		}
		
		System.out.println("svsdvdfvdsvvsdvdsvfdvdv");
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
			}
			System.out.println();
		}
		System.out.println("svsdvdfvdsvvsdvdsvfdvdv");

		// Collections.sort(empties);

		// try {
		// String path = filename.substring(0, filename.length() - 4) + "Solution.txt";
		// PrintWriter writer = new PrintWriter(path, "UTF-8");
		// writer.println(boardSize);

		// Output
		if (!backtrackSearchAnvanced(vals, empties)) {
			System.out.println("No solution found.");
			// writer.println("No solution found.");
			// writer.close();
			return;
		}
		System.out.println("\nOutput\n");
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				System.out.printf("%3d", vals[i][j]);
				// writer.print(vals[i][j] + " ");
			}
			System.out.println();
			// writer.println();
		}
		// writer.close();
		// } catch (IOException ex) {
		// ex.printStackTrace();
		// }
	}

	public static boolean solve(int vals[][]) {
		for (int j = 0; j < boardSize; j++) {
			for (int i = 0; i < boardSize; i++) {
				if (vals[i][j] == 0) {
					return false;
				}
			}
		}
		return true;
	}

	public static boolean isInRow(int[][] val, int row, int n) {
		for (int i = 0; i < boardSize; i++) {
			if (n == val[row][i]) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInColumn(int[][] val, int col, int n) {
		for (int i = 0; i < boardSize; i++) {
			if (n == val[i][col]) {
				return true;
			}
		}
		return false;
	}

	public static boolean isInBox(int[][] val, int row, int col, int n) {
		int boxRow = row - row % partitionSize;
		int boxCol = col - col % partitionSize;
		for (int i = 0; i < partitionSize; i++) {
			for (int j = 0; j < partitionSize; j++) {
				if (n == val[i + boxRow][j + boxCol]) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean inCheck(int[][] val, int row, int col, int n) {
		return isInBox(val, row, col, n) || isInColumn(val, col, n) || isInRow(val, row, n);
	}

	// For 9*9
	public static boolean backtrackSearch(int[][] vals) {
		if (solve(vals)) {
			return true;
		}
		for (int i = 0; i < boardSize; i++) {
			for (int j = 0; j < boardSize; j++) {
				if (vals[i][j] == 0) {
					for (int k = 1; k <= boardSize; k++) {
						if (!inCheck(vals, i, j, k)) {
							vals[i][j] = k;
							if (backtrackSearch(vals)) {
								return true;
							} else {
								vals[i][j] = 0;
							}
						}
					}
					return false;
				}
			}
		}
		return true;
	}

	// For 16*16 or higher
	public static boolean backtrackSearchAnvanced(int[][] vals, ArrayList<Blank2> empties) {
		if (solve(vals)) {
			return true;
		}
		for (int i = 0; i < empties.size(); i++) {
			Blank2 currentBlank2 = empties.get(i);
			if (vals[currentBlank2.getRow()][currentBlank2.getCol()] == 0) {
				if (currentBlank2.getValue() == 0) {
					ArrayList<Integer> possibles = currentBlank2.getPossibleValues();
					for (int k = 0; k < possibles.size(); k++) {
						if (!inCheck(vals, currentBlank2.getRow(), currentBlank2.getCol(), possibles.get(k))) {
							vals[currentBlank2.getRow()][currentBlank2.getCol()] = possibles.get(k);
							currentBlank2.setValue(possibles.get(k));
							boolean c = inference(vals, empties);
							while (c) {
								c = inference(vals, empties);
							}
							if (backtrackSearchAnvanced(vals, empties)) {
								return true;
							} else {
//								System.out.println("svsdvdfvdsvvsdvdsvfdvdv");
//								for (int ii = 0; ii < boardSize; ii++) {
//									for (int jj = 0; jj < boardSize; jj++) {
//										System.out.printf("%3d", vals[ii][jj]);
//									}
//									System.out.println();
//								}
//								System.out.println("svsdvdfvdsvvsdvdsvfdvdv");
								vals[currentBlank2.getRow()][currentBlank2.getCol()] = 0;
								currentBlank2.setValue(0);
							}
						}
					}
				}
				return false;
			}
		}
		return true;
	}

	public static void restrict(int row, int col, int num, ArrayList<Blank2> empties) {
		for (int i = 0; i < empties.size(); i++) {
			Blank2 current = empties.get(i);
			if (current.getRow() == row || current.getCol() == col
					|| (col - col % partitionSize == current.getCol() - current.getCol() % partitionSize
							&& row - row % partitionSize == current.getRow() - current.getRow() % partitionSize)) {
				ArrayList<Integer> possibles = current.getPossibleValues();
				for (int j = 0; j < possibles.size(); j++) {
					if (possibles.get(j) == num) {
						possibles.remove(j);
					}
				}
			}
		}
	}

	public static boolean inference(int[][] vals, ArrayList<Blank2> empties) {
		boolean newChange = false;
		for (int i = 0; i < empties.size(); i++) {
			Blank2 currentBlank = empties.get(i);
			if (currentBlank.getPossibleValues().size() == 1 && currentBlank.getValue() == 0) {
				newChange = true;
				currentBlank.setValue(currentBlank.getPossibleValues().get(0));
				vals[currentBlank.getRow()][currentBlank.getCol()] = currentBlank.getValue();
				restrict(currentBlank.getRow(), currentBlank.getCol(), currentBlank.getValue(), empties);
			}
		}
		return newChange;
	}
}
