package com.beust.jcommander;

import java.util.Iterator;
import java.util.List;

/**
 * Encapsulation of either a main application or an individual command.
 */
final class ProgramName implements IKey {

  final String m_name;
  private final List<String> m_aliases;

  ProgramName(String name, List<String> aliases) {
    m_name = name;
    m_aliases = aliases;
  }

  @Override
  public String getName() {
    return m_name;
  }

  String getDisplayName() {
    StringBuilder sb = new StringBuilder();
    sb.append(m_name);
    if (!m_aliases.isEmpty())
    {
      sb.append("(");
      Iterator<String> aliasesIt = m_aliases.iterator();
      while (aliasesIt.hasNext())
      {
        sb.append(aliasesIt.next());
        if (aliasesIt.hasNext())
        {
          sb.append(",");
        }
      }
      sb.append(")");
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ProgramName other = (ProgramName) obj;
    if (m_name == null)
    {
      if (other.m_name != null)
        return false;
    } else if (!m_name.equals(other.m_name))
      return false;
    return true;
  }

  /*
   * Important: ProgramName#toString() is used by longestName(Collection)
   * function to format usage output.
   */
  @Override
  public String toString() {
    return getDisplayName();

  }
}