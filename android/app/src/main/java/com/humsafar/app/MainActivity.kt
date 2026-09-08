package com.humsafar.app

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val Ink=Color(0xFF09292B);private val Paper=Color(0xFFF7F8F3);private val Muted=Color(0xFF6F8584);private val Mint=Color(0xFFCDE8D8);private val Lime=Color(0xFFB8D941);private val Coral=Color(0xFFED755F)

class MainActivity:ComponentActivity(){
    private val permission=registerForActivityResult(ActivityResultContracts.RequestPermission()){}
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{MaterialTheme(colorScheme=lightColorScheme(primary=Ink,background=Paper)){HumsafarApp{if(checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED)permission.launch(Manifest.permission.RECORD_AUDIO)}}}}
}

@Composable fun HumsafarApp(requestMic:()->Unit,vm:HumsafarViewModel=viewModel()){var tab by remember{mutableStateOf(0)};Surface(color=Paper,modifier=Modifier.fillMaxSize()){Column{Header(vm.modelReady.value);when(tab){0->Feed(vm);1->Report(vm,requestMic);2->Insights(vm);3->EventMode(vm)}BottomTabs(tab){tab=it}}}}
@Composable fun Header(modelReady:Boolean){Column(Modifier.padding(horizontal=20.dp,vertical=18.dp)){Row(verticalAlignment=Alignment.CenterVertically){Text("humsafar",fontSize=25.sp,fontWeight=FontWeight.Bold,color=Ink);Spacer(Modifier.weight(1f));Text("●  ${if(modelReady)"LOCAL MODEL READY" else "DEMO FALLBACK"}",fontSize=10.sp,color=if(modelReady)Lime else Coral)}};Text("YOUR CROWD, THINKING TOGETHER.",fontSize=11.sp,color=Coral,fontWeight=FontWeight.Bold);Text("No cloud. No tower. No guesswork.",fontSize=14.sp,color=Muted)}}
@Composable fun Feed(vm:HumsafarViewModel){Column(Modifier.padding(horizontal=20.dp)){Row(verticalAlignment=Alignment.Bottom,modifier=Modifier.padding(vertical=18.dp)){Column{Text("VERIFIED SIGNALS",fontSize=10.sp,color=Muted);Text("What the crowd knows",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Ink)};Spacer(Modifier.weight(1f));TextButton(onClick=vm::reset){Text("Reset",color=Coral)}};LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)){items(vm.events,key={it.id}){event->EventCard(event)}}}}
@Composable fun EventCard(event:FusedEvent){var expanded by remember{mutableStateOf(false)};Card(colors=CardDefaults.cardColors(containerColor=Color.White),shape=RoundedCornerShape(2.dp),modifier=Modifier.fillMaxWidth().clickable{expanded=!expanded}){Column(Modifier.padding(18.dp)){Row{Text("${event.category.uppercase()}  ·  ${"%.0f".format(event.confidence*100)}%",fontSize=10.sp,color=Muted);Spacer(Modifier.weight(1f));Text("${event.evidence.size} reports",fontSize=10.sp,color=Muted)};Text(event.canonical,fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=Ink,modifier=Modifier.padding(vertical=12.dp));LinearProgressIndicator(progress={event.confidence},color=if(event.category=="safety")Coral else Lime,trackColor=Color(0xFFE9EFEA),modifier=Modifier.fillMaxWidth().height(5.dp));Text(if(event.category=="safety")"Alert event safety staff immediately" else "Evidence-backed crowd signal",fontSize=11.sp,color=Muted,modifier=Modifier.padding(top=12.dp));if(expanded){HorizontalDivider(Modifier.padding(vertical=12.dp));event.evidence.forEach{Text("${it.reporter} · ${it.language}: ${it.text}",fontSize=12.sp,color=Muted,modifier=Modifier.padding(vertical=3.dp))}}}}}
@Composable fun Report(vm:HumsafarViewModel,requestMic:()->Unit){var text by remember{mutableStateOf("")};var reporter by remember{mutableStateOf("Reporter 1")};var language by remember{mutableStateOf("en")};val context=LocalContext.current;Column(Modifier.padding(20.dp)){Text("ADD A SIGNAL",fontSize=10.sp,color=Muted);Text("What did you notice?",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Ink);Spacer(Modifier.height(18.dp));OutlinedTextField(value=text,onValueChange={text=it},label={Text("Your observation")},placeholder={Text("Gate 3 parking is completely full")},modifier=Modifier.fillMaxWidth().height(150.dp),trailingIcon={TextButton(onClick={requestMic();startSpeech(context){text=it}}){Text("VOICE",color=Coral,fontSize=10.sp)}});Spacer(Modifier.height(12.dp));Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){OutlinedButton(onClick={reporter=if(reporter=="Reporter 1")"Reporter 2" else "Reporter 1"},modifier=Modifier.weight(1f)){Text(reporter,fontSize=11.sp)};OutlinedButton(onClick={language=when(language){"en"->"hi";"hi"->"ta";else->"en"}},modifier=Modifier.weight(1f)){Text(language.uppercase(),fontSize=11.sp)}};Spacer(Modifier.height(18.dp));Button(onClick={vm.submit(reporter,text,language);text=""},enabled=text.isNotBlank()&&!vm.processing.value,colors=ButtonDefaults.buttonColors(containerColor=Ink),modifier=Modifier.fillMaxWidth()){Text(if(vm.processing.value)"FUSING…" else "SUBMIT TO THE CROWD FEED")};Text(vm.notice.value,fontSize=11.sp,color=Coral,modifier=Modifier.padding(top=10.dp));Spacer(Modifier.height(24.dp));Text("The model runs locally when the .task file is installed. Until then, the same fusion contract uses a deterministic fallback so the demo remains rehearsable.",fontSize=13.sp,color=Muted,lineHeight=19.sp)}}
@Composable fun Insights(vm:HumsafarViewModel){val categories=vm.reports.groupingBy{it.category?:"other"}.eachCount();Column(Modifier.padding(20.dp)){Text("EVENT INTELLIGENCE",fontSize=10.sp,color=Muted);Text("See the shape of the crowd.",fontSize=28.sp,fontWeight=FontWeight.Bold,color=Ink);Spacer(Modifier.height(18.dp));Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("REPORTS",vm.reports.size.toString());Stat("REPORTERS",vm.reports.map{it.reporter}.distinct().size.toString())};Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Stat("SAFETY",vm.reports.count{it.category=="safety"}.toString());Stat("LANGUAGES",vm.reports.map{it.language}.distinct().size.toString())};Spacer(Modifier.height(24.dp));Text("REPORTS BY CATEGORY",fontSize=10.sp,color=Muted);categories.forEach{(category,count)->Row(Modifier.fillMaxWidth().padding(vertical=7.dp),verticalAlignment=Alignment.CenterVertically){Text(category,Modifier.width(90.dp),fontSize=12.sp);LinearProgressIndicator(progress={count.toFloat()/vm.reports.size.coerceAtLeast(1)},color=Coral,trackColor=Color(0xFFE9EFEA),modifier=Modifier.weight(1f).height(7.dp));Text("  $count",fontSize=11.sp,color=Muted)}}}}
@Composable fun Stat(label:String,value:String){Card(colors=CardDefaults.cardColors(containerColor=Ink),modifier=Modifier.weight(1f)){Column(Modifier.padding(15.dp)){Text(label,fontSize=9.sp,color=Muted);Text(value,fontSize=27.sp,fontWeight=FontWeight.Bold,color=Lime)}}}
@Composable fun BottomTabs(current:Int,onChange:(Int)->Unit){NavigationBar(containerColor=Paper){listOf("FEED","REPORT","INSIGHTS","EVENT").forEachIndexed{i,label->NavigationBarItem(selected=current==i,onClick={onChange(i)},icon={Text(if(i==0)"◒" else if(i==1)"＋" else if(i==2)"▦" else "⌁")},label={Text(label,fontSize=9.sp)})}}}
private fun startSpeech(context:android.content.Context,onText:(String)->Unit){if(!SpeechRecognizer.isRecognitionAvailable(context))return;val recognizer=SpeechRecognizer.createSpeechRecognizer(context);recognizer.setRecognitionListener(object:RecognitionListener{override fun onResults(results:Bundle?){results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()?.let(onText);recognizer.destroy()};override fun onError(error:Int){recognizer.destroy()};override fun onReadyForSpeech(p0:Bundle?){ };override fun onBeginningOfSpeech(){};override fun onRmsChanged(p0:Float){};override fun onBufferReceived(p0:ByteArray?){ };override fun onEndOfSpeech(){};override fun onPartialResults(p0:Bundle?){ };override fun onEvent(p0:Int,p1:Bundle?){}});recognizer.startListening(Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply{putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS,false)})}
