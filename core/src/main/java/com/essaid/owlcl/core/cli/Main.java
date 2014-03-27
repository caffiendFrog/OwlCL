package com.essaid.owlcl.core.cli;

import com.essaid.owlcl.core.util.DefaultOwlclManager;

public class Main {

  public static void main(String[] args) throws Exception {
    // this sets up default paths. Custom paths can be setup with the other
    // constructor if OwlCL is being used as a library.
    DefaultOwlclManager dm = new DefaultOwlclManager();

    // needed to avoid class not found errors
    DoMain.run(dm, args);

  }
}
