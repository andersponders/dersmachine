package com.andersponders.dersmachine;

public class HeapCell {
	public String tag;
	public int data;
	public HeapCell(String tag, int data)
	{
		this.tag=tag;//STR, REF
		this.data=data;
	}
}
