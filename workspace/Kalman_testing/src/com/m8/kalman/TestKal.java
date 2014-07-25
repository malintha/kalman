package com.m8.kalman;

import java.io.ObjectInputStream.GetField;
import java.util.HashMap;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

public class TestKal {

	private HashMap gpshm = new HashMap();
	
	
	
	public static void main(String[] args) {
		double dt = 2d;
		RealMatrix A = new Array2DRowRealMatrix(new double[][] { 	{ 1d, dt, 0d, 0d }, 
																	{ 0d, 1d, 0d, 0d }, 
																	{ 0d, 0d, 1d, dt },
																	{ 0d, 0d, 0d, 1d } 
																});
		
		
		
		RealMatrix H = new Array2DRowRealMatrix(new double[][] { 	{ 1d, 0d, 0d, 0d }, 
																	{ 0d, 0d, 1d, 0d }
																});
		
		RealVector x = new ArrayRealVector(new double[] { 23.234d,1 , 12.343d, 1 });
		
		RealVector mNoise = new ArrayRealVector(2);
		mNoise.setEntry(0, 0.5 * 2);
		mNoise.setEntry(1, 0.5 * 4);
		
		//x = A*x
		x = A.operate(x);
		double[] da = {1d,2d};
		RealMatrix R = new Array2DRowRealMatrix(da);

		try{
			x.setEntry(0, 12.44d);
			x.setEntry(1, 13.44d);
			x.setEntry(2, 14.44d);
			x.setEntry(3, 15.44d);
		//R.setColumn(0, new double[] {4d,5d});
		//System.out.println(R.getEntry(0, 0)+"\n"+R.getEntry(1, 0));
			
		}
		catch(Exception e){
			System.out.println(e.getLocalizedMessage());
		}
		
		//z = H*x + mnoise
		RealVector z = H.operate(x).add(mNoise);

		
//		System.out.println(H.getColumnDimension()+","+H.getRowDimension());
//		for(int i=0;i<4;i++)
		//System.out.println(x);
//		System.out.println(z.getEntry(0)+"\n"+z.getEntry(1));
//		
//		System.out.println(x.getEntry(0)+"\n"+x.getEntry(1)+"\n"+x.getEntry(2)+"\n"+x.getEntry(3));
//		System.out.println(R.getColumnDimension()+","+R.getRowDimension());
		double[] dxy = KalmanF.dXY(310.1, 4.25);
		System.out.println(dxy[0]+","+dxy[1]);
		RealVector xt = new ArrayRealVector(new double[] { 2, 1, 3, 4 });
		RealMatrix Ht = new Array2DRowRealMatrix(new double[][]  {{ 1 },
																	{ 2 },
																	{ 3 },
																	{ 4 }});
		
		System.out.println(Ht.getRowDimension()+","+Ht.getColumnDimension()+"*"+xt.getDimension());
		RealVector zt = Ht.operate(xt);
		System.out.println(zt);
		
	}

}
