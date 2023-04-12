package ru.rexchange.drawing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

public class SeriesDrawer {
	protected Map<String, List<Float>> series = new HashMap<>();
	protected Map<String, Color> colors = new HashMap<>();
	protected int canvasWidth;
	protected int canvasHeigth;
	protected int margin;
	protected Float minValue = null;
	protected Float maxValue = null;
	protected String caption = null;
	protected Date start = null;
	protected Date end = null;

	public SeriesDrawer(int canvasWidth, int canvasHeigth, int margin) {
		this.canvasWidth = canvasWidth;
		this.canvasHeigth = canvasHeigth;
		this.margin = margin;
	}

	public void addSerie(String serieName, List<Float> data, Color c) {
		series.put(serieName, data);
		colors.put(serieName, c);
		for (Float value : data) {
			processNewValue(value);
		}
	}

	public void addValue(String serieName, float value) {
		if (!series.containsKey(serieName)) {
			series.put(serieName, new LinkedList<Float>());
		}
		series.get(serieName).add(value);
		processNewValue(value);
	}

	protected final void processNewValue(float value) {
		if (minValue == null || minValue > value) {
			minValue = value;
		}
		if (maxValue == null || maxValue < value) {
			maxValue = value;
		}
	}

	public void setColor(String serieName, Color c) {
		colors.put(serieName, c);
	}

	public void drawSeries(OutputStream os) throws IOException {
		int margins = margin * 2;
		BufferedImage img = new BufferedImage(canvasWidth + margins, canvasHeigth + margins,
				BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setBackground(new Color(255, 250, 220));
		g.clearRect(0, 0, img.getWidth(), img.getHeight());
		drawGrid(g);
		for (Map.Entry<String, List<Float>> e : series.entrySet()) {
			drawOneSerie(g, colors.get(e.getKey()), e.getValue());
		}
		g.setColor(Color.BLACK);
		g.drawString(maxValue.toString(), canvasWidth - margin, margin);
		g.drawString(minValue.toString(), canvasWidth - margin, canvasHeigth + margin);
		g.drawString(String.valueOf((minValue + maxValue) / 2), canvasWidth - margin,
				canvasHeigth / 2 + margin);
		if (caption != null) {
			g.drawString(caption, margin, margin);
		}
		DateFormat df = new SimpleDateFormat("dd.MM HH:mm");
		g.setFont(new Font("Lucida", Font.PLAIN, margin / 2));
		if (start != null) {
			g.drawString(df.format(start), margin, canvasHeigth + (int) (margins * 0.75));
		}
		if (end != null) {
			g.drawString(df.format(end), canvasWidth - margin,
					canvasHeigth + (int) (margins * 0.75));
		}
		if (start != null && end != null) {
			g.drawString(df.format(new Date(start.getTime() + (end.getTime() - start.getTime()) / 2)), canvasWidth / 2,
					canvasHeigth + (int) (margins * 0.75));
		}
		ImageIO.write(img, "png", os);
	}

	protected void drawGrid(Graphics2D g) {
		g.setColor(new Color(200, 200, 200));
		g.drawLine(margin, margin, canvasWidth - margin, margin);
		g.drawLine(margin, canvasHeigth / 2 + margin, canvasWidth - margin,
				canvasHeigth / 2 + margin);
		g.drawLine(margin, canvasHeigth + margin, canvasWidth - margin, canvasHeigth + margin);
		g.drawLine(margin, margin, margin, canvasHeigth + margin);
		g.drawLine(margin + canvasWidth / 2, margin, margin + canvasWidth / 2, canvasHeigth + margin);
}

	protected void drawOneSerie(Graphics2D g, Color c, List<Float> serie) {
		g.setColor(c);
		float distanceUnit = canvasWidth / serie.size();
		for (int i = 0; i < serie.size() - 1; i++) {
			g.drawLine(margin + (int) (i * distanceUnit),
					margin + (int) project(serie.get(i), minValue, maxValue, 0, canvasHeigth),
					margin + (int) ((i + 1) * distanceUnit),
					margin + (int) project(serie.get(i + 1), minValue, maxValue, 0, canvasHeigth));
		}

	}

	protected static float projectReversed(float value, float minRange, float maxRange,
			int minCanvas, int maxCanvas) {
		float canvasUnitSize = (maxCanvas - minCanvas) / (maxRange - minRange);
		return (value - minRange) * canvasUnitSize;
	}

	protected static float project(float value, float minRange, float maxRange, int minCanvas,
			int maxCanvas) {
		float canvasUnitSize = (maxCanvas - minCanvas) / (maxRange - minRange);
		return maxCanvas - (value - minRange) * canvasUnitSize;
	}

	public void setCaption(String caption) {
		this.caption = caption;
	}

	public void setStart(long time) {
		this.start = new Date(time);
	}

	public void setEnd(long time) {
		this.end = new Date(time);
	}

	public static void main(String[] args) throws Throwable {
		final int height = 100;
		final int margins = 40;
		final int width = 220;

		int margin = margins / 2;
		SeriesDrawer sd = new SeriesDrawer(width, height, margin);
		sd.addValue("random", 0.1f);
		sd.addValue("random", 0.5f);
		sd.setCaption("Test");
		sd.setStart(new Date().getTime());
		sd.setEnd(new Date().getTime());
		sd.drawSeries(new FileOutputStream("test.png"));
	}
}
