/*******************************************************************************
 * Copyright (c) 2007-2008 Sonatype Inc
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Eugene Kuleshov (Sonatype)
 *    Tam�s Cserven�k (Sonatype)
 *    Brian Fox (Sonatype)
 *    Jason Van Zyl (Sonatype)
 *******************************************************************************/
package org.sonatype.nexus.index.scan;

import java.io.File;

import org.sonatype.nexus.index.ArtifactScanningListener;
import org.sonatype.nexus.index.context.IndexingContext;

/** @author Jason van Zyl */
public interface ArtifactDiscoveryEvent
{
    IndexingContext getIndexingContext();
    
    /**
     * File can be pom, jar (including jars with the classifier), war, rar, etc. 
     */
    File getFile();
    
    ArtifactScanningListener getArtifactScanningListener();
}
