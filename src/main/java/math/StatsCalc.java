package math;

public class StatsCalc {

    int cols;
    int num;
    double[] sum;

    public StatsCalc(int n) {
        cols = n;
        sum = zero(cols);
        num=0;
    }

    public void log(double... x) {
        for( int i=0; i<x.length; i++ ) {
            sum[i] += x[i];
        }
        num++;
    }

    public void printResults() {
        if(num>0){
            System.out.format( "buyPR=[ %.2f, %.2f ], sellPR=[ %.2f, %.2f ]\n",
                    sum[0]/num,sum[1]/num,sum[2]/num,sum[3]/num );
        }
    }

    private static double[] zero(int n) {
        double[] x = new double[n];
        for( int i=0; i < n; i++ ) x[i]=0;
        return x;
    }
}
