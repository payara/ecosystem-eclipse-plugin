# Eclipse Payara tools
This is a fork of the Eclipse GlassFish Tools, tailored and improved to work with Payara.

## Building

To build set the `JDK_8_HOME` environment variable to point to a valid JDK 8 installation, for instance:

```
export JDK_8_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_191.jdk/Contents/Home/
```

Then type `ant` in the root of this project. 

This will cause build helper tools (Corundum) to be build first and then the main feature to be build targeting **Eclipse oxygen.2**. 
Archived update sites will be found in `[project root]/build/packages`

To use the project source code in Eclipse, install Sapphire from http://download.eclipse.org/sapphire/latest and
optionally define ANT_HOME for the build helper tools (Corundum).


GlassFish is a trademark of Eclipse Foundation.
Payara is a trademark of Payara Foundation.

