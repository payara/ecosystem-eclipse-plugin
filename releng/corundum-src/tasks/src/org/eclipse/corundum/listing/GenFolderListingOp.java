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

package org.eclipse.corundum.listing;

import static org.eclipse.corundum.FileUtil.write;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.corundum.ClassResourceLoader;
import org.eclipse.corundum.Operation;
import org.eclipse.corundum.OperationContext;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenFolderListingOp extends Operation {
    private static final long KB = 1024;
    private static final long MB = KB * 1024;
    private static final long GB = MB * 1024;

    private static final String NL = System.getProperty("line.separator");

    private static final ClassResourceLoader RESOURCE_LOADER = new ClassResourceLoader(GenFolderListingOp.class);
    private static final String LISTING_PAGE_TEMPLATE = RESOURCE_LOADER.resource("ListingPageTemplate.txt").text();
    private static final String LISTING_ENTRY_TEMPLATE = RESOURCE_LOADER.resource("ListingEntryTemplate.txt").text();
    private static final String LISTING_ENTRY_WITHOUT_SPARKLINE_TEMPLATE = RESOURCE_LOADER.resource("ListingEntryWithoutSparklineTemplate.txt").text();
    private static final String SUMMARY_ENTRY_TEMPLATE = RESOURCE_LOADER.resource("SummaryEntryTemplate.txt").text();

    private File folder;
    private final Set<File> excludes = new HashSet<>();

    public File getFolder() {
        return folder;
    }

    public void setFolder(File folder) {
        this.folder = folder;
    }

    public final Set<File> getExcludes() {
        return excludes;
    }

    @Override
    public void execute(OperationContext context) {
        generate(context, folder);
    }

    private Entry generate(OperationContext context, File target) {
        context.log("Generating listing for " + target.getPath());

        List<Entry> entries = new ArrayList<>();

        Date overallDateModified = new Date(0);
        long maxSize = 0;
        long totalSize = 0;
        int folderCount = 0;
        int fileCount = 0;

        for (File file : target.listFiles()) {
            boolean isFile = file.isFile();
            boolean isDirectory = file.isDirectory();
            String name = file.getName();

            if ((isFile || isDirectory) && !name.equals("index.html") && !this.excludes.contains(file)) {
                Entry entry;

                if (isFile) {
                    entry = new Entry(name, new Date(file.lastModified()), file.length());
                    fileCount++;
                } else {
                    entry = generate(context, file);
                    folderCount++;
                }

                entries.add(entry);

                Date dateModified = entry.getDateModified();

                if (dateModified.compareTo(overallDateModified) > 0) {
                    overallDateModified = dateModified;
                }

                long size = entry.getSize();

                if (size > maxSize) {
                    maxSize = size;
                }

                totalSize += size;
            }
        }

        Collections.sort(entries, (x, y) -> {
            int result = (x.isFolder() ? 0 : 1) - (y.isFolder() ? 0 : 1);

            if (result == 0) {
                result = x.getName().compareTo(y.getName());
            }

            return result;
        });

        int count = fileCount + folderCount;
        long segmentSize = maxSize / 200;
        StringBuilder listing = new StringBuilder();

        for (Entry entry : entries) {
            String block = (count > 1 ? LISTING_ENTRY_TEMPLATE : LISTING_ENTRY_WITHOUT_SPARKLINE_TEMPLATE)
                    .replace("${name}", entry.getName())
                    .replace("${href}", entry.getName() + (entry.isFolder() ? "/index.html" : ""))
                    .replace("${type}", entry.isFolder() ? "folder" : "file")
                    .replace("${size}", toSizeForDisplay(entry.getSize()));

            if (count > 1) {
                int segments = (int) Math.round((double) entry.getSize() / segmentSize);
                segments = segments == 0 ? 1 : segments;

                block = block.replace("${segments}", String.valueOf(segments));
            }

            append(listing, block, "\n");
        }

        StringBuilder summary = new StringBuilder();

        if (folderCount > 0) {
            appendSummaryLine(summary, "Folders", String.valueOf(folderCount));
        }

        if (fileCount > 0) {
            appendSummaryLine(summary, "Files", String.valueOf(fileCount));
        }

        appendSummaryLine(summary, "Size", toSizeForDisplay(totalSize));
        appendSummaryLine(summary, "Date Modified", new SimpleDateFormat("yyyy-MM-dd").format(overallDateModified));

        String text = LISTING_PAGE_TEMPLATE
                        .replace("${listing}", listing.toString())
                        .replace("${summary}", summary.toString())
                        .replace("\n", NL);

        try {
            write(context.file(new File(target, "index.html")), text);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return new Entry(target.getName(), overallDateModified, totalSize, true);
    }

    private static String toSizeForDisplay(long size) {
        String text;

        if (size < KB) {
            text = String.valueOf(size) + " b";
        } else if (size < MB) {
            text = String.valueOf(Math.round((double) size / KB)) + " kb";
        } else if (size < GB) {
            text = String.valueOf(Math.round((double) size / MB)) + " mb";
        } else {
            text = String.valueOf(Math.round((double) size / GB)) + " gb";
        }

        return text;
    }

    private static void appendSummaryLine(StringBuilder summary, String key, String value) {
        String block = SUMMARY_ENTRY_TEMPLATE
                        .replace("${key}", key)
                        .replace("${value}", value);

        append(summary, block, "\n");
    }

    private static void append(StringBuilder string, String segment, String separator) {
        if (string.length() > 0) {
            string.append(separator);
        }

        string.append(segment);
    }

    private static final class Entry {
        private final String name;
        private final long size;
        private final boolean folder;
        private final Date dateModified;

        public Entry(String name, Date dateModified, long size) {
            this(name, dateModified, size, false);
        }

        public Entry(String name, Date dateModified, long size, boolean folder) {
            if (name == null) {
                throw new IllegalArgumentException();
            }

            if (dateModified == null) {
                throw new IllegalArgumentException();
            }

            this.name = name;
            this.dateModified = dateModified;
            this.size = size;
            this.folder = folder;
        }

        public String getName() {
            return name;
        }

        public Date getDateModified() {
            return dateModified;
        }

        public long getSize() {
            return size;
        }

        public boolean isFolder() {
            return folder;
        }
    }

}