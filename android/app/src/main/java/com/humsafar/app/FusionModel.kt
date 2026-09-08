package com.humsafar.app

import android.content.Context
import com.google.mediapipe.tasks.genai.llminference.LlmInference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.time.Instant
import java.util.Locale
import java.util.UUID

// Tier 0 keeps the session in memory. The web rehearsal server remains separate and optional.
data class RawReport(val id:String=UUID.randomUUID().toString(), val reporter:String, val text:String, val language:String, val timestamp:String=Instant.now().toString(), val category:String?=null, val key:String?=null, val canonical:String?=null, val contradiction:Boolean=false, val urgency:Boolean=false)
data class FusedEvent(val id:String, val category:String, val canonical:String, val action:String, val confidence:Float, val evidence:List<RawReport>)

data class FusionResult(val category:String, val key:String, val canonical:String, val action:String, val contradiction:Boolean=false, val urgency:Boolean=false)

class FusionEngine(context:Context) {
    // Keep the model outside the APK; the 1GB+ asset is copied to app storage during
    // device setup so the demo build stays small and installable.
    private val modelFile=File(context.filesDir, "humsafar/gemma-2b-it-int4.task")
    private var inference:LlmInference?=null
    private val prompt="""You are the crowd-report fusion engine for an offline event-safety app.
Translate the new report to English if needed. Classify it into exactly one category:
parking, entry, food, washroom, safety, other.
Use the same key for reports about the same category and approximate location.
Merge corroborating reports. Mark contradiction=true when a report disputes an existing event.
For a new event use confidence 0.55. For a merge use min(0.5 + 0.07 * evidence_count, 0.98).
Safety reports set urgency=true and action to alert event safety staff immediately.

Worked examples:
New report: Gate 3 parking is completely full
Existing events: []
Output: {"category":"parking","key":"gate 3-parking","canonical":"Gate 3 parking is full","action":"Try another gate","contradiction":false,"urgency":false}
New report: Gate 3 pe parking bhar gaya hai
Existing events: [Gate 3 parking is full]
Output: {"category":"parking","key":"gate 3-parking","canonical":"Gate 3 parking is full","action":"Try another gate","contradiction":false,"urgency":false}
New report: Gate 3 parking still has some space near the back
Existing events: [Gate 3 parking is full]
Output: {"category":"parking","key":"gate 3-parking","canonical":"Gate 3 parking has some space near the back","action":"Check the rear of the parking area","contradiction":true,"urgency":false}

Respond with ONLY one JSON object, no markdown:
{"category":"...","key":"...","canonical":"...","action":"...","contradiction":false,"urgency":false}
"""

    init { if (modelFile.exists()) runCatching { inference=LlmInference.createFromOptions(context, LlmInference.LlmInferenceOptions.builder().setModelPath(modelFile.absolutePath).setMaxTokens(150).setTemperature(0.15f).build()) } }

    suspend fun analyze(report:String, existing:List<FusedEvent>):FusionResult=withContext(Dispatchers.Default){
        val context="\nExisting events: ${existing.joinToString { it.canonical }}\nNew report: $report"
        val raw=inference?.let { runCatching { it.generateResponse(prompt+context).trim() }.getOrNull() }
        parseJson(raw)?.let { return@withContext it }
        heuristic(report)
    }
    fun modelReady()=inference!=null
    private fun parseJson(raw:String?):FusionResult? { if(raw==null)return null;val block=Regex("\\{.*?\\}",RegexOption.DOT_MATCHES_ALL).find(raw)?.value?:return null;fun field(name:String)=Regex("\\\"$name\\\"\\s*:\\s*\\\"(.*?)\\\"",RegexOption.DOT_MATCHES_ALL).find(block)?.groupValues?.get(1);fun bool(name:String)=Regex("\\\"$name\\\"\\s*:\\s*(true|false)").find(block)?.groupValues?.get(1)=="true";val category=field("category")?:return null;return FusionResult(category,field("key")?:category,field("canonical")?:return null,field("action")?:"Keep an eye on this area",bool("contradiction"),bool("urgency"))}
    private fun heuristic(text:String):FusionResult { val lower=text.lowercase(Locale.getDefault());if(lower.matches(Regex(".*(injur|hurt|help|danger|fire|emergency|medical).*")))return FusionResult("safety",if("east" in lower)"east-injury" else "safety",if("east" in lower)"Someone needs help near the east entrance" else "A safety concern was reported","Alert event safety staff immediately",urgency=true);if(lower.matches(Regex(".*(park|parking|gate [0-9]).*"))){val gate=Regex("gate\\s*[0-9]").find(lower)?.value?:"the gate";val full=Regex(".*(full|bhar|no space|packed).*" ).matches(lower);return FusionResult("parking","$gate-parking","${gate.replaceFirstChar{it.uppercase()}} parking ${if(full)"is full" else "has some space near the back"}",if(full)"Try another gate" else "Check the rear of the parking area")};if(lower.matches(Regex(".*(food|counter|queue|wait|meal|snack).*")))return FusionResult("food",if("north" in lower)"north-food" else "food",if("north" in lower)"North food counter has a 5 minute wait" else "A food counter has a queue","Try another food counter");if(lower.matches(Regex(".*(wash|toilet|restroom|bathroom).*")))return FusionResult("washroom","washroom","A washroom queue was reported","Try the next washroom");return FusionResult("other","other",text.trim(),"Keep an eye on this area") }
    fun close(){inference?.close()}
}
