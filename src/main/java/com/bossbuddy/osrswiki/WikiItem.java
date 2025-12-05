package com.bossbuddy.osrswiki;

import com.bossbuddy.util.Util;
import java.text.NumberFormat;
import java.util.Objects;


public class WikiItem
{

	private final String imageUrl;
	private final String name;
	private final int quantity;
	private final String quantityStr;
	private final String rarityStr;
	private final double rarity;
	private final int exchangePrice;
	private final int alchemyPrice;

	final NumberFormat nf = NumberFormat.getNumberInstance();

	public WikiItem(String imageUrl, String name, int quantity, String quantityStr, String rarityStr, double rarity, int exchangePrice, int alchemyPrice)
	{
		this.imageUrl = imageUrl;
		this.name = name;
		this.quantity = quantity;
		this.quantityStr = quantityStr.replace(",", "");
		this.rarityStr = rarityStr.replace(",", "");
		this.rarity = rarity;
		this.exchangePrice = exchangePrice;
		this.alchemyPrice = alchemyPrice;
	}

	public String getName()
	{
		return name;
	}

	public int getQuantity()
	{
		return quantity;
	}

	public String getQuantityStr()
	{
		return quantityStr;
	}

	public double getRarity()
	{
		return rarity;
	}

	public String getRarityStr()
	{
		return rarityStr;
	}

	public String getRaritySimple()
	{
		if (rarityStr.contains("/"))
		{
			String[] values = rarityStr.split(("/"));
			int[] numDem = simplifyFraction(Integer.parseInt(values[0]), Integer.parseInt(values[1]));
			return numDem[0] + "/" + numDem[1];
		}
		return rarityStr;
	}

	public int getExchangePrice()
	{
		return exchangePrice;
	}

	public int getAlchemyPrice()
	{
		return alchemyPrice;
	}

	public String getImageUrl()
	{
		return imageUrl;
	}

	public int[] getQuantityBounds()
	{
		if (quantityStr.contains("-"))
		{
			String[] quantityBound = quantityStr.split("-");
			int[] intNumbers = new int[quantityBound.length];

			for (int i = 0; i < quantityBound.length; i++)
			{
				try
				{
					intNumbers[i] = Integer.parseInt(quantityBound[i].trim());
				}
				catch (NumberFormatException e)
				{
					System.err.println("1 Could not parse '" + quantityBound[i] + "' to an integer. Skipping.");
				}
			}

			return intNumbers;
		}
		else if (Objects.equals(quantityStr, "N/A"))
		{
			return new int[]{0};
		}
		return new int[]{Integer.parseInt(quantityStr)};
	}

	public boolean quantityMatch(int amount)
	{
		if (quantityStr.contains("-"))
		{
			String[] quantityBound = quantityStr.split("-");

			if (quantityStr.contains("noted"))
			{
				String notedQuantity = quantityStr.split("\\(noted\\)")[0];
				quantityBound = notedQuantity.split("-");
			}
			int[] intNumbers = new int[quantityBound.length];

			for (int i = 0; i < quantityBound.length; i++)
			{
				try
				{
					intNumbers[i] = Integer.parseInt(quantityBound[i].trim().replace(",", "")); // .trim() removes leading/trailing whitespace
				}
				catch (NumberFormatException e)
				{
					System.err.println("2 Could not parse '" + quantityBound[i] + "' to an integer. Skipping.");
				}
			}

			return intNumbers[0] <= amount && amount <= intNumbers[1];
		}
		else if (quantityStr.contains("noted"))
		{
			String notedQuantity = quantityStr.split("\\(noted\\)")[0];

			String[] quantityBound = notedQuantity.split("-");
			int[] intNumbers = new int[quantityBound.length];

			for (int i = 0; i < quantityBound.length; i++)
			{
				try
				{
					intNumbers[i] = Integer.parseInt(quantityBound[i].trim().replace(",", ""));
				}
				catch (NumberFormatException e)
				{
					System.err.println("3 Could not parse '" + quantityBound[i] + "' to an integer. Skipping.");
				}
			}

			if (quantityBound.length == 1)
			{
				return amount == Integer.parseInt(notedQuantity.trim());
			}

			return intNumbers[0] <= amount && amount <= intNumbers[1];
		}
		else if (Objects.equals(quantityStr, "N/A"))
		{
			return false;
		}
		return amount == Integer.parseInt(quantityStr);
	}

	public String getQuantityLabelText()
	{
		if (quantityStr.contains("-") || quantityStr.endsWith(" (noted)"))
		{
			return "x" + quantityStr;
		}
		return quantity > 0 ? "x" + nf.format(quantity) : quantityStr;
	}

	public String getQuantityLabelTextShort()
	{
		if (quantityStr.endsWith(" (noted)"))
		{
			return "x" + quantityStr.replaceAll("\\(.*\\)", "(n)").trim();
		}
		return getQuantityValueText();
	}

	public String getQuantityValueText()
	{
		return quantity > 0 ? "x" + Util.rsFormat(quantity) : "";
	}

	public String getRarityLabelText(boolean percentMode)
	{
		String rarityLabelStr = rarityStr.contains(";") || rarityStr.equals("Always") || rarityStr.contains(" × ") ? rarityStr : Util.convertDecimalToFraction(rarity);
		if (percentMode)
		{
			rarityLabelStr = Util.toPercentage(rarity, rarity <= 0.0001 ? 3 : 2);
		}
		return rarityLabelStr;
	}

	public String getExchangePriceLabelText()
	{
		String priceLabelStr = exchangePrice > 0 ? nf.format(exchangePrice) + "gp" : "Not sold";
		if (name.equals("Nothing"))
		{
			priceLabelStr = "";
		}
		return priceLabelStr;
	}

	public String getExchangePriceLabelTextShort()
	{
		String priceLabelStr = exchangePrice > 0 ? Util.rsFormat(exchangePrice) : "";
		if (name.equals("Nothing"))
		{
			priceLabelStr = "";
		}
		return priceLabelStr;
	}

	public String getAlchemyPriceLabelText()
	{
		String priceLabelStr = nf.format(alchemyPrice) + "gp";
		if (name.equals("Nothing") || alchemyPrice < 0)
		{
			priceLabelStr = "";
		}
		return priceLabelStr;
	}

	public String getAlchemyPriceLabelTextShort()
	{
		String priceLabelStr = alchemyPrice > 0 ? nf.format(alchemyPrice) + "gp" : "";
		if (name.equals("Nothing") || alchemyPrice < 0)
		{
			priceLabelStr = "";
		}
		return priceLabelStr;
	}

	public static int findGCD(int a, int b)
	{
		if (b == 0)
		{
			return a;
		}
		return findGCD(b, a % b);
	}

	public static int[] simplifyFraction(int numerator, int denominator)
	{
		if (denominator == 0)
		{
			throw new IllegalArgumentException("Denominator cannot be zero.");
		}

		int gcd = findGCD(numerator, denominator);
		int simplifiedNumerator = numerator / gcd;
		int simplifiedDenominator = denominator / gcd;

		return new int[]{simplifiedNumerator, simplifiedDenominator};
	}
}