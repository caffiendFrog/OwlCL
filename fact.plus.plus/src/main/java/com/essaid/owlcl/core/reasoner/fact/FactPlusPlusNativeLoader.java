package com.essaid.owlcl.core.reasoner.fact;

import java.io.InputStream;

import com.essaid.owlcl.core.IOwlclManager;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class FactPlusPlusNativeLoader {

  @Inject
  private void setManager(IOwlclManager manager) {
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

    String libPath = manager.getNativeResourcePrefix("fact.plus.plus") + libName;

    ClassLoader cl = this.getClass().getClassLoader();

    InputStream is = cl.getResourceAsStream(libPath);

    manager.loadNativeLibrary(is);
  }

}
