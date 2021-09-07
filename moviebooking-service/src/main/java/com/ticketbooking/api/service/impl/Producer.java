package com.ticketbooking.api.service.impl;

import java.util.concurrent.BlockingQueue;

//Java program to demonstrate producer code

//Implement Runnable since object
//of this class will be executed by
//a separate thread
class Producer implements Runnable {

 BlockingQueue<Integer> obj;

 public Producer(BlockingQueue<Integer> obj)
 {
     // accept an ArrayBlockingQueue object from
     // constructor
     this.obj = obj;
 }

 
 
 public Producer() {
	super();
	// TODO Auto-generated constructor stub
}



@Override public void run()
 {
       
      // Produce numbers in the range [1,4]
      // and put them in the buffer
     for (int i = 1; i <= 4; i++) {
         try {
             obj.put(i);
             System.out.println("Produced " + i);
         }
         catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 }
}