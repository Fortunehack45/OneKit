package com.one.utility.core.workflow

import android.net.Uri

sealed class WorkflowStep(val name: String, val description: String) {
    object SelectImages : WorkflowStep("Select Images", "Choose photos to include")
    object CompressImages : WorkflowStep("Compress Images", "Optimize file sizes")
    object CreatePdf : WorkflowStep("Generate PDF", "Assemble into A4 document")
    object Complete : WorkflowStep("Finished", "Save or share output")
}

data class WorkflowState(
    val currentStepIndex: Int = 0,
    val steps: List<WorkflowStep> = listOf(
        WorkflowStep.SelectImages,
        WorkflowStep.CompressImages,
        WorkflowStep.CreatePdf,
        WorkflowStep.Complete
    ),
    val selectedUris: List<Uri> = emptyList(),
    val compressedUris: List<Uri> = emptyList(),
    val outputPdfPath: String? = null,
    val isProcessing: Boolean = false,
    val progress: Float = 0f
) {
    val currentStep: WorkflowStep get() = steps.getOrElse(currentStepIndex) { WorkflowStep.Complete }
    val isDone: Boolean get() = currentStepIndex >= steps.size - 1
}

class WorkflowEngine {
    fun createChainedWorkflow(stepNames: List<String>): List<WorkflowStep> {
        val mapped = mutableListOf<WorkflowStep>(WorkflowStep.SelectImages)
        stepNames.forEach { name ->
            when (name) {
                "compressor" -> mapped.add(WorkflowStep.CompressImages)
                "image_to_pdf" -> mapped.add(WorkflowStep.CreatePdf)
            }
        }
        mapped.add(WorkflowStep.Complete)
        return mapped
    }
}
