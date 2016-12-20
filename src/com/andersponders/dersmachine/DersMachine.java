package com.andersponders.dersmachine;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

public class DersMachine {
	private int H; //top of heap register
	private int S; //next subterm
	private int P; //next instruction
	private int CP; //save instruction pointer (to return from call)
	private List<HeapCell> heap;
	private String[] code; //code area
	private int[] X; //general-purpose registers
	private Stack<Integer> PDL;
	private Mode currentMode;
	private static final String labelRegex = "^([a-z]+/\\d :).*";
	
	public DersMachine() //default constructor
	{
		X = new int[32];
		heap = new ArrayList<HeapCell>();
		S = 0;
		H = 0;
		P = 0;
		CP = 0;
		currentMode = Mode.WRITE;
		PDL = new Stack<Integer>();
	}
	
	public void load(String text)
	{
		code = text.split("\\r?\\n");
		boolean done = false;
		while (!done)
		{
			String instr = code[P];
			String[] split;
			if (instr.matches(labelRegex))
			{
				split = instr.split(" : ");
				String label = split[0];
				instr = split[1];
			}
			split = instr.split(" ", 2);
			String opcode = split[0];
			pp_heap();
			pp_registers();
			System.out.println(opcode);
			String operands = "";
			if (!opcode.equals("proceed") && !opcode.equals("done"))
			{
				operands=split[1];
			}
			String[] splitOperands;
			switch (opcode)
			{
			case "put_structure":
				splitOperands = operands.split(",");
				put_structure(splitOperands[0], Integer.parseInt(splitOperands[1]));
				break;
			case "set_variable":
				set_variable(Integer.parseInt(operands));
				break;
			case "set_value":
				set_value(Integer.parseInt(operands));
				break;
			case "get_structure":
				splitOperands = operands.split(",");
				get_structure(splitOperands[0], Integer.parseInt(splitOperands[1]));
				break;
			case "unify_variable":
				unify_variable(Integer.parseInt(operands));
				break;
			case "unify_value":
				unify_value(Integer.parseInt(operands));
				break;
			case "put_variable":
				splitOperands = operands.split(",");
				put_variable(Integer.parseInt(splitOperands[0]), Integer.parseInt(splitOperands[1]));
				break;
			case "put_value":
				splitOperands = operands.split(",");
				put_value(Integer.parseInt(splitOperands[0]), Integer.parseInt(splitOperands[1]));
				break;
			case "get_variable":
				splitOperands = operands.split(",");
				get_variable(Integer.parseInt(splitOperands[0]), Integer.parseInt(splitOperands[1]));
				break;
			case "get_value":
				splitOperands = operands.split(",");
				get_value(Integer.parseInt(splitOperands[0]), Integer.parseInt(splitOperands[1]));
				break;
			case "call": 
				call(operands);
				break;
			case "proceed": 
				proceed();
				break;
			case "done":
				done=true;
				break;
			default: 
				//this is an error
				throw new RuntimeException("Invalid opcode: " + opcode);
			}
			if (P>=code.length) done=true;
		}
	}
	
	//instructions
	
	public void put_structure(String functorname, int i)
	{
		heap.add(new HeapCell("STR", H+1));
		X[i]=H;
		heap.add(new HeapCell(functorname, -1));
		H+=2;
		P++;
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
			if (heap.get(cell.data).tag.equals(functorname))
			{
				S=cell.data+1;
				currentMode=Mode.READ;
			}
			break;
		default:
			//this should not happen
			pp_heap();
			throw new RuntimeException("System error - invalid heap tag:" + cell.tag);
		}
		P++;
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
		P++;
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
		P++;
	}
	
	public void set_variable(int i)
	{
		heap.add(new HeapCell("REF", H));
		X[i]=H;
		H++;
		P++;
	}
	
	public void set_value(int i)
	{
		heap.add(new HeapCell("REF", X[i]));
		H++;
		P++;
	}
	
	public void put_variable(int n, int i)
	{
		heap.add(new HeapCell("REF", H));
		X[n]=H;
		X[i]=H;
		H++;
		P++;
	}
	
	public void put_value(int n, int i)
	{
		X[i]=X[n];
		P++;
	}
	
	public void get_variable(int n, int i)
	{
		X[n]=X[i];
		P++;
	}
	
	public void get_value(int n, int i)
	{
		unify(X[n], X[i]);
		P++;
	}
	
	public void call(String functor)
	{
		CP = P+1;//point to next instruction when we resume
		int addr=-1;
		for (int i=0; i<code.length; i++)
		{
			if (code[i].startsWith(functor)){ 
				addr=i;
				break;
			}
		}
		if (addr!=-1)
		{
			P=addr;
		}
		else
		{
			throw new RuntimeException("Invalid functor (not found): " + functor);
		}
	}
	
	public void proceed()
	{
		P=CP;//restore to the next instruction after call()
	}
	
	//utility functions
	private int deref(int address)
	{
		HeapCell cell = heap.get(address);
		if (cell.tag.equals("REF") && cell.data!=address)
			return deref(cell.data);
		else return address;
	}
	
	private void bind(int a1, int a2)
	{
		String t1,t2;
		t1=heap.get(a1).tag;
		t2=heap.get(a2).tag;
		if (t1.equals("REF") && (!t2.equals("REF")|a2<a1))
		{
			heap.get(a1).data=heap.get(a2).data;
			heap.get(a1).tag=heap.get(a2).tag;
		}
		else 
		{
			heap.get(a2).data=heap.get(a1).data;
			heap.get(a2).tag=heap.get(a1).tag;
		}
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
				if (t1.tag.equals("REF")|t2.tag.equals("REF"))
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
					if (f1.tag.equals(f2.tag) && arity1==arity2)
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
	public void pp_registers()
	{
		System.out.print("H:[ " + H + " ] " );
		System.out.print("S:[ " + S + " ] " );
		System.out.print("P:[ " + P + " ] " );
		System.out.print("CP:[ " + CP + " ] " );
		System.out.println("MODE:[ " + currentMode.toString() + " ]");
		for (int i=0; i<X.length; i+=8)
		{
			for (int j=0; j<8; j++)
			{
				System.out.print("X"+ (i+j) + ":[ " + X[i+j] + " ] " );
			}
			System.out.println();
		}
	}
	public static void main(String[] args) throws IOException
	{
		Path filepath = Paths.get(args[0]);
		String program = new String(Files.readAllBytes(filepath));
		DersMachine machine = new DersMachine();
		machine.load(program);
		machine.pp_heap();
		machine.pp_registers();
	}
}
