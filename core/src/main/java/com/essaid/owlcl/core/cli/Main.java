package com.essaid.owlcl.core.cli;

import com.essaid.owlcl.util.OwlclUtil2;

public class Main {

  public static void main(String[] args) throws Exception {
    OwlclUtil2.instance().init();

    // need this to avoid class not found
    DoMain domain = new DoMain();
    domain.run(args);

  }
}
