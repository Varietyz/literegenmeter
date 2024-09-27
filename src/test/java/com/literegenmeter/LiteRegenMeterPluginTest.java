package com.literegenmeter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class LiteRegenMeterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(LiteRegenMeterPlugin.class);
		RuneLite.main(args);
	}
}