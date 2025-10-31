package datamodels;

public class Forecast {
    public String title;
    public double recall1, precision1, recall2, precision2;
    public double[] orig1;
    public double[] act1;
    public double[] orig2;
    public double[] act2;
    public double[] price;
    public String[] ref;

    public Forecast(String _title, int length){
        title = _title;
        orig1 = new double[length];
        act1 = new double[length];
        orig2 = new double[length];
        act2 = new double[length];
        price = new double[length];
        ref = new String[length];
    }

    public void normalizePrice(){
        double maxPrice = price[0];
        for(int i = 1; i < price.length; i++){
            if(price[i] > maxPrice){ maxPrice = price[i]; }
        }
        for(int i = 0; i < price.length; i++){
            price[i] = price[i]/maxPrice;
        }
    }

}
