package com.essaid.owlcl.core.reasoner.fact162;

import java.io.InputStream;

import com.essaid.owlcl.core.IOwlclManager;
import com.essaid.owlcl.core.util.IInitializable;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FactPlusPlusNativeLoader implements IInitializable {

  @Inject
  IOwlclManager manager;

  @Override
  public void initialize() {
    String libName = null;
    if (manager.isLinux())
    {
      libName = "libFaCTPlusPlusJNI.so";
      System.out.println("============= linux fact library");
    } else if (manager.isWindows())
    {
      libName = "FaCTPlusPlusJNI.dll";
    } else if (manager.isMacOs())
    {
      libName = "libFaCTPlusPlusJNI.jnilib";
    }

    if (libName == null)
    {
      throw new IllegalStateException("Could not find native library path for Fact++");
    }

    String libPath = manager.getNativeResourcePrefix("fact162") + libName;

    // for Daniela's CentOS build server
    String qualifiers = System.getProperty(IOwlclManager.OWLCL_ARCH_QUALIFIERS_PROPERTY);
    if (qualifiers != null && qualifiers.contains("glibc2.2"))
    {
      libPath += "-glibc2.2";
    }

    ClassLoader cl = this.getClass().getClassLoader();

    InputStream is = cl.getResourceAsStream(libPath);

    manager.loadNativeLibrary(is, libName);

  }

}
