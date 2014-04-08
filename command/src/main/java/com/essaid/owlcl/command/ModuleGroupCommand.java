package com.essaid.owlcl.command;

import java.util.ArrayList;
import java.util.List;

import com.essaid.owlcl.core.OwlclCommand;
import com.essaid.owlcl.core.util.OntologyLoadingDescriptor;

public class ModuleGroupCommand extends AbstractCommand {
  
  private List<OntologyLoadingDescriptor> sourceDescriptors = new ArrayList<OntologyLoadingDescriptor>();

  public ModuleGroupCommand(OwlclCommand main) {
    super(main);
    // TODO Auto-generated constructor stub
  }

  @Override
  protected void addCommandActions(List<String> actionsList) {
    // TODO Auto-generated method stub

  }

  @Override
  public Object call() throws Exception {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void doInitialize() {
    // TODO Auto-generated method stub

  }

}
