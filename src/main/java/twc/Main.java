package twc;

import environment.Utilities;
import tasks.ForecastProcessor;
import tasks.NetworkTrainingProcessor;
import tasks.TrainingSetProcessor;
import tasks.VectorsProcessor;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    static final String MENU = """
    Main menu:
    0 - display help
    1 - load/update KPI files. usage: java -jar Diesel.jar 1 <kpi> <netw> <out> "tkr1,tkr2...."
    2 - train buy/sell networks. usage: java -jar Diesel.jar 2 <kpi> <netw> <out> "tkr1,tkr2...." <period-filter> <config> <mult> <iterations> <neurons>
    3 - backtest buy/sell networks. usage: java -jar Diesel.jar 3 <kpi> <netw> <out> <model> "tkr1,tkr2...." <period-filter> <config> <mult> 
    4 - optimize models. usage: java -jar Diesel.jar 4 <kpi> <netw> <out> <tkr> "model1,model2,...." <period-filter> <config> <mult> 
    5 - optimize models. usage: java -jar Diesel.jar 5 <kpi> <netw-set> <out> <tkr> <model> <period-filter> <config> <mult>, optimize based on a directory of models
    6 - backtest models. usage: java -jar Diesel.jar 6 <2=symbol> <3=buyModels> <4=sellModels> <5=buyThreshold> <6=sellThreshold> <7=config> <8=mult> <9=neurons>
    """;

    //debugging:
    static final int ITERATIONS = 1000;
    static final int NEURONS = 4096; //"1024";
    static final String KPIS = "c:/_db2/kpis/";
    static final String TS = "c:/_db2/ts/";
    static final String NETS = "c:/_db2/nets2/";
    static final String OUTS = "c:/_arcturus/2025-10-28/";
    static final String VECTOR = "cmf,obv,willR,atrPct,kcMPct,kcUPct,macdv,macdvSignal";
    static final String HISTORY = "3";

    static String[] dji30 = {"WMT","GS","MSFT","CAT","HD","UNH","V","SHW","AXP","JPM",
            "MCD","AMGN","IBM","TRV","AAPL","CRM","BA","AMZN","HON","JNJ","NVDA","MMM",
            "CVX","PG","DIS","MRK","CSCO","NKE","KO","VZ","COIN","NNBR","MPW", "MSTR", "GDXJ", "NVDA", "PLUG" };



    public static void main(String[] args) {

        String task="4"; //args[0];
        StringBuilder sb = new StringBuilder();
        String outFile = OUTS+"task_"+Utilities.getTimeTag()+".txt";

        if( task.contentEquals("1") ){
            for( String tickerId : dji30 ){
                VectorsProcessor vp = new VectorsProcessor(sb);
                String kpiFile = KPIS+tickerId+"_kpi.txt";
                vp.runTask(tickerId,kpiFile);
            }

        }else if( task.contentEquals("2") ) {
            for( String tickerId : dji30 ){
                TrainingSetProcessor tsp = new TrainingSetProcessor(sb);
                String kpiFile = KPIS+tickerId+"_kpi.txt";
                String tsFile = TS+tickerId+"_209.txt";
                tsp.runTask(tickerId,kpiFile,tsFile);
            }
           // TrainingProcessor tp = new TrainingProcessor(sb);
           // tp.runTask();

        }else if( task.contentEquals("3") ) {
            for( String tickerId : dji30 ){
                String tsFile = TS+tickerId+"_209.txt";
                String netFile = NETS+tickerId+"_209.txt";

                NetworkTrainingProcessor ntp = new NetworkTrainingProcessor(sb);
                ntp.runTask(NEURONS, ITERATIONS, tsFile, netFile);
            }

        }else if( task.contentEquals("4") ) {
            for( String tickerId : /*dji30*/ new String[]{"WMT"} ){
                String tsFile = TS+"MCD"+"_209.txt";
                String netFile = NETS+tickerId+"_209.txt";

                ForecastProcessor fp = new ForecastProcessor(sb);
                fp.runTask("Training chart", netFile, tsFile, "2018-01-01","2023-12-31");
                fp.runTask("Forecast Chart", netFile, tsFile, "2024-01-01","2025-12-31");
                //break;
            }

        }else{
            System.out.println( MENU );
        }

        Utilities.writeFile(outFile, sb );

    }
}