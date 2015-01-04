package com.beust.jcommander;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class VariableArityTest {

  public static class ModelGenerationConfig {

    @Parameter(
        names = { "-m", "--matrixData" },
        variableArity = true,
        description = "File containing a list of instances and their runtimes on various configurations",
        required = false)
    public List<String> modelMatrixFile = new LinkedList<String>();

    @Parameter(names = { "-f", "--featureData" }, variableArity = true,
        description = "File containing a list of instances and their corresponding features",
        required = true)
    public List<String> featureFile = new LinkedList<String>();

    @Parameter(names = { "-c", "--configData" }, variableArity = true,
        description = "File containing a list of configuration parameter values")
    public List<String> configFile = new LinkedList<String>();

    @Parameter(names = { "-o", "--outputFile" },
        description = "File to output the resulting data to. Defaults to ./matrix-generation.zip",
        required = false)
    public String outputFile = "matrix-generation.zip";

    @Parameter(names = { "--seed" }, description = "Seed used for PRNG [0 means don't use a Seed]")
    public long seed = 0;

    public void print() {
      System.out.println("modelMatrixFile: " + modelMatrixFile);
      System.out.println("featureData: " + featureFile);
      System.out.println("configFile: " + configFile);
      System.out.println("output:  " + outputFile);
      System.out.println("seed: " + seed);

    }
  }

  @Test
  public void verifyVariableArity() {
    String input = "-m foo --seed 1024 -c foo -o foo -f foo ";

    String[] split = input.split("\\s+");

    ModelGenerationConfig config = new ModelGenerationConfig();
    JCommander com = new JCommander(config);
    com.setProgramName("modelgen");

    com.parse(split);

    // config.print();
    Assert.assertNotEquals(config.seed, 0);
    Assert.assertEquals(config.modelMatrixFile, Arrays.asList(new String[] { "foo" }));
    Assert.assertEquals(config.featureFile, Arrays.asList(new String[] { "foo" }));
    Assert.assertEquals(config.seed, 1024);
    Assert.assertEquals(config.outputFile, "foo");
  }

  public static void main(String[] args) {
    new VariableArityTest().verifyVariableArity();
  }
}