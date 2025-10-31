package tasks;

import datamodels.Forecast;
import datamodels.TrainingSet;
import deeplearning.DeepLayer;
import math.PlotSeries;

public class ForecastProcessor extends TaskProcessor {

    Forecast fcst;

    public ForecastProcessor(StringBuilder sb) {
        super(sb, "ForecastProcessor");
    }


    public void runTask(  String netFile, String tsFile, String dtFrom, String dtTo ){

        TrainingSet ts = new TrainingSet(tsFile,dtFrom, dtTo);

        DeepLayer network = new DeepLayer( netFile )
                .prepareDataSet( ts.buySignal, ts.sellSignal, ts.tsTensor);
                //.evaluate();
        fcst = network.evaluateResults( ts, ts.tsTensor);

    }

    public void runTaskWithKnockouts(  String netFile, String tsFile, String dtFrom, String dtTo ){

        TrainingSet ts = new TrainingSet(tsFile,dtFrom, dtTo);

        DeepLayer network = new DeepLayer( netFile );

        for( int mode=0; mode<(5+17); mode++ ){
            double[][] inputs =  knockOut1(ts.tsTensor,mode);
            network.prepareDataSet( ts.buySignal, ts.sellSignal, inputs );
            fcst = network.evaluateResults( ts, inputs );
            System.out.format("%s,%d,%.2f,%.2f,%.2f,%.2f\n",
                    netFile.substring(14),
                    mode,
                    fcst.recall1, fcst.precision1, fcst.recall2, fcst.precision2 );

        }

    }

    /*tsSize = lengthPriceSignature( numDays )                // 20*5 = 100
                + lengthVolumeSignature( numDays )              // 20
                + lengthKpiSignature( history, kpiVectorSize )  // 5*17 = 85
                + lengthCalendarSignature();                    // 4
    // total = 209*/

    private double[][] knockOut(double[][] tsVector, int mode) {
        double[][] y= new double[tsVector.length][tsVector[0].length];
        for( int i=0; i<tsVector.length; i++ ){
            for( int j=0; j<tsVector[i].length; j++ ){
                y[i][j] = 0;
                if( mode==0 ){
                    y[i][j] = tsVector[i][j];;
                }else if( mode==1 ){
                    if( j<100 ) y[i][j] = tsVector[i][j];

                }else if( mode==2 ){
                    if( j>=100 && j<120 ) y[i][j] = tsVector[i][j];

                }else if( mode==3 ){
                    if( j>=120 && j<205 ) y[i][j] = tsVector[i][j];

                }else if( mode==4 ){
                    if( j>=205 ) y[i][j] = tsVector[i][j];

                }
            }
        }
        return y;
    }

    private double[][] knockOut1(double[][] tsVector, int mode) {
        double[][] y= new double[tsVector.length][tsVector[0].length];
        for( int i=0; i<tsVector.length; i++ ){
            for( int j=0; j<tsVector[i].length; j++ ){
                y[i][j] = tsVector[i][j];
                if( mode==0 ){
                    y[i][j] = tsVector[i][j];;
                }else if( mode==1 ){
                    if( j<100 ) y[i][j] = 0;

                }else if( mode==2 ){
                    if( j>=100 && j<120 ) y[i][j] = 0;

                }else if( mode==3 ){
                    if( j>=120 && j<205 ) y[i][j] = 0;

                }else if( mode==4 ){
                    if( j>=205 ) y[i][j] =0;

                }else{
                    int ko = mode-4;
                    if( j==120+ko*5 ) y[i][j] = 0;
                    if( j==121+ko*5 ) y[i][j] = 0;
                    if( j==122+ko*5 ) y[i][j] = 0;
                    if( j==123+ko*5 ) y[i][j] = 0;
                    if( j==124+ko*5 ) y[i][j] = 0;
                }
            }
        }
        return y;
    }


    public Forecast getForecast() {
        return fcst;
    }

    public void drawChart(String title, Forecast forecast, String pngFile) {
        PlotSeries plot = new PlotSeries(6);
        fcst.normalizePrice();
        plot.addSeries(0, "price", fcst.price, 1);
        plot.addSeries(1, "price", fcst.price, -1);
        plot.addSeries(2, "b1", fcst.orig1, 1.25);
        plot.addSeries(3, "b2", fcst.act1, 1);
        plot.addSeries(4, "S1", fcst.orig2, -1.25);
        plot.addSeries(5, "S2", fcst.act2, -1);
        plot.plot(title, "price", pngFile);
    }
}
