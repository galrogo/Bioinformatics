import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.*;

import javax.swing.JTable.PrintMode;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;

public class Alignments {
	final int minInf = -99999;
	final byte diagM = 0;
	final byte diagX = 1;
	final byte diagY = 2;
	final byte insM = 3;
	final byte insX = 4;
	final byte insY = 5;
	final byte delM = 6;
	final byte delX = 7;
	final byte delY = 8;
	final int A = 0;
	final int T = 1;
	final int G = 2;
	final int C = 3;
	final int U = 4;
	final int N = 5;
	final int Space = 6;
	final String scoreMatFile;
	int gapA;
	int gapB;
	int score[][];
	String target, query;
	int s2[], s1[];
	int n,m;
	//  m+1 x n+1 x 2 		third dimension for the trace back arrow
	// M[i][j][1]	values: 0 for M/R	1 for I		2 for D
	int M[][];
	int Ix[][];
	int Iy[][];
	byte pointers[][];
	String targetOutput ="";
	String queryOutput = "";
	int finalScore;
	

	public Alignments(String str2, String str1, String scoreMatFile) {
		this.target = str2.toUpperCase();
		this.query = str1.toUpperCase();
		this.scoreMatFile = scoreMatFile;
	}
	
	////////////////////////////////////
	// Initializing
	////////////////////////////////////
	
	public void initAll() {
		initStrings(this.target, this.query);
		initScoreMatrix();
	}
	
	public void initScoreMatrix() {
//		initDefaultScoreMatrix();
//		initTestScoreMat();
		readScoreMat();
	}
	
	public void initDefaultScoreMatrix() {
	/*
	    A   T   G   C   U   N   *
	A   5  -4  -4  -4  -4   5   -1
	T  -4   5  -4  -4   5   5   -1
	G  -4  -4   5  -4  -4   5   -1
	C  -4  -4  -4   5  -4   5   -1
	U  -4   5  -4  -4   5   5   -1
	N   5   5   5   5   5   5   -1
	*  -1  -1  -1  -1  -1  -1   -1
	*/
		this.score = new int[7][7];
		this.score[0] = new int[] { 5,-4,-4,-4,-4, 5,-1};
		this.score[1] = new int[] {-4, 5,-4,-4, 5, 5,-1};
		this.score[2] = new int[] {-4,-4, 5,-4,-4, 5,-1};
		this.score[3] = new int[] {-4,-4,-4, 5,-4, 5,-1};
		this.score[4] = new int[] {-4, 5,-4,-4, 5, 5,-1};
		this.score[5] = new int[] { 5, 5, 5, 5, 5, 5,-1};
		this.score[6] = new int[] {-1,-1,-1,-1,-1,-1,-1};
	}

	public void initTestScoreMat() {
		this.score = new int[7][7];
		this.score[0] = new int[] { 5,-3,-3,-3,-3, 5,-2};
		this.score[1] = new int[] {-3, 5,-3,-3, 5, 5,-2};
		this.score[2] = new int[] {-3,-4, 5,-3,-3, 5,-2};
		this.score[3] = new int[] {-3,-3,-3, 5,-3, 5,-2};
		this.score[4] = new int[] {-3, 5,-3,-3, 5, 5,-2};
		this.score[5] = new int[] { 5, 5, 5, 5, 5, 5,-2};
		this.score[6] = new int[] {-2,-2,-2,-2,-2,-2,-2};
	}
	
	public void readScoreMat() {
		this.score = new int[7][7];
		try{
			  // Open the file that is the first 
			  // command line parameter
			  FileInputStream fstream = new FileInputStream(this.scoreMatFile);
			  // Get the object of DataInputStream
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  //Read File Line By Line
			  while ((strLine = br.readLine()) != null)   {
			  // Print the content on the console
				  if (!strLine.startsWith("#"))
					  break;
			  }
			  for (int i = 0; i < 7; i++) {
				  if ((strLine = br.readLine()) != null) {
					  for (int j = 0; j < 6; j++) {
						  strLine = (strLine.substring(strLine.indexOf(" "))).trim();  
						  this.score[i][j] = Integer.parseInt(strLine.substring(0, strLine.indexOf(" ")));
					  }
					  strLine = (strLine.substring(strLine.indexOf(" "))).trim();  
					  this.score[i][6] = Integer.parseInt(strLine);
				  }
			  }
			  while ((strLine = br.readLine()) != null)   {
				  if (strLine.startsWith("A")) {
					  this.gapA = Integer.parseInt(strLine.substring(1).trim());
				  } else if (strLine.startsWith("B")) {
					  this.gapB = Integer.parseInt(strLine.substring(1).trim());
				  }
			  }
			  
			  //Close the input stream
			  in.close();
		}catch (Exception e){//Catch exception if any
			  System.err.println("Error: " + e.getMessage());
		}
	}

