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
package org.sonatype.nexus.proxy;

import java.io.IOException;

import org.sonatype.jettytestsuite.ServletServer;
import org.sonatype.nexus.proxy.item.StorageFileItem;
import org.sonatype.nexus.proxy.item.StorageItem;
import org.sonatype.nexus.proxy.repository.Repository;
import org.sonatype.nexus.proxy.repository.RepositoryWritePolicy;

public class RepositoryRedeployTest
    extends AbstractProxyTestEnvironment
{

    private M2TestsuiteEnvironmentBuilder jettyTestsuiteEnvironmentBuilder;

    @Override
    protected EnvironmentBuilder getEnvironmentBuilder()
        throws Exception
    {
        ServletServer ss = (ServletServer) lookup( ServletServer.ROLE );
        this.jettyTestsuiteEnvironmentBuilder = new M2TestsuiteEnvironmentBuilder( ss );
        return jettyTestsuiteEnvironmentBuilder;
    }

    protected Repository getRepository()
        throws NoSuchResourceStoreException, IOException
    {
        Repository repo1 = getRepositoryRegistry().getRepository( "repo1" );

        repo1.setWritePolicy( RepositoryWritePolicy.ALLOW_WRITE );
        
        getApplicationConfiguration().saveConfiguration();

        return repo1;
    }

    public void retrieveItem()
        throws Exception
    {
        StorageItem item = getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", false ) );

        checkForFileAndMatchContents( item );
    }

    public void testSimple()
        throws Exception
    {
        // get some initial proxied content
        retrieveItem();

        StorageFileItem item = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", true ) );

        ResourceStoreRequest to = new ResourceStoreRequest(
            "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy",
            true );

        getRepository().storeItem( to, item.getInputStream(), null );

        // and repeat all this
        item = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar", true ) );

        to = new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy", true );

        getRepository().storeItem( to, item.getInputStream(), null );

        StorageFileItem dest = (StorageFileItem) getRepository().retrieveItem(
            new ResourceStoreRequest( "/activemq/activemq-core/1.2/activemq-core-1.2.jar-copy", true ) );

        checkForFileAndMatchContents( dest, getRemoteFile( getRepositoryRegistry().getRepository(
            "repo1" ), "/activemq/activemq-core/1.2/activemq-core-1.2.jar" ) );

    }

}
