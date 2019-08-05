package com.main;

import java.io.IOException;
import java.util.concurrent.Callable;

import org.jsoup.Jsoup;

public class GetPingTime implements Callable<Long> {
	private long start;
	public long runtime;
	private String addr;
	public GetPingTime(String addr) {
		this.addr=addr;
	}
	
	@Override
	public Long call() throws Exception {
		this.start=System.currentTimeMillis();
		try {
			Jsoup.connect("https://ec2." + addr + ".amazonaws.com/ping").get();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    //this.runtime = System.currentTimeMillis() - start;
	    return System.currentTimeMillis() - start;
	}
	
}
