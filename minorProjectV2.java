package minorproject;

import java.util.ArrayList;
import java.io.IOException;
import java.util.Timer;

import org.firmata4j.IODevice;
import org.firmata4j.I2CDevice;
import org.firmata4j.firmata.FirmataDevice;
import org.firmata4j.Pin;
import org.firmata4j.ssd1306.SSD1306;
public class minorProjectV2 {
    public static void main(String[] args) throws InterruptedException, IOException{

        // Create IODevice arduinoObject on port COM3
        IODevice arduinoObject = new FirmataDevice("COM3");

        // Create Timer
        Timer timer = new Timer();

        // Create Array List
        ArrayList<Long> analogVal = new ArrayList<>();

        try {

            // Initialize the Arduino board
            arduinoObject.start();
            arduinoObject.ensureInitializationIsDone();

            // Initialize the Arduino microcontrollers

                // OLED Display
            I2CDevice i2cObject = arduinoObject.getI2CDevice((byte) 0x3C);
            SSD1306 display = new SSD1306(i2cObject, SSD1306.Size.SSD1306_128_64);
            display.init();

                // MOSFET Switch & Pump
            var pumpSwitch = arduinoObject.getPin(2);
            pumpSwitch.setMode(Pin.Mode.OUTPUT);

                // LED Indicator
            var ledLight = arduinoObject.getPin(4);
            ledLight.setMode(Pin.Mode.OUTPUT);

                // Moisture Sensor
            var soilSensor = arduinoObject.getPin(15);

                // Potentiometer
            var potMeter = arduinoObject.getPin(17);
            potMeter.setMode(Pin.Mode.ANALOG);

            // Create listener task
            var task = new sensorListenerv2(display, pumpSwitch, ledLight, soilSensor, analogVal, potMeter);

            // Run task on a timer interval of every half second.

            timer.schedule(task, 0, 500);

        } catch (IOException | IllegalArgumentException | InterruptedException e) {
        }

    }
}
