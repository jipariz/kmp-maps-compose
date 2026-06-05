package eu.buney.maps.jsinterop

import kotlin.js.Promise
import kotlinx.coroutines.await

internal actual fun injectBootstrap(
    apiKey: String,
    version: String,
    language: String?,
    region: String?,
) {
    val alreadyInjected = js(
        "typeof google !== 'undefined' && !!google.maps && typeof google.maps.importLibrary === 'function'"
    ).unsafeCast<Boolean>()
    if (alreadyInjected) return

    runBootstrap(apiKey, version, language ?: "", region ?: "")
}

// Same minified loader as the wasmJs sibling, sans async/await (Kotlin/JS rejects async/await
// inside js() blocks). language/region are forwarded only when non-empty.
@Suppress("UNUSED_PARAMETER")
private fun runBootstrap(apiKey: String, version: String, language: String, region: String) {
    // Compared to the wasmJs sibling: optional chaining (?.nonce) is replaced with explicit
    // ternary because Kotlin/JS rejects ?. inside js() blocks.
    js(
        "(g=>{var h,a,k,p='The Google Maps JavaScript API',c='google',l='importLibrary',q='__ib__',m=document,b=window;b=b[c]||(b[c]={});var d=b.maps||(b.maps={}),r=new Set,e=new URLSearchParams,u=()=>h||(h=new Promise((f,n)=>{a=m.createElement('script');e.set('libraries',[...r]+'');for(k in g)e.set(k.replace(/[A-Z]/g,t=>'_'+t[0].toLowerCase()),g[k]);e.set('callback',c+'.maps.'+q);a.src='https://maps.'+c+'apis.com/maps/api/js?'+e;d[q]=f;a.onerror=()=>{h=null;n(Error(p+' could not load.'))};var nn=m.querySelector('script[nonce]');a.nonce=nn?nn.nonce:'';m.head.append(a)}));d[l]?console.warn(p+' only loads once. Ignoring:',g):d[l]=(f,...n)=>r.add(f)&&u().then(()=>d[l](f,...n))})({key:apiKey,v:version,language:language||undefined,region:region||undefined})"
    )
}

internal actual suspend fun importLibrarySuspend(name: String) {
    val promise = js("google.maps.importLibrary(name)").unsafeCast<Promise<dynamic>>()
    promise.await()
}
