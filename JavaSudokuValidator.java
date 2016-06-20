/**
 * Multi-threaded Sudoku Solution Validator by Sarmad Hashmi
 *
 * This program defines a sudoku puzzle solution and then determines whether 
 * the puzzle solution is valid using 27 threads. 9 for each 3x3 subsection, 9
 * for the 9 columns, and 9 for the 9 rows. Each thread updates their index in 
 * a global array to true indicating that the corresponding region in the puzzle
 * they were responsible for is valid. The program then waits for all threads
 * to complete their execution and checks if all entries in the valid array have
 * been set to true. If yes, the solution is valid. If not, solution is invalid.
 */

public class JavaSudokuValidator {
	// Global constant for number of threads
	private static final int NUM_THREADS = 27;
	// Sudoku puzzle solution to validate
	private static final int[][] sudoku = {
			{6, 2, 4, 5, 3, 9, 1, 8, 7},
			{5, 1, 9, 7, 2, 8, 6, 3, 4},
			{8, 3, 7, 6, 1, 4, 2, 9, 5},
			{1, 4, 3, 8, 6, 5, 7, 2, 9},
			{9, 5, 8, 2, 4, 7, 3, 6, 1},
			{7, 6, 2, 3, 9, 1, 4, 5, 8},
			{3, 7, 1, 9, 5, 6, 8, 4, 2},
			{4, 9, 6, 1, 8, 2, 5, 7, 3},
			{2, 8, 5, 4, 7, 3, 9, 1, 6}
			};
	// Array that worker threads will update
	private static boolean[] valid;
	
	// General object that will be extended by worker thread objects, only contains
	// the row and column relevant to the thread
	public static class RowColumnObject {
		int row;
		int col;
		RowColumnObject(int row, int column) {
			this.row = row;
			this.col = column;
		}
	}
	
	// Runnable object that determines if numbers 1-9 only appear once in a row
	public static class IsRowValid extends RowColumnObject implements Runnable {		
		IsRowValid(int row, int column) {
			super(row, column); 
		}

		@Override
		public void run() {
			if (col != 0 || row > 8) {
				System.out.println("Invalid row or column for row subsection!");				
				return;
			}
			
			// Check if numbers 1-9 only appear once in the row
			boolean[] validityArray = new boolean[9];
			int i;
			for (i = 0; i < 9; i++) {
				// If the corresponding index for the number is set to 1, and the number is encountered again,
				// the valid array will not be updated and the thread will exit.
				int num = sudoku[row][i];
				if (num < 1 || num > 9 || validityArray[num - 1]) {
					return;
				} else if (!validityArray[num - 1]) {
					validityArray[num - 1] = true;
				}
			}
			// If reached this point, row subsection is valid.
			valid[9 + row] = true;
		}

	}
	
	// Runnable object that determines if numbers 1-9 only appear once in a column
	public static class IsColumnValid extends RowColumnObject implements Runnable {
		IsColumnValid(int row, int column) {
			super(row, column); 
		}

		@Override
		public void run() {
			if (row != 0 || col > 8) {
				System.out.println("Invalid row or column for col subsection!");				
				return;
			}
			
			// Check if numbers 1-9 only appear once in the column
			boolean[] validityArray = new boolean[9];
			int i;
			for (i = 0; i < 9; i++) {
				// If the corresponding index for the number is set to 1, and the number is encountered again,
				// the valid array will not be updated and the thread will exit.
				int num = sudoku[i][col];
				if (num < 1 || num > 9 || validityArray[num - 1]) {
					return;
				} else if (!validityArray[num - 1]) {
					validityArray[num - 1] = true;
				}
			}
			// If reached this point, column subsection is valid.
			valid[18 + col] = true;			
		}		
	}
	
	// Runnable object that determines if numbers 1-9 only appear once in a 3x3 subsection
	public static class Is3x3Valid extends RowColumnObject implements Runnable {
		Is3x3Valid(int row, int column) {
			super(row, column); 
		}

		@Override
		public void run() {
			// Confirm valid parameters
			if (row > 6 || row % 3 != 0 || col > 6 || col % 3 != 0) {
				System.out.println("Invalid row or column for subsection!");
				return;
			}
			
			// Check if numbers 1-9 only appear once in 3x3 subsection
			boolean[] validityArray = new boolean[9];			
			for (int i = row; i < row + 3; i++) {
				for (int j = col; j < col + 3; j++) {
					int num = sudoku[i][j];
					if (num < 1 || num > 9 || validityArray[num - 1]) {
						return;
					} else {
						validityArray[num - 1] = true;		
					}
				}
			}
			// If reached this point, 3x3 subsection is valid.
			valid[row + col/3] = true; // Maps the subsection to an index in the first 8 indices of the valid array			
		}
		
	}
	
	public static void main(String[] args) {
		valid = new boolean[NUM_THREADS];		
		Thread[] threads = new Thread[NUM_THREADS];
		int threadIndex = 0;
		// Create 9 threads for 9 3x3 subsections, 9 threads for 9 columns and 9 threads for 9 rows.
		// This will end up with a total of 27 threads.
		for (int i = 0; i < 9; i++) {
			for (int j = 0; j < 9; j++) {						
				if (i%3 == 0 && j%3 == 0) {
					threads[threadIndex++] = new Thread(new Is3x3Valid(i, j));				
				}
				if (i == 0) {					
					threads[threadIndex++] = new Thread(new IsColumnValid(i, j));
				}
				if (j == 0) {
					threads[threadIndex++] = new Thread(new IsRowValid(i, j));					
				}
			}
		}
		
		// Start all threads
		for (int i = 0; i < threads.length; i++) {
			threads[i].start();
		}
		
		// Wait for all threads to finish
		for (int i = 0; i < threads.length; i++) {
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		// If any of the entries in the valid array are 0, then the sudoku solution is invalid
		for (int i = 0; i < valid.length; i++) {
			if (!valid[i]) {
				System.out.println("Sudoku solution is invalid!");
				return;
			}
		}
		System.out.println("Sudoku solution is valid!");
	}
}