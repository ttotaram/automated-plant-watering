package minorproject;

import org.firmata4j.IODevice;
import org.firmata4j.IODeviceEventListener;
import org.firmata4j.IOEvent;
import org.firmata4j.Pin;
import org.firmata4j.ssd1306.SSD1306;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.util.TimerTask;
import java.io.IOException;
import java.util.ArrayList;

public class sensorListenerv2 extends TimerTask implements IODeviceEventListener {
    private final SSD1306 display;
    private final Pin pumpSwitch;
    private final Pin ledLight;
    private final Pin soilSensor;
    private final Pin potMeter;
    private final ArrayList<Long> analogVal;

    sensorListenerv2(SSD1306 display, Pin pumpSwitch, Pin ledLight, Pin soilSensor, ArrayList<Long> analogVal, Pin potMeter) {
        this.soilSensor = soilSensor;
        this.pumpSwitch = pumpSwitch;
        this.ledLight = ledLight;
        this.display = display;
        this.analogVal = analogVal;
        this.potMeter = potMeter;
    }

    @Override
    public void run() {
        // Constant variable so while loop never ends.
        long meterVal = potMeter.getValue();

        try {

            XYSeriesCollection moistValCollection = new XYSeriesCollection();
            XYSeries series = new XYSeries("Moisture Values");
            moistValCollection.addSeries(series);

            String title = "Moisture Values [V] vs. Time [s]";
            String xAxisLabel = "Time [s]";
            String yAxisLabel = "Moisture Value [V]";

            JFreeChart chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, moistValCollection);

            XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
            render.setSeriesPaint(0, Color.BLUE);
            render.setSeriesShapesVisible(0, true);
            render.setSeriesLinesVisible(0, true);
            chart.getXYPlot().setRenderer(render);

            ChartFrame frame = new ChartFrame("Self-Watering Plant Graph", chart);
            frame.pack();
            frame.setVisible(true);

            if (meterVal == 1023) {
                display.clear();
                Thread.sleep(1000);

                display.getCanvas().drawString(0, 0, ("SWITCHED OFF \nPlease turn p.meter \nto the RIGHT \nto start."));
                display.display();

                Thread.sleep(2000);
                display.clear();
            } else {
                display.clear();
            }

            while (meterVal < 1023) {
                // grab values
                long readVoltage = soilSensor.getValue();
                long currentTime = System.currentTimeMillis();
                int i = 0;

                // read voltage and add to analogVal
                analogVal.add(readVoltage);
                System.out.println(readVoltage);

                // update graph
                frame.setVisible(true);
                series.add(currentTime, analogVal.size());

                    if (readVoltage <= 800 && readVoltage >= 650) {

                        // Print to the System that a signal was recieved.
                        System.out.println("Signal Recieved. Dry Soil Detected.");
                        System.out.println("Watering...");

                        // Write to the OLED that the pump is turned on and that the soil is very dry.
                        display.getCanvas().setCursor(25, 0);
                        display.getCanvas().drawString(0, 0, ("PUMP ON \n\nMoisture Level =" + readVoltage + "\nSoil is dry!"));
                        display.display();

                        // Turn on the LED light on to show that the pump is on
                        ledLight.setValue(1);

                        // Turn on water pump
                        pumpSwitch.setValue(1);

                        // Get a new potValue
                        meterVal = potMeter.getValue();

                    } else {

                        // Print to the System that a signal was received.
                        System.out.println("Signal Received. Wet Soil Detected.");
                        System.out.println("There is an adequate amount of water. Soil will not be watered further.");

                        // Write to the OLED that the pump is on standby and that the soil is very wet.
                        display.getCanvas().setCursor(25, 0);
                        display.getCanvas().drawString(0, 0, ("STANDBY \n\n" + "Moisture Level: " + readVoltage + "\nSoil is wet!"));
                        display.display();

                        // Turn on the LED light off to show that the pump is not active
                        ledLight.setValue(0);

                        // Turn on water pump
                        pumpSwitch.setValue(0);
                        Thread.sleep(1000);

                        // Get a new potValue
                        meterVal = potMeter.getValue();

                    }
                }
            } catch(IOException | InterruptedException e) {
            e.printStackTrace();
        }
        }



    @Override
    public void onStart(IOEvent ioEvent) {

    }

    @Override
    public void onStop(IOEvent ioEvent) {

    }

    @Override
    public void onPinChange(IOEvent ioEvent) {

    }

    @Override
    public void onMessageReceive(IOEvent ioEvent, String s) {

    }
}
