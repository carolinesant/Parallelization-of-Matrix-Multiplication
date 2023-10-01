import java.util.concurrent.CyclicBarrier;

public class Oblig2 {
    static int n,seed,p;

    public static void main(String[] args) {
        Oblig2 ob = new Oblig2();
		// Get seed and n value from the command line arguments
        seed = Integer.parseInt(args[0]);
        n = Integer.parseInt(args[1]);
        
		// Generate the matrixes
		double[][]a = Oblig2Precode.generateMatrixA(seed, n);
		double[][]b = Oblig2Precode.generateMatrixB(seed, n);
        
        // Calls all the different methods
		ob.classic(a, b);
        ob.transposeA(a, b);
        ob.transposeB(a, b);
	}

    //Calls both the classic sequential and parallel methods
    void classic(double[][] a, double[][] b) {
        
        //Calling the classic sequential algorithm and measuring the time it takes
        long start = System.currentTimeMillis();
        double[][] res = matrixMult(a, b,1);
        long end = System.currentTimeMillis();
        double timeSeq = end - start;
        System.out.println("Classic sequential algorithm time: " + (end - start) + "ms");
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.SEQ_NOT_TRANSPOSED, res);

        //Calling the classic parallel algorithm and measuring the time it takes
        start = System.currentTimeMillis();
        double[][] result = matrixMultPar(a,b,1);
        end = System.currentTimeMillis();
        double timePar = end - start;
        System.out.println("Classic parallel algorithm time: " + (end - start) + "ms");
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.PARA_NOT_TRANSPOSED, result);
        
        System.out.println("Are the result matrixes equal: " + areMatrixEqual(res, result));
        System.out.println("Seeduptime is: " + String.format("%.2f", timeSeq/timePar) + "\n");
    }

    //Calls both the transpose A sequential and parallel methods
    void transposeA(double[][] a, double[][] b) {
        
        //Calling the transpose A sequential algorithm and measuring the time it takes
        long start = System.currentTimeMillis();
        double[][] res = matrixMult(transposeMatrix(a), b,2);
        long end = System.currentTimeMillis();
        double timeSeq = end - start;
        System.out.println("Transposed A sequential algorithm time: " + (end - start) + "ms");
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.SEQ_A_TRANSPOSED, res);

        //Calling the transpose A parallel algorithm and measuring the time it takes
        start = System.currentTimeMillis();
        double[][] result = matrixMultPar(transposeMatrix(a), b,2);
        end = System.currentTimeMillis();
        double timePar = end - start;
        System.out.println("Transposed A parallel algorithm time: " + (end - start) + "ms");
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.PARA_A_TRANSPOSED, result);
        
        System.out.println("Are the result matrixes equal: " + areMatrixEqual(res, result));
        System.out.println("Seeduptime is: " + String.format("%.2f", timeSeq/timePar) + "\n");
    }
    
    //Calls both the transpose B sequential and parallel methods
    void transposeB(double[][] a, double[][] b) {

        //Calling the transpose B sequential algorithm and measuring the time it takes
        long start = System.currentTimeMillis();
        double[][] res = matrixMult(a, transposeMatrix(b),3);
        long end = System.currentTimeMillis();
        double timeSeq = end - start;
        System.out.println("Transposed B sequential algorithm time: " + (end - start) + "ms");
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.SEQ_B_TRANSPOSED, res);

        //Calling the transpose B parallel algorithm and measuring the time it takes
        start = System.currentTimeMillis();
        double[][] result = matrixMultPar(a, transposeMatrix(b),3);
        end = System.currentTimeMillis();
        double timePar = end - start;
        System.out.println("Transposed B paralell algorithm time: " + (end - start) + "ms");
        Oblig2Precode.saveResult(seed, Oblig2Precode.Mode.PARA_B_TRANSPOSED, result);
    
        System.out.println("Are the result matrixes equal: " + areMatrixEqual(res, result));
        System.out.println("Seeduptime is: " + String.format("%.2f", timeSeq/timePar) + "\n");
    }

    //Method for matrix multiplication, for the classic algorithm and for a and b transposed
    double[][] matrixMult(double[][] a, double[][] b, int mode) {
        double [][] c = new double[n][n];
        for(int i=0;i<n;i++)
			for(int j=0;j<n;j++)
				for(int k=0;k<n;k++) {
                    if (mode == 1) c[i][j] += a[i][k] * b[k][j]; //Normal matrix mult
                    else if (mode == 2) c[i][j] += a[k][i] * b[k][j]; //Matrix mult with a transposed
                    else c[i][j] += a[i][k] * b[j][k]; //Matrix with b transposed
                }	                   
                    
        return c;
    }

    //Method for parallel matrix multiplication, starts as many threads as cores and multiplies different parts of the matrix
    double[][] matrixMultPar(double[][]a, double[][] b, int mode) {
        p = Runtime.getRuntime().availableProcessors();
        CyclicBarrier cb = new CyclicBarrier(p+1);
        int interval = n / p;
        double[][] result = new double[n][n];

        for (int i = 0; i < p; i++) {
            int s = i * interval; int e = (i+1) * interval;
            
            if (i == p - 1) e = n - 1;
            (new Thread(new Multiplier(s, e, a, b, result,cb,mode))).start();
        }
        try {
            cb.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    //Transposes a matrix "norm" by changing rows to columns and columns to rows, returining the transposed matrix "trans" 
    double[][] transposeMatrix(double[][] norm) {
        double[][] trans = new double[n][n];
        int i, j;
        for (i = 0; i < n; i++)
            for (j = 0; j < n; j++)
                trans[i][j] = norm[j][i];
        return trans;
    }

    //Print method to print out the matrix
    void printMatrix(double[][] m) {
        for (int i = 0; i < n; i++) {
            System.out.println();
            for (int j = 0; j < n; j++){
                System.out.print(String.format("%.2f", m[i][j]) + "  ");
            }
        }
        System.out.println();
    }

    //Method to check if two matrices are equal
    boolean areMatrixEqual(double[][] m1, double[][] m2) {
        if (m1.length != m2.length) return false;

        for (int i = 0; i < m1.length; i++) {
            if (m1[i].length != m2[i].length) return false;
            for (int j = 0; j < m1[i].length; j++) {
                if (m1[i][j] != m2[i][j]) return false;
            }
        }
        return true;
    }

    /*     Thread class that gets a start end end index for the rows in the matrix, 
    so that the different threads handles different parts of the matrix muliplication */
    class Multiplier implements Runnable {
        int s,e,mode;
        double[][] a,b,res;
        CyclicBarrier cb;

        public Multiplier(int s, int e, double[][] a, double[][] b, double[][] res, CyclicBarrier cb, int mode) {
            this.s = s; this.e = e; this.mode = mode;
            this.a = a; this.b = b; this.res = res;
            this.cb = cb;
        }

        @Override
        public void run() {
            for (int i = s; i <= e; i++) {
                for (int j = 0; j < n; j++) {
                    double sum = 0;
                    for (int k = 0; k < n; k++) {
                        if (mode == 1) sum += a[i][k] * b[k][j]; //Matrix classic
                        else if (mode == 2) sum += a[k][i] * b[k][j]; //Matrix with a transposed
                        else sum += a[i][k] * b[j][k]; //Matrix with b transposed
                    }
                    res[i][j] = sum;
                }
            }
            try {
                cb.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
    }
}
