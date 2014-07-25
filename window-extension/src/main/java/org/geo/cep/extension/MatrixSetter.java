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
}
