# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class com.startapp.** {
      *;
}

-keep class com.truenet.** {
      *;
}

-keepattributes Exceptions, InnerClasses, Signature, Deprecated, SourceFile,
LineNumberTable, *Annotation*, EnclosingMethod
-dontwarn android.webkit.JavascriptInterface
-dontwarn com.startapp.**

-dontwarn org.jetbrains.annotations.**

-dontwarn com.google.android.gms.ads.AdFormat
-dontwarn com.google.android.gms.ads.AdListener
-dontwarn com.google.android.gms.ads.AdRequest$Builder
-dontwarn com.google.android.gms.ads.AdRequest
-dontwarn com.google.android.gms.ads.FullScreenContentCallback
-dontwarn com.google.android.gms.ads.InterstitialAd
-dontwarn com.google.android.gms.ads.OnUserEarnedRewardListener
-dontwarn com.google.android.gms.ads.interstitial.InterstitialAd
-dontwarn com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
-dontwarn com.google.android.gms.ads.query.AdInfo
-dontwarn com.google.android.gms.ads.query.QueryInfo
-dontwarn com.google.android.gms.ads.query.QueryInfoGenerationCallback
-dontwarn com.google.android.gms.ads.rewarded.RewardedAd
-dontwarn com.google.android.gms.ads.rewarded.RewardedAdCallback
-dontwarn com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
-dontwarn com.squareup.okhttp.Cache
-dontwarn com.squareup.okhttp.CacheControl$Builder
-dontwarn com.squareup.okhttp.CacheControl
-dontwarn com.squareup.okhttp.Call
-dontwarn com.squareup.okhttp.OkHttpClient
-dontwarn com.squareup.okhttp.Request$Builder
-dontwarn com.squareup.okhttp.Request
-dontwarn com.squareup.okhttp.Response
-dontwarn com.squareup.okhttp.ResponseBody
