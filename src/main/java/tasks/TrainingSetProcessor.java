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

    static String[] KPI_VECTOR2 ={"mfi","pvo","obv","kcLPct","kcUPct","macdv","mPhase","mDir" };

    public JSON runTask(String tickerId, String kpiFile, String tsFile ) {
//-----------------------------------------------------------------------------------------------------------------
        System.out.println("TrainingSetProcessor.runTask - STARTING");

        TrainingSet trainingSet = new TrainingSet();

        trainingSet.loadData(kpiFile, KPI_VECTOR2, 2006 );
        trainingSet.prepareTrainingSet( 10,10);
        trainingSet.save( tsFile );

        return (JSON.DATA(null));
    }

}
