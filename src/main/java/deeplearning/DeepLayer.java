package deeplearning;

import datamodels.Forecast;
import datamodels.TrainingSet;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.IntStream;

public class DeepLayer {

    NeuralNetwork network;
    DataSet dataSet;
    String label;


    public DeepLayer( String ticker, int _vectorSize, int _neurons ){
        network = new NeuralNetwork( "DEFAULT-CONFIG", 1, _vectorSize, _neurons );
        dataSet = null;
        label = ticker;
    }

    public DeepLayer(String netFile) {
        network = new NeuralNetwork( netFile );
        label = netFile;
    }

    public DeepLayer prepareDataSet( double[] signal1, double[] signal2, double[][] inputs ){

        int size = inputs.length;
        if( size==0 || signal1.length<size || signal2.length<size || inputs[0].length==0  ){
            // abort - data does not fit
            System.out.format("ERROR: data vectors do not match:  signal1=%d, signal2=%d, input=%d\n", signal1.length, signal2.length, inputs.length);
            dataSet = null;
            return null;
        }

        int vectorSize = inputs[0].length;
        INDArray features = Nd4j.create(size, vectorSize);
        INDArray labels = Nd4j.create(size, 3);

        int[] counts = new int[3];
        //counts[0] = counts[1] = 0;
        for (int i = 0; i < size; i++) {
            features.putRow(i, Nd4j.create(inputs[i]));
            double[] y = new double[3];
            y[0] = 1;
            y[1] = 0;
            y[2] = 0;
            if (signal1[i] > 0.5) {
                y[0] = 0;
                y[1] = 1;
                counts[1]++;
            }
            if (signal2[i] > 0.5) {
                y[0] = 0;
                y[2] = 1;
                counts[2]++;
            }
            labels.putRow(i, Nd4j.create(y));
        }
        //System.out.format("Values: {0}=%d {1}=%d, {2}=%d\n", size-counts[1]-counts[2], counts[1], counts[2]);
        /*for( int i=0; i<labels.rows(); i++ ){
            INDArray x = labels.get(NDArrayIndex.point(i),NDArrayIndex.all());
            if( x.getDouble(1)>0.8 ){
                System.out.println(i+">"+x.getDouble(0)+" "+x.getDouble(1));
            }
        }*/

        dataSet = new DataSet(features, labels);
        //System.out.println(ds);

        return this;
    }


    public double[] feedForward(double[] vector) {
        INDArray features = Nd4j.create(1, vector.length);
        //System.out.println(Arrays.toString(features.shape()));
        INDArray row = Nd4j.create(vector);
        //System.out.println(Arrays.toString(features.shape()));
        features.putRow(0, row );
        INDArray output = network.feedForward(features);
        double[] y = output.toDoubleVector();
        return y;
    }


    public DeepLayer setLearningRate(double learningRate) {
        network.network.setLearningRate(learningRate);
        return this;
    }

    public DeepLayer train(int iterations) {
        network.train(dataSet,iterations);
        return this;
    }

    public DeepLayer evaluate() {
        network.evaluate(dataSet);
        return this;
    }

    public double calculateMSE(double[] output, double[] y) {
        return 0;
    }

    public DeepLayer save(String netFile) {
        network.saveTopology(netFile);
        return this;
    }

    public Forecast checkPrecision(String title, TrainingSet ts) {

        int buy_tt=0, buy_tf=0, buy_ft=0, buy_ff=0;
        int sell_tt=0, sell_tf=0, sell_ft=0, sell_ff=0;

        Forecast fcst = new Forecast(title, ts.tsVector.length);

        for( int d=0; d<ts.tsVector.length; d++){
            double[] y = feedForward(ts.tsVector[d]);

            fcst.orig1[d] = ts.buySignal[d];
            fcst.orig2[d] = ts.sellSignal[d];
            fcst.act1[d] = y[1];
            fcst.act2[d] = y[2];
            fcst.ref[d] = ts.dates[d];
            fcst.price[d] = ts.price[d];

            if( ts.buySignal[d]>0.5 && y[1]>0.5 ) buy_tt++;
            if( ts.buySignal[d]>0.5 && y[1]<=0.5 ) buy_tf++;
            if( ts.buySignal[d]<=0.5 && y[1]>0.5 ) buy_ft++;
            if( ts.buySignal[d]<=0.5 && y[1]<=0.5 ) buy_ff++;

            if( ts.sellSignal[d]>0.5 && y[2]>0.5 ) sell_tt++;
            if( ts.sellSignal[d]>0.5 && y[2]<=0.5 ) sell_tf++;
            if( ts.sellSignal[d]<=0.5 && y[2]>0.5 ) sell_ft++;
            if( ts.sellSignal[d]<=0.5 && y[2]<=0.5 ) sell_ff++;
        }

        double buyRecall = (buy_tt+buy_tf)>0 ? buy_tt * 100.0 / (buy_tt+buy_tf) : 0;
        double buyPrecision = (buy_tt+buy_ft)>0 ? buy_tt * 100.0 / (buy_tt+buy_ft) : 0;
        double sellRecall = (sell_tt+sell_tf)>0 ? sell_tt * 100.0 / (sell_tt+sell_tf) : 0;
        double sellPrecision = (sell_tt+sell_ft)>0 ? sell_tt * 100.0 / (sell_tt+sell_ft) : 0;
        //System.out.format("buy=[%d,%d,%d,%d], sell=[%d,%d,%d,%d]\n",buy_tt,buy_tf,buy_ft,buy_ff,sell_tt,sell_tf,sell_ft,sell_ff);
        System.out.format("model='%s', bR=%.2f, bP=%.2f, sR=%.2f, sP=%.2f\n",
                label, buyRecall, buyPrecision, sellRecall, sellPrecision );
        fcst.recall1 = buyRecall;
        fcst.precision1 = buyPrecision;
        fcst.recall2 = sellRecall;
        fcst.precision2 = sellPrecision;

        return fcst;
    }
}
