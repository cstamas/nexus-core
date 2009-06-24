package org.sonatype.nexus.templates;

import java.util.List;

import org.codehaus.plexus.util.StringUtils;

public abstract class AbstractTemplateProvider<T extends Template>
    implements TemplateProvider<T>
{
    public T getTemplateById( String id )
        throws NoSuchTemplateIdException
    {
        // TODO: some other selection that simple iteration?
        List<T> templates = getTemplates();

        for ( T template : templates )
        {
            if ( StringUtils.equals( id, template.getId() ) )
            {
                return template;
            }
        }

        throw new NoSuchTemplateIdException( "Template for class='" + getTemplateClass().getName() + "' with Id='" + id
            + "' not found!" );
    }
}