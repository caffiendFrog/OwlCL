package com.essaid.owlcl.core;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

public interface IOwlclManager {

  String OWLCL_EXT_DIR = "owlcl-extensions";
  String OWLCL_WORK_DIR_PROPERTY = "owlcl.work.dir";

  public File getTemporaryDirectory();

  public void loadNativeLibrary(InputStream stream);

  public String getNativeResourcePrefix(String resourceGroupName);

  public boolean isWindows();

  public boolean isMacOs();

  public boolean isLinux();

  public boolean is32Arch();

  public boolean is64Arch();

  public URL getCodeUrl();

  public File getCodeJar();

  public File getCodeDirectory();

  public File getCodeExtDirectory();

  public File getHomeDirectory();

  public File getCurrentDirectory();

  public File getWorkDirectory();

  public File getWorkExtDirectory();

}