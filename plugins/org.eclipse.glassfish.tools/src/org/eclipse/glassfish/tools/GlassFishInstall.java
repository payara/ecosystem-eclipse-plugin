/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.glassfish.tools;

import static org.eclipse.glassfish.tools.internal.ManifestUtil.readManifestEntry;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.DirectoryScanner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.glassfish.tools.internal.SystemLibrariesSetting;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.util.ListFactory;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntime;
import org.eclipse.wst.common.project.facet.core.runtime.IRuntimeComponent;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class GlassFishInstall
{
    private static final Pattern VERSION_PATTERN = Pattern.compile( "([0-9]\\.[0-9](\\.[0-9])?(\\.[0-9])?)(\\..*)?" );
    
    private static final String[] LIBRARIES_3_1 =
    {
         "glassfish/modules/javax.*.jar",
         "glassfish/modules/weld-osgi-bundle.jar",
         "glassfish/modules/bean-validator.jar",
         "glassfish/modules/jersey-*.jar",
         "glassfish/modules/grizzly-comet.jar",
         "glassfish/modules/grizzly-websockets.jar",
         "glassfish/modules/glassfish-api.jar",
         "glassfish/modules/ha-api.jar",
         "glassfish/modules/endorsed/*.jar",
         "glassfish/modules/jsf-api.jar",
         "glassfish/modules/jsf-impl.jar",
         "glassfish/modules/jstl-impl.jar",
         "glassfish/modules/org.eclipse.persistence*.jar",
         "glassfish/modules/jaxb*.jar",
         "glassfish/modules/webservices*.jar",
         "glassfish/modules/woodstox-osgi*.jar",
         "mq/lib/jaxm-api*.jar"
    };
    
    private static final String[] LIBRARIES_3_1_2 =
    {
         "glassfish/modules/javax.*.jar",
         "glassfish/modules/weld-osgi-bundle.jar",
         "glassfish/modules/bean-validator.jar",
         "glassfish/modules/jersey-*.jar",
         "glassfish/modules/grizzly-comet.jar",
         "glassfish/modules/grizzly-websockets.jar",
         "glassfish/modules/glassfish-api.jar",
         "glassfish/modules/ha-api.jar",
         "glassfish/modules/endorsed/*.jar",
         "glassfish/modules/org.eclipse.persistence*.jar",
         "glassfish/modules/jaxb*.jar",
         "glassfish/modules/webservices*.jar",
         "glassfish/modules/woodstox-osgi*.jar",
         "mq/lib/jaxm-api*.jar"
    };
    
    private static final String[] LIBRARIES_4 =
    {
        "glassfish/modules/javax.*.jar",
        "glassfish/modules/weld-osgi-bundle.jar",
        "glassfish/modules/bean-validator.jar",
        "glassfish/modules/jersey-*.jar",
        "glassfish/modules/glassfish-api.jar",
        "glassfish/modules/ha-api.jar",
        "glassfish/modules/endorsed/*.jar",
        "glassfish/modules/org.eclipse.persistence*.jar",
        "glassfish/modules/jaxb*.jar",
        "glassfish/modules/webservices*.jar",
        "glassfish/modules/cdi-api.jar",
        "mq/lib/jaxm-api.jar"        
    };

    private static final String[] LIBRARIES_5 =
    {
        "glassfish/modules/javax.*.jar",
        "glassfish/modules/weld-osgi-bundle.jar",
        "glassfish/modules/bean-validator.jar",
        "glassfish/modules/jersey-*.jar",
        "glassfish/modules/glassfish-api.jar",
        "glassfish/modules/ha-api.jar",
        "glassfish/modules/endorsed/*.jar",
        "glassfish/modules/org.eclipse.persistence*.jar",
        "glassfish/modules/jaxb*.jar",
        "glassfish/modules/webservices*.jar",
        "glassfish/modules/cdi-api.jar",
        "mq/lib/jaxm-api.jar"        
    };

    private String versionString = null;
    
    
    //Defined as:
    //<extension point="org.eclipse.wst.common.project.facet.core.runtimes">
    //<runtime-component-type id="glassfish"/>
    private static final String RUNTIME_COMPONENT_ID = "glassfish";//$NON-NLS-1$ 
    
    private static final Map<File,SoftReference<GlassFishInstall>> CACHE = new HashMap<File,SoftReference<GlassFishInstall>>();
    
    public static synchronized GlassFishInstall find( final File location )
    {
        for( Iterator<Map.Entry<File,SoftReference<GlassFishInstall>>> itr = CACHE.entrySet().iterator(); itr.hasNext(); )
        {
            if( itr.next().getValue().get() == null )
            {
                itr.remove();
            }
        }
        
        GlassFishInstall gf = null;
        
        if( location != null )
        {
            final SoftReference<GlassFishInstall> ref = CACHE.get( location );
            
            if( ref != null )
            {
                gf = ref.get();
            }
            
            if( gf == null )
            {
                try
                {
                    gf = new GlassFishInstall( location );
                }
                catch( IllegalArgumentException e )
                {
                    return null;
                }
                
                CACHE.put( location, new SoftReference<GlassFishInstall>( gf ) );
            }
        }
        
        return gf;
    }
    
    public static synchronized GlassFishInstall find( final IRuntimeComponent component )
    {
        if( component != null )
        {
        	if( component.getRuntimeComponentType().getId().equals( RUNTIME_COMPONENT_ID ) )
            {
                final String location = component.getProperty( "location" );
                
                if( location != null )
                {
                    return find( new File( location ) );
                }
            }
        }
        
        return null;
    }
    
    public static synchronized GlassFishInstall find( final IRuntime runtime )
    {
        if( runtime != null )
        {
            for( IRuntimeComponent component : runtime.getRuntimeComponents() )
            {
                final GlassFishInstall gf = find( component );
                
                if( gf != null )
                {
                    return gf;
                }
            }
        }
        
        return null;
    }
    
    public static synchronized GlassFishInstall find( final IFacetedProject project )
    {
        if( project != null )
        {
            final IRuntime primary = project.getPrimaryRuntime();
            
            if( primary != null )
            {
                GlassFishInstall gf = find( primary );
                
                if( gf != null )
                {
                    return gf;
                }
                
                for( IRuntime runtime : project.getTargetedRuntimes() )
                {
                    if( runtime != primary )
                    {
                        gf = find( runtime );
                        
                        if( gf != null )
                        {
                            return gf;
                        }
                    }
                }
            }
        }
        
        return null;
    }
    
    public static synchronized GlassFishInstall find( final IProject project )
    {
        if( project != null )
        {
            IFacetedProject fproj = null;
            
            try
            {
                fproj = ProjectFacetsManager.create( project );
            }
            catch( CoreException e )
            {
                // Intentionally ignored. If project isn't faceted or another error occurs, all that
                // matters is that GlassFish install is not found, which is signaled by null return.
            }
            
            if( fproj != null )
            {
                return find( fproj );
            }
        }
        
        return null;
    }

    public static synchronized GlassFishInstall find( final IJavaProject project )
    {
        if( project != null )
        {
            return find( project.getProject() );
        }
        
        return null;
    }
    
    private final File location;
    private final Version version;
    private final List<File> libraries;
    
    private GlassFishInstall( final File location )
    {
        if( location == null )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! location.exists() )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! location.isDirectory() )
        {
            throw new IllegalArgumentException();
        }
        
        final File gfApiJar = new File( location, "modules/glassfish-api.jar" );
        
        if( ! gfApiJar.exists() )
        {
            throw new IllegalArgumentException();
        }
        
        if( ! gfApiJar.isFile() )
        {
            throw new IllegalArgumentException();
        }
        
        
        try
        {
            versionString = readManifestEntry( gfApiJar, "Bundle-Version" );
        }
        catch( IOException e )
        {
            throw new IllegalArgumentException( e );
        }
        
        final Matcher versionMatcher = VERSION_PATTERN.matcher( versionString ); 
        
        if( ! versionMatcher.matches() )
        {
            throw new IllegalArgumentException();
        }
        
        final String partialVersionString = versionMatcher.group( 1 );
        
        this.location = location;
        this.version = new Version( partialVersionString );
        
        final ListFactory<File> librariesListFactory = ListFactory.start();
        final String[] libraryIncludes;
        
        if( this.version.matches( "[5" ) )
        {
            libraryIncludes = LIBRARIES_5;
        }
        else if( this.version.matches( "[4-5)" ) )
        {
            libraryIncludes = LIBRARIES_4;
        }
        else if( this.version.matches( "[3.1.2-4)" ) )
        {
            libraryIncludes = LIBRARIES_3_1_2;
        }
        else if( this.version.matches( "[3.1-3.1.2)" ) )
        {
            libraryIncludes = LIBRARIES_3_1;
        }
        else
        {
            libraryIncludes = null;
        }
        
        if( libraryIncludes != null )
        {
            final File parentFolderToLocation = this.location.getParentFile();
            final DirectoryScanner scanner = new DirectoryScanner();
            
            scanner.setBasedir( parentFolderToLocation );
            scanner.setIncludes( libraryIncludes );
            scanner.scan();
            
            for( String libraryRelativePath : scanner.getIncludedFiles() )
            {
                librariesListFactory.add( new File( parentFolderToLocation, libraryRelativePath ) );
            }
        }
        
        this.libraries = librariesListFactory.result();
    }
    
    public File location()
    {
        return this.location;
    }
    
    public Version version()
    {
        return this.version;
    }
    
    public String versionString(){
    	return this.versionString;
    }

    
    public List<File> libraries()
    {
        return this.libraries;
    }
    
    public List<IClasspathEntry> classpath(IProject proj)
    {
        final ListFactory<IClasspathEntry> classpathListFactory = ListFactory.start();
        
        final URL doc;
        final String v = ( this.version.matches( "[5" ) ? "8" : ( this.version.matches( "[4" ) ? "7" : "6" ) );

        try
        {
            doc = new URL( "http://docs.oracle.com/javaee/" + v + "/api/" );
        }
        catch( MalformedURLException e )
        {
            throw new RuntimeException( e );
        }
        
        SystemLibrariesSetting libSettings = SystemLibrariesSetting.load(proj);
        
        for( File library : this.libraries )
        {
        	File srcPath = libSettings!=null ? 
        			libSettings.getSourcePath( library ) : null;
            classpathListFactory.add( createLibraryEntry( new Path( library.toString() ), srcPath , doc ) );
        }
        
        List<IClasspathEntry> classpath = classpathListFactory.result();
        return classpath;
    }
    
    private static IClasspathEntry createLibraryEntry( final IPath library, final File src, final URL javadoc )
    {
        final IPath srcpath = src == null ? null : new Path( src.getAbsolutePath() );
        final IAccessRule[] access = {};
        final IClasspathAttribute[] attrs;
        
        if( javadoc == null )
        {
            attrs = new IClasspathAttribute[ 0 ];
        }
        else
        {
            attrs = new IClasspathAttribute[]
            { 
               JavaCore.newClasspathAttribute( IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, javadoc.toExternalForm() )
            };
        }
        
        return JavaCore.newLibraryEntry( library, srcpath, null, access, attrs, false );
    }

}
