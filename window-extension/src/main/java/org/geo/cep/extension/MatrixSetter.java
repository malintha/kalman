package org.geo.cep.extension;

import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

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
    private final String FILE_PATH = "/home/malintha/Desktop/Kalman_project/Workspace/Kalman_testing/src/com/m8/kalman/kalmanConfig.csv";
    private int Matcounter = 0;
    private int Linecounter = 0;

    private RealVector x;
    private RealMatrix B;
    private RealMatrix Q;
    private RealMatrix P0;
    private RealMatrix R;
    private RealMatrix H;
    private RealMatrix A;

    public void testingKalman(){

        System.out.println("###Dooing Kalman");
        KalmanFIlterEx kf = new KalmanFIlterEx();
        //KalmanFIlter.doKalman(6.881243, 79.89043, 133.00, 20.000);
        kf.testEffingKalman();
        //KalmanFIlter.doCorrect();
        //System.out.println("\n##c##"+KalmanFIlter.getEstimation()[0]+", "+KalmanFIlter.getEstimation()[1]);
//
//
//        KalmanFIlter.setgpshm(6.880080, 79.89117, 179.00, 24.000, 1.1, 1.8);
//        //KalmanFIlter.doCorrect();
//        System.out.println("\n##c##"+KalmanFIlter.getEstimation()[0]+", "+KalmanFIlter.getEstimation()[1]);
    }

//    public static void main(String[] args) {
//       KalmanFIlter.doKalman(6.881243, 79.89043, 133.00, 20.000);
//
//        KalmanFIlter.setgpshm(6.880756, 79.89090, 139.00, 29.000, 1.4, 2.1);
//        KalmanFIlter.doCorrect();
//        System.out.println("\n##c##"+KalmanFIlter.getEstimation()[0]+", "+KalmanFIlter.getEstimation()[1]);
//
//
//        KalmanFIlter.setgpshm(6.880080, 79.89117, 179.00, 24.000, 1.1, 1.8);
//        KalmanFIlter.doCorrect();
//        System.out.println("\n##c##"+KalmanFIlter.getEstimation()[0]+", "+KalmanFIlter.getEstimation()[1]);
//
//    }
}
