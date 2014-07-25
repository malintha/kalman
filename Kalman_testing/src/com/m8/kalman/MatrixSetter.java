package com.m8.kalman;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;


/***
 * 
 * @author malintha
 * Matrices should be in this order and separated by \n (new line)
 * character. 
 * 
 * 1). dt
 * 2). X
 * 3). A
 * 4). B
 * 5). Q
 * 6). R
 * 7). H
 * 8). P0
 *  
 */

public class MatrixSetter {

	private final static String FILE_PATH = "/home/malintha/Desktop/Kalman_project/Workspace/Kalman_testing/src/com/m8/kalman/kalmanConfig.csv";
	private static int Matcounter = 0;
	private static int Linecounter = 0;
	
	private static RealVector x;
	private static RealMatrix B;
	private static RealMatrix Q;
	private static RealMatrix P0;
	private static RealMatrix R;
	private static RealMatrix H;
	private static RealMatrix A;
	
	
	public static void main(String[] args) {
		try {
			BufferedReader fbr = new BufferedReader(new FileReader(FILE_PATH));
			String line = fbr.readLine();
			while(line!=null){
				Linecounter++;
				
				if(line.matches("(?s)")){
					Matcounter++;
					//System.out.println("# of rows of matrix "+Matcounter+" = "+(Linecounter-1));
					Linecounter = 0;
					System.out.println();
				}
				
				else{
					initializeMatrices((Matcounter+1), Linecounter, line);
				}
				line = fbr.readLine();
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("###"+e.getLocalizedMessage());
		} catch (IOException e) {
			System.out.println("###"+e.getLocalizedMessage());
		}

	}
	
	/**
	 * this calls per each line with data
	 * 
	 * @param matrixNumber 		to which matrix the data comes
	 * @param rowNum 			to which row the double array needs to be set to
	 * @param line				what is current row number
	 */
	public static void initializeMatrices(int matrixNumber, int rowNum, String line){
		if(line.equals("null")){
			System.out.println("initialized with null");
			//get matrix number and initialize with null
		}
		else {
		double[] t = parseLine(line);
		doInitializeMatrices(matrixNumber, rowNum, t);
		}
	}
	
	public static void doInitializeMatrices(int matrixNumber, int rowNum, double[] t) {
//		got the double array and put it in the matrix
		
		switch (matrixNumber) {
		case 1:
			//dt is a double value
			break;
		case 2:
			//x, just initialize with 0 as length of t
			break;
		case 3:
			//A, t.length^2
			//set column double array
			break;
		case 4:
			//B, 
		
		case 5:
			//Q
			break;
		case 6:
			//R
			break;
		case 7:
			//H
			break;
		case 8:
			//P0
			break;
		default :
			//no other cases
			break;
	}
	}
	
	public static double[] parseLine(String line) {
		String[] data = line.split(",");
		double[] dataVal = new double[data.length];
		for(int i =0; i<data.length; i++){
			dataVal[i] = Double.parseDouble(data[i]);
		}
		return dataVal;
	}
}