	public void initStrings(String str1,String str2) {
		this.n = this.target.length();
		this.m = this.query.length();
		this.s2 = new int[n+1];
		this.s1 = new int[m+1];
		this.s2[0]=-1;
		this.s1[0]=-1;
		char c;
		for (int i = 0; i < n; i++) {
			c = this.target.charAt(i);
			switch (c) {
	        	case 'A' : this.s2[i+1] = A;   break;
	        	case 'T' : this.s2[i+1] = T;   break;
	        	case 'G' : this.s2[i+1] = G;   break;
	        	case 'C' : this.s2[i+1] = C;   break;
	        	case 'U' : this.s2[i+1] = U;   break;
	        	case 'N' : this.s2[i+1] = N;   break;
			}
		}
		for (int i = 0; i < m; i++) {
			c = this.query.charAt(i);
			switch (c) {
			case 'A' : this.s1[i+1] = A;   break;
			case 'T' : this.s1[i+1] = T;   break;
			case 'G' : this.s1[i+1] = G;   break;
			case 'C' : this.s1[i+1] = C;   break;
			case 'U' : this.s1[i+1] = U;   break;
			case 'N' : this.s1[i+1] = N;   break;
			}
		}
		
	}
	
	////////////////////////////////////
	// Global Alignment
	////////////////////////////////////
	
	public void runGlobalAlignment(boolean gap , boolean affine) {
		initAll();
		if (affine) globalAffineAlignment();
		else if (gap) globalGapAlignment();
		else globalAlignment();
		printMat();
		printGlobalResults();
	}
	
	public void globalAlignment() {
		this.M = new int[m+1][n+1];	// creating matrix
		this.pointers = new byte[m+1][n+1];
		// initializing matrix
		this.M[0][0] = 0;
		for (int i = 1; i <= n; i++) {	// base row
			this.M[0][i] = this.M[0][i-1] + this.score[6][s2[i]];
			this.pointers[0][i] = 1;
		}
		for (int i = 1; i <= m; i++) {	// base col
			this.M[i][0] = this.M[i-1][0] + this.score[s1[i]][6];
			this.pointers[i][0] = 2;
		}
		// filling matrix
		int res1,res2,res3;
		for (int i = 1; i <= m; i++) {		// rows S2[i]
			for (int j = 1; j <= n; j++) {	// cols S1[j]
				res1 = this.M[i-1][j-1] + this.score[this.s1[i]][this.s2[j]];	// M\R
				res2 = this.M[i-1][j] + this.score[this.s1[i]][6];				// D
				res3 = this.M[i][j-1] + this.score[6][this.s2[j]];				// I
//				System.out.println("["+i+"]["+j+"]");
//				System.out.println(res1 + " " + res2 + " " + res3);
				if (res1>res2) {
					this.M[i][j] = res1;
					this.pointers[i][j] = 0;
				} else {
					this.M[i][j] = res2;
					this.pointers[i][j] = 2;
				}
				if (res3 > this.M[i][j]) {
					this.M[i][j] = res3;
					this.pointers[i][j] = 1;
				}
			}
		}
		this.finalScore = this.M[m][n];
		//trace back
		globalTraceBack(m,n);
	}
	
