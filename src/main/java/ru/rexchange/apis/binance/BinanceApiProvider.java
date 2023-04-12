package ru.rexchange.apis.binance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.binance.connector.client.impl.SpotClientImpl;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

public class BinanceApiProvider {
		private static final Logger LOGGER = Logger.getLogger(BinanceApiProvider.class);
		/*
		private static final String API_SECRET_KEY = "12345";
		private static final String API_TOKEN = "00000000-11111111-22222222-33333333-44444444";
		*/

		private static SpotClientImpl client = new SpotClientImpl();

		public static class PriceResponse {
			public double mins;
			public double price;
		}

		public static Double getPrice(String[] pair) {
			for (int i = 0; i < pair.length; i++) {
				if ("USD".equalsIgnoreCase(pair[i])) {
					pair[i] = "USDT";
				}
			}
			LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
			params.put("symbol", String.format("%s%s", pair[0], pair[1]));
			String result = client.createMarket().averagePrice(params);
			try {
				Gson gson = new Gson();
				PriceResponse cont = gson.fromJson(result, PriceResponse.class);
				return cont.price;
			} catch (JsonSyntaxException e) {
				LOGGER.error(String.format("Ошибка разбора ответа:%n%s", result), e);
				return null;
			}
		}
		
    public static class CandleData {
      public long openTime;
      public double openPrice;
      public double highPrice;
      public double lowPrice;
      public double closePrice;
      public double volume;
      public long closeTime;
      public double quotedVolume;
      //public long trades;

      public CandleData(String[] data) {
        openTime = Long.parseLong(data[0]);
        openPrice = Double.parseDouble(data[1]);
        closePrice = Double.parseDouble(data[4]);
        closeTime = Long.parseLong(data[6]);
        volume = Double.parseDouble(data[5]);
        quotedVolume = Double.parseDouble(data[7]);
        lowPrice = Double.parseDouble(data[3]);
        highPrice = Double.parseDouble(data[2]);
      }

      public String toString() {
        return String.format("Time: %s. Low: %s. High: %s. Open: %s. Close: %s. Volume: %s. Quoted vol: %s",
            new Date(openTime), lowPrice, highPrice, openPrice, closePrice, volume, quotedVolume);
      }

      public static String[] getColumnsHeader() {
        return new String[] { "Time", "Low", "High", "Open", "Close", "Volume", "Quoted vol" };
      }

      public static String getColumnsHeaderString() {
        return String.format(Locale.ROOT, "%s, %s, %s, %s, %s, %s, %s", (Object[]) getColumnsHeader());
      }

      public String toCSVString() {
        return String.format(Locale.ROOT, "%s, %.2f, %.2f, %.2f, %.2f, %.1f, %.2f",
            openTime, lowPrice, highPrice, openPrice, closePrice, volume, quotedVolume);
      }
    }

		/**
		 * Parameters:
		    symbol STRING  YES 
        interval  ENUM  YES 
        startTime LONG  NO  
        endTime LONG  NO  
        limit INT NO  Default 500; max 1000.
		 */
		/**
		 * Rate limit intervals (interval)
        SECOND
        MINUTE
        DAY
		 */
	  public static void main(String[] args) {
      //getPrice(new String[] { "WAVES", "USDT" });
      LinkedHashMap<String, Object> params = new LinkedHashMap<String, Object>();
      DateFormat df = new SimpleDateFormat("yy.MM.dd");
      long limit = 1000;
      int intervalInHours = 4;
      String symbol = "WAVESUSDT", interval = "4h";
      //long lastStartDate = df.parse("17.09.01", new ParsePosition(0)).getTime();
      long lastStartDate = df.parse("19.04.01", new ParsePosition(0)).getTime();

      long actualStartDate = -1;
      params.put("symbol", symbol);
      params.put("interval", interval);
      params.put("limit", limit);
      String fName = String.format("%s_%s_%s_export.txt", symbol, df.format(new Date(lastStartDate)), new Date().getTime());
      try (FileOutputStream os = new FileOutputStream(fName)) {
        os.write(CandleData.getColumnsHeaderString().getBytes());
        os.write(System.lineSeparator().getBytes());
        while (true) {
          params.put("startTime", lastStartDate);
          String response = client.createMarket().klines(params);
          //System.out.println(response);
          JsonParser parser = new JsonParser();
          JsonArray array = parser.parse(response).getAsJsonArray();
          Gson gson = new Gson();
          int arrSize = array.size();
          for (int i = 0; i < arrSize; i++) {
            String[] cont = gson.fromJson(array.get(i), String[].class);
            CandleData cd = new CandleData(cont);
            System.out.println(cd.toString());
            os.write(cd.toCSVString().getBytes());
            os.write(System.lineSeparator().getBytes());
            if (actualStartDate == -1) {
              actualStartDate = cd.openTime;
            }
            if (i == arrSize - 1) {
              lastStartDate = cd.openTime;
            }
          }
          os.flush();
          //startDate += ((limit) * intervalInHours * 60 * 60 * 1000);
          lastStartDate += (intervalInHours * 60 * 60 * 1000);
          if (lastStartDate >= new Date().getTime())
            break;
        }
      } catch (JsonSyntaxException e) {
        LOGGER.error(String.format("Ошибка разбора ответа"), e);
      } catch (IOException e1) {
        LOGGER.error(String.format("Ошибка записи в файл"), e1);
      }

      String newName = String.format("%s_%s-%s_%s_export.txt", symbol, df.format(new Date(actualStartDate)), df.format(new Date()), interval);
      if (!new File(fName).renameTo(new File(newName)))
        LOGGER.warn("Не удалось переименовать файл " + fName);
	  }
		
}
