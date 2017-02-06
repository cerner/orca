package com.netflix.spinnaker.orca.clouddriver.tasks.providers.dcos

import com.netflix.spinnaker.orca.clouddriver.tasks.job.JobRunner
import com.netflix.spinnaker.orca.pipeline.model.Stage
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
class DcosJobRunner implements JobRunner {

  boolean katoResultExpected = false
  String cloudProvider = "dcos"

  @Override
  List<Map> getOperations(Stage stage) {
    def operation = [:]

    // If this stage was synthesized by a parallel deploy stage, the operation properties will be under 'cluster'.
    if (stage.context.containsKey("cluster")) {
      operation.putAll(stage.context.cluster as Map)
    } else {
      operation.putAll(stage.context)
    }

    DcosContainerFinder.populateFromStage(operation, stage)

    return [[(OPERATION): operation]]
  }
}

