package com.netflix.spinnaker.orca.clouddriver.tasks.providers.dcos

import com.netflix.spinnaker.orca.pipeline.model.Pipeline
import com.netflix.spinnaker.orca.pipeline.model.Stage

class DcosContainerFinder {
  static void populateFromStage(Map operation, Stage stage) {
    // If this is a stage in a pipeline, look in the context for the baked image.
    def deploymentDetails = (stage.context.deploymentDetails ?: []) as List<Map>

    def imageDescription = (Map<String, Object>) operation.docker.image

    if (imageDescription.fromContext) {
      def image = deploymentDetails.find {
        // stageId is used here to match the source of the image to the find image stage specified by the user.
        // Previously, this was done by matching the pattern used to the pattern selected in the deploy stage, but
        // if the deploy stage's selected pattern wasn't updated before submitting the stage, this step here could fail.
        it.refId == imageDescription.stageId
      }
      if (!image) {
        throw new IllegalStateException("No image found in context for pattern $imageDescription.pattern.")
      } else {
        imageDescription = [registry: image.registry, tag: image.tag, repository: image.repository]
      }
    }

    if (imageDescription.fromTrigger) {
      if (stage.execution instanceof Pipeline) {
        Map trigger = ((Pipeline) stage.execution).trigger

        if (trigger?.account == imageDescription.account && trigger?.repository == imageDescription.repository) {
          imageDescription.tag = trigger.tag
        }
      }

      if (!imageDescription.tag) {
        throw new IllegalStateException("No tag found for image ${imageDescription.registry}/${imageDescription.repository} in trigger context.")
      }
    }
  }
}
