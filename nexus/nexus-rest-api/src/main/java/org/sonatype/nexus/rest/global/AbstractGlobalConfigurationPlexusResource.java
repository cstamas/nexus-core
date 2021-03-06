/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.rest.global;

import java.util.ArrayList;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.sonatype.nexus.configuration.application.AuthenticationInfoConverter;
import org.sonatype.nexus.configuration.application.GlobalHttpProxySettings;
import org.sonatype.nexus.configuration.application.GlobalRemoteConnectionSettings;
import org.sonatype.nexus.configuration.application.GlobalRestApiSettings;
import org.sonatype.nexus.configuration.model.CRemoteAuthentication;
import org.sonatype.nexus.configuration.model.CRemoteConnectionSettings;
import org.sonatype.nexus.configuration.model.CRemoteHttpProxySettings;
import org.sonatype.nexus.configuration.model.CRestApiSettings;
import org.sonatype.nexus.configuration.model.CSmtpConfiguration;
import org.sonatype.nexus.email.NexusEmailer;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.notification.NotificationCheat;
import org.sonatype.nexus.notification.NotificationManager;
import org.sonatype.nexus.notification.NotificationTarget;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.nexus.rest.model.AuthenticationSettings;
import org.sonatype.nexus.rest.model.ErrorReportingSettings;
import org.sonatype.nexus.rest.model.RemoteConnectionSettings;
import org.sonatype.nexus.rest.model.RemoteHttpProxySettings;
import org.sonatype.nexus.rest.model.RestApiSettings;
import org.sonatype.nexus.rest.model.SmtpSettings;
import org.sonatype.nexus.rest.model.SystemNotificationSettings;

/**
 * The base class for global configuration resources.
 * 
 * @author cstamas
 */
