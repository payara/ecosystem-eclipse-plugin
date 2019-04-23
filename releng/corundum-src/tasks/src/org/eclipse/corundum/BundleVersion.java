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

import static java.lang.Math.min;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class BundleVersion implements Comparable<BundleVersion> {
    private static final String SEPARATOR = ".";
    private static final String SEPARATOR_REGEX = "\\.";

    private long[] segments;

    public BundleVersion(final long... segments) {
        this.segments = segments;
    }

    public BundleVersion(String version) {
        if (version == null) {
            System.out.println("WARNING: Bundle veriosn is null");
            segments = new long[] { 1L, 0L, 0L };
            return;
        }
        
        String trimmed = version.trim();

        String[] stringSegments = trimmed.split(SEPARATOR_REGEX);
        int length = Math.min(stringSegments.length, 3);
        segments = new long[length];

        for (int i = 0; i < length; i++) {
            segments[i] = Long.parseLong(stringSegments[i]);
        }
    }

    public int length() {
        return segments.length;
    }

    public long segment(int position) {
        return segments[position];
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        for (long segment : segments) {
            if (buf.length() > 0) {
                buf.append(SEPARATOR);
            }
            buf.append(segment);
        }

        return buf.toString();
    }

    @Override
    public int hashCode() {
        int hashCode = 0;

        for (long segment : segments) {
            hashCode += segment;
        }

        return hashCode;
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof BundleVersion)) {
            return false;
        }

        final BundleVersion other = (BundleVersion) object;

        if (this.segments.length != other.segments.length) {
            return false;
        }

        for (int i = 0; i < this.segments.length; i++) {
            if (this.segments[i] != other.segments[i]) {
                return false;
            }
        }

        return true;
    }

    @Override
    public int compareTo(final BundleVersion other) {
        if (this == other) {
            return 0;
        }

        for (int i = 0, n = min(this.segments.length, other.segments.length); i < n; i++) {
            final long result = this.segments[i] - other.segments[i];

            if (result < 0) {
                return -1;
            } else if (result > 0) {
                return 1;
            }
        }

        if (this.segments.length > other.segments.length) {
            return 1;
        } else if (this.segments.length < other.segments.length) {
            return -1;
        }

        return 0;
    }
}
