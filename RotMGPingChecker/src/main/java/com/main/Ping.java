package com.main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class Ping {
	static ArrayList<String> regions = new ArrayList<String>();
	static ArrayList<String> sNames = new ArrayList<String>();
	static Hashtable<String, String> servers = new Hashtable<String, String>();
	public static void main(String args[]) {
		System.out.println("Starting RotMG ping finder - Ruusey");
		
		long totalTime = 0;
		try {
			Document doc = null;
			totalTime = System.currentTimeMillis();
			doc = Jsoup.connect("https://realmofthemadgodhrd.appspot.com/char/list").get();
			Elements ips = doc.getElementsByTag("DNS");
			Elements names = doc.getElementsByTag("Name");
			names.remove(0);
			for (int i = 0; i < ips.size(); i++) {
				String ip = ips.get(i).text();
				String name = names.get(i).text();
				sNames.add(name);
				InetAddress host;

				host = InetAddress.getByName(ip);

				String hostDns = host.getHostName();
				// ec2-54-153-32-11.us-west-1.compute.amazonaws.com

				// System.out.println(name+" "+gson.serialize(host));
				int startIdx = hostDns.indexOf(".") + 1;
				String region = hostDns.substring(startIdx, hostDns.indexOf(".", startIdx + 1));
				if (hostDns.contains("compute-1")) {
					//System.out.println(name);
					servers.put("us-west-1", name);
					regions.add("us-west-1");
				} else {
					regions.add(region);
					servers.put(region, name);
				}

			}
			long min = Long.MAX_VALUE;
			String fastestServer = null;
			

			

			
			for (int i = 0; i < regions.size(); i++) {
				
				try {
					
					Jsoup.connect("https://ec2." + regions.get(i) + ".amazonaws.com/ping").get();
					long end = getPingTime(regions.get(i));
					System.out.println("ping for server[" + sNames.get(i) + "] was "
							+ end + " ms");
					if(end<min) {
						min=end;
						fastestServer=sNames.get(i);
					}
				} catch (Exception e) {

				}
			}
			System.out.println("Total runtime ["+ (System.currentTimeMillis()-totalTime)+"]ms");
			System.out.println("Your best server is {"+fastestServer+"} with ping["+ (min)+"]ms");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static long getPingTime(String addr) {
		long start = 0;
		start = System.currentTimeMillis();
		try {
			Jsoup.connect("https://ec2." + addr + ".amazonaws.com/ping").get();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		long end = (System.currentTimeMillis() - start);
		return end;
		
	}
//	public static void main(String[] args) {
//		  InetAddress addr = null;
//		try {
//			addr = InetAddress.getByName("13.57.182.96");
//		} catch (UnknownHostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		  String host = addr.getHostName();
//		  
//		String ip = host;
//		runSystemCommand("ping " + ip);
//
//	
//	}

}
