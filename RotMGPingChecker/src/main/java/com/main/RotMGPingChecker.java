//------------------------------------------------------------------------------------------------------------------------------------------
// Robert Usey ("DEVELOPER")
// Unpublished Copyright (c) 2013-2019 Robert Usey, All Rights Reserved.        
//------------------------------------------------------------------------------------------------------------------------------------------

package com.main;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.google.common.util.concurrent.MoreExecutors;

public class RotMGPingChecker {
	//Static lists and maps for servers and their EC2 region name
	static ArrayList<String> regions = new ArrayList<String>();
	static ArrayList<String> sNames = new ArrayList<String>();
	static Hashtable<String, String> servers = new Hashtable<String, String>();

	public static void main(String args[]) {
		System.out.println("Starting RotMG ping finder - Ruusey");

		try {
			Document doc = null;
			
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
			ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(100);
	           long totalTime = System.currentTimeMillis();
	          List<Future<Long>> resultList = new ArrayList<>();
	          for (int i = 0; i < regions.size(); i++) {
	        	  GetPingTime ping = new GetPingTime(regions.get(i));
	        	  Future<Long> result = executor.submit(ping);
	        	  resultList.add(result);
	          }
	          for(Future<Long> future : resultList)
	          {
	                try
	                {
	                    System.out.println("Future result is : " + future.get());
	                }
	                catch (InterruptedException | ExecutionException e)
	                {
	                    e.printStackTrace();
	                }
	            }
	          System.out.println("Total runtime [" + (System.currentTimeMillis() - totalTime) + "]ms");
	            executor.shutdown();						
										
										
								
			
			//Test all the servers ping time using AWS ping API
			long min = Long.MAX_VALUE;
			String fastestServer = null;
//			for (int i = 0; i < regions.size(); i++) {
//				try {
//					final int ii = i;
//					Future<Long> future = executorService.submit(() -> getPingTime(regions.get(ii)));
//					
//					System.out.println("ping for server[" + sNames.get(i) + "] was " + future.get() + " ms");
////					if (end < min) {
////						min = end;
////						fastestServer = sNames.get(i);
////					}
//				} catch (Exception e) {
//					//We couldnt get a response from the API or other exception was thrown
//					System.out.println("ERROR: No response for server[" + sNames.get(i) + "]  ");
//				}
//			}
			//Pr
			System.out.println("Your best server is {" + fastestServer + "} with ping[" + (min) + "]ms");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
//	public static long getPingTime(String addr) {
//		GetPingTime pt = new GetPingTime();
//		pt.addr=addr;
//		pt.run();
//		return pt.runtime;
//	}
}
