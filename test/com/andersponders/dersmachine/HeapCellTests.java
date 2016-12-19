package com.andersponders.dersmachine;


import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class HeapCellTests {

	@Test
	public void testHeapCellArray()
	{
		List<HeapCell> heap = new ArrayList<HeapCell>();
		heap.add(new HeapCell("REF", 0));
		assertEquals("REF", heap.get(0).tag);
	}
}
