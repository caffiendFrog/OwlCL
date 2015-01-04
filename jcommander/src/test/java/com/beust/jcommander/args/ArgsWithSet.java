package com.beust.jcommander.args;

import java.util.SortedSet;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.SetConverter;

public class ArgsWithSet {

  @Parameter(names = "-s", converter = SetConverter.class)
  public SortedSet<Integer> set;
}