package com.bossbuddy.osrswiki;

import com.bossbuddy.util.Constants;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import okhttp3.*;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class WikiScraper
{
	private final static String baseUrl = "https://oldschool.runescape.wiki";
	private final static String baseWikiUrl = baseUrl + "/w/";

	public static CompletableFuture<WikiItem[]> getWikiItemsByMonster(OkHttpClient okHttpClient, String monsterName, int monsterId)
	{
		CompletableFuture<WikiItem[]> future = new CompletableFuture<>();

		String rawURL;
		if (monsterId > -1)
		{
			rawURL = getWikiUrlWithIdRaw(monsterName, monsterId);
		}
		else
		{
			rawURL = getWikiUrlRaw(monsterName);
		}

		requestAsync(okHttpClient, rawURL).whenCompleteAsync((responseHTML, ex) -> {
			List<WikiItem> wikiItems = new ArrayList<>();

			if (ex != null)
			{
				WikiItem[] result = new WikiItem[0];
				future.complete(result);
			}

			List<String> matches = new ArrayList<>();
			Pattern pattern = Pattern.compile("^.*DropsLine.*$", Pattern.MULTILINE);
			Matcher matcher = pattern.matcher(responseHTML);

			while (matcher.find())
			{
				matches.add(matcher.group());
			}

			for (String match : matches)
			{
				WikiItem wikiItem = parseWikiItemString(match);
				wikiItems.add(wikiItem);
			}

			WikiItem[] result = wikiItems.toArray(new WikiItem[wikiItems.size()]);
			future.complete(result);
		});

		return future;
	}

	private static WikiItem parseWikiItemString(String wikiItemString){
		String name = "";
		int quantity = -1;
		double rarity = -1;
		String quantityString = "";
		String rarityString = "";

		String[] itemProperties = wikiItemString.split("\\|");
		NumberFormat nf = NumberFormat.getNumberInstance();

		for(String prop : itemProperties){
			String[] propKvP = prop.split("=");
			if (propKvP.length != 2)
				continue;

			String propName = propKvP[0];
			String propValue = propKvP[1].replace("}}","");

			switch(propName){
				case "name":
					name = propValue;
					break;
				case "quantity":
					quantityString = propValue;
					try
					{
						String[] quantityStrs = quantityString.replaceAll("\\s+", "").split("-");
						String firstQuantityStr = quantityStrs.length > 0 ? quantityStrs[0] : null;
						quantity = nf.parse(firstQuantityStr).intValue();
					}
					catch (ParseException ignored)
					{
					}
					break;
				case "rarity":
					rarityString = propValue;
					if (rarityString.startsWith("~"))
					{
						rarityString = rarityString.substring(1);
					}
					else if (rarityString.startsWith("2 × ") || rarityString.startsWith("3 × "))
					{
						rarityString = rarityString.substring(4);
					}

					try
					{
						String[] rarityStrs = rarityString.replaceAll("\\s+", "").split(";");
						String firstRarityStr = rarityStrs.length > 0 ? rarityStrs[0] : null;

						if (firstRarityStr != null)
						{
							if (firstRarityStr.equals("Always"))
							{
								rarity = 1.0;
							}
							else
							{
								String[] fraction = firstRarityStr.split("/");
								if (fraction.length > 1)
								{
									double numer = nf.parse(fraction[0]).doubleValue();
									double denom = nf.parse(fraction[1]).doubleValue();
									rarity = numer / denom;
								}

							}
						}
					}
					catch (ParseException ignored)
					{
					}
				}
			}
		return new WikiItem(name,quantity,quantityString,rarityString, rarity);
	}

	public static String getWikiUrl(String itemOrMonsterName)
	{
		String sanitizedName = sanitizeName(itemOrMonsterName);
		return baseWikiUrl + sanitizedName;
	}

	public static String getWikiUrlRaw(String monsterName)
	{
		String sanitizedName = sanitizeName(monsterName);
		return baseWikiUrl + sanitizedName + "?action=raw";
	}

	public static String getWikiUrlWithIdRaw(String monsterName, int monsterId)
	{
		String sanitizedName = sanitizeName(monsterName);
		if (monsterId == 7851 || monsterId == 7852)
		{
			sanitizedName = "Grotesque_Guardians";
		}
		return baseWikiUrl + sanitizedName + "?action=raw";
	}

	public static String sanitizeName(String name)
	{
		if (name.equalsIgnoreCase("tzhaar-mej"))
		{
			name = "tzhaar-mej (monster)";
		}
		if (name.equalsIgnoreCase("dusk") || name.equalsIgnoreCase("dawn"))
		{
			name = "grotesque guardians";
		}

		name = name.trim().toLowerCase().replaceAll("\\s+", "_");
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	private static CompletableFuture<String> requestAsync(OkHttpClient okHttpClient, String url)
	{
		CompletableFuture<String> future = new CompletableFuture<>();

		Request request = new Request.Builder().url(url).header("User-Agent", Constants.USER_AGENT).build();

		okHttpClient
			.newCall(request)
			.enqueue(
				new Callback()
				{
					@Override
					public void onFailure(Call call, IOException ex)
					{
						future.completeExceptionally(ex);
					}

					@Override
					public void onResponse(Call call, Response response) throws IOException
					{
						try (ResponseBody responseBody = response.body())
						{
							if (!response.isSuccessful())
							{
								future.complete("");
							}

							future.complete(responseBody.string());
						}
						finally
						{
							response.close();
						}
					}
				});

		return future;
	}
}