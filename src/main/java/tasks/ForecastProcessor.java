package tasks;

import datamodels.TrainingSet;
import deeplearning.DeepLayer;

public class ForecastProcessor extends TaskProcessor {

    public ForecastProcessor(StringBuilder sb) {
        super(sb, "ForecastProcessor");
    }


    public void runTask(String netFile, String tsFile, String dtFrom, String dtTo) {

        TrainingSet ts = new TrainingSet(tsFile,dtFrom, dtTo);

        DeepLayer network = new DeepLayer( netFile )
                .prepareDataSet( ts.buySignal, ts.sellSignal, ts.tsVector )
                .evaluate();

    }
}
