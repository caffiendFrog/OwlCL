package com.essaid.owlcl.core.cli;

import com.essaid.owlcl.core.util.DefaultOwlclManager;

public class Main {

  public static void main(String[] args) throws Exception {
    DefaultOwlclManager dm = new DefaultOwlclManager();

    // need this to avoid class not found
    new DoMain(dm, args);

  }
}
