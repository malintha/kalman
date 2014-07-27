package org.geo.cep.extension;

import es.prodevelop.gvsig.mini.utiles.Real;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import java.io.*;

/**
 * Created by malintha on 7/25/14.
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
    private int Matcounter = 0;
    private int Linecounter = 0;
    private RealVector x;
    private RealMatrix B;
    private RealMatrix Q;
    private RealMatrix P0;
    private RealMatrix R;
    private RealMatrix H;
    private RealMatrix A;
    private double dt;

    public MatrixSetter() {

    }

    /**
     * first call this method
     */
    public void readFile(){
        try {
            InputStream is = MatrixSetter.class.getResourceAsStream("kalmanConfig.csv");
            BufferedReader fbr = new BufferedReader(new InputStreamReader(is));
            String line = fbr.readLine();
            while(line!=null){
                Linecounter++;

                if(line.matches("(?s)")){
                    Matcounter++;
                    //System.out.println("# of rows of matrix "+Matcounter+" = "+(Linecounter-1));
                    Linecounter = 0;
                    //System.out.println();
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

    private void initializeMatrices(int matrixNumber, int rowNum, String line){
            double[] t = parseLine(line);
            doInitializeMatrices(matrixNumber, rowNum, t);
    }

    public void doInitializeMatrices(int matrixNumber, int rowNum, double[] t) {
//		got the double array and put it in the matrix
        switch (matrixNumber) {

            case 1:
                //dt
                if(rowNum == 2)
                dt = t[0];
                break;

            case 2:
                //get the length of x vector
                if(rowNum==1){
                    Number size = t[0];
                    x = new ArrayRealVector(size.intValue());
                }
                break;

            case 3:
                //A t.length^2
                if(rowNum==1){
                    Number r = t[0];
                    Number c = t[1];
                    A = new Array2DRowRealMatrix(r.intValue(),c.intValue());
                }
                else {
                    A.setRow(rowNum-2, t);
                }
                break;

            case 4:
                //B
                if(rowNum==1 && t[0]==0 && t[0]==0){
                    B = null;
                    break;
                }
                else if(rowNum == 1){
                    Number r = t[0];
                    Number c = t[1];
                    B = new Array2DRowRealMatrix(r.intValue(),c.intValue());
                }
                else if(rowNum>1 && B!=null) {
                    B.setRow(rowNum-2,t);
                }
                break;

            case 5:
                //Q
                if(rowNum==1 && t[0]==0 && t[0]==0){
                    Q = null;
                    break;
                }
                else if(rowNum == 1){
                    Number r = t[0];
                    Number c = t[1];
                    Q = new Array2DRowRealMatrix(r.intValue(),c.intValue());
                }
                else if(rowNum>1 && Q!=null) {
                    Q.setRow(rowNum-2,t);
                }
                break;

            case 6:
                //R
                if(rowNum==1 && t[0]==0 && t[0]==0){
                    R = null;
                    break;
                }
                else if(rowNum == 1){
                    Number r = t[0];
                    Number c = t[1];
                    R = new Array2DRowRealMatrix(r.intValue(),c.intValue());
                }
                else if(rowNum>1 && R!=null) {
                    R.setRow(rowNum - 2, t);
                }
                break;

            case 7:
                //H
                if(rowNum==1 && t[0]==0 && t[0]==0){
                    H = null;
                    break;
                }
                else if(rowNum == 1){
                    Number r = t[0];
                    Number c = t[1];
                    H = new Array2DRowRealMatrix(r.intValue(),c.intValue());
                }
                else if(rowNum>1 && H!=null) {
                    H.setRow(rowNum - 2, t);
                }
                break;

            case 8:
                //P0
                if(rowNum==1 && t[0]==0 && t[0]==0){
                    P0 = null;
                    break;
                }
                else if(rowNum == 1){
                    Number r = t[0];
                    Number c = t[1];
                    P0 = new Array2DRowRealMatrix(r.intValue(),c.intValue());
                }
                else if(rowNum>1 && H!=null) {
                    P0.setRow(rowNum - 2, t);
                }
                break;

            default :
                //no other cases
                break;
        }
    }
    public double[] parseLine(String line) {
        String[] data = line.split(",");
        double[] dataVal = new double[data.length];
        for(int i =0; i<data.length; i++){
            dataVal[i] = Double.parseDouble(data[i]);
        }
        return dataVal;
    }

    public double getdt(){
        return dt;
    }
    public RealVector getX() {
        return x;
    }
    public RealMatrix getA() {
        return A;
    }
    public RealMatrix getB() {
        return B;
    }
    public RealMatrix getQ() {
        return Q;
    }
    public RealMatrix getR() {
        return R;
    }
    public RealMatrix getH() {
        return H;
    }
    public RealMatrix getP0() {
        return P0;
    }
}
