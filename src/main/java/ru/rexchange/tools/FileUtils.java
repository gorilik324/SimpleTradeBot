package ru.rexchange.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileUtils {
	public static String readToString(InputStream is, String encoding, int maxSize)
			throws IOException {
		char[] buf = new char[4096];
		BufferedReader in = new BufferedReader(new InputStreamReader(is, encoding));

		try {
			StringBuilder sb = new StringBuilder();

			do {
				int c = in.read(buf);
				if (c < 0) {
					String c1 = sb.toString();
					return c1;
				}

				sb.append(buf, 0, c);
			} while (maxSize <= 0 || sb.length() < maxSize);

			sb.setLength(maxSize);
			String var7 = sb.toString();
			return var7;
		} finally {
			in.close();
		}
	}

	public static String readToString(InputStream is, String encoding) throws IOException {
		return readToString((InputStream) is, encoding, -1);
	}

}
