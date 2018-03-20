/******************************************************************************
 * Copyright (c) 2018 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.payara.tools.utils;

import static org.eclipse.payara.tools.GlassfishToolsPlugin.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.Bundle;

/**
 * Utility methods that are helpful for implementing extension points.
 *
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PluginUtil
{
    private static final String ATTR_BUNDLE_VERSION = "Bundle-Version";
    private static final String PLUGIN_LOCATION_PREFIX = "reference:file:";

    public static final class InvalidExtensionException

        extends Exception

    {
        private static final long serialVersionUID = 1L;
    }

    private PluginUtil() {}

    public static Collection<IExtension> findExtensions( final String pluginId,
                                                         final String extensionPointId )
    {
        final IExtensionRegistry registry = Platform.getExtensionRegistry();
        final IExtensionPoint point = registry.getExtensionPoint( pluginId, extensionPointId );

        if( point == null )
        {
            throw new RuntimeException();
        }

        final List<IExtension> extensions = new ArrayList<IExtension>();

        for( IExtension extension : point.getExtensions() )
        {
            extensions.add( extension );
        }

        return extensions;
    }

    public static Collection<IConfigurationElement> getTopLevelElements( final Collection<IExtension> extensions )
    {
        final List<IConfigurationElement> elements = new ArrayList<IConfigurationElement>();

        for( IExtension extension : extensions )
        {
            for( IConfigurationElement element : extension.getConfigurationElements() )
            {
                elements.add( element );
            }
        }

        return elements;
    }

    public static void reportInvalildElement( final IExtension extension,
                                              final IConfigurationElement element )
    {
        final String msg
            = NLS.bind( Resources.invalidElement, extension.getSimpleIdentifier(), element.getName() );

        logError( extension.getContributor().getName(), msg );
    }

    public static void reportMissingAttribute( final IConfigurationElement el,
                                               final String attribute )
    {
        final String msg
            = NLS.bind( Resources.missingAttribute, el.getName(), attribute );

        logError( el.getContributor().getName(), msg );
    }

    public static void reportMissingElement( final IConfigurationElement el,
                                             final String element )
    {
        final String msg
            = NLS.bind( Resources.missingElement, el.getName(), element );

        logError( el.getContributor().getName(), msg );
    }
    
    private static void logError( final String bundle, final String message )
    {
        logError( bundle, message, null );
    }

    private static void logError( final String bundle, final String message, final Exception e )
    {
        log( new Status( IStatus.ERROR, bundle, 0, message, e ) );
    }
    
    public static String findRequiredAttribute( final IConfigurationElement el,
                                                final String attribute )

        throws InvalidExtensionException

    {
        final String val = el.getAttribute( attribute );

        if( val == null )
        {
            reportMissingAttribute( el, attribute );
            throw new InvalidExtensionException();
        }

        return val;
    }
    
    public static String findOptionalAttribute( final IConfigurationElement el,
    		final String attribute ) {
    	final String val = el.getAttribute( attribute );
    	return val;
    }


    public static IConfigurationElement findRequiredElement( final IConfigurationElement el,
                                                             final String childElement )

        throws InvalidExtensionException

    {
        final IConfigurationElement[] children = el.getChildren( childElement );

        if( children.length == 0 )
        {
            reportMissingElement( el, childElement );
            throw new InvalidExtensionException();
        }

        return children[ 0 ];
    }

    public static IConfigurationElement findOptionalElement( final IConfigurationElement el,
                                                             final String childElement )
    {
        final IConfigurationElement[] children = el.getChildren( childElement );

        if( children.length == 0 )
        {
            return null;
        }
        else
        {
            return children[ 0 ];
        }
    }
    
    public static String getElementValue( final IConfigurationElement el,
                                          final String defaultValue )
    {
        if( el != null )
        {
            String text = el.getValue();
            
            if( text != null )
            {
                text = text.trim();
                
                if( text.length() > 0 )
                {
                    return text;
                }
            }
        }
        
        return defaultValue;
    }
    
    public static <T> Class<T> loadClass( final String pluginId,
                                          final String clname )
    {
        return loadClass( pluginId, clname, null );
    }

    @SuppressWarnings( "unchecked" )
    public static <T> Class<T> loadClass( final String pluginId,
                                          final String clname,
                                          final Class<T> interfc )
    {
        final Bundle bundle = Platform.getBundle( pluginId );
        final Class<?> cl;

        try
        {
            cl = bundle.loadClass( clname );
        }
        catch( Exception e )
        {
            final String msg
                = Resources.bind( Resources.failedToLoadClass, clname, pluginId );

            logError( pluginId, msg, e );

            return null;
        }

        if( interfc != null && ! interfc.isAssignableFrom( cl ) )
        {
            final String msg
                = Resources.bind( Resources.doesNotImplement, clname,
                                  interfc.getName() );

            logError( pluginId, msg );

            return null;
        }

        return (Class<T>) cl;
    }

    public static <T> T instantiate( final String pluginId,
                                     final Class<T> cl )
    {
        try
        {
            return cl.newInstance();
        }
        catch( Exception e )
        {
            final String msg
                = NLS.bind( Resources.failedToInstantiate, cl.getName(), pluginId );

            logError( pluginId, msg, e );

            return null;
        }
    }

    public static <T> T instantiate( final String pluginId,
                                     final String clname )
    {
        return instantiate( pluginId, clname, (Class<T>) null );
    }

    public static <T> T instantiate( final String pluginId,
                                     final String clname,
                                     final Class<T> interfc )
    {
        final Class<T> cl = loadClass( pluginId, clname, interfc );

        if( cl == null )
        {
            return null;
        }

        return instantiate( pluginId, cl );
    }

    public static String getPluginVersion( final String pluginId )
    {
        final Bundle bundle = Platform.getBundle( pluginId );
        return bundle.getHeaders().get( ATTR_BUNDLE_VERSION );
    }

    public static boolean waitForPluginToActivate( final String bundleId )
    {
        final Bundle bundle = Platform.getBundle( bundleId );
        return waitForPluginToActivate( bundle );
    }

    public static boolean waitForPluginToActivate( final Bundle bundle )
    {
        while( bundle.getState() != Bundle.ACTIVE )
        {
            try
            {
                Thread.sleep( 500 );
            }
            catch( InterruptedException e )
            {
                return false;
            }
        }

        return true;
    }

    public static IPath getPluginLocation( final String bundleId )
    {
        final Bundle bundle = Platform.getBundle( bundleId );
        return getPluginLocation( bundle );
    }

    public static IPath getPluginLocation( final Bundle bundle )
    {
        String location = bundle.getLocation();

        if( location.startsWith( PLUGIN_LOCATION_PREFIX ) )
        {
            int offset = PLUGIN_LOCATION_PREFIX.length();

            if( location.length() > offset + 1 && location.charAt( offset ) == '/' )
            {
                offset++;
            }

            location = location.substring( offset );
        }

        final Path pluginLocation = new Path( location );

        if( pluginLocation.isAbsolute() )
        {
            return pluginLocation;
        }
        else
        {
            final String installPath = Platform.getInstallLocation().getURL().getPath();
            return new Path( installPath + "/" + pluginLocation ); //$NON-NLS-1$
        }
    }

    public static final class ClassInfo
    {
        public final String pluginId;
        public final String className;

        public ClassInfo( final String pluginId,
                          final String className )
        {
            this.pluginId = pluginId;
            this.className = className;
        }
    }

    private static final class Resources

        extends NLS

    {
        public static String invalidElement;
        public static String missingAttribute;
        public static String missingElement;
        public static String failedToLoadClass;
        public static String failedToInstantiate;
        public static String doesNotImplement;

        static
        {
            initializeMessages( PluginUtil.class.getName(),
                                Resources.class );
        }
    }

}
