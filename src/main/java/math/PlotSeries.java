package math;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class PlotSeries {

    String[] labels;
    XYSeries[] xySeries;

    public PlotSeries(int num) {
        labels = new String[num];
        xySeries = new XYSeries[num];
    }

    public void addSeries(int _index, String _label, double[] _y, double scaling){
        xySeries[_index] = new XYSeries( _label);
        for( int i = 0; i < _y.length; i++ ){
            xySeries[_index].add( i, _y[i]/scaling );
        }
    }

    public void writeAsPNG(JFreeChart chart, OutputStream out, int width, int height )
    {
        try
        {
            BufferedImage chartImage = chart.createBufferedImage( width, height, null);
            ImageIO.write( chartImage, "png", out );
        }
        catch (Exception e)
        {
        }
    }


    /*

    https://www.jfree.org/forum/viewtopic.php?t=17629
    public void writeAsPNG( JFreeChart chart, OutputStream out, int width, int height )
{
try
{
BufferedImage chartImage = chart.createBufferedImage( width, height, null);
ImageIO.write( chartImage, "png", out );
}
catch (Exception e)
{
LOG.error( e );
}
}
     */

    public void plot(String _title, String _yAxis, String filePath){
        XYSeriesCollection dataset = new XYSeriesCollection();

        for( int i = 0; i < xySeries.length; i++ ){
            dataset.addSeries(xySeries[i]);
        }

        JFreeChart chart = ChartFactory.createXYLineChart(
                _title,
                "Days",
                _yAxis,
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = chart.getXYPlot();
        // Example: Set different colors for each series
        plot.getRenderer().setSeriesPaint(0, Color.GRAY); // Series 1
        plot.getRenderer().setSeriesPaint(1, Color.GRAY);  // Series 2
        plot.getRenderer().setSeriesPaint(2, Color.LIGHT_GRAY);  // Series 2
        plot.getRenderer().setSeriesPaint(3, Color.GREEN);  // Series 2
        plot.getRenderer().setSeriesPaint(4, Color.LIGHT_GRAY);  // Series 2
        plot.getRenderer().setSeriesPaint(5, Color.RED);  // Series 2
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame frame = new JFrame("Plot results");
        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setVisible(true);

        if( filePath!=null && !filePath.equals("") ){
            try (FileOutputStream out = new FileOutputStream(filePath)) {
                writeAsPNG(chart, out, 2400, 1000 );
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}
