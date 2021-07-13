package com.project.thread;

import java.util.List;

public class ThreadTest extends Thread {
	
	private int i;
	private List<Integer> ids;
	
	public ThreadTest(int i, List<Integer> ids) {
		this.i = i;
		this.ids = ids;
	}

	public void run(){
		this.ids.add(this.i);
	}
	
	private String printIds() {
		String listString = "[";
		for(int l=0; l<ids.size(); l++) {
			listString+=" " + ids.get(l) + " ";
		}
		listString+="]";
		return listString;
	}
}
