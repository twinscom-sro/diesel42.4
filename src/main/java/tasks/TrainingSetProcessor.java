package tasks;

import datamodels.JSON;
import datamodels.TrainingSet;

public class TrainingSetProcessor extends TaskProcessor {

    public TrainingSetProcessor(StringBuilder sb) {
        super(sb,"TrainingSetProcessor");
    }


    static String[] KPI_VECTOR ={"closeMA200xo","closeMA50xo","cmf",
            "macd","macdSignal","atrPct","mfi","pvo","obv","willR",
            "kcLPct","kcMPct","kcUPct","macdv","macdvSignal","mPhase","mDir" };

    public JSON runTask(String tickerId, String kpiFile, String tsFile ) {
//-----------------------------------------------------------------------------------------------------------------
        System.out.println("TrainingSetProcessor.runTask - STARTING");

        TrainingSet trainingSet = new TrainingSet();

        trainingSet.loadData(kpiFile, KPI_VECTOR, 2006 );
        trainingSet.prepareTrainingSet( 20,5);
        trainingSet.save( tsFile );

        return (JSON.DATA(null));
    }

}
