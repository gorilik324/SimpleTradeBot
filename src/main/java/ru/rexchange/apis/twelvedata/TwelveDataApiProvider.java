package ru.rexchange.apis.twelvedata;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import ru.rexchange.apis.ApiConnectionManager;

public class TwelveDataApiProvider {
  private static final Logger LOGGER = Logger.getLogger(TwelveDataApiProvider.class);
  private static final String SUPER_KEY = "MTUxY2M3ZGI0NjYyNGIyYWFmY2MxNjg1ODVmZmMzNzM";
  private static ApiConnectionManager apiManager = new ApiConnectionManager(
      "https://api.twelvedata.com/");

  // https://api.twelvedata.com/time_series?symbol=DXY&output=1000&interval=4h&apikey=placeholder
  //READ ON https://twelvedata.com/docs

  public static synchronized Data[] getHistoricalData(String symbol, String interval, Integer limit) throws IOException {
    Map<String, String> params = new HashMap<>();
    params.put("symbol", symbol);
    params.put("interval", interval);
    params.put("apikey", new String(Base64.decodeBase64(SUPER_KEY)));
    params.put("outputsize", String.valueOf(limit));
    params.put("format", "JSON");//JSON/CSV
    params.put("delimiter", ";");

    String result = apiManager.sendGet("time_series", params);

    //result = result.replaceAll(String.format("\"%S\":", quotedCurrency.toLowerCase()), "\"price_info\":");

    Gson gson = new Gson();
    try {
      OrdersResponse resp = gson.fromJson(result, OrdersResponse.class);
      return resp.data;
    } catch (JsonSyntaxException e) {
      LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
      return null;
    }
  }

  public static class OrdersResponse {
    public Status status;
    public Data[] data;
  }

  public static class Status {

  }

  public static class Data {
    public int id;
    public String name;
    public String symbol;
    public Date last_updated;
    public QuoteInfo quote;
  }

  public static class QuoteInfo {
    public PriceInfo price_info;
  }

  public static class PriceInfo {
    public float price;
    public double market_cap;
    public Date last_updated;
  }

  public static void main(String[] args) {
    try {
      getHistoricalData("DXY", "4h", 5000);
      getHistoricalData("NDX", "4h", 5000);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
