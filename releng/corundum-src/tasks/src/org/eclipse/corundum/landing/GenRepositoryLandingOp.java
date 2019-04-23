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

package org.eclipse.corundum.landing;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.corundum.ClassResourceLoader;
import org.eclipse.corundum.DelegatingOperationContext;
import org.eclipse.corundum.FileUtil;
import org.eclipse.corundum.Operation;
import org.eclipse.corundum.OperationContext;
import org.eclipse.corundum.listing.GenFolderListingOp;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class GenRepositoryLandingOp extends Operation {
    private static final String NL = System.getProperty("line.separator");

    private static final ClassResourceLoader RESOURCE_LOADER = new ClassResourceLoader(GenRepositoryLandingOp.class);
    private static final String LANDING_PAGE_TEMPLATE = RESOURCE_LOADER.resource("LandingPageTemplate.txt").text();

    private File repository;
    private String name = "Repository";
    private final Set<File> excludes = new HashSet<>();

    public File getRepository() {
        return repository;
    }

    public void setRepository(File repository) {
        this.repository = repository;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? "Repository" : name;
    }

    public final Set<File> getExcludes() {
        return excludes;
    }

    @Override
    public void execute(OperationContext context) {
        File listingPage = context.file(new File(this.repository, "content.html"));

        if (listingPage.exists() && !listingPage.delete()) {
            throw new RuntimeException();
        }

        File imagesFolder = context.file(new File(this.repository, "images"));

        if (!imagesFolder.exists() && !imagesFolder.mkdirs()) {
            throw new RuntimeException();
        }

        GenFolderListingOp genFolderListingOp = new GenFolderListingOp();
        genFolderListingOp.setFolder(repository);
        genFolderListingOp.getExcludes().addAll(excludes);
        genFolderListingOp.getExcludes().add(imagesFolder);

        genFolderListingOp.execute(new DelegatingOperationContext(context) {
            @Override
            public File file(File file) {
                if (file.getParentFile().equals(GenRepositoryLandingOp.this.repository) && file.getName().equals("index.html")) {
                    return listingPage;
                }

                return super.file(file);
            }
        });

        context.log("Generating repository landing page for " + this.repository.getPath());

        RESOURCE_LOADER.resource("InstallDialog.png").copy(imagesFolder);

        String text = LANDING_PAGE_TEMPLATE.replace("${repository-name}", name).replace("\n", NL);

        try {
            FileUtil.write(context.file(new File(repository, "index.html")), text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}