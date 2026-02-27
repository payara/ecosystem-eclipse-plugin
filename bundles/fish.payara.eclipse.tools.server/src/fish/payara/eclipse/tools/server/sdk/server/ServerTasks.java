/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

/******************************************************************************
 * Copyright (c) 2018-2026 Payara Foundation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package fish.payara.eclipse.tools.server.sdk.server;

import static fish.payara.eclipse.tools.server.sdk.server.JDK.JDK_VERSION;
import static fish.payara.eclipse.tools.server.sdk.server.JDK.isCorrectJDK;
import static fish.payara.eclipse.tools.server.sdk.server.ServerTasks.StartMode.DEBUG;
import static fish.payara.eclipse.tools.server.sdk.server.parser.TreeParser.readXml;
import static fish.payara.eclipse.tools.server.sdk.utils.JavaUtils.javaVmExecutableFullPath;
import static fish.payara.eclipse.tools.server.sdk.utils.JavaUtils.javaVmVersion;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.GFV3_JAR_MATCHER;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.GF_DERBY_ROOT_PROPERTY;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.GF_DOMAIN_ROOT_PROPERTY;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.GF_HOME_PROPERTY;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.GF_JAVA_ROOT_PROPERTY;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.getDerbyRoot;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.getDomainPath;
import static fish.payara.eclipse.tools.server.sdk.utils.ServerUtils.getJarName;
import static java.io.File.separator;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fish.payara.eclipse.tools.server.PayaraServer;
import fish.payara.eclipse.tools.server.sdk.PayaraIdeException;
import fish.payara.eclipse.tools.server.sdk.admin.CommandStartDAS;
import fish.payara.eclipse.tools.server.sdk.admin.ResultProcess;
import fish.payara.eclipse.tools.server.sdk.admin.ServerAdmin;
import fish.payara.eclipse.tools.server.sdk.data.StartupArgs;
import fish.payara.eclipse.tools.server.sdk.logging.Logger;
import fish.payara.eclipse.tools.server.sdk.server.parser.JvmConfigReader;
import fish.payara.eclipse.tools.server.sdk.server.parser.JvmConfigReader.JvmOption;
import fish.payara.eclipse.tools.server.sdk.utils.JavaUtils;
import fish.payara.eclipse.tools.server.sdk.utils.JavaUtils.JavaVersion;
import fish.payara.eclipse.tools.server.sdk.utils.OsUtils;
import fish.payara.eclipse.tools.server.sdk.utils.Utils;

/**
 * This class should contain task methods for GF server.
 * <p/>
 *
 * @author Tomas Kraus, Peter Benedikovic
 */
public class ServerTasks {

    ////////////////////////////////////////////////////////////////////////////
    // Inner classes //
    ////////////////////////////////////////////////////////////////////////////

    public enum StartMode {
        /** Regular server start. */
        START,
        /** Start server in debug mode. */
        DEBUG,
        /** Start server in profiling mode. */
        PROFILE;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes //
    ////////////////////////////////////////////////////////////////////////////

    /** Logger instance for this class. */
    private static final Logger LOGGER = new Logger(ServerTasks.class);

    /** Default name of the DAS server. */
    private static final String DAS_NAME = "server";

    private static Pattern debugPortPattern = Pattern.compile("-\\S+jdwp[:=]\\S*address=([0-9]+)");
    private static Pattern debugSuspendPattern = Pattern.compile("-\\S+jdwp[:=]\\S*suspend=([n]+)");

    private static final Set<String> MULTI_VALUE_OPTIONS = new HashSet<>(Arrays.asList(
            "--add-exports",
            "--add-modules",
            "--add-opens",
            "--add-reads",
            "--limit-modules",
            "--patch-module"
    ));

    /**
     * Convenient method to start Payara in START mode.
     * <p/>
     *
     * @param server Payara server entity.
     * @param args Startup arguments provided by caller.
     * @return ResultProcess returned by CommandStartDAS to give caller opportunity to monitor the start
     * process.
     * @throws PayaraIdeException
     */
    public static ResultProcess startServer(PayaraServer server, StartupArgs args) throws PayaraIdeException {
        return startServer(server, args, StartMode.START, false);
    }

