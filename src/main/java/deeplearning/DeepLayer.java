package deeplearning;

import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class DeepLayer {

    NeuralNetwork network;
    DataSet dataSet;


    public DeepLayer( int _vectorSize, int _neurons ){
        network = new NeuralNetwork( "DEFAULT-CONFIG", 1, _vectorSize, _neurons );
        dataSet = null;
    }

    public DeepLayer(String netFile) {
        network = new NeuralNetwork( netFile );
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
        System.out.format("Values: {0}=%d {1}=%d, {2}=%d\n", size-counts[1]-counts[2], counts[1], counts[2]);
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

        INDArray features = Nd4j.create(vector);
        return network.feedForward(features);
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
        try {
            network.network.save(new File(netFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return this;
    }
}
