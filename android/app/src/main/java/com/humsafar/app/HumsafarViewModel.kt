package com.humsafar.app

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HumsafarViewModel(app:Application):AndroidViewModel(app) {
    private val engine=FusionEngine(app)
    val reports=mutableStateListOf<RawReport>()
    val events=mutableStateListOf<FusedEvent>()
    var processing=androidx.compose.runtime.mutableStateOf(false)
    var modelReady=androidx.compose.runtime.mutableStateOf(engine.modelReady())
    var notice=androidx.compose.runtime.mutableStateOf("")

    init { seed() }
    private fun seed(){
        listOf(
            RawReport("r_001","Reporter 1","Parking at Gate 3 is completely full","en"),
            RawReport("r_002","Reporter 2","Gate 3 pe parking bhar gaya hai","hi"),
            RawReport("r_003","Reporter 3","Gate 3 la parking full ah iruku, Gate 4 try pannunga","ta"),
            RawReport("r_004","Reporter 4","North food counter, about 5 minute wait","en"),
            RawReport("r_005","Reporter 5","Gate 3 parking still has some space near the back","hi",contradiction=true),
            RawReport("r_006","Reporter 6","Someone injured near the east entrance, needs help","en",urgency=true)
        ).forEach { addLocal(it) }
    }
    fun submit(reporter:String,text:String,language:String){if(text.isBlank())return;processing.value=true;notice.value="Local fusion engine is reading the report…";viewModelScope.launch{val result=engine.analyze(text,events);val report=RawReport(reporter=reporter,text=text,language=language,category=result.category,key=result.key,canonical=result.canonical,contradiction=result.contradiction,urgency=result.urgency);addLocal(report);processing.value=false;notice.value="Signal fused into the crowd feed."}}
    private fun addLocal(report:RawReport){reports.add(report);val key=report.key?:report.category?:"other";val index=events.indexOfFirst{it.id==key};if(index<0){events.add(FusedEvent(key,report.category?:"other",report.canonical?:report.text,"Keep an eye on this area",if(report.contradiction).38f else .57f,listOf(report)))}else{val old=events[index];val evidence=old.evidence+report;val contradictions=evidence.count{it.contradiction};val positive=evidence.size-contradictions;val confidence=(.5f+.07f*positive-.12f*contradictions).coerceIn(.38f,.98f);events[index]=old.copy(confidence=confidence,evidence=evidence)}}
    fun reset(){reports.clear();events.clear();notice.value="Room reset. Add the first observation."}
    override fun onCleared(){engine.close();super.onCleared()}
}
