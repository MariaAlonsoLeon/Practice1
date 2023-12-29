package org.ulpgc.dacd.view;

import com.google.gson.Gson;
import org.ulpgc.dacd.control.LocationRecommendationService;
import spark.Request;
import spark.Response;

import java.util.Arrays;
import java.util.Set;

import static spark.Spark.*;

public class HotelRecommendationAPI {
    private final LocationRecommendationService locationRecommendationService;

    public HotelRecommendationAPI(LocationRecommendationService locationRecommendationService) {
        this.locationRecommendationService = locationRecommendationService;
    }

    public void init() {
        //Spark.debugDebug(true); // Activa el modo de depuración de Spark

        staticFiles.location("/public"); // Permite servir archivos estáticos desde la carpeta /public

        // Definir rutas
        get("/climas", (req, res) -> {
            try {
                return getTiposClima(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Error interno del servidor: " + e.getMessage();
            }
        });

        /*get("/localizaciones/:clima", (req, res) -> {
            try {
                return getLocalizaciones(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Error interno del servidor: " + e.getMessage();
            }
        });*/

        get("/localizaciones/:clima", (req, res) -> {
            try {
                return getLocalizaciones(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Error interno del servidor: " + e.getMessage();
            }
        });

        get("/oferta/:localizacion/:fecha", (req, res) -> {
            try {
                return getOfertaMasBarata(req, res);
            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                return "Error interno del servidor: " + e.getMessage();
            }
        });
    }

    private String getTiposClima(Request request, Response response) {
        response.type("application/json");
        return new Gson().toJson(Arrays.asList("COLD", "RAINY", "WARM", "SNOWY", "CLEAR"));
    }

    private String getLocalizaciones(Request request, Response response) {
        response.type("application/json");
        String tipoClima = request.params(":clima");
        System.out.println(tipoClima);
        Set<String> localizaciones = locationRecommendationService.obtenerLocalizacionesPorTipoClima(tipoClima);
        System.out.println(localizaciones);
        return new Gson().toJson(localizaciones);
    }

    private String getOfertaMasBarata(Request request, Response response) {
        response.type("application/json");
        String localizacion = request.params(":localizacion");
        String fechaReserva = request.params(":fecha");
        double tarifaMasBarata = locationRecommendationService.obtenerTarifaMasBarata(localizacion, fechaReserva);

        return new Gson().toJson(new Oferta(localizacion, fechaReserva, tarifaMasBarata));
    }
}