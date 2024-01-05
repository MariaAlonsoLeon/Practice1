package org.ulpgc.dacd.control;

import org.ulpgc.dacd.model.HotelOfferNode;
import org.ulpgc.dacd.model.WeatherNode;

import java.sql.*;
import java.time.Instant;

public class SqLiteDataMartStore implements DataMartStore {

    private final String dbPath;

    public SqLiteDataMartStore(String dbPath) {
        this.dbPath = dbPath;
    }

    // Métodos públicos de la clase
    public void saveHotelOffer(HotelOfferNode hotelDataEntry) {
        String tableName = "HotelOffers";
        try (Connection connection = connect(dbPath)) {
            Statement statement = connection.createStatement();
            createHotelTable(statement, tableName);
            updateOrInsertHotelData(hotelDataEntry, connection, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveWeather(WeatherNode weatherDataEntry) {
        String tableName = "Weathers";
        try (Connection connection = connect(dbPath)) {
            Statement statement = connection.createStatement();
            createWeatherTable(statement, tableName);
            updateOrInsertWeatherData(weatherDataEntry, connection, tableName);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        // Implementa el cierre si es necesario
    }

    // Métodos relacionados con la creación de tablas
    private static void createHotelTable(Statement statement, String tableName) throws SQLException {
        System.out.println("Tabla creada");
        statement.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "location TEXT," +
                "date TEXT," +
                "name TEXT," +
                "price REAL," +
                "PRIMARY KEY (location, date)" +
                ");");
    }

    private static void createWeatherTable(Statement statement, String tableName) throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "location TEXT," +
                "date TEXT," +
                "temperature REAL," +
                "rainProbability REAL," +
                "humidity INTEGER," +
                "windSpeed REAL," +
                "weatherType TEXT," +
                "PRIMARY KEY (location, date)" +
                ");");
    }

    public String buildTableName(String locationName) {
        String tableName = locationName.replaceAll("\\s", "_");
        return tableName;
    }

    // Métodos relacionados con la manipulación de datos de hoteles
    private static void updateOrInsertHotelData(HotelOfferNode hotelDataEntry, Connection connection, String tableName)
            throws SQLException {
        if (isDateTimeAndLocationInTable(connection, tableName, hotelDataEntry.locationName(), hotelDataEntry.predictionTime())) {
            updateHotelData(connection, hotelDataEntry);
        } else {
            insertHotelData(connection, hotelDataEntry);
        }
    }

    private static void insertHotelData(Connection connection, HotelOfferNode hotelDataEntry) throws SQLException {
        String tableName = "HotelOffers";
        String insertSQL = String.format(
                "INSERT INTO %s (location, date, name, price) VALUES (?, ?, ?, ?)", tableName);
        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            setInsertHotelParameters(preparedStatement, hotelDataEntry);
            preparedStatement.executeUpdate();
        }
    }

    private static void updateHotelData(Connection connection, HotelOfferNode hotelDataEntry) throws SQLException {
        String tableName = "HotelOffers";
        String updateSQL = String.format(
                "UPDATE %s SET price = ? WHERE location = ? AND date = ?", tableName);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            setUpdateHotelParameters(preparedStatement, hotelDataEntry);
            preparedStatement.executeUpdate();
        }
    }

    private static void setInsertHotelParameters(PreparedStatement preparedStatement, HotelOfferNode hotelDataEntry) throws SQLException {
        preparedStatement.setString(1, hotelDataEntry.locationName());
        preparedStatement.setString(2, String.valueOf(hotelDataEntry.predictionTime()));
        preparedStatement.setString(3, hotelDataEntry.name());
        preparedStatement.setDouble(4, hotelDataEntry.tax());
    }

    private static void setUpdateHotelParameters(PreparedStatement preparedStatement, HotelOfferNode hotelDataEntry) throws SQLException {
        preparedStatement.setDouble(1, hotelDataEntry.tax());
        preparedStatement.setString(2, hotelDataEntry.locationName());
        preparedStatement.setString(3, String.valueOf(hotelDataEntry.predictionTime()));
    }

    // Métodos relacionados con la manipulación de datos de clima
    private static void updateOrInsertWeatherData(WeatherNode weatherDataEntry, Connection connection, String tableName)
            throws SQLException {
        if (isDateTimeAndLocationInTable(connection, tableName, weatherDataEntry.location(), weatherDataEntry.predictionTime())) {
            updateWeatherData(connection, weatherDataEntry);
        } else {
            insertWeatherData(connection, weatherDataEntry);
        }
    }

    private static void insertWeatherData(Connection connection, WeatherNode weatherDataEntry) throws SQLException {
        String tableName = "Weathers";
        String insertSQL = String.format(
                "INSERT INTO %s (location, date, temperature, rainProbability, humidity, windSpeed, weatherType) VALUES (?, ?, ?, ?, ?, ?, ?)", tableName);

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            setInsertWeatherParameters(preparedStatement, weatherDataEntry);
            preparedStatement.executeUpdate();
        }
    }

    private static void updateWeatherData(Connection connection, WeatherNode weatherDataEntry) throws SQLException {
        String tableName = "Weathers";
        String updateSQL = String.format(
                "UPDATE %s SET temperature = ?, rainProbability = ?, humidity = ?, windSpeed = ?, weatherType = ? WHERE location = ? AND date = ?", tableName);

        try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQL)) {
            setUpdateWeatherParameters(preparedStatement, weatherDataEntry);
            preparedStatement.executeUpdate();
        }
    }

    private static void setInsertWeatherParameters(PreparedStatement preparedStatement, WeatherNode weatherDataEntry) throws SQLException {
        preparedStatement.setString(1, weatherDataEntry.location());
        preparedStatement.setString(2, String.valueOf(weatherDataEntry.predictionTime()));
        preparedStatement.setDouble(3, weatherDataEntry.temperature());
        preparedStatement.setDouble(4, weatherDataEntry.rainProbability());
        preparedStatement.setInt(5, weatherDataEntry.humidity());
        //preparedStatement.setDouble(6, weatherDataEntry.windSpeed());
        preparedStatement.setString(7, String.valueOf(weatherDataEntry.weatherType()));
    }

    private static void setUpdateWeatherParameters(PreparedStatement preparedStatement, WeatherNode weatherDataEntry) throws SQLException {
        preparedStatement.setDouble(1, weatherDataEntry.temperature());
        preparedStatement.setDouble(2, weatherDataEntry.rainProbability());
        preparedStatement.setInt(3, weatherDataEntry.humidity());
        //preparedStatement.setDouble(4, weatherDataEntry.windSpeed());
        preparedStatement.setString(5, String.valueOf(weatherDataEntry.weatherType()));
        preparedStatement.setString(6, weatherDataEntry.location());
        preparedStatement.setString(7, String.valueOf(weatherDataEntry.predictionTime()));
    }

    // Métodos auxiliares
    private static boolean isDateTimeAndLocationInTable(Connection connection, String tableName, String location, String predictionTime)
            throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE location = ? AND date = ?";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, location);
            preparedStatement.setString(2, String.valueOf(predictionTime));
            return countDateTimeInTable(preparedStatement);
        }
    }

    private static boolean countDateTimeInTable(PreparedStatement preparedStatement) throws SQLException {
        try (ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next() && resultSet.getInt(1) > 0;
        }
    }

    public static Connection connect(String dbPath) {
        Connection connection = null;
        try {
            String url = "jdbc:sqlite:" + dbPath;
            connection = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return connection;
    }
}