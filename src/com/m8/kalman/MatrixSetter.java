package com.m8.kalman;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import javax.sound.sampled.Line;

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
					putLineInMatrix((Matcounter+1), Linecounter, line);
				}
				line = fbr.readLine();
			}
			
			
		} catch (FileNotFoundException e) {
			System.out.println("###"+e.getLocalizedMessage());
		} catch (IOException e) {
			System.out.println("###"+e.getLocalizedMessage());
		}

	}
	
	public static void putLineInMatrix(int matrixNumber, int rowNum, String line){
		if(line.equals("null")){
			System.out.println("initialized with null");
		}
		else {
		double[] t = parseLine(line);
		for(double d : t)
			System.out.print(d+" ");
		System.out.println();
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
