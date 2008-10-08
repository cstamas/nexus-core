package org.sonatype.nexus.rest;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.RuntimeConstants;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.util.StringUtils;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.ext.velocity.TemplateRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.plexus.rest.resource.AbstractPlexusResource;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;

@Component( role = ManagedPlexusResource.class, hint = "indexTemplate" )
public class IndexTemplatePlexusResource
    extends AbstractPlexusResource
    implements ManagedPlexusResource
{
    @Requirement( role = NexusResourceBundle.class )
    private Map<String, NexusResourceBundle> bundles;

    public IndexTemplatePlexusResource()
    {
        super();

        setReadable( true );

        setModifiable( false );
    }

    @Override
    public Object getPayloadInstance()
    {
        // RO resource
        return null;
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return null;
    }

    @Override
    public String getResourceUri()
    {
        return "/index.html";
    }

    public List<Variant> getVariants()
    {
        List<Variant> result = super.getVariants();

        result.clear();

        result.add( new Variant( MediaType.APPLICATION_XHTML_XML ) );

        return result;
    }

    public Representation get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        return render( context, request, response, variant );
    }

    protected TemplateRepresentation render( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        Map<String, Object> templatingContext = new HashMap<String, Object>();

        templatingContext.put( "serviceBase", "service/local" );

        templatingContext.put( "contentBase", "content" );

        TemplateRepresentation templateRepresentation = getTemplateRepresentation( "index.vm", context );

        // gather plugin stuff

        Map<String, Object> topContext = new HashMap<String, Object>( templatingContext );

        Map<String, Object> pluginContext = null;

        List<String> pluginHeadContributions = new ArrayList<String>();

        List<String> pluginBodyContributions = new ArrayList<String>();

        for ( String key : bundles.keySet() )
        {
            pluginContext = new HashMap<String, Object>( topContext );

            NexusResourceBundle bundle = bundles.get( key );

            pluginContext.put( "bundle", bundle );

            pluginContext.put( "pluginResourcesBase", "plugin_resources/" + key );

            pluginContext.put( "pluginServicesBase", "service/local/plugins/" + key );

            evaluateIfNeeded(
                templateRepresentation.getEngine(),
                pluginContext,
                bundle.getHeadContribution(),
                pluginHeadContributions );

            evaluateIfNeeded(
                templateRepresentation.getEngine(),
                pluginContext,
                bundle.getBodyContribution(),
                pluginBodyContributions );
        }

        templatingContext.put( "pluginHeadContributions", pluginHeadContributions );

        templatingContext.put( "pluginBodyContributions", pluginBodyContributions );

        templateRepresentation.setDataModel( templatingContext );

        return templateRepresentation;
    }

    protected TemplateRepresentation getTemplateRepresentation( String templateName, Context context )
    {
        TemplateRepresentation templateRepresentation = new TemplateRepresentation( "index.vm", MediaType.TEXT_HTML );

        VelocityEngine engine = templateRepresentation.getEngine();

        engine.setProperty( RuntimeConstants.RUNTIME_LOG_LOGSYSTEM, new RestletLogChute( context ) );

        engine.setProperty( RuntimeConstants.RESOURCE_LOADER, "class" );

        engine.setProperty(
            "class.resource.loader.class",
            "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader" );

        return templateRepresentation;
    }

    protected void evaluateIfNeeded( VelocityEngine engine, Map<String, Object> context, String template,
        List<String> results )
        throws ResourceException
    {
        if ( !StringUtils.isEmpty( template ) )
        {
            StringWriter result = new StringWriter();

            try
            {
                if ( engine.evaluate( new VelocityContext( context ), result, getClass().getName(), template ) )
                {
                    results.add( result.toString() );
                }
                else
                {
                    throw new ResourceException(
                        Status.SERVER_ERROR_INTERNAL,
                        "Was not able to interpolate (check the logs for Velocity messages about the reason)!" );
                }
            }
            catch ( IOException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got IO exception during Velocity invocation!",
                    e );
            }
            catch ( ParseErrorException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got ParseErrorException exception during Velocity invocation!",
                    e );
            }
            catch ( MethodInvocationException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got MethodInvocationException exception during Velocity invocation!",
                    e );
            }
            catch ( ResourceNotFoundException e )
            {
                throw new ResourceException(
                    Status.SERVER_ERROR_INTERNAL,
                    "Got ResourceNotFoundException exception during Velocity invocation!",
                    e );
            }
        }
    }
}
