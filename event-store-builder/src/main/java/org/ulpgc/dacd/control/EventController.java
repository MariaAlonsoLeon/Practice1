package org.ulpgc.dacd.control;
import javax.jms.JMSException;
import java.util.ArrayList;

public class EventController {
    private final Listener listener;
    private final WeatherStore weatherStore;

    public EventController(Listener listener, WeatherStore weatherStore) {
        this.listener = listener;
        this.weatherStore = weatherStore;
    }

    public void execute() throws JMSException {
        ArrayList<String> weathers = this.listener.getWeather();
        System.out.println("Hola");
        if (weathers != null) {
            for (String weather : weathers) {
                weatherStore.save(weather);
            }
        }
    }
}