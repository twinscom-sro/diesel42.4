package tasks;

import datamodels.JSON;
import datamodels.TradeData;
import environment.AlphavantageAPI;
import math.IndicatorsSet;

import java.util.List;

public class VectorsProcessor extends TaskProcessor {

    public VectorsProcessor(StringBuilder sb) {
        super(sb,"VectorsProcessor");
    }

    public JSON runTask(String tickerId, String fileName) {

//-----------------------------------------------------------------------------------------------------------------
        System.out.println("VectorsProcessor.runTask - STARTING");
        AlphavantageAPI apiConnect = new AlphavantageAPI();
        //System.out.println("Stage 1: Importing ticker prices for code="+tickerId);
        List<TradeData> dataseries = apiConnect.retrieveData(tickerId, true);
        int length = dataseries.size();
        System.out.println("Loaded records=" + length);
        if (length == 0) {
            return (JSON.ERROR("No data loaded"));
        }

        IndicatorsSet indicators = new IndicatorsSet(tickerId);
        indicators.recalculateOHLC(dataseries);
        indicators.recalculateStandardKPIs();
        indicators.writeDataToKPI(fileName);

        return (JSON.DATA(null));
    }

}
