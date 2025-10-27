package deeplearning;

public class NeuralNetwork {

    String label;
    int neurons;
    MultiLayerConfiguration conf;
    MultiLayerNetwork network;


    public NeuralNetwork( String _label, int _config, int _neurons){
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
                        .nIn(VECTOR_SIZE)
                        .nOut(NEURONS)
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
    }

    void initialize(){
        network.init();
    }

}
