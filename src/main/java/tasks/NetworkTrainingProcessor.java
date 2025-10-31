package tasks;

import datamodels.TrainingSet;
import deeplearning.DeepLayer;

public class NetworkTrainingProcessor extends TaskProcessor {


    public NetworkTrainingProcessor(StringBuilder sb) {
        super(sb, "NetworkTrainingProcessor");
    }


    public void runTask(int neurons, int iterations, String tsFile, String netFile) {

        TrainingSet ts = new TrainingSet(tsFile,"2018-01-01","2023-12-31");
        int vectorSize = ts.tsSize;

        DeepLayer network = new DeepLayer( netFile, vectorSize, neurons )
                .prepareDataSet( ts.buySignal, ts.sellSignal, ts.tsTensor)
                .setLearningRate(0.02)
                .train(iterations)
                .evaluate()
                .save( netFile );


    }
}
