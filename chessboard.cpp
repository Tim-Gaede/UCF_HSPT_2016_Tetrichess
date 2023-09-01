#include <iostream>
#include <fstream>
#include <string>
#include <vector>
#include <algorithm>
#include <cstdio>

using namespace std;

bool** board;
bool*** pieceRotations;
int n;

//This function returns a 90 degree rotation of piece
bool** rotatePiece(bool** piece){
    bool** rotated = new bool*[n];
    for(int i = 0; i < n; i++){
    	rotated[i] = new bool[n];
    }

    for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
            rotated[i][j] = piece[j][n-i-1];
        }
    }

    return rotated;

}

//This function is used to take out a piece that was previously placed
void revert(vector<int> fillCors){
	int size = fillCors.size();
		for (int i = 0; i < size; i+=2) {
			int row = fillCors[i];
			int col = fillCors[i+1];

			board[row][col] = false;
		}
}

//Attempts to fill the chessboard. It is forced that all rows above r are filled,
//and all columns left of the current column c are filled for the current row r.
bool placePieces(int r, int c){
    if(c == 8){
        return placePieces(r+1,0);
    }
    if(r == 8) return true;

    if(board[r][c]) return placePieces(r,c+1);
    vector<int> fillCors;

    //for each rotation I only need need to try placing by the tile that is in the topmost row and then leftmost column
    //of the piece at r and c due to how the the board is currently filled
    for(int rotation = 0; rotation < 4; rotation++){
    	//This boolean tells whether I ran into the first tile of the piece
        bool foundFirst = false;
        //This tells if there was a problem with placing the piece
        bool isbad = false;

        //These variables are to translate the piece coordinates into board coordinates
        int transR = 0;
        int transC = 0;
        for(int placeR = 0; placeR < n && !isbad; placeR++){
            for(int placeC = 0; placeC < n && !isbad; placeC++){
                if(pieceRotations[rotation][placeR][placeC] && !foundFirst){
                    transR = r -placeR;
                    transC = c - placeC;
                    foundFirst = true;
                }

                //Trying to place a tile from the piece to the board
                if(pieceRotations[rotation][placeR][placeC]){
                	//geting the translated positions for the board
                    int tr = placeR+transR;
                    int tc = placeC+transC;
                    //if board[tr][tc] is true it's already filled and the tile can't be placed
                    if(tr <0 || tr >= 8 || tc < 0 || tc >= 8 || board[tr][tc]){
                        isbad = true;
                        break;
                    }
                    board[tr][tc] = true;
                    //saving the position of the placed tile so it can be removed later
                    fillCors.push_back(tr);
                    fillCors.push_back(tc);
                }
            }
        }

        if(isbad){
        	//Take back what I did and try something else
            revert(fillCors);
        }
        else{
        	//try continuing on
            if(placePieces(r,c+1)) return true;
            //It didn't work out, take back what I did and try something else
            revert(fillCors);
        }

    }

    return false;
}



int main()
{
	//p from the input
    int p;
    cin >> p;

    //looping over every test case
    for(int piece = 1; piece <= p; piece++){
    	//n from the input
        cin >> n;
        int numtiles = 0;

       //initializing the three dimensional pieceRotations boolean array
        //It is 4 x n x n, which are the 4 rotations of the n by n piece
        //true is for a tile (*), and false is for empty (.)
        pieceRotations = new bool**[4];
        for(int i =0; i < 4; i++){
        	pieceRotations[i] = new bool*[n];
        	for(int j =0; j < n; j++){
        		pieceRotations[i][j] = new bool[n];
        		for(int k =0; k < n; k++){
        			pieceRotations[i][j][k] = false;
        		}
        	}
        }

        //reading in the piece into rotation 0
        for(int i = 0; i <n; i++){
            string line;
            if(i == 0) getline(cin, line); //reading in the empty newline
            getline(cin, line);
            for(int j = 0; j < n; j++){
                if(line[j] == '*'){
                	pieceRotations[0][i][j] = true;
                	numtiles++; //counting the number of tiles
                }
                else pieceRotations[0][i][j] = false;
            }
       }

        //setting the rotations with the rotatePiece function
        pieceRotations[1] = rotatePiece(pieceRotations[0]);
        pieceRotations[2] = rotatePiece(pieceRotations[1]);
        pieceRotations[3] = rotatePiece(pieceRotations[2]);

        //initializing the board
        board = new bool*[8];
        for(int i =0; i < 8; i++){
        	board[i] = new bool[8];
        	for(int j =0; j < 8; j++){
        		board[i][j] = false;
        	}
        }

        //writing the output
        cout << "Piece #" << piece << ": ";

        //placePieces attempts to fill the chessboard by placing pieces.
        //numtiles is used for a speed up. If the chessboard is 8x8 there are 64 squares.
        //Since everytime you place a piece all of its tiles must be placed on the board,
        //the number of tiles in the piece must divide evenly into 64.
        if(numtiles != 0 && 64%numtiles == 0 && placePieces(0,0)) cout << "YES" << endl;
        else cout << "NO" << endl;

        delete[] pieceRotations;
        delete[] board;
    }
}



