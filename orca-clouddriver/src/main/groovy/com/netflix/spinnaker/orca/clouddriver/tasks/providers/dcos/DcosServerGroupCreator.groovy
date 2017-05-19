package com.netflix.spinnaker.orca.clouddriver.tasks.providers.dcos

import com.netflix.spinnaker.orca.clouddriver.tasks.servergroup.ServerGroupCreator
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
class DcosServerGroupCreator implements ServerGroupCreator {

  boolean katoResultExpected = false
  String cloudProvider = "dcos"

  @Override
  List<Map> getOperations(Stage stage) {
    def operation = [:]

    // TODO: this is side-effecty and not good... but it works.
    //
    // Have to do this here because during a deploy stage in a pipeline run, a disconnect between how the region is
    // sent in from deck (which may contain forward slashes) and how the region is formatted and written by clouddriver
    // (using underscores) causes the ParallelDeployStage to fail when trying to lookup server groups keyed by region.
    // The map contains a region with underscores, but the lookup occurs using a region with forward slashes.
    stage.context.region = stage.context.region.replaceAll('/', '_')

    // If this stage was synthesized by a parallel deploy stage, the operation properties will be under 'cluster'.
    if (stage.context.containsKey("cluster")) {
      operation.putAll(stage.context.cluster as Map)
    } else {
      operation.putAll(stage.context)
    }

    DcosContainerFinder.populateFromStage(operation, stage)

    return [[(OPERATION): operation]]
  }

  @Override
  Optional<String> getHealthProviderName() {
    return Optional.empty()
  }
}
