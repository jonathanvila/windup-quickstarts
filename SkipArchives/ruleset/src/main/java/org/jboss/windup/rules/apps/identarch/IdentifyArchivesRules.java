package org.jboss.windup.rules.apps.identarch;

import org.jboss.windup.rules.apps.identarch.model.IdentifiedArchiveModel;
import org.jboss.windup.rules.apps.identarch.model.GAV;
import java.util.logging.Logger;
import org.jboss.windup.config.AbstractRuleProvider;
import org.jboss.windup.config.GraphRewrite;

import org.jboss.windup.config.metadata.RuleMetadata;
import org.jboss.windup.config.operation.Iteration;
import org.jboss.windup.config.operation.iteration.AbstractIterationOperation;
import org.jboss.windup.config.phase.ArchiveMetadataExtractionPhase;
import org.jboss.windup.config.phase.InitialAnalysisPhase;
import org.jboss.windup.config.query.Query;
import org.jboss.windup.graph.GraphContext;
import org.jboss.windup.graph.model.ArchiveModel;
import org.jboss.windup.graph.service.GraphService;
import org.jboss.windup.rules.apps.identarch.lib.ArchiveGAVIdentifier;
import org.jboss.windup.rules.apps.identarch.model.GAVModel;
import org.jboss.windup.rules.apps.identarch.util.GraphService2;
import org.jboss.windup.util.Logging;
import org.ocpsoft.rewrite.config.Configuration;
import org.ocpsoft.rewrite.config.ConfigurationBuilder;
import org.ocpsoft.rewrite.context.EvaluationContext;

/**
 * Rules which support skipping certain archives by their G:A:V definition.
 * The purpose is to speed up processing of the scanned deployments.
 * The archive that is defined to be skipped (currently in a bundled text file)
 * is marked in the graph with a "w:skip" property.
 *
 * @author <a href="mailto:ozizka@redhat.com">Ondrej Zizka</a>
 */
@RuleMetadata(tags = "java", after = {ArchiveMetadataExtractionPhase.class, IdentifyArchivesLoadConfigRules.class}, phase = InitialAnalysisPhase.class)
public class IdentifyArchivesRules extends AbstractRuleProvider
{
    private static final Logger log = Logging.get(IdentifyArchivesRules.class);


    // @formatter:off
    @Override
    public Configuration getConfiguration(final GraphContext grCtx)
    {
        return ConfigurationBuilder.begin()

        // Check the jars
        .addRule()
        .when(Query.fromType(ArchiveModel.class))
        .perform(Iteration.over(ArchiveModel.class) // TODO: Use IteratingRuleProvider?
            .perform(new AbstractIterationOperation<ArchiveModel>()
                {
                    @Override
                    public void perform(GraphRewrite event, EvaluationContext evCtx, ArchiveModel arch)
                    {
                        log.fine("\tIdentArch identifying archive: " + arch.getFilePath());

                        GAV archiveGav = ArchiveGAVIdentifier.getGAVFromSHA1(arch.getSHA1Hash());
                        if (null == archiveGav)
                        {
                            log.fine("\tUnidentified archive: " + arch.getFilePath());
                            return;
                        }

                        // Store the identified GAV to the graph.
                        IdentifiedArchiveModel idArch = GraphService.addTypeToModel(grCtx, arch, IdentifiedArchiveModel.class);
                        // Copy to a real model.
                        GAVModel gavM = new GraphService2<>(event.getGraphContext(), GAVModel.class).merge(archiveGav);
                        idArch.setGAV(gavM);
                    }

                    @Override
                    public String toString()
                    {
                        return "Checking archives with IdentArch";
                    }
                }
            ).endIteration()
        ).withId("CheckArchivesWithSkipArchives");
    }
    // @formatter:on
}