	// TODO write this thing
	public void globalAffineAlignment() {
		System.out.println("Running Global allignment with affine gap");
		this.M = new int[m+1][n+1];	// creating matrix
		this.Ix = new int[m+1][n+1];
		this.Iy = new int[m+1][n+1];
		// initializing matrix
		this.M[0][0] = 0;
		this.Ix[0][0] = -this.gapA;
		this.Iy[0][0] = -this.gapA;
		
		for (int i = 1; i <= n; i++) {	// base row
			this.M[0][i] = this.minInf;
			this.Ix[0][i] = this.minInf;
			this.Iy[0][i] = -(this.gapA +(this.gapB*i));
		}
		for (int i = 1; i <= m; i++) {	// base col
			this.M[i][0] = this.minInf;
			this.Ix[i][0] = -(this.gapA +(this.gapB*i));
			this.Iy[i][0] = this.minInf;
		}
		// filling matrix
		for (int i = 1; i <= m; i++) {		// rows S2[i]
			for (int j = 1; j <= n; j++) {	// cols S1[j]
				// Calculating M[i][j]
				if (this.M[i-1][j-1] > this.Ix[i-1][j-1]) {
					if (this.M[i-1][j-1] > this.Iy[i-1][j-1]) {								// M > Ix & Iy
						this.M[i][j] = this.M[i-1][j-1] + this.score[this.s1[i]][this.s2[j]];
					} else {																// Iy > M > Ix
						this.M[i][j] = this.Iy[i-1][j-1] + this.score[this.s1[i]][this.s2[j]];
					}
				} else if (this.Ix[i-1][j-1] > this.Iy[i-1][j-1]) {							// Ix > M & Iy
						this.M[i][j] = this.Ix[i-1][j-1] + this.score[this.s1[i]][this.s2[j]];
				} else {																	// Iy > Ix > M
						this.M[i][j] = this.Iy[i-1][j-1] + this.score[this.s1[i]][this.s2[j]];
				}
				
				// Calculating Ix[i][j]
				if (this.M[i-1][j] - this.gapA > this.Ix[i-1][j]) {
					this.Ix[i][j] =  this.M[i-1][j] - (this.gapA + this.gapB);
				} else {
					this.Ix[i][j] =  this.Ix[i-1][j] - this.gapB;
				}
			}
		}
		if (this.M[m][n] > this.Ix[m][n]) {
			if (this.M[m][n] > this.Iy[m][n]) {
				this.finalScore = this.M[m][n];
			} else {
				this.finalScore = this.Iy[m][n];
			}
		} else if (this.Ix[m][n] > this.Iy[m][n]) {
				this.finalScore = this.Ix[m][n];
		} else {
				this.finalScore = this.Iy[m][n];
		}
		
		//trace back
		globalAffineTraceBack(m,n);
	}
	
	// TODO write this thing
	public void globalGapAlignment() {
		System.out.println("Running Global allignment with gaps");
		globalAlignment();
	}
	
	
	// TODO FIX THAT SHIT
	public void globalAffineTraceBack(int row, int col) {
//		System.out.println("targetOutput= " + this.targetOutput);
//		System.out.println("queryOutput=  " + this.queryOutput);
		if ((row==0) & (col==0)) return;
//		System.out.println(this.M[row][col][1]);
//		System.out.println("row="+row + " col="+col );
		switch (this.pointers[row][col]) {
		case 0 :		// Match / Replace
			this.targetOutput = this.target.charAt(col-1) + this.targetOutput ;
			this.queryOutput = this.query.charAt(row-1) + this.queryOutput ;
			globalTraceBack(row-1, col-1);
			break;
		case 1 :		// Insertion
			this.targetOutput = this.target.charAt(col-1) + this.targetOutput ;
			this.queryOutput = "_" + this.queryOutput ;
			globalTraceBack(row, col-1);
			break;
		case 2 :		// Deletion
			this.targetOutput = "_" + this.targetOutput ;
			this.queryOutput = this.query.charAt(row-1) + this.queryOutput ;
			globalTraceBack(row-1, col);
			break;
		}
	}
	
	public void globalTraceBack(int row, int col) {
//		System.out.println("targetOutput= " + this.targetOutput);
//		System.out.println("queryOutput=  " + this.queryOutput);
		if ((row==0) & (col==0)) return;
//		System.out.println(this.M[row][col][1]);
//		System.out.println("row="+row + " col="+col );
		switch (this.pointers[row][col]) {
			case 0 :		// Match / Replace
				this.targetOutput = this.target.charAt(col-1) + this.targetOutput ;
				this.queryOutput = this.query.charAt(row-1) + this.queryOutput ;
				globalTraceBack(row-1, col-1);
				break;
			case 1 :		// Insertion
				this.targetOutput = this.target.charAt(col-1) + this.targetOutput ;
				this.queryOutput = "_" + this.queryOutput ;
				globalTraceBack(row, col-1);
				break;
			case 2 :		// Deletion
				this.targetOutput = "_" + this.targetOutput ;
				this.queryOutput = this.query.charAt(row-1) + this.queryOutput ;
				globalTraceBack(row-1, col);
				break;
		}
	}
	
	public void printGlobalResults() {
		System.out.println("Output:");
		System.out.println(this.targetOutput);
		System.out.println(this.queryOutput);
		System.out.println("Score: " + this.finalScore);
	}
	
	////////////////////////////////////
	// Local Alignment
	////////////////////////////////////
	
	public void runLocalAlignment(boolean gap, boolean affine) {
		initAll();
		if (affine) localAffineAlignment();
		else if (gap)  localGapAlignment();
		else localAlignment();
		printMat();
		printLocalResults();
	}
	
