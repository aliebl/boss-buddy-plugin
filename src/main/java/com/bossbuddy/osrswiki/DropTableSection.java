package com.bossbuddy.osrswiki;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Setter
@Getter
public class DropTableSection
{
	private String header;
	private Map<String, WikiItem[]> table;

	public DropTableSection()
	{
	}

	public DropTableSection(String header, Map<String, WikiItem[]> table)
	{
		this.header = header;
		this.table = table;
	}

}