    /**
     * Starts local Payara server.
     * <p/>
     * The own start is done by calling CommandStartDAS. This method prepares command-line arguments
     * that need to be provided for the command. The parameters come from domain.xml and from parameter
     * <code>args</code> provided by the caller.
     * <p/>
     *
     * @param server Payara server entity.
     * @param args Startup arguments provided by caller.
     * @param mode Mode which we are starting GF in.
     * @return ResultProcess returned by CommandStartDAS to give caller opportunity to monitor the start
     * process.
     * @throws PayaraIdeException
     */
    public static ResultProcess startServer(PayaraServer server, StartupArgs args, StartMode mode, boolean suspendOnStart) throws PayaraIdeException {
        String METHOD = "startServer";

        // Reading jvm config section from domain.xml
        JvmConfigReader jvmConfigReader = new JvmConfigReader(DAS_NAME);
        String domainAbsolutePath = server.getDomainsFolder() + separator + server.getDomainName();
        String domainXmlPath = domainAbsolutePath + separator + "config" + separator + "domain.xml";
        if (!readXml(new File(domainXmlPath), jvmConfigReader)) {
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "readXMLerror"), domainXmlPath);
        }

        JDK.Version jdkVersion = getJavaVersion(args);
        JDK.Version targetJDKVersion = jdkVersion != null ? jdkVersion : JDK_VERSION;
        String selectedJavaHome = jdkVersion != null
                ? args.getJavaHome()
                : System.getProperty("java.home");
        // Filter out all options that are not applicable
        List<String> optList
	        = jvmConfigReader.getJvmOptions()
	                .stream()
                        .filter(fullOption -> isCorrectJDK(
                        targetJDKVersion,
                        fullOption.vendor,
                        fullOption.minVersion,
                        fullOption.maxVersion,
                        fullOption.option,
                        selectedJavaHome))
	                .map(fullOption -> fullOption.option)
	                .collect(toList());

        Map<String, String> propMap = jvmConfigReader.getPropMap();
        addJavaAgent(server, jvmConfigReader);

