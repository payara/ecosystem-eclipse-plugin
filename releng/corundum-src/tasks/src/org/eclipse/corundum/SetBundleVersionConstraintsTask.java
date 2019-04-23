/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.corundum;

import static org.eclipse.corundum.BundleInfo.isValidBundle;
import static org.eclipse.corundum.SetBundleVersionConstraintsTask.RuleVariable.M1;
import static org.eclipse.corundum.SetBundleVersionConstraintsTask.RuleVariable.M2;
import static org.eclipse.corundum.SetBundleVersionConstraintsTask.RuleVariable.M3;
import static org.eclipse.corundum.SetBundleVersionConstraintsTask.RuleVariable.T1;
import static org.eclipse.corundum.SetBundleVersionConstraintsTask.RuleVariable.T2;
import static org.eclipse.corundum.SetBundleVersionConstraintsTask.RuleVariable.T3;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SetBundleVersionConstraintsTask extends AbstractTask {
    private static final String PROP_REQUIRE_BUNDLE = "Require-Bundle";
    private static final String ATTR_BUNDLE_VERSION = "bundle-version";

    private static final Rule DEFAULT_RULE = new Rule("*", "[M1.M2.M3,T1+1.0.0)");

    private File minPlatformInventoryFile;
    private File targetPlatformInventoryFile;
    private File pluginsDirectory;
    private boolean failIfVersionSpecified = true;
    private boolean failOnUnknownBundle = true;
    private final List<ExcludeEntry> excludes = new ArrayList<>();
    private final List<Rule> rules = new ArrayList<>();

    public void setMinPlatformInventory(File minPlatformInventory) {
        this.minPlatformInventoryFile = minPlatformInventory;
    }

    public void setTargetPlatformInventory(final File targetPlatformInventory) {
        this.targetPlatformInventoryFile = targetPlatformInventory;
    }

    public void setPluginsDirectory(File pluginsDirectory) {
        this.pluginsDirectory = pluginsDirectory;
    }

    public void setFailIfVersionSpecified(boolean failIfVersionSpecified) {
        this.failIfVersionSpecified = failIfVersionSpecified;
    }

    public void setFailOnUnknownBundle(boolean failOnUnknownBundle) {
        this.failOnUnknownBundle = failOnUnknownBundle;
    }

    public void setExcludes(String excludes) {
        this.excludes.clear();

        for (String entry : excludes.split(";")) {
            info("Excluding " + entry);
            this.excludes.add(new ExcludeEntry(entry));
        }
    }

    public ExcludeEntry createExclude() {
        ExcludeEntry exclude = new ExcludeEntry();
        excludes.add(exclude);
        
        return exclude;
    }

    private boolean isExcluded(String id) {
        for (ExcludeEntry entry : excludes) {
            String pattern = entry.getId();

            if (pattern != null && id.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    public void setRules(final String rules) {
        this.rules.clear();

        for (String entry : rules.split(";")) {
            String[] entryParts = entry.split("=");

            if (entryParts.length != 2) {
                throw new BuildException();
            }

            this.rules.add(new Rule(entryParts[0], entryParts[1]));
        }
    }

    public Rule createRule() {
        final Rule rule = new Rule();
        this.rules.add(rule);
        return rule;
    }

    private Rule getRule(final String bundleId) {
        for (Rule rule : this.rules) {
            if (rule.matchBundleId(bundleId)) {
                return rule;
            }
        }

        return DEFAULT_RULE;
    }

    @Override

    public void execute() throws BuildException {
        try {
            BundleInventory inventory = new BundleInventory();

            if (!pluginsDirectory.exists()) {
                fail(pluginsDirectory.toString() + " does not exist!");
            }

            if (pluginsDirectory.exists()) {
                for (File location : pluginsDirectory.listFiles()) {
                    if (isValidBundle(location)) {
                        try {
                            inventory.addBundle(new BundleInfo(location));
                        } catch (Exception e) {
                            if ("true".equals(System.getProperty("debug"))) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            BundleInventory minPlatformInventory = new BundleInventory();
            minPlatformInventory.read(this.minPlatformInventoryFile);

            BundleInventory targetPlatformInventory = new BundleInventory();
            targetPlatformInventory.read(this.targetPlatformInventoryFile);

            for (BundleInfo bundle : inventory.getBundles()) {
                String id = bundle.getId();

                if (isExcluded(id)) {
                    info(id + " : excluded");
                    continue;
                }

                File location = bundle.getLocation();

                String existingRequireBundle = ManifestUtil.readManifestEntry(location, PROP_REQUIRE_BUNDLE);

                if (existingRequireBundle == null) {
                    info(id + " : no Require-Bundle found");
                    continue;
                }

                info(id + " : processing...");

                List<ManifestBundlesListEntry> entries = ManifestBundlesListEntry.parse(existingRequireBundle);

                for (ManifestBundlesListEntry entry : entries) {
                    boolean hasVersionConstraint = false;

                    for (String attribute : entry.attributes()) {
                        if (attribute.startsWith(ATTR_BUNDLE_VERSION)) {
                            hasVersionConstraint = true;
                        }
                    }

                    if (hasVersionConstraint) {
                        String message = "Manifest of bundle " + bundle.getId() + " contains bundle version constraints!";

                        if (failIfVersionSpecified) {
                            fail(message);
                        } else {
                            error(message);
                            continue;
                        }
                    }

                    String bundleId = entry.bundle();

                    BundleInfo minPlatformBundleInfo;
                    BundleInfo targetPlatformBundleInfo;

                    BundleInfo productBundle = inventory.getBundle(bundleId);

                    if (productBundle != null) {
                        minPlatformBundleInfo = productBundle;
                        targetPlatformBundleInfo = productBundle;
                    } else {
                        minPlatformBundleInfo = minPlatformInventory.getBundle(bundleId);

                        if (minPlatformBundleInfo == null) {
                            String message = "Minimum platform inventory does not contain bundle " + bundleId;

                            if (failOnUnknownBundle) {
                                fail(message);
                            } else {
                                error(message);
                                continue;
                            }
                        }

                        targetPlatformBundleInfo = targetPlatformInventory.getBundle(bundleId);

                        if (targetPlatformBundleInfo == null) {
                            final String message = "Build platform inventory does not contain bundle " + bundleId;

                            if (this.failOnUnknownBundle) {
                                fail(message);
                            } else {
                                error(message);
                                continue;
                            }
                        }
                    }

                    BundleVersion minVersion = minPlatformBundleInfo.getVersion();
                    int minVersionLen = minVersion.length();
                    BundleVersion targetVersion = targetPlatformBundleInfo.getVersion();
                    int targetVersionLen = targetVersion.length();

                    Map<RuleVariable, Long> substitutions = new HashMap<>();
                    substitutions.put(M1, minVersion.segment(0));
                    substitutions.put(M2, minVersionLen > 1 ? minVersion.segment(1) : 0);
                    substitutions.put(M3, minVersionLen > 2 ? minVersion.segment(2) : 0);
                    substitutions.put(T1, targetVersion.segment(0));
                    substitutions.put(T2, targetVersionLen > 1 ? targetVersion.segment(1) : 0);
                    substitutions.put(T3, targetVersionLen > 2 ? targetVersion.segment(2) : 0);

                    String range = getRule(bundleId).evaluate(substitutions);

                    StringBuilder constraint = new StringBuilder();
                    constraint.append(ATTR_BUNDLE_VERSION);
                    constraint.append("=\"");
                    constraint.append(range);
                    constraint.append("\"");

                    entry.attributes().add(constraint.toString());
                }

                StringBuilder newRequireBundle = new StringBuilder();
                boolean isFirst = true;

                for (ManifestBundlesListEntry entry : entries) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        newRequireBundle.append(',');
                    }

                    newRequireBundle.append(entry.toString());
                }

                File manifestFile = new File(location, ManifestUtil.MANIFEST_PATH);

                ManifestUtil.setManifestEntry(manifestFile, PROP_REQUIRE_BUNDLE, newRequireBundle.toString());
            }
        } catch (IOException e) {
            throw new BuildException(e);
        }
    }

    public static final class ExcludeEntry {
        private String id;

        public ExcludeEntry() {
        }

        public ExcludeEntry(final String id) {
            this.id = id;
        }

        public String getId() {
            return this.id;
        }

        public void setId(final String id) {
            this.id = id;
        }
    }

    public enum RuleVariable {
        M1, M2, M3, T1, T2, T3
    }

    public static final class Rule {
        private String bundleId;
        private Pattern bundleIdPattern;
        private boolean startInclusive;
        private List<Particle> startVersion;
        private boolean endInclusive;
        private List<Particle> endVersion;

        public Rule() {
        }

        public Rule(final String bundleId, final String expr) {
            setBundle(bundleId);
            setExpr(expr);
        }

        public String getBundle() {
            return this.bundleId;
        }

        public void setBundle(final String bundleId) {
            this.bundleId = bundleId;

            final String bundleIdExpr = this.bundleId.replace(".", "\\.").replace("*", ".*");
            this.bundleIdPattern = Pattern.compile(bundleIdExpr);
        }

        public boolean matchBundleId(final String bundleId) {
            return (this.bundleIdPattern.matcher(bundleId).matches());
        }

        public void setExpr(final String expr) {
            if (expr.startsWith("[")) {
                this.startInclusive = true;
            } else if (expr.startsWith("(")) {
                this.startInclusive = false;
            } else {
                throw new IllegalArgumentException(expr);
            }

            if (expr.endsWith("]")) {
                this.endInclusive = true;
            } else if (expr.endsWith(")")) {
                this.endInclusive = false;
            } else {
                throw new IllegalArgumentException(expr);
            }

            final int comma = expr.indexOf(',');

            if (comma == -1) {
                throw new IllegalArgumentException(expr);
            }

            final String vStart = expr.substring(1, comma);
            final String vEnd = expr.substring(comma + 1, expr.length() - 1);

            this.startVersion = parseVersion(vStart);
            this.endVersion = parseVersion(vEnd);
        }

        private List<Particle> parseVersion(final String string) {
            final List<Particle> particles = new ArrayList<>();

            for (String particle : string.split("\\.")) {
                particles.add(parseParticle(particle));
            }

            return particles;
        }

        private Particle parseParticle(final String string) {
            if (string.indexOf('+') != -1) {
                final int operand = string.indexOf('+');
                final String aString = string.substring(0, operand).trim();
                final String bString = string.substring(operand + 1).trim();
                return new SumParticle(parseParticle(aString), parseParticle(bString));
            } else if (string.indexOf('-') != -1) {
                final int operand = string.indexOf('-');
                final String aString = string.substring(0, operand).trim();
                final String bString = string.substring(operand + 1).trim();
                return new DifferenceParticle(parseParticle(aString), parseParticle(bString));
            } else {
                RuleVariable variable = null;

                try {
                    variable = RuleVariable.valueOf(string);
                } catch (IllegalArgumentException e) {
                }

                if (variable == null) {
                    final long literal = Long.parseLong(string);
                    return new LiteralParticle(literal);
                } else {
                    return new VariableParticle(variable);
                }
            }
        }

        public String evaluate(final Map<RuleVariable, Long> substitutions) {
            final StringBuilder buf = new StringBuilder();

            buf.append(this.startInclusive ? '[' : '(');

            for (int i = 0, n = this.startVersion.size(); i < n; i++) {
                final Particle particle = this.startVersion.get(i);

                if (i > 0) {
                    buf.append('.');
                }

                buf.append(String.valueOf(particle.evaluate(substitutions)));
            }

            buf.append(',');

            for (int i = 0, n = this.endVersion.size(); i < n; i++) {
                final Particle particle = this.endVersion.get(i);

                if (i > 0) {
                    buf.append('.');
                }

                buf.append(String.valueOf(particle.evaluate(substitutions)));
            }

            buf.append(this.endInclusive ? ']' : ')');

            return buf.toString();
        }

        private static abstract class Particle {
            public abstract long evaluate(Map<RuleVariable, Long> substitutions);
        }

        private static final class LiteralParticle extends Particle {
            private final long literal;

            public LiteralParticle(final long literal) {
                this.literal = literal;
            }

            @Override
            public long evaluate(final Map<RuleVariable, Long> substitutions) {
                return this.literal;
            }
        }

        private static final class VariableParticle extends Particle {
            private final RuleVariable variable;

            public VariableParticle(final RuleVariable variable) {
                this.variable = variable;
            }

            @Override
            public long evaluate(final Map<RuleVariable, Long> substitutions) {
                return substitutions.get(this.variable);
            }
        }

        private static final class SumParticle extends Particle {
            private final Particle a;
            private final Particle b;

            public SumParticle(final Particle a, final Particle b) {
                this.a = a;
                this.b = b;
            }

            @Override
            public long evaluate(final Map<RuleVariable, Long> substitutions) {
                return this.a.evaluate(substitutions) + this.b.evaluate(substitutions);
            }
        }

        private static final class DifferenceParticle extends Particle {
            private final Particle a;
            private final Particle b;

            public DifferenceParticle(final Particle a, final Particle b) {
                this.a = a;
                this.b = b;
            }

            @Override
            public long evaluate(final Map<RuleVariable, Long> substitutions) {
                return this.a.evaluate(substitutions) - this.b.evaluate(substitutions);
            }
        }
    }

}
