package com.essaid.jcommander.example;

public class B extends A {

  protected void hello() {
    System.out.println("Hello from B");
    super.hello();
  }

  public static void main(String[] args) {
    new B().callHello();
  }
}
