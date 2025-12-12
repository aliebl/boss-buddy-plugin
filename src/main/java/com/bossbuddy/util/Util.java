package com.bossbuddy.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.function.Consumer;


import static com.bossbuddy.util.Icons.noteImg;

public class Util
{
	public static void showHandCursorOnHover(Component component)
	{
		component.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent evt)
			{
				evt.getComponent().setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent evt)
			{
				evt.getComponent().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
	}

	public static String colorToHex(Color color)
	{
		return "#" + Integer.toHexString(color.getRGB()).substring(2);
	}

	public static String rsFormat(double number)
	{
		int power;
		String suffix = " KMBT";
		String formattedNumber = "";

		NumberFormat formatter = new DecimalFormat("#,###.#");
		power = (int) StrictMath.log10(number);
		number = number / (Math.pow(10, (power / 3) * 3));
		formattedNumber = formatter.format(number);
		formattedNumber = formattedNumber + suffix.charAt(power / 3);
		return formattedNumber.length() > 4 ? formattedNumber.replaceAll("\\.[0-9]+", "") : formattedNumber;
	}

	public static String toPercentage(double n, int digits)
	{
		return String.format("%." + digits + "f", n * 100) + "%";
	}

	public static String convertDecimalToFraction(double x)
	{
		if (x < 0)
		{
			return "-" + convertDecimalToFraction(-x);
		}

		double tolerance = 1.0E-6;
		double h1 = 1;
		double h2 = 0;
		double k1 = 0;
		double k2 = 1;
		double b = x;
		do
		{
			double a = Math.floor(b);
			double aux = h1;
			h1 = a * h1 + h2;
			h2 = aux;
			aux = k1;
			k1 = a * k1 + k2;
			k2 = aux;
			b = 1 / (b - a);
		} while (Math.abs(x - h1 / k1) > x * tolerance);

		int h1Int = (int) h1;
		int k1Int = (int) k1;

		double denom = k1 / h1;

		int denomInt = k1Int / h1Int;

		String denomStr = String.valueOf(Math.round(denom * 100.0) / 100.0);
		if (Math.floor(denom) == denom)
		{
			denomStr = String.valueOf(denomInt);
		}

		return 1 + "/" + denomStr;
	}
}