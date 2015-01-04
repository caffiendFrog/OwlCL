package com.beust.jcommander.dynamic;

import java.util.List;

import com.beust.jcommander.DynamicParameter;

public class DSimpleBad {

  @DynamicParameter(names = "-D")
  public List<String> params;
}
