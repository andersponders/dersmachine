package com.andersponders.dersmachine;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class DersMachine {
	private int H; //top of heap register
	private int S; //next subterm
	private List<HeapCell> heap;
	private int[] X; //list of general-purpose registers
	private Stack<Integer> PDL;
	private Mode currentMode;
	
	public DersMachine() //default constructor
	{
		X = new int[32];
		heap = new ArrayList<HeapCell>();
		S = 0;
		H = 0;
		currentMode = Mode.WRITE;
		PDL = new Stack<Integer>();
	}
	
	public void load(String code)
	{
		String[] lines = code.split("\\r?\\n");
		for (String line : lines)
		{
			String[] split = line.split(" ", 2);
			String opcode = split[0];
			String operands;
			switch (opcode)
			{
			case "put_structure":
				operands = split[1];
				String[] splitOperands = operands.split(",");
				put_structure(splitOperands[0], Integer.parseInt(splitOperands[1]));
				break;
			case "set_variable":
				operands = split[1];
				set_variable(Integer.parseInt(operands));
				break;
			case "set_value":
				operands = split[1];
				set_value(Integer.parseInt(operands));
				break;
			case "get_structure":
				break;
			case "unify_variable":
				break;
			case "unify_value":
				break;
			default: 
				//this is an error
				throw new RuntimeException("Invalid opcode.");
			}
				
		}
	}
	
	//instructions
	
	public void put_structure(String functorname, int i)
	{
		heap.add(new HeapCell("STR", H+1));
		X[i]=H+1;
		heap.add(new HeapCell(functorname, -1));
		H+=2;
	}
	
	public void get_structure(String functorname, int i)
	{
		int addr = deref(X[i]);
		HeapCell cell = heap.get(addr);
		switch (cell.tag)
		{
		case "REF":
			heap.add(new HeapCell("STR", H+1));
			heap.add(new HeapCell(functorname, -1));
			bind(addr, H);
			H+=2;
			currentMode=Mode.WRITE;
			break;
		case "STR":
			if (heap.get(cell.data).tag==functorname)
			{
				S=cell.data+1;
				currentMode=Mode.READ;
			}
			break;
		default:
			//this should not happen
			throw new RuntimeException("System error - invalid heap tag:" + cell.tag);
		}
	}
	
	public void unify_variable(int i)
	{
		switch (currentMode)
		{
		case READ:
			X[i]=heap.get(S).data;
			break;
		case WRITE:
			heap.add(new HeapCell("REF", H));
			X[i]=heap.get(H).data;
			H++;
			break;
		}
		S++;
	}
	
	public void unify_value(int i)
	{
		switch (currentMode)
		{
		case READ:
			unify(X[i],S);
			break;
		case WRITE:
			heap.add(new HeapCell("REF", X[i]));
		}
		S++;
	}
	
	public void set_variable(int i)
	{
		heap.add(new HeapCell("REF", H));
		X[i]=H;
		H++;
	}
	
	public void set_value(int i)
	{
		heap.add(new HeapCell("REF", X[i]));
		H++;
	}
	
	//utility functions
	private int deref(int address)
	{
		HeapCell cell = heap.get(address);
		if (cell.tag=="REF" && cell.data!=address)
			return deref(cell.data);
		else return address;
	}
	
	private void bind(int addr, int TOS)
	{
		//TODO
	}
	
	private void unify(int a1, int a2)
	{
		PDL.push(a1);
		PDL.push(a2);
		boolean fail = false;
		while (!(PDL.empty()|fail))
		{
			int d1, d2;
			d1 = deref(PDL.pop());
			d2 = deref(PDL.pop());
			if (d1!=d2)
			{
				HeapCell t1 = heap.get(d1);
				HeapCell t2 = heap.get(d2);
				if (t1.tag=="REF"|t2.tag=="REF")
				{
					//one or both is a reference
					bind(d1, d2);
				}
				else
				{
					//both are structures, try to match their functors and arity
					HeapCell f1 = heap.get(t1.data);
					HeapCell f2 = heap.get(t2.data);
					int arity1 = Integer.parseInt(f1.tag.split("/")[1]);
					int arity2 = Integer.parseInt(f2.tag.split("/")[1]);
					if (f1.tag==f2.tag && arity1==arity2)
					{
						for(int i=1; i<=arity1; i++)
						{
							PDL.push(t1.data+i);
							PDL.push(t2.data+i);
						}
					}
				}
			}
		}
	}
	public void pp_heap()
	{
		int addr = 0;
		for (HeapCell cell : heap)
		{
			System.out.print(addr + "\t" + cell.tag);
			if (cell.data!=-1) System.out.println("  " + cell.data);
			else System.out.println();
			addr++;
		}
	}
}
