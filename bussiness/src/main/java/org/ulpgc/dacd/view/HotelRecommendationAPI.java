package org.ulpgc.dacd.view;

import com.google.gson.Gson;
import org.ulpgc.dacd.control.commands.Command;
import org.ulpgc.dacd.control.DataMartConsultant;
import org.ulpgc.dacd.control.exceptions.DataMartConsultingException;
import org.ulpgc.dacd.view.model.HotelOffer;
import org.ulpgc.dacd.view.model.WeathersByLocations;
import spark.Request;
import spark.Response;
import org.ulpgc.dacd.view.model.Offer;

import java.util.List;
import java.util.Map;
import static spark.Spark.*;

public class HotelRecommendationAPI {
    private final DataMartConsultant dataMartConsultant;
    private final Map<String, Command> commands;

    public HotelRecommendationAPI(DataMartConsultant dataMartConsultant, Map<String, Command> commands) {
        this.dataMartConsultant = dataMartConsultant;
        this.commands = commands;
    }

    public void init() {
        staticFiles.location("/public");
        get("/locations", this::getLocations);
        get("/offer/:location/:date", this::getCheapestOffer);
        get("/cheapest-offers", this::getCheapestOffersByWeatherAndDate);
    }

    private String getLocations(Request request, Response response) throws DataMartConsultingException {
        response.type("application/json");
        String weatherType = request.queryParams("weather");
        String date = request.queryParams("date");
        WeathersByLocations locations = (WeathersByLocations) commands.get("getLocationsByWeatherAndDate").execute(List.of(weatherType, date));
        //Map<String, Weather> locations = dataMartConsultant.getLocationsByWeatherAndDate(weatherType, date);
        //System.out.println(locations);
        return new Gson().toJson(locations);
    }

    private String getCheapestOffer(Request request, Response response) throws DataMartConsultingException {
        response.type("application/json");
        String location = request.params(":location");
        String bookingDate = request.params(":date");
        HotelOffer cheapestOffer = (HotelOffer) commands.get("getCheapestOfferByLocationAndDate").execute(List.of(location, bookingDate));
        //HotelOffer cheapestOffer = dataMartConsultant.getCheapestOffer(location, bookingDate);
        return new Gson().toJson(cheapestOffer);
    }

    private String getCheapestOffersByWeatherAndDate(Request request, Response response) throws DataMartConsultingException {
        response.type("application/json");
        String weatherType = request.queryParams("weather");
        String date = request.queryParams("date");
        Offer cheapestOffers = (Offer) commands.get("getCheapestOffersByWeatherAndDate").execute(List.of(weatherType,date));
        //Offer cheapestOffers = dataMartConsultant.getCheapestOffersByWeatherAndDate(weatherType, date);
        return new Gson().toJson(cheapestOffers);
    }
}
