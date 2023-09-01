import java.util.*;
import java.io.*;

public class chessboard {

	public static ArrayList<Long> rotations;
	public static ArrayList<Long> topRightRotations;
	public static ArrayList<Long> allPieces;

	public static int pieceSize;

	public static void main(String[] args) {
		Scanner in = new Scanner(System.in);

		int numCases = in.nextInt();

		for(int caseN = 1; caseN <= numCases; caseN++) {
			// Read in the piece size
			pieceSize = in.nextInt();

			// Reset all of our arrays
			rotations = new ArrayList<Long>();
			topRightRotations = new ArrayList<Long>();
			allPieces = new ArrayList<Long>();

			// Read in the initial piece data
			char[][] piece = new char[pieceSize][];
			for(int row = 0; row < pieceSize; row++) {
				piece[row] = in.next().toCharArray();
			}

			// Pieces of size 1, 2 are always possible
			// Pieces not of a multiple of two are impossible
			if(pieceSize == 1 || pieceSize == 2) {
				System.out.printf("Piece #%d: YES\n", caseN);
				continue;
			} else if(pieceSize != 4 && pieceSize != 8 && pieceSize != 16 && pieceSize != 32 && pieceSize != 64) {
				System.out.printf("Piece #%d: NO\n", caseN);
				continue;
			}

			// Trim our piece
			int[] trimDims = getTrimDims(piece);
			int row1 = trimDims[0];
			int col1 = trimDims[1];
			int row2 = trimDims[2];
			int col2 = trimDims[3];

			int numRows = row2 - row1 + 1;
			int numCols = col2 - col1 + 1;

			// If the piece is too big, it won't fit
			if(numRows > 8 || numCols > 8) {
				System.out.printf("Piece #%d: NO\n", caseN);
				continue;
			}

			// Now we trim the piece into an 8x8 array
			boolean[][] trimmedPiece = new boolean[8][8];
			for(int r = row1; r <= row2; r++)
				for(int c = col1; c <= col2; c++)
					if(piece[r][c] == '*')
						trimmedPiece[r - row1][c - col1] = true;

			// Now we build the long to represent the piece
			// Every byte in the long represents 1 row
			long finalPiece = 0L;
			for(int r = 0; r < 8; r++)
				for(int c = 0; c < 8; c++)
					if(trimmedPiece[r][c])
						finalPiece |= (1L << (r * 8 + c));

			// Hard code in pieces of size 4
			if(pieceSize == 4) {
				// Check if this is a squigly piece
				// The numbers below are the long values of all 4 rotations
				// of the piece below
				// ##.
				// .##
				if(finalPiece == 774 || finalPiece == 1539 ||
				   finalPiece == 66306 || finalPiece == 131841)
					System.out.printf("Piece #%d: NO\n", caseN);
				else
					System.out.printf("Piece #%d: YES\n", caseN);
				continue;
			}

			// Hard code the 64 piece
			if(pieceSize == 64) {
				// We know it works because any piece other piece with 64
				// Would have been caught earlier because it would be too big
				System.out.printf("Piece #%d: YES\n", caseN);
				continue;
			}

			// Next, we generate all of our pieces
			genAllPieces(finalPiece);

			// Now we check if it will work
			boolean possible = false;
			for(long rotation : rotations) {
				// For each bottom-left rotation, place the piece
				long board = rotation;
				for(long topRightRotation : topRightRotations) {
					// For each top-right rotation, place the piece if we can
					if((board & topRightRotation) != 0) continue;

					// If this board can be filled, we are done
					if(go(board | topRightRotation, pieceSize * 2)) {
						possible = true;
						break;
					}
				}
				if(possible) break;
			}

			// Print our answer
			System.out.printf("Piece #%d: %s\n", caseN, possible ? "YES" : "NO");
		}
	}

	public static boolean go(long board, int totalBlocks) {
		// If the board is full, return true
		if(totalBlocks == 64) return true;

		// Try placing each piece on the board
		for(long piece : allPieces) {
			if((board & piece) != 0) continue;
			// If it fits, place it then recurse down
			if(go(board | piece, totalBlocks + pieceSize))
				return true;
		}

		return false;
	}

