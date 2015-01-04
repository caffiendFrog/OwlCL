package com.beust.jcommander;

public abstract class ParameterGroup implements IKey {

  private String name;

  public ParameterGroup(String groupName) {
    if (groupName == null)
    {
      throw new IllegalStateException("ParameterGroup's name can't be null.");
    }

    this.name = groupName;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (other == null)
      return false;
    if (getClass() != other.getClass())
    {
      return false;
    }
    return name.equals(((ParameterGroup) other).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

}