public abstract class AbstractGlobalConfigurationPlexusResource
    extends AbstractNexusPlexusResource
{
    public static final String SECURITY_OFF = "off";

    public static final String SECURITY_SIMPLE = "simple";

    public static final String SECURITY_CUSTOM = "custom";

    @Requirement
    private NexusEmailer nexusEmailer;

    @Requirement
    private GlobalHttpProxySettings globalHttpProxySettings;

    @Requirement
    private GlobalRemoteConnectionSettings globalRemoteConnectionSettings;

    @Requirement
    private GlobalRestApiSettings globalRestApiSettings;

    @Requirement
    private AuthenticationInfoConverter authenticationInfoConverter;

    @Requirement
    private ErrorReportingManager errorReportingManager;

    protected NexusEmailer getNexusEmailer()
    {
        return nexusEmailer;
    }

    protected GlobalHttpProxySettings getGlobalHttpProxySettings()
    {
        return globalHttpProxySettings;
    }

    protected GlobalRemoteConnectionSettings getGlobalRemoteConnectionSettings()
    {
        return globalRemoteConnectionSettings;
    }

    protected GlobalRestApiSettings getGlobalRestApiSettings()
    {
        return globalRestApiSettings;
    }

    protected AuthenticationInfoConverter getAuthenticationInfoConverter()
    {
        return authenticationInfoConverter;
    }

    protected ErrorReportingManager getErrorReportingManager()
    {
        return errorReportingManager;
    }

    public static SmtpSettings convert( NexusEmailer nexusEmailer )
    {
        if ( nexusEmailer == null )
        {
            return null;
        }

        SmtpSettings result = new SmtpSettings();

        result.setHost( nexusEmailer.getSMTPHostname() );

        result.setPort( nexusEmailer.getSMTPPort() );

        result.setSslEnabled( nexusEmailer.isSMTPSslEnabled() );

        result.setTlsEnabled( nexusEmailer.isSMTPTlsEnabled() );

        result.setUsername( nexusEmailer.getSMTPUsername() );

        if ( !StringUtils.isEmpty( nexusEmailer.getSMTPPassword() ) )
        {
            result.setPassword( PASSWORD_PLACE_HOLDER );
        }

        result.setSystemEmailAddress( nexusEmailer.getSMTPSystemEmailAddress().getMailAddress() );

        return result;
    }

    public static ErrorReportingSettings convert( ErrorReportingManager errorReportingManager )
    {
        ErrorReportingSettings result = new ErrorReportingSettings();

        result.setJiraUsername( errorReportingManager.getJIRAUsername() );
        if ( StringUtils.isEmpty( errorReportingManager.getJIRAPassword() ) )
        {
            result.setJiraPassword( errorReportingManager.getJIRAPassword() );
        }
        else
        {
            result.setJiraPassword( PASSWORD_PLACE_HOLDER );
        }
        result.setUseGlobalProxy( true );
        result.setReportErrorsAutomatically( errorReportingManager.isEnabled() );

        return result;
    }
    
    public static SystemNotificationSettings convert( NotificationManager manager )
    {
        if ( manager == null )
        {
            return null;
        }
        
        SystemNotificationSettings settings = new SystemNotificationSettings();
        settings.setEnabled( manager.isEnabled() );
        
        NotificationTarget target = manager.readNotificationTarget( NotificationCheat.AUTO_BLOCK_NOTIFICATION_GROUP_ID );
        
        if ( target == null )
        {
            return settings;
        }
        
        settings.getRoles().addAll( target.getTargetRoles() );
        
        StringBuffer sb = new StringBuffer();
        
        for ( String email : target.getExternalTargets() )
        {
            sb.append( email ).append( "," );
        }
        
        // drop last comma
        if ( sb.length() > 0 )
        {
            sb.setLength( sb.length() - 1 );
        }
        
        settings.setEmailAddresses( sb.toString() );
        
        return settings;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteConnectionSettings convert( GlobalRemoteConnectionSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteConnectionSettings result = new RemoteConnectionSettings();

        result.setConnectionTimeout( settings.getConnectionTimeout() / 1000 );

        result.setRetrievalRetryCount( settings.getRetrievalRetryCount() );

        result.setQueryString( settings.getQueryString() );

        result.setUserAgentString( settings.getUserAgentCustomizationString() );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteHttpProxySettings convert( GlobalHttpProxySettings settings )
    {
        if ( settings == null || !settings.isEnabled() )
        {
            return null;
        }

        RemoteHttpProxySettings result = new RemoteHttpProxySettings();

        result.setProxyHostname( settings.getHostname() );

        result.setProxyPort( settings.getPort() );

        result.setAuthentication( convert( settings.getProxyAuthentication() ) );

        result.setNonProxyHosts( new ArrayList<String>( settings.getNonProxyHosts() ) );

        return result;
    }

    public static RestApiSettings convert( GlobalRestApiSettings settings )
    {
        if ( settings == null || !settings.isEnabled() )
        {
            return null;
        }

        RestApiSettings result = new RestApiSettings();

        result.setBaseUrl( settings.getBaseUrl() );

        result.setForceBaseUrl( settings.isForceBaseUrl() );

        result.setUiTimeout( settings.getUITimeout() / 1000 );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static AuthenticationSettings convert( RemoteAuthenticationSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        AuthenticationSettings auth = new AuthenticationSettings();

        if ( settings instanceof ClientSSLRemoteAuthenticationSettings )
        {
            // huh?
        }
        else if ( settings instanceof NtlmRemoteAuthenticationSettings )
        {
            NtlmRemoteAuthenticationSettings up = (NtlmRemoteAuthenticationSettings) settings;

            auth.setUsername( up.getUsername() );

            auth.setPassword( PASSWORD_PLACE_HOLDER );

            auth.setNtlmHost( up.getNtlmHost() );

            auth.setNtlmDomain( up.getNtlmDomain() );

        }
        else if ( settings instanceof UsernamePasswordRemoteAuthenticationSettings )
        {
            UsernamePasswordRemoteAuthenticationSettings up = (UsernamePasswordRemoteAuthenticationSettings) settings;

            auth.setUsername( up.getUsername() );

            auth.setPassword( PASSWORD_PLACE_HOLDER );
        }

        return auth;
    }

    // ==

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteConnectionSettings convert( CRemoteConnectionSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteConnectionSettings result = new RemoteConnectionSettings();

        result.setConnectionTimeout( settings.getConnectionTimeout() / 1000 );

        result.setRetrievalRetryCount( settings.getRetrievalRetryCount() );

        result.setQueryString( settings.getQueryString() );

        result.setUserAgentString( settings.getUserAgentCustomizationString() );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static RemoteHttpProxySettings convert( CRemoteHttpProxySettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RemoteHttpProxySettings result = new RemoteHttpProxySettings();

        result.setProxyHostname( settings.getProxyHostname() );

        result.setProxyPort( settings.getProxyPort() );

        result.setAuthentication( convert( settings.getAuthentication() ) );

        result.setNonProxyHosts( settings.getNonProxyHosts() );

        return result;
    }

    public static RestApiSettings convert( CRestApiSettings settings )
    {
        if ( settings == null )
        {
            return null;
        }

        RestApiSettings result = new RestApiSettings();

        result.setBaseUrl( settings.getBaseUrl() );

        result.setForceBaseUrl( settings.isForceBaseUrl() );

        result.setUiTimeout( settings.getUiTimeout() / 1000 );

        return result;
    }

    /**
     * Externalized Nexus object to DTO's conversion.
     * 
     * @param resource
     */
    public static AuthenticationSettings convert( CRemoteAuthentication settings )
    {
        if ( settings == null )
        {
            return null;
        }

        AuthenticationSettings auth = new AuthenticationSettings();

        auth.setUsername( settings.getUsername() );

        auth.setPassword( PASSWORD_PLACE_HOLDER );

        auth.setNtlmHost( settings.getNtlmHost() );

        auth.setNtlmDomain( settings.getNtlmDomain() );

        // auth.setPrivateKey( settings.getPrivateKey() );

        // auth.setPassphrase( settings.getPassphrase() );

        return auth;
    }

    public static SmtpSettings convert( CSmtpConfiguration settings )
    {
        if ( settings == null )
        {
            return null;
        }

        SmtpSettings result = new SmtpSettings();

        result.setHost( settings.getHostname() );

        result.setPassword( PASSWORD_PLACE_HOLDER );

        result.setPort( settings.getPort() );

        result.setSslEnabled( settings.isSslEnabled() );

        result.setSystemEmailAddress( settings.getSystemEmailAddress() );

        result.setTlsEnabled( settings.isTlsEnabled() );

        result.setUsername( settings.getUsername() );

        return result;
    }

}
