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
package org.sonatype.nexus.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sonatype.scheduling.ScheduledTask;

public class DummyWaitingNexusTask
    implements NexusTask<Object>
{
    private boolean allowConcurrentSubmission = false;

    private boolean allowConcurrentExecution = false;

    private long sleepTime = 10000;

    private Map<String, String> parameters;

    private Object result;

    public boolean isExposed()
    {
        return true;
    }

    public void addParameter( String key, String value )
    {
        getParameters().put( key, value );
    }

    public String getParameter( String key )
    {
        return getParameters().get( key );
    }

    public Map<String, String> getParameters()
    {
        if ( parameters == null )
        {
            parameters = new HashMap<String, String>();
        }

        return parameters;
    }

    public boolean allowConcurrentSubmission( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        return allowConcurrentSubmission;
    }

    public boolean allowConcurrentExecution( Map<String, List<ScheduledTask<?>>> activeTasks )
    {
        return allowConcurrentExecution;
    }

    public void setAllowConcurrentSubmission( boolean allowConcurrentSubmission )
    {
        this.allowConcurrentSubmission = allowConcurrentSubmission;
    }

    public void setAllowConcurrentExecution( boolean allowConcurrentExecution )
    {
        this.allowConcurrentExecution = allowConcurrentExecution;
    }

    public long getSleepTime()
    {
        return sleepTime;
    }

    public void setSleepTime( long sleepTime )
    {
        this.sleepTime = sleepTime;
    }

    public void setResult( Object resul )
    {
        this.result = resul;
    }

    public Object call()
        throws Exception
    {
        System.out.println( "BEFORE SLEEP" );
        Thread.sleep( getSleepTime() );
        System.out.println( "AFTER SLEEP" );

        return result;
    }

    protected String getAction()
    {
        return "DUMMY";
    }

    protected String getMessage()
    {
        return "A Dummy task, waits for some time";
    }

    public String getId()
    {
        return "dummyId";
    }

    public String getName()
    {
        return "dummyName";
    }

    public boolean shouldSendAlertEmail()
    {
        return false;
    }

    public String getAlertEmail()
    {
        return null;
    }

    public TaskActivityDescriptor getTaskActivityDescriptor()
    {
        return null;
    }

}
