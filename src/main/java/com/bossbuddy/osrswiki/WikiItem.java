package com.bossbuddy.osrswiki;

import com.bossbuddy.util.Util;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
					log.error("2 Could not parse '{}' to an integer. Skipping.", quantityBound[i]);
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
					log.error("3 Could not parse '{}' to an integer. Skipping.", quantityBound[i]);
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

	public String getRarityLabelText(boolean percentMode)
	{
		String rarityLabelStr = rarityStr.contains(";") || rarityStr.equals("Always") || rarityStr.contains(" × ") ? rarityStr : Util.convertDecimalToFraction(rarity);
		if (percentMode)
		{
			rarityLabelStr = Util.toPercentage(rarity, rarity <= 0.0001 ? 3 : 2);
		}
		return rarityLabelStr;
	}
}