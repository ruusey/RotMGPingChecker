//------------------------------------------------------------------------------------------------------------------------------------------
// Robert Usey ("DEVELOPER")
// Unpublished Copyright (c) 2013-2019 Robert Usey, All Rights Reserved.        
//------------------------------------------------------------------------------------------------------------------------------------------

package com.main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class RotMGPingChecker {
	//Static lists and maps for servers and their EC2 region name
	static ArrayList<String> regions = new ArrayList<String>();
	static ArrayList<String> sNames = new ArrayList<String>();
	static Hashtable<String, String> servers = new Hashtable<String, String>();

	public static void main(String args[]) {
		System.out.println("Starting RotMG ping finder - Ruusey");
		long totalTime = 0;
		try {
			Document doc = null;
			totalTime = System.currentTimeMillis();
			//Grab current list of servers from rotmg website
			doc = Jsoup.connect("https://realmofthemadgodhrd.appspot.com/char/list").get();
			Elements ips = doc.getElementsByTag("DNS");
			Elements names = doc.getElementsByTag("Name");
			//Remove character name entry from XML
			names.remove(0);
			for (int i = 0; i < ips.size(); i++) {
				//Get each ip and server name
				String ip = ips.get(i).text();
				String name = names.get(i).text();
				sNames.add(name);
				InetAddress host;
				//Get the domain name for the servers IP
				host = InetAddress.getByName(ip);
				String hostDns = host.getHostName();
				//Trim the ec2 region from the domain
				int startIdx = hostDns.indexOf(".") + 1;
				String region = hostDns.substring(startIdx, hostDns.indexOf(".", startIdx + 1));
				//A few servers are on the us-west-1 subnet which the base adress is not reachable.
				//Substitute us-west-1 where we find any of these servers
				if (hostDns.contains("compute-1")) {
					// System.out.println(name);
					servers.put("us-west-1", name);
					regions.add("us-west-1");
				} else {
					regions.add(region);
					servers.put(region, name);
				}
			}
			//Test all the servers ping time using AWS ping API
			long min = Long.MAX_VALUE;
			String fastestServer = null;
			for (int i = 0; i < regions.size(); i++) {
				try {
					long end = getPingTime(regions.get(i));
					System.out.println("ping for server[" + sNames.get(i) + "] was " + end + " ms");
					if (end < min) {
						min = end;
						fastestServer = sNames.get(i);
					}
				} catch (Exception e) {
					//We couldnt get a response from the API or other exception was thrown
					System.out.println("ERROR: No response for server[" + sNames.get(i) + "]  ");
				}
			}
			//Print total runtime and best server
			System.out.println("Total runtime [" + (System.currentTimeMillis() - totalTime) + "]ms");
			System.out.println("Your best server is {" + fastestServer + "} with ping[" + (min) + "]ms");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static long getPingTime(String addr) {
		long start = 0;
		start = System.currentTimeMillis();
		//See how long it takes for us to get a response from AWS for this server
		try {
			Jsoup.connect("https://ec2." + addr + ".amazonaws.com/ping").get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = (System.currentTimeMillis() - start);
		return end;

	}
}
