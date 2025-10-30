package tasks;

import datamodels.StockDataSet;
import deeplearning.DeepLayer;
import environment.Utilities;
import java.util.ArrayList;
import java.util.List;

public class TrainingProcessor extends TaskProcessor {

    public StockDataSet stockData;
    public double[] avg;
    public double[] stdev;


    public TrainingProcessor(StringBuilder sb) {
        super(sb,"TrainingProcessor");
        stockData = new StockDataSet();
    }

    double[][] inputVector;
    int samples;
    int inputSize;

    public void loadDataSet(String dataFile, String[] filters, String[] _inputKPIs, int history) {
        stockData.loadDataSet(dataFile, filters, _inputKPIs, history);
        inputVector = stockData.inputVector;
        samples = stockData.samples;
        inputSize = stockData.inputSize;
    }


    public void calculateKPIStats() {
        if( inputVector==null ) return;

        double[] sumX = new double[inputSize];
        double[] sumX2 = new double[inputSize];
        double[] nX = new double[inputSize];

        for( int i=0; i<samples; i++ ){
            for( int j=0; j<inputSize; j++ ){
                sumX[j] += inputVector[i][j];
                sumX2[j] += Math.pow(inputVector[i][j],2);
                nX[j] += 1;
            }
        }

        avg = new double[inputSize];
        stdev = new double[inputSize];

        for( int i=0; i<inputSize; i++ ){
            avg[i] = nX[i]>0 ? sumX[i]/nX[i] : 0;
            stdev[i] = nX[i]>1 ? Math.sqrt( (sumX2[i]+Math.pow(sumX[i],2)/nX[i]) / (nX[i]-1) ) : 0;
            System.out.format("Standardizing column %d:  avg=%.4f, stdev=%.4f\n",i,avg[i],stdev[i]);
        }
    }


    public void writeTrainingSet(String tsFile) {
        StringBuilder sb = new StringBuilder();
        for( int i=0; i<samples; i++ ){
            sb.append( String.format("%10s, %10.2f, %10.2f, %3.1f, %3.1f",
                    stockData.dates[i], stockData.price[i], stockData.zigZag[i], stockData.buySignal[i], stockData.sellSignal[i] ) );
            for( double x : inputVector[i] ) {
                sb.append( String.format(", %.5f", x ) );
            }
            sb.append("\n");
        }
        Utilities.writeFile(tsFile, sb);
    }

    public void writePredictions(DeepLayer nn1, double[] buySignal, DeepLayer nn2, double[] sellSignal, String outFile) {

        StringBuilder sb = new StringBuilder();
        int TT1=0;
        int FF1=0;
        int TF1=0;
        int FT1=0;
        int TT2=0;
        int FF2=0;
        int TF2=0;
        int FT2=0;

        for( int i=0; i<samples; i++ ){
            double[] signal1 = nn1.feedForward(inputVector[i]);
            double[] signal2 = nn2.feedForward(inputVector[i]);
            sb.append( String.format("%10s, %10.2f, %10.2f, %3.1f, %3.1f, %3.1f, %3.1f",
                    stockData.dates[i], stockData.price[i], stockData.zigZag[i], buySignal[i], sellSignal[i], signal1[0], signal2[0] ) );
            sb.append("\n");

            boolean exp1 = signal1[0]>0.8;
            boolean act1 = buySignal[i]>0.8;
            if( exp1 && act1 ) TT1++;
            if( !exp1 && !act1 ) FF1++;
            if( !exp1 && act1 ) FT1++;
            if( exp1 && !act1 ) TF1++;

            boolean exp2 = signal2[0]>0.8;
            boolean act2 = sellSignal[i]>0.8;
            if( exp2 && act2 ) TT2++;
            if( !exp2 && !act2 ) FF2++;
            if( !exp2 && act2 ) FT2++;
            if( exp2 && !act2 ) TF2++;

        }
        double recall1 = (TT1+TF1)>0 ? TT1*100.0/(TT1+TF1) : 0;
        double precision1 = (TT1+FT1)>0 ? TT1*100.0/(TT1+FT1) : 0;
        double recall2 = (TT2+TF2)>0 ? TT2*100.0/(TT2+TF2) : 0;
        double precision2 = (TT2+FT2)>0 ? TT2*100.0/(TT2+FT2) : 0;
        String buf1 = String.format("\nbuy.side=[ %3d, %3d, %3d, %3d], precision.buy=%.3f, recall.buy=%.3f",TT1, TF1, FT1, FF1, precision1, recall1);
        System.out.println( buf1 );
        String buf2 = String.format("\nsell.side=[ %3d, %3d, %3d, %3d], precision.sell=%.3f, recall.sell=%.3f",TT2, TF2, FT2, FF2, precision2, recall2);
        System.out.println( buf2 );
        sb.append(buf1).append(buf2);
        Utilities.writeFile(outFile, sb);

        buySignalRecall = recall1;
        sellSignalRecall = recall2;
        buySignalPrecision = precision1;
        sellSignalPrecision = precision2;
    }

    public double buySignalRecall;
    public double sellSignalRecall;
    public double buySignalPrecision;
    public double sellSignalPrecision;

    public static void train(DeepLayer network, double[] y, double[][] x, double learningRate, int iterationsNum, String outFile){
        int tsSize = y.length;
        network.setLearningRate(learningRate);
        int milestone1 = iterationsNum/4;
        int milestone2 = iterationsNum/2;
        int milestone3 = (3*iterationsNum)/4;
        double[] entropy = new double[ iterationsNum/500 ];

        List<Integer> Signal1 = new ArrayList<Integer>();
        List<Integer> Signal0 = new ArrayList<Integer>();
        StringBuilder sb = new StringBuilder();

        for( int i=0; i< tsSize; i++ ) {
            if( y[i]>0.8 ) Signal1.add(i); else Signal0.add(i);
        }

        System.out.format("Training signal(1)=%d\n",Signal1.size());
        System.out.format("Training signal(0)=%d\n",Signal0.size());

        if(Signal1.isEmpty() || Signal0.isEmpty()) {
            System.out.println("Aborting...");
            return;
        }


            //write predictions
            for (int i = 0; i < x.length; i++) {
                double[] output = network.feedForward(x[i]);
                sb.append( String.format("%d,%.4f\n",i,output[0]));
            }

            Utilities.writeFile(outFile,sb);

    }

}