	// Generates all pieces in every rotation at every position
	public static void genAllPieces(long piece) {
		for(int rot = 0; rot < 4; rot++) {
			if(!rotations.contains(piece)) {
				rotations.add(piece);
				topRightRotations.add(moveToTopRight(piece));
			}
			long shifted1 = piece;
			for(int r = 0; r < 8; r++) {
				long shifted2 = shifted1;
				for(int c = 0; c < 8; c++) {
					if(Long.bitCount(shifted2) == pieceSize && !allPieces.contains(shifted2)) {
						allPieces.add(shifted2);
					}
					shifted2 = shiftRight(shifted2);
				}
				shifted1 = shiftUp(shifted1);
			}
			piece = rotate(piece);
		}
	}

	// Returns this piece rotated 90 degrees clockwise
	public static long rotate(long piece) {
		long newPiece = 0L;
		for(int r = 0; r < 8; r++) {
			for(int c = 0; c < 8; c++) {
				if((piece & (1L << (r * 8 + c))) == 0) continue;
				int newRow = 7 - c;
				int newCol = r;
				newPiece |= 1L << (newRow * 8 + newCol);
			}
		}
		return moveToBottomLeft(newPiece);
	}

	// Shifts the piece up once
	public static long shiftUp(long piece) {
		return piece << 8;
	}

	// Shifts the piece down once
	public static long shiftDown(long piece) {
		return piece >> 8;
	}

	// Shifts the piece one to the right
	public static long shiftRight(long piece) {
		long newPiece = 0L;
		for(int r = 0; r < 8; r++) {
			long curRow = piece >> (8 * r);
			curRow &= (1L << 8) - 1L;
			curRow <<= 1;
			curRow &= (1L << 8) - 1L;
			newPiece |= curRow << (8 * r);
		}
		return newPiece;
	}

	// Shifts the piece one to the left
	public static long shiftLeft(long piece) {
		long newPiece = 0L;
		for(int r = 0; r < 8; r++) {
			long curRow = piece >> (8 * r);
			curRow &= (1L << 8) - 1L;
			curRow >>= 1;
			curRow &= (1L << 8) - 1L;
			newPiece |= curRow << (8 * r);
		}
		return newPiece;
	}

	// Moves the piece to the bottom left corner
	public static long moveToBottomLeft(long piece) {
		long temp;
		int count = Long.bitCount(piece);
		while(true) {
			temp = shiftDown(piece);
			if(Long.bitCount(temp) != count) break;
			piece = temp;
		}
		while(true) {
			temp = shiftLeft(piece);
			if(Long.bitCount(temp) != count) break;
			piece = temp;
		}
		return piece;
	}

	// Moves the piece to the top right corner
	public static long moveToTopRight(long piece) {
		long temp;
		int count = Long.bitCount(piece);
		while(true) {
			temp = shiftUp(piece);
			if(Long.bitCount(temp) != count) break;
			piece = temp;
		}
		while(true) {
			temp = shiftRight(piece);
			if(Long.bitCount(temp) != count) break;
			piece = temp;
		}
		return piece;
	}

	// Gets the start and end rows/cols for this piece after trimming '.'s
	public static int[] getTrimDims(char[][] piece) {
		int row1 = 0;
		int col1 = 0;
		int row2 = pieceSize - 1;
		int col2 = pieceSize - 1;

		for(int row = 0; row < pieceSize; row++) {
			boolean hasBlock = false;
			for(int col = 0; col < pieceSize; col++) {
				if(piece[row][col] == '*') {
					hasBlock = true;
					break;
				}
			}
			if(hasBlock) break;
			row1++;
		}

		for(int col = 0; col < pieceSize; col++) {
			boolean hasBlock = false;
			for(int row = 0; row < pieceSize; row++) {
				if(piece[row][col] == '*') {
					hasBlock = true;
					break;
				}
			}
			if(hasBlock) break;
			col1++;
		}

		for(int row = pieceSize - 1; row >= 0; row--) {
			boolean hasBlock = false;
			for(int col = 0; col < pieceSize; col++) {
				if(piece[row][col] == '*') {
					hasBlock = true;
					break;
				}
			}
			if(hasBlock) break;
			row2--;
		}

		for(int col = pieceSize - 1; col >= 0; col--) {
			boolean hasBlock = false;
			for(int row = 0; row < pieceSize; row++) {
				if(piece[row][col] == '*') {
					hasBlock = true;
					break;
				}
			}
			if(hasBlock) break;
			col2--;
		}

		return new int[]{row1, col1, row2, col2};
	}
}