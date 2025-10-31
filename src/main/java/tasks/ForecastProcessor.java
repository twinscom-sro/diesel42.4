package tasks;

import datamodels.Forecast;
import datamodels.TrainingSet;
import deeplearning.DeepLayer;
import math.PlotSeries;

public class ForecastProcessor extends TaskProcessor {

    public ForecastProcessor(StringBuilder sb) {
        super(sb, "ForecastProcessor");
    }


    public void runTask( String title, String netFile, String tsFile, String dtFrom, String dtTo ){

        TrainingSet ts = new TrainingSet(tsFile,dtFrom, dtTo);

        DeepLayer network = new DeepLayer( netFile )
                .prepareDataSet( ts.buySignal, ts.sellSignal, ts.tsVector );
                //.evaluate();

        Forecast fcst = network.checkPrecision( title, ts );

        PlotSeries plot = new PlotSeries(6);
        fcst.normalizePrice();
        plot.addSeries(0, "price", fcst.price, 1);
        plot.addSeries(1, "price", fcst.price, -1);
        plot.addSeries(2, "b1", fcst.orig1, 1.25);
        plot.addSeries(3, "b2", fcst.act1, 1);
        plot.addSeries(4, "S1", fcst.orig2, -1.25);
        plot.addSeries(5, "S2", fcst.act2, -1);
        plot.plot(title, "price", null);

    }
}
