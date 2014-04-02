package com.essaid.owlcl.command.module.builder;

import com.essaid.owlcl.command.module.IModule;

public interface IModuleBuilder {

  String SIMPLE = "simple";
  String SIMPLE_INFERRED = "simple-inferred";

  /**
   * This is called in the order it is specified in the module. A module (the
   * same module object) can be build many times. For example, once for the
   * "generate" version and another time for the "generateInferred" version. The
   * builder should check the isGenerate and isGenerateInferred to check what
   * needs to be done.
   * 
   * @param module
   */
  void build(IModule module, boolean inferred);

  /**
   * This is called when a module build is finished. The builders are held in a
   * Set in the Module (make sure builder equals() is appropriate) and each one
   * called at the end in no specific order.
   * 
   * 
   * @param module
   */
  void buildFinished(IModule module);

}
