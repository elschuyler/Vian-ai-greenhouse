/*
 * Vian AI Greenhouse - VGH Tag Parser
 * PRD v4.3 Section 3.3 - 3.6: Tag format, extraction, fallback parsing
 * Parses @@VGH-PUSH-* and @@VGH-ATTACH-REPO-* tags from AI responses
 */

package com.jamal2367.styx.vgh.bridge

import com.jamal2367.styx.vgh.push.QueuedFilePush

data class VghPushTag(
    val filePath: String,
    val reason: String,
    val codeContent: String,
    val tier: Int = 1,
    val isComplete: Boolean = true
)

data class VghAttachTag(
    val url: String,
    val isMirrorLink: Boolean = false
)

class VghTagParser {

    companion object {
        private const val TAG_PREFIX = "@@VGH-"
        private const val PUSH_START = "VGH-PUSH-START"
        private const val PUSH_END = "VGH-PUSH-END"
        private const val ATTACH_REPO = "VGH-ATTACH-REPO"
    }

    // Tier 1: Forgiving Regex (PRD Section 3.6)
    private val startRegex = Regex(
        """@@VGH-PUSH-START\s*(?:\[file:\s*([^\]]+)\])?\s*(?:\[reason:\s*([^\]]*)\])?\s*(?:@@)?""",
        RegexOption.IGNORE_CASE
    )

    private val endRegex = Regex(
        """@@VGH-PUSH-END\s*(?:\[file:\s*([^\]]+)\])?\s*(?:@@)?""",
        RegexOption.IGNORE_CASE
    )

    private val attachRegex = Regex(
        """@@VGH-ATTACH-REPO\[url:([^\]]+)\]@@""",
        RegexOption.IGNORE_CASE
    )

    fun parsePushTags(htmlContent: String): List<VghPushTag> {
        val tags = mutableListOf<VghPushTag>()

        // Tier 1: Standard regex parsing
        val startMatches = startRegex.findAll(htmlContent)
        val endMatches = endRegex.findAll(htmlContent)

        startMatches.forEach { startMatch ->
            val filePath = startMatch.groupValues[1].trim()
            val reason = startMatch.groupValues[2].trim().ifEmpty { "Code update" }

            // Find matching end tag
            val endMatch = endMatches.find { end ->
                val endPath = end.groupValues[1].trim()
                endPath.isEmpty() || endPath == filePath || endPath.substringAfterLast('/') == filePath.substringAfterLast('/')
            }

            if (endMatch != null) {
                // Extract code content between tags
                val startIndex = startMatch.range.last + 1
                val endIndex = endMatch.range.first
                val codeContent = if (startIndex < endIndex && endIndex <= htmlContent.length) {
                    htmlContent.substring(startIndex, endIndex).trim()
                } else {
                    ""
                }

                if (codeContent.isNotEmpty()) {
                    tags.add(VghPushTag(
                        filePath = filePath,
                        reason = reason,
                        codeContent = codeContent,
                        tier = 1,
                        isComplete = true
                    ))
                }
            } else {
                // Tier 4: Dangling Start (truncation fallback)
                tags.add(VghPushTag(
                    filePath = filePath,
                    reason = reason,
                    codeContent = htmlContent.substring(startMatch.range.last + 1).trim(),
                    tier = 4,
                    isComplete = false
                ))
            }
        }

        return tags
    }

    fun parseAttachTags(htmlContent: String): List<VghAttachTag> {
        val tags = mutableListOf<VghAttachTag>()

        attachRegex.findAll(htmlContent).forEach { match ->
            val url = match.groupValues[1].trim()
            val isMirrorLink = url.contains("mirror-for-ai.vialewis31.workers.dev")

            tags.add(VghAttachTag(
                url = url,
                isMirrorLink = isMirrorLink
            ))
        }

        return tags
    }

    // Tier 5: Manual Selection Fallback (PRD Section 3.6)
    fun createManualPushTag(selectedText: String, filePath: String, reason: String): VghPushTag {
        return VghPushTag(
            filePath = filePath,
            reason = reason.ifEmpty { "Manual selection" },
            codeContent = selectedText,
            tier = 5,
            isComplete = true
        )
    }

    // Validate tag format (PRD Section 3.9)
    fun validateTagFormat(filePath: String, codeContent: String): TagValidationResult {
        if (filePath.isBlank()) {
            return TagValidationResult.Invalid("File path cannot be empty")
        }

        if (codeContent.isBlank()) {
            return TagValidationResult.Invalid("Code content cannot be empty")
        }

        if (filePath.contains("@@")) {
            return TagValidationResult.Invalid("File path cannot contain tag markers")
        }

        return TagValidationResult.Valid
    }

    sealed class TagValidationResult {
        object Valid : TagValidationResult()
        data class Invalid(val error: String) : TagValidationResult()
    }
}