        // Try to find bootstraping jar - usually glassfish.jar
        File bootstrapJar = getJarName(server.getServerHome(), GFV3_JAR_MATCHER);
        if (bootstrapJar == null) {
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "noBootstrapJar"));
        }

        // Compute classpath using properties from jvm-config element of domain.xml
        String classPath = computeClassPath(propMap, new File(domainAbsolutePath), bootstrapJar);

        StringBuilder javaOpts = new StringBuilder(1024);
        StringBuilder glassfishArgs = new StringBuilder(256);

        // Preparing variables to replace placeholders in options
        Map<String, String> varMap = varMap(server, args.getJavaHome());

        // Add debug parameters read from domain.xml.
        // It's important to add them before java options specified by user
        // in case users specified it themselves
        if (mode.equals(DEBUG)) {
            String debugOptions = propMap.get("debug-options");

            // Set suspend to "y" if it's "n"
            if (suspendOnStart) {
                Matcher debugSuspendMatcher = debugSuspendPattern.matcher(debugOptions);
                StringBuffer buf = new StringBuffer();
                while (debugSuspendMatcher.find()) {
                    debugSuspendMatcher.appendReplacement(buf,
                        debugOptions.substring(debugSuspendMatcher.start(), debugSuspendMatcher.start(1)) +
                        "y" +
                        debugOptions.substring(debugSuspendMatcher.end(1), debugSuspendMatcher.end()));
                }

                debugOptions = debugSuspendMatcher.appendTail(buf).toString();
            }

            optList.addAll(asList(debugOptions.split("\\s+(?=-)")));
        }

        // Appending IDE specified options after the ones got from domain.xml
        // IDE specified are taking precedence this way
        if (args.getJavaArgs() != null) {
            optList.addAll(args.getJavaArgs());
        }
        appendOptions(javaOpts, optList, varMap);
        appendVarMap(javaOpts, varMap);
        if (args.getGlassfishArgs() != null) {
            appendGlassfishArgs(glassfishArgs, args.getGlassfishArgs());
        }

        // Starting the server using command

        try {
            return ServerAdmin.<ResultProcess>exec(server,
                    new CommandStartDAS(
                            args.getJavaHome(),
                            classPath,
                            javaOpts.toString(),
                            glassfishArgs.toString(),
                            domainAbsolutePath))
                    .get();
        } catch (InterruptedException | ExecutionException e) {
            throw new PayaraIdeException(LOGGER.excMsg(METHOD, "failed"), e);
        }
    }

    private static JDK.Version getJavaVersion(StartupArgs args) {
        String javaHome = System.getProperty("java.home");
        Path defaultPath = Paths.get(javaHome);
        Path selectedPath = Paths.get(args.getJavaHome());

        if (selectedPath.equals(defaultPath)
                || (javaHome.endsWith("jre") && selectedPath.equals(defaultPath.getParent()))) {
            return JDK_VERSION;
        }

        return JavaUtils.getJavaVersion(args.getJavaHome());
    }

    public static Integer getDebugPort(ResultProcess process) {
        Matcher debugPortMatcher = debugPortPattern.matcher(process.getValue().getArguments());
        if (debugPortMatcher.find()) {
            return Integer.parseInt(debugPortMatcher.group(1));
        }

        throw new IllegalArgumentException("Debug port not found in process args!");
    }

    /**
     * Build server variables map.
     * <p/>
     *
     * @param server GlassFish server entity
     * @param javaHome Java SE JDK home used to run Glassfish.
     */
    private static Map<String, String> varMap(PayaraServer server, String javaHome) {
        HashMap<String, String> varMap = new HashMap<>();

        varMap.put(GF_HOME_PROPERTY, server.getServerHome());
        varMap.put(GF_DOMAIN_ROOT_PROPERTY, getDomainPath(server));
        varMap.put(GF_JAVA_ROOT_PROPERTY, javaHome);
        varMap.put(GF_DERBY_ROOT_PROPERTY, getDerbyRoot(server));

        return varMap;
    }

    /**
     * Add java agents into server options.
     * <p/>
     *
     * @param server GlassFish server entity.
     * @param jvmConfigReader Contains <code>jvm-options</code> from <code>domain.xwl</code>.
     */
    private static void addJavaAgent(PayaraServer server, JvmConfigReader jvmConfigReader) {
    	List<JvmOption> optList = jvmConfigReader.getJvmOptions();
        File serverHome = new File(server.getServerHome());
        File btrace = new File(serverHome, "lib/monitor/btrace-agent.jar");
        File flight = new File(serverHome, "lib/monitor/flashlight-agent.jar");
        if (jvmConfigReader.isMonitoringEnabled()) {
            if (btrace.exists()) {
                optList.add(new JvmOption("-javaagent:" + Utils.quote(btrace.getAbsolutePath()) + "=unsafe=true,noServer=true")); // NOI18N
            } else if (flight.exists()) {
                optList.add(new JvmOption("-javaagent:" + Utils.quote(flight.getAbsolutePath())));
            }
        }
    }

    /**
     * Adds server variables from variables map into Java VM options for server startup.
     * <p/>
     *
     * @param javaOpts Java VM options {@link StringBuilder} instance.
     * @param varMap Server variables map.
     */
    private static void appendVarMap(StringBuilder javaOpts, Map<String, String> varMap) {
        for (Map.Entry<String, String> entry : varMap.entrySet()) {
            javaOpts.append(' ');
            JavaUtils.systemProperty(javaOpts, entry.getKey(), entry.getValue());
        }
    }

    private static JavaVersion getJavaVersion(String javaHome) {
        return javaVmVersion(new File(javaVmExecutableFullPath(javaHome)));
    }

    /**
     * Computing class path for <code>-cp</code> option of java.
     * <p/>
     *
     * @param propMap Attributes of <code>jvm-config</code> element of <code>domain.xml</code>.
     * @param domainDir Relative paths will be added to this directory.
     * @param bootstrapJar Bootstrap jar will be also added to class path.
     * @return Class path for <code>-cp</code> option of java.
     */
    private static String computeClassPath(Map<String, String> propMap, File domainDir, File bootstrapJar) {
        final String METHOD = "computeClassPath";
        String result = null;
        List<File> prefixCP = Utils.classPathToFileList(propMap.get("classpath-prefix"), domainDir);
        List<File> suffixCP = Utils.classPathToFileList(propMap.get("classpath-suffix"), domainDir);
        boolean useEnvCP = "false".equals(propMap.get("env-classpath-ignored"));
        List<File> envCP = Utils.classPathToFileList(useEnvCP ? System.getenv("CLASSPATH") : null, domainDir);
        List<File> systemCP = Utils.classPathToFileList(propMap.get("system-classpath"), domainDir);

        if (prefixCP.size() > 0 || suffixCP.size() > 0 || envCP.size() > 0 || systemCP.size() > 0) {
            List<File> mainCP = Utils.classPathToFileList(bootstrapJar.getAbsolutePath(), null);

            if (mainCP.size() > 0) {
                List<File> completeCP = new ArrayList<>(32);
                completeCP.addAll(prefixCP);
                completeCP.addAll(mainCP);
                completeCP.addAll(systemCP);
                completeCP.addAll(envCP);
                completeCP.addAll(suffixCP);

                // Build classpath in proper order - prefix / main / system
                // / environment / suffix
                // Note that completeCP should always have at least 2 elements
                // at this point (1 from mainCP and 1 from some other CP
                // modifier)
                StringBuilder classPath = new StringBuilder(1024);
                Iterator<File> iter = completeCP.iterator();
                classPath.append(Utils.quote(iter.next().getPath()));
                while (iter.hasNext()) {
                    classPath.append(File.pathSeparatorChar);
                    classPath.append(Utils.quote(iter.next().getPath()));
                }
                result = classPath.toString();
            } else {
                LOGGER.log(Level.WARNING, METHOD, "cpError");
            }
        }
        return result;
    }

    /**
     * Takes an list of java options and produces a valid string that can be put on command line.
     * <p/>
     * There are two kinds of options that can be found in option list: <code>key=value</code> and
     * simple options not containing <code>=</code>. In the list there are both options from domain.xml
     * and users options. Thus some of them can be there more than once. For <code>key=value</code> ones
     * we can detect it and only the latest one in list will be appended to command-line. For simple
     * once maybe some duplicate detection will be added in the future.
     * <p/>
     *
     * @param argumentBuf Returned string.
     * @param optList List of java options.
     * @param varMap Map to be used for replacing place holders, Contains <i>place holder</i> - <i>place
     * holder</i> value pairs.
     */
    private static void appendOptions(StringBuilder argumentBuf,
            List<String> optList, Map<String, String> varMap) {
        final String METHOD = "appendOptions";
        List<String> moduleOptions = new ArrayList<>();
        Map<String, String> keyValueArgs = new HashMap<>();
        List<String> keyOrder = new LinkedList<>();
        String name, value;

        // First process optList acquired from domain.xml
        for (String opt : optList) {
            // do placeholder substitution
            opt = Utils.doSub(opt.trim(), varMap);

            int splitIndex = opt.indexOf('=');

            // && !opt.startsWith("-agentpath:") is a temporary hack to
            // not touch already quoted -agentpath. Later we should handle it
            // in a better way.
            if (splitIndex != -1 && !opt.startsWith("-agentpath:")) {

                // key=value type of option

                name = opt.substring(0, splitIndex);
                value = Utils.quote(opt.substring(splitIndex + 1));
                LOGGER.log(Level.FINER, METHOD,
                        "jvmOptVal", new Object[] { name, value });

            } else if (opt.startsWith("-Xbootclasspath")) {

                // -Xbootclasspath:<path> or -Xbootclasspath/p:<path> or -Xbootclasspath/a:<path>

                name = opt;
                value = null;

                int colonIndex = opt.indexOf(':');
                if (colonIndex != -1) {
                    String optionName = opt.substring(0, colonIndex);
                    String optonValue = Utils.quote(opt.substring(colonIndex + 1));

                    name = optionName + ":" + optonValue;
                }
            } else {
                name = opt;
                value = null;
                LOGGER.log(Level.FINER, METHOD, "jvmOpt", name);
            }

            // seperate modules options
            if (MULTI_VALUE_OPTIONS.contains(name)) {
                moduleOptions.add(opt);
            } else {
                if (!keyValueArgs.containsKey(name)) {
                    keyOrder.add(name);
                }
                keyValueArgs.put(name, value);
            }
        }

        // Override the values that are found in the domain.xml file.
        // this is totally a copy/paste from StartTomcat...
       final String[] PROXY_PROPS = {
            "http.proxyHost",
            "http.proxyPort",
            "http.nonProxyHosts",
            "https.proxyHost",
            "https.proxyPort",
        };
        boolean isWindows = OsUtils.isWin();
        for (String prop : PROXY_PROPS) {
            value = System.getProperty(prop);
            if (value != null && value.trim().length() > 0) {
                if (isWindows && "http.nonProxyHosts".equals(prop)) {
                    // enclose in double quotes to escape the pipes separating
                    // the hosts on windows
                    value = "\"" + value + "\"";
                }
                keyValueArgs.put(JavaUtils.systemPropertyName(prop), value);
            }
        }
        // appending module options --add-modules --add-opens --add-exports
        argumentBuf.append(String.join(" ", moduleOptions));

        // Appending key=value options to the command line argument
        // using the same order as they came in argument - important!
        for (String key : keyOrder) {
            argumentBuf.append(' ');
            argumentBuf.append(key);
            if (keyValueArgs.get(key) != null) {
                argumentBuf.append("=");
                argumentBuf.append(keyValueArgs.get(key));
            }
        }
    }

    /**
     * Append GlassFish startup arguments to given {@link StringBuilder}.
     * <p/>
     *
     * @param glassfishArgs Target {@link StringBuilder} to append arguments.
     * @param glassfishArgsList Arguments to be appended.
     */
    private static void appendGlassfishArgs(StringBuilder glassfishArgs, List<String> glassfishArgsList) {
        for (String arg : glassfishArgsList) {
            glassfishArgs.append(' ');
            glassfishArgs.append(arg);
        }

        // Remove the first space
        if (glassfishArgs.length() > 0) {
            glassfishArgs.deleteCharAt(0);
        }
    }
}
