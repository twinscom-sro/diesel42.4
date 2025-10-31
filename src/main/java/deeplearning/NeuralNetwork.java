package deeplearning;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.learning.config.Nadam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;
import java.util.stream.IntStream;

public class NeuralNetwork {

    String label;
    int vectorSize;
    int neurons;
    MultiLayerConfiguration conf;
    MultiLayerNetwork network;

    public NeuralNetwork(String fileName) {
        try {
            //System.out.println("Loading Neural Network from " + fileName);
            network = MultiLayerNetwork.load(new File(fileName), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NeuralNetwork( String _label, int _config, int _vectorSize, int _neurons){
        vectorSize = _vectorSize;
        neurons = _neurons;
        label = _label;
        conf = new NeuralNetConfiguration.Builder()
                //.updater(new Sgd(0.1))
                //.seed(123)
                .weightInit(WeightInit.XAVIER) // Xavier weight initialization
                .activation(Activation.RELU) // Default activation for hidden layers
                //.biasInit(0.1) // init the bias with 0 - empirical value, too
                // The networks can process the input more quickly and more accurately by ingesting
                // minibatches 5-10 elements at a time in parallel.
                // This example runs better without, because the dataset is smaller than the mini batch size
                .miniBatch(false)
                .l2(0.02)
                .updater(new Nadam())
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(vectorSize)
                        .nOut(neurons)
                        //.activation(Activation.SIGMOID)
                        // random initialize weights with values between 0 and 1
                        //.weightInit(new UniformDistribution(-1, 1))
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nOut(3)
                        .activation(Activation.SOFTMAX)
                        //.weightInit(new UniformDistribution(-1, 1))
                        .build())
                .build();

        network = new MultiLayerNetwork(conf);
        network.init();
        // Print the number of parameters in the network (and for each layer)
        System.out.println(network.summary());
    }


    public INDArray feedForward(INDArray features) {
        INDArray y = network.output(features);
        return y;
    }

    public void train(DataSet ds, int iterations) {
        network.setListeners(new ScoreIterationListener(500));

        // here the actual learning takes place
        IntStream.range(0, iterations).mapToObj(i -> ds).forEach(network::fit);

       /* //evaluate model training performance
        INDArray output = network.output(ds.getFeatures());
        Evaluation eval = new Evaluation();
        eval.eval(ds.getLabels(), output);
        System.out.println(eval.stats());*/

    }

    public void evaluate(DataSet ds) {
        evaluate(ds.getLabels(), ds.getFeatures());
    }

    public void evaluate(INDArray labels, INDArray features) {

        //evaluate model training performance
        INDArray output = network.output(features);
        Evaluation eval = new Evaluation();
        eval.eval(labels, output);
        System.out.println(eval.stats());
        /*System.out.format("br=%.2f, bp=%.2f, sr=%.2f, sp=%.2f\n",
                eval.recall(1),eval.precision(1),
                eval.recall(2),eval.precision(2));*/

    }


    public void saveTopology(String fileName){
        try {
            network.save(new File(fileName));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
