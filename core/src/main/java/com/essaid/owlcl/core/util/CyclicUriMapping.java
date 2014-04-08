package com.essaid.owlcl.core.util;

public class CyclicUriMapping extends Exception {

  /**
   * 
   */
  private static final long serialVersionUID = 8062809715805143329L;

  public CyclicUriMapping(String string) {
    super(string);
  }

}
