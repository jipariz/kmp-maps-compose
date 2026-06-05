@file:OptIn(ExperimentalWasmJsInterop::class)

package eu.buney.maps.jsinterop

import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise
import kotlinx.coroutines.await

// Wasm-restricted js("...") form: each js() call must be a single-expression package-level
// function body, and js bodies cannot use async/await. The bootstrap is therefore split into
// small synchronous pieces and pieced together from Kotlin.

private fun bootstrapAlreadyInjected(): Boolean =
    js("typeof google !== 'undefined' && !!google.maps && typeof google.maps.importLibrary === 'function'")

private fun runBootstrap(apiKey: String, version: String, language: String, region: String): Unit = js(
    "(g=>{var h,a,k,p='The Google Maps JavaScript API',c='google',l='importLibrary',q='__ib__',m=document,b=window;b=b[c]||(b[c]={});var d=b.maps||(b.maps={}),r=new Set,e=new URLSearchParams,u=()=>h||(h=new Promise((f,n)=>{a=m.createElement('script');e.set('libraries',[...r]+'');for(k in g)e.set(k.replace(/[A-Z]/g,t=>'_'+t[0].toLowerCase()),g[k]);e.set('callback',c+'.maps.'+q);a.src='https://maps.'+c+'apis.com/maps/api/js?'+e;d[q]=f;a.onerror=()=>{h=null;n(Error(p+' could not load.'))};a.nonce=m.querySelector('script[nonce]')?.nonce||'';m.head.append(a)}));d[l]?console.warn(p+' only loads once. Ignoring:',g):d[l]=(f,...n)=>r.add(f)&&u().then(()=>d[l](f,...n))})({key:apiKey,v:version,language:language||undefined,region:region||undefined})"
)

internal actual fun injectBootstrap(
    apiKey: String,
    version: String,
    language: String?,
    region: String?,
) {
    if (bootstrapAlreadyInjected()) return
    runBootstrap(apiKey, version, language ?: "", region ?: "")
}

private fun callImportLibrary(name: String): Promise<JsAny?> =
    js("google.maps.importLibrary(name)")

internal actual suspend fun importLibrarySuspend(name: String) {
    callImportLibrary(name).await<JsAny?>()
}
