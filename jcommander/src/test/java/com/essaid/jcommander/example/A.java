package com.essaid.jcommander.example;


public class A {
  
  protected void callHello(){
    System.out.println("calling hello from A");
    hello();
  }
  
  protected void hello(){
    System.out.println("Hello from A");
  }

  public static void main(String[] args) {
    new A().hello();

  }

}