	public void localAlignment() {
		int maxC=0;
		int maxR=0;
		this.finalScore = 0;
		this.M = new int[m+1][n+1];	// creating matrix
		this.pointers = new byte[m+1][n+1];
		// initializing matrix
		this.M[0][0] = 0;
		for (int i = 1; i <= n; i++) {	// base row
			this.M[0][i] = 0;
			this.pointers[0][i] = 1;
		}
		for (int i = 1; i <= m; i++) {	// base col
			this.M[i][0] = 0;
			this.pointers[i][0] = 2;
		}
		// filling matrix
		int res1,res2,res3;
		for (int i = 1; i <= m; i++) {		// rows S2[i]
			for (int j = 1; j <= n; j++) {	// cols S1[j]
				res1 = this.M[i-1][j-1] + this.score[this.s1[i]][this.s2[j]];	// M\R
				res2 = this.M[i-1][j] + this.score[this.s1[i]][6];				// D
				res3 = this.M[i][j-1] + this.score[6][this.s2[j]];				// I
//				System.out.println("["+i+"]["+j+"]");
//				System.out.println(res1 + " " + res2 + " " + res3);
				if (res1>res2) {
					this.M[i][j] = res1;
					this.pointers[i][j] = 0;
				} else {
					this.M[i][j] = res2;
					this.pointers[i][j] = 2;
				}
				if (res3 > this.M[i][j]) {
					this.M[i][j] = res3;
					this.pointers[i][j] = 1;
				}
				if (0 > this.M[i][j]) {
					this.M[i][j] = 0;
				}
				if (this.M[i][j] > this.finalScore) {
					this.finalScore = this.M[i][j];
					maxC = j;
					maxR = i;
				}
			}
		}
		//trace back
		localTraceBack(maxR,maxC);
	}
	
	// TODO do this thing
	public void localAffineAlignment() {
		System.out.println("Running Local allignment with affine gap");
		localAlignment();
	}
	
	// TODO do this thing
	public void localGapAlignment() {
		System.out.println("Running Local allignment with gaps");
		localAlignment();
	}
	
	public void localTraceBack(int row, int col) {
//		System.out.println("targetOutput= " + this.targetOutput);
//		System.out.println("queryOutput=  " + this.queryOutput);
		if (this.M[row][col]==0) return;
//		System.out.println(this.M[row][col][1]);
//		System.out.println("row="+row + " col="+col );
		switch (this.pointers[row][col]) {
		case 0 :		// Match / Replace
			this.targetOutput = this.target.charAt(col-1) + this.targetOutput ;
			this.queryOutput = this.query.charAt(row-1) + this.queryOutput ;
			localTraceBack(row-1, col-1);
			break;
		case 1 :		// Insertion
			this.targetOutput = this.target.charAt(col-1) + this.targetOutput ;
			this.queryOutput = "_" + this.queryOutput ;
			localTraceBack(row, col-1);
			break;
		case 2 :		// Deletion
			this.targetOutput = "_" + this.targetOutput ;
			this.queryOutput = this.query.charAt(row-1) + this.queryOutput ;
			localTraceBack(row-1, col);
			break;
		}
	}
	
	public void printLocalResults() {
		System.out.println("Output:");
		System.out.println(this.targetOutput);
		System.out.println(this.queryOutput);
		System.out.println("Score: " + this.finalScore);
	}
	

	////////////////////////////////////
	// Fun Stuff
	////////////////////////////////////
	
	public void printMat() { 
		if (this.M==null) System.out.println("no matrix to print");
		for (int i = 0; i <= m; i++) {
			for (int j = 0; j <= n; j++) {
				System.out.print(this.M[i][j] +"("+this.pointers[i][j]+")"+ " , ");
			}
			System.out.println();
		}
	}
	
	public void printScoreMat() {
		for (int i = 0; i < 7; i++) {
			for (int j = 0; j < 7; j++) {
				System.out.print(this.score[i][j] + " | ");
			}
			System.out.println("");
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Main started");
		String scoreFile = "";
		boolean global = false;
		boolean local = false;
		boolean affine = false;
		boolean gap = false;
		for (int i = 0; i < args.length; i++) {
			if (args[i].startsWith("-")) {
				switch (args[i]) {
				case "-g" : global = true; break;
				case "-l" : local = true; break;
				case "-a" : affine = true; break;
				case "-p" : gap = true; break;
				default : System.out.println("wrong flag"); break;
				}
			} else if (args[i].endsWith("matrix")) {
				scoreFile = args[i];
			}
		}
		if (scoreFile.length()==0) scoreFile = args[args.length-3];
		Alignments al = new Alignments(args[args.length-2] ,args[args.length-1] , scoreFile);
		if (global) {
			al.runGlobalAlignment(gap, affine);
		} else if (local) {
			al.runLocalAlignment(gap, affine);
		} else {
			System.out.println("Global or Local alignment flag was not provided");
		}
//		al.runGlobalAlignment();
//		al.runLocalAlignment();
//		al.readScoreMat();
//		al.printScoreMat();
	}
	
	
	

}
