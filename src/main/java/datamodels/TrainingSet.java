package datamodels;

import environment.Utilities;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TrainingSet {

    public String[] dates;
    public double[] buySignal;
    public double[] sellSignal;
    public double[] zigZag;
    public double[] open,close,low,high,volume;
    public double[] priceOpen, priceClose, price;
    public int length;

    public String[] inputKPIs;
    public double[][] kpiVector;
    public int kpiVectorSize;

    public TrainingSet(){
        length=0;
    }

    public double[][] tsTensor;
    public int tsSize;

    public TrainingSet(String tsFile, String fromDate, String toDate) {
        int columns=0;
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(tsFile))) {
            String line;
            if( (line = br.readLine()) != null) {
               String[] fields = line.split(",");
               columns = fields.length;
            }
            while( (line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if( isBetween(fields[0], fromDate, toDate) ){
                    lines.add(line);
                }else{
                   // System.out.format("Skipping %s not between %s and %s\n", fields[0], fromDate, toDate);
                }
            }

        } catch (IOException ex) {
 //           throw new RuntimeException(ex);
            ex.printStackTrace();
        }

        length = lines.size();
        tsSize = columns - 7;
        //System.out.format("readin TS file of size [%d x %d]\n", length, tsSize);
        tsTensor = new double[length][tsSize];
        dates = new String[length];
        buySignal = new double[length];
        sellSignal = new double[length];
        zigZag = new double[length];
        price = new double[length];
        priceOpen = new double[length];
        priceClose = new double[length];

        for( int d=0; d<length; d++ ){
            String[] fields = lines.get(d).split(",");
            dates[d] = fields[0];
            buySignal[d] = Double.parseDouble(fields[1]);
            sellSignal[d] = Double.parseDouble(fields[2]);
            price[d] = Double.parseDouble(fields[3]);
            zigZag[d] = Double.parseDouble(fields[4]);
            priceOpen[d] = Double.parseDouble(fields[5]);
            priceClose[d] = Double.parseDouble(fields[6]);
            for( int k=0; k<tsSize; k++ ){
                tsTensor[d][k] = Double.parseDouble(fields[7+k]);
            }
        }
   }

    private boolean isBetween(String curDate, String fromDate, String toDate) {
        if(toDate==null){
            return (curDate.compareTo(fromDate) >= 0);
        }else if(fromDate==null){
            return (curDate.compareTo(toDate) <= 0);
        }else {
            return (curDate.compareTo(fromDate) >= 0 && curDate.compareTo(toDate) <= 0);
        }
    }

    public void loadData(String kpiFile, String[] _inputKPIs, int fromYear ) {
   /*
    All KPI columns:
    dates,open,high,low,close,volume,dividend,split,adjOpen,adjHigh,adjLow,adjClose,
    closeMA200,closeMA200xo,closeMA50,closeMA50xo,
    cmf,macd,macdSignal,atrDaily,atr,atrPct,mfi,pvo,obv,willR,
    kcLwr,kcMid,KcUpr,kcLPct,kcMPct,kcUPct,
    macdv,macdvSignal,mPhase,mDir,
    pf8,zigZag,pf15,buySignal8,sellSignal8,buySignal15,sellSignal15
     */
        inputKPIs = _inputKPIs;
        List<String> days = new ArrayList<>();
        List<Integer> inputColumns = new ArrayList<>();
        List<double[]> records = new ArrayList<>();
        List<Integer> priceColumns = new ArrayList<>();
        List<double[]> pricing = new ArrayList<>();
        String[] priceVector = {"volume","adjOpen", "adjHigh","adjLow","adjClose","open","close","buySignal8","sellSignal8","zigZag"};
        String line;

        // Use try-with-resources to ensure the BufferedReader is closed automatically
        try (BufferedReader br = new BufferedReader(new FileReader(kpiFile))) {

            int nLine=0;
            while ((line = br.readLine()) != null) {
                if( nLine++==0 ){
                    String[] values = line.split(","); //DEFAULT_DELIMITER + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    for( String kpi : inputKPIs ){
                        //System.out.println("Scanning columns for "+kpi);
                        int col=0;
                        for( String v : values ) {
                            if (kpi.contentEquals(v)) {
                                inputColumns.add(col);
                            }
                            col++;
                        }
                    }
                    //System.out.println( "Input columns vector = "+Arrays.toString(inputColumns.toArray()) );
                    for( String y : priceVector ){
                        int col=0;
                        for( String v : values ) {
                            if (y.contentEquals(v)) {
                                priceColumns.add(col);
                            }
                            col++;
                        }
                    }
                    kpiVectorSize = inputColumns.size();
                    //System.out.println( "Output columns vector = "+Arrays.toString(priceColumns.toArray()) );
                    if(kpiVectorSize ==0) return;
                    continue;
                }
                String[] values = line.split(","); //DEFAULT_DELIMITER + "(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                int year = Integer.parseInt(values[0].substring(0, 4));
                if( year>=fromYear ){
                    // Clean up any residual quotes from the split process
                    days.add( values[0] );
                    int pos = 0;
                    double[] v = new double[kpiVectorSize];
                    for (int i : inputColumns ) {
                        v[pos++] = Double.parseDouble( values[i] );
                    }
                    records.add(v);
                    double[] y = new double[priceColumns.size()];
                    pos=0;
                    for (int i : priceColumns ) {
                        y[pos++] = Double.parseDouble( values[i] );
                    }
                    pricing.add(y);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //System.out.println("records=" + records.size());
        //System.out.println("cols=" + records.get(0).length);
        length = records.size();
        kpiVector = new double[length][kpiVectorSize];
        dates = new String[length];
        buySignal = new double[length];
        sellSignal = new double[length];
        zigZag = new double[length];
        open = new double[length];
        high = new double[length];
        low = new double[length];
        close = new double[length];
        volume = new double[length];
        price = new double[length];
        priceOpen = new double[length];
        priceClose = new double[length];

        for( int k=0; k<length; k++ ) {
            dates[k] = days.get(k);
            for(int i = 0; i < kpiVectorSize; i++) {
                kpiVector[k][i] = records.get(k)[i];
            }
            volume[k] = pricing.get(k)[0];
            open[k] = pricing.get(k)[1];
            high[k] = pricing.get(k)[2];
            low[k] = pricing.get(k)[3];
            close[k] = pricing.get(k)[4];
            priceOpen[k] = pricing.get(k)[5];
            priceClose[k] = pricing.get(k)[6];
            price[k] = (open[k] + close[k]) / 2.0;
            buySignal[k] = pricing.get(k)[7];
            sellSignal[k] = pricing.get(k)[8];
            zigZag[k] = pricing.get(k)[9];

         //if( k%50==0) System.out.println( k + "["+dates[k]+"]-> " + Arrays.toString(inputVector[k]) );
        }

        // standardize values
        normalizeInputs("closeMA200xo",4.74292099,14.26543601);
        normalizeInputs("closeMA50xo",1.27341681,6.92262897);
        normalizeInputs("cmf",0.03265432,0.20682017);
        normalizeInputs("macd",0.52668275,3.23695993);
        normalizeInputs("macdSignal",0.51746509,3.04182862);
        normalizeInputs("atrDaily",3.88681522,6.09221636);
        normalizeInputs("atr",3.88247562,5.75621837);
        normalizeInputs("atrPct",2.3035259,3.42048145);
        normalizeInputs("mfi",52.80884259,76.37326945);
        normalizeInputs("pvo",-0.69947629,8.27405592);
        normalizeInputs("obv",3.74872051,24.64325854);
        normalizeInputs("willR",44.20454974,69.46943854);
        normalizeInputs("kcLPct",-4.90150646,7.7722257);
        normalizeInputs("kcMPct",0.30185095,1.48703185);
        normalizeInputs("kcUPct",4.29372565,7.92920582);
        normalizeInputs("macdv",19.9256551,82.23265115);
        normalizeInputs("macdvSignal",19.73303331,77.78983534);
        //normalizeInputs("mPhase",49.90514991,78.88413791);
        //normalizeInputs("mDir",0.52910053,100.03080758);
        normalizeInputs("mPhase",50,50);
        normalizeInputs("mDir",0,100);

     }

public void normalizeInputs(String kpi, double mu, double sigma) {
    for(int i = 0; i< kpiVectorSize; i++ ){
        if( inputKPIs[i].contentEquals(kpi) ) {
            for(int d = 0; d < kpiVector.length; d++) {
                kpiVector[d][i] = (kpiVector[d][i]-mu)/sigma;
            }
            break;
        }
    }
}


    public void prepareTrainingSet(int numDays, int history){
     /*   tsSize = lengthPriceSignature( numDays )                // 20*5 = 100
                + lengthVolumeSignature( numDays )              // 20
                + lengthKpiSignature( history, kpiVectorSize )  // 5*17 = 85
                + lengthCalendarSignature();                    // 4
                                                                // total = 209*/

        tsSize = lengthVolumeSignature( numDays )              // 10
                + lengthKpiSignature( history, kpiVectorSize )  // 10*8
                + lengthCalendarSignature();                    // 4
        // total = 94


        tsTensor = new double[length][tsSize];

        for( int d=0; d<length; d++ ) {

            double[] priceSignature = composePriceSignature( d, numDays );
            double[] volumeSignature = composeVolumeSignature( d, numDays );
            double[] kpiSignature = composeKpiSignature( d, history, kpiVectorSize);
            double[] calendarSignature = composeCalendarSignature( dates[d] );

            tsVectorPut( tsTensor[d], /*priceSignature*/null, volumeSignature, kpiSignature, calendarSignature );

        }

    }


    private void tsVectorPut(double[] y, double[] a, double[] b, double[] c, double[] d) {
        int k=0;
        if( a!=null ) for( int i=0; i<a.length; i++ ) y[k++] = a[i];
        if( b!=null ) for( int i=0; i<b.length; i++ ) y[k++] = b[i];
        if( c!=null ) for( int i=0; i<c.length; i++ ) y[k++] = c[i];
        if( d!=null ) for( int i=0; i<d.length; i++ ) y[k++] = d[i];
    }

    private int lengthPriceSignature(int numDays) {
        return numDays*5;
    }
    private double[] composePriceSignature(int d, int numDays) {
        double[] y = new double[lengthPriceSignature(numDays)];
        int O=0; int H=1; int L=2; int C=3;

        double[] pv1 = new double[4];
        double[] pv2 = new double[4];
        pv1[O] = open[d];
        pv1[H] = high[d];
        pv1[L] = low[d];
        pv1[C] = close[d];
        int k = 0;
        for( int i=1; i<=numDays; i++ ) {
            pv2[O] = d-i>=0 ? open[d-i] : 0;
            pv2[H] = d-i>=0 ? high[d-i] : 0;
            pv2[L] = d-i>=0 ? low[d-i] : 0;
            pv2[C] = d-i>=0 ? close[d-i] : 0;
            /* c2c-1, o2c-1, h2o, l2c, c2o */
            y[k++] = pv2[C]>0 ? pv1[C]/pv2[C] : 0;
            y[k++] = pv2[C]>0 ? pv1[O]/pv2[C] : 0;
            y[k++] = pv1[O]>0 ? (pv1[H]-pv1[O])/pv1[O] : 0;
            y[k++] = pv1[C]>0 ? (pv1[C]-pv1[L])/pv1[C] : 0;
            y[k++] = pv1[O]>0 ? (pv1[C]-pv1[O])/pv1[O] : 0;

            pv1[O] = pv2[O];
            pv1[H] = pv2[H];
            pv1[L] = pv2[L];
            pv1[C] = pv2[C];
        }

        return y;
    }


    private int lengthVolumeSignature(int numDays) {
        return numDays;
    }
    private double[] composeVolumeSignature(int d, int numDays) {
        double[] y = new double[lengthVolumeSignature(numDays)];
        double maxVolume = 0;
        for( int i=0; i<numDays; i++ ) {
            y[i] = d-i>=0 ? volume[d-i] : 0;
            if( y[i] > maxVolume ) maxVolume = y[i];
        }
        if( maxVolume > 0 ){
            for( int i=0; i<numDays; i++ ) {
                y[i] = y[i]/maxVolume;
            }
        }

        return y;
    }



    private int lengthCalendarSignature() {
        return 4;  /* dayOfWeek, dayOfMonth, month, quarter */
    }
    private double[] composeCalendarSignature(String dateString) {
        double[] y = new double[lengthCalendarSignature()];

        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        try {
            date = formatter.parse(dateString);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            date = null;
        }
        if( date != null ) {
            cal.setTime(date);
            y[0] = (cal.get(Calendar.DAY_OF_WEEK)+1)/7.0; /*dayOfWeek*/
            y[1] = (cal.get(Calendar.DAY_OF_MONTH)+1)/31.0; /*dayOfMonth*/
            y[2] = (cal.get(Calendar.MONTH)+1)/12.0; /*month*/
            y[3] = ((cal.get(Calendar.MONTH) % 3)+1)/3.0; /*month of quarter*/
        }else{
            y[0] = 0;
            y[1] = 0;
            y[2] = 0;
            y[3] = 0;
        }

        return y;
    }




    private int lengthKpiSignature(int history, int kpiVectorSize) {
        return history * kpiVectorSize;
    }
    private double[] composeKpiSignature(int d, int history, int kpiVectorSize) {
        double[] y = new double[ lengthKpiSignature(history, kpiVectorSize) ];

        int k=0;
        for( int i=0; i<kpiVectorSize; i++ ){
            for( int j=0; j<history; j++ ){
                y[k++] = d-j>=0 ? kpiVector[d-j][i] : 0;
            }
        }

        return y;
    }


    public void save(String tsFile) {
        System.out.println("Saving " + tsFile + " vectorSize="+tsSize);
        StringBuilder out = new StringBuilder();

        for( int d=0; d<length; d++ ) {
            out.append( String.format("%10s,%3.1f,%3.1f,%.2f,%.2f,%.2f,%.2f",
                    dates[d],buySignal[d],sellSignal[d],price[d],zigZag[d],priceOpen[d],priceClose[d]) );
            for( int j=0; j<tsSize; j++ ) {
                out.append( String.format(",%.6f", tsTensor[d][j] ) );
            }
            out.append( System.lineSeparator() );
        }

        Utilities.writeFile(tsFile, out);
    }
